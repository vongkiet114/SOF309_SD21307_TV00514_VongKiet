package Repository;

import Models.BangDiemHocPhan;
import Models.DiemSo;
import Models.ThanhPhanDiem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DiemRepository {
    public enum BatchResult {
        SAVED,
        SECTION_NOT_OWNED,
        UNKNOWN_MEMBERSHIP,
        UNKNOWN_COMPONENT,
        RELATION_MISMATCH
    }

    private static final String UPSERT =
            "MERGE dbo.diemso AS target "
                    + "USING (VALUES (?, ?, ?)) AS src "
                    + "(ma_danhsach, ma_thanhphan, gia_tri_diem) "
                    + "ON target.ma_danhsach = src.ma_danhsach "
                    + "AND target.ma_thanhphan = src.ma_thanhphan "
                    + "WHEN MATCHED THEN UPDATE SET gia_tri_diem = src.gia_tri_diem "
                    + "WHEN NOT MATCHED THEN INSERT "
                    + "(ma_danhsach, ma_thanhphan, gia_tri_diem) "
                    + "VALUES (src.ma_danhsach, src.ma_thanhphan, src.gia_tri_diem);";
    private static final String DELETE =
            "DELETE FROM dbo.diemso WHERE ma_danhsach = ? AND ma_thanhphan = ?";

    public List<DiemSo> findAllByMaNguoiDung(int maNguoiDung) {
        List<DiemSo> list = new ArrayList<>();
        String sql = "SELECT ds.*, tpd.ten_thanhphan, tpd.trong_so "
                + "FROM diemso ds "
                + "JOIN thanhphan_diem tpd ON ds.ma_thanhphan = tpd.ma_thanhphan "
                + "JOIN danhsach_sv_lhp dssv ON ds.ma_danhsach = dssv.ma_danhsach "
                + "WHERE dssv.ma_nguoidung = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
        return list;
    }

    public List<DiemSo> findByMaDanhSach(int maDanhSach) {
        List<DiemSo> list = new ArrayList<>();
        String sql = "SELECT ds.*, tpd.ten_thanhphan, tpd.trong_so "
                + "FROM diemso ds "
                + "JOIN thanhphan_diem tpd ON ds.ma_thanhphan = tpd.ma_thanhphan "
                + "WHERE ds.ma_danhsach = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDanhSach);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
        return list;
    }

    public Map<Integer, List<DiemSo>> findByMaLopHocPhan(int maLopHocPhan) {
        List<DiemSo> allScores = new ArrayList<>();
        String sql = "SELECT ds.*, tpd.ten_thanhphan, tpd.trong_so "
                + "FROM diemso ds "
                + "JOIN thanhphan_diem tpd ON ds.ma_thanhphan = tpd.ma_thanhphan "
                + "JOIN danhsach_sv_lhp dssv ON ds.ma_danhsach = dssv.ma_danhsach "
                + "WHERE dssv.ma_lophocphan = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLopHocPhan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) allScores.add(map(rs));
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
        return allScores.stream().collect(Collectors.groupingBy(DiemSo::getMaDanhsach));
    }

    public boolean isSectionOwned(int teacherId, int sectionId) {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM dbo.lophocphan "
                + "WHERE ma_lophocphan = ? AND ma_giangvien = ?) THEN 1 ELSE 0 END";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public BatchResult saveBatch(int teacherId, int sectionId, List<DiemSo> values) {
        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer subjectId = lockOwnedSection(conn, teacherId, sectionId);
                if (subjectId == null) {
                    conn.rollback();
                    return BatchResult.SECTION_NOT_OWNED;
                }

                Set<Integer> requestedMemberships = new LinkedHashSet<>();
                Set<Integer> requestedComponents = new LinkedHashSet<>();
                for (DiemSo value : values) {
                    requestedMemberships.add(value.getMaDanhsach());
                    requestedComponents.add(value.getMaThanhphan());
                }

                Map<Integer, Integer> memberships = loadRelations(conn,
                        "danhsach_sv_lhp", "ma_danhsach", "ma_lophocphan",
                        requestedMemberships);
                if (memberships.size() != requestedMemberships.size()) {
                    conn.rollback();
                    return BatchResult.UNKNOWN_MEMBERSHIP;
                }
                for (Integer relatedSection : memberships.values()) {
                    if (relatedSection != sectionId) {
                        conn.rollback();
                        return BatchResult.RELATION_MISMATCH;
                    }
                }

                Map<Integer, Integer> components = loadRelations(conn,
                        "thanhphan_diem", "ma_thanhphan", "ma_monhoc",
                        requestedComponents);
                if (components.size() != requestedComponents.size()) {
                    conn.rollback();
                    return BatchResult.UNKNOWN_COMPONENT;
                }
                for (Integer relatedSubject : components.values()) {
                    if (!relatedSubject.equals(subjectId)) {
                        conn.rollback();
                        return BatchResult.RELATION_MISMATCH;
                    }
                }

                writeBatch(conn, values);
                conn.commit();
                return BatchResult.SAVED;
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            } catch (RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public List<BangDiemHocPhan> findGradebookByStudent(int studentId) {
        String sql = "SELECT roster.ma_danhsach, section.ma_lophocphan, section.ma_lhp, "
                + "subject.ten_monhoc, component.ma_thanhphan, component.ten_thanhphan, "
                + "component.trong_so, component.ma_monhoc, score.ma_diem, score.gia_tri_diem "
                + "FROM dbo.danhsach_sv_lhp roster "
                + "JOIN dbo.lophocphan section "
                + "ON section.ma_lophocphan = roster.ma_lophocphan "
                + "JOIN dbo.monhoc subject ON subject.ma_monhoc = section.ma_monhoc "
                + "LEFT JOIN dbo.thanhphan_diem component "
                + "ON component.ma_monhoc = subject.ma_monhoc "
                + "LEFT JOIN dbo.diemso score "
                + "ON score.ma_danhsach = roster.ma_danhsach "
                + "AND score.ma_thanhphan = component.ma_thanhphan "
                + "WHERE roster.ma_nguoidung = ? "
                + "ORDER BY section.ma_lophocphan, component.ma_thanhphan";
        Map<Integer, BangDiemHocPhan> sections = new LinkedHashMap<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sectionId = rs.getInt("ma_lophocphan");
                    BangDiemHocPhan gradebook = sections.get(sectionId);
                    if (gradebook == null) {
                        gradebook = new BangDiemHocPhan();
                        gradebook.setMaLopHocPhan(sectionId);
                        gradebook.setMaLhp(rs.getString("ma_lhp"));
                        gradebook.setTenMonHoc(rs.getString("ten_monhoc"));
                        sections.put(sectionId, gradebook);
                    }
                    Integer componentId = nullableInt(rs, "ma_thanhphan");
                    if (componentId != null) {
                        ThanhPhanDiem component = new ThanhPhanDiem(
                                componentId,
                                rs.getString("ten_thanhphan"),
                                rs.getBigDecimal("trong_so"),
                                rs.getInt("ma_monhoc"));
                        gradebook.getThanhPhanDiems().add(component);
                    }
                    Integer scoreId = nullableInt(rs, "ma_diem");
                    if (scoreId != null) {
                        DiemSo score = new DiemSo(
                                scoreId,
                                rs.getInt("ma_danhsach"),
                                componentId,
                                rs.getBigDecimal("gia_tri_diem"));
                        score.setTenThanhPhan(rs.getString("ten_thanhphan"));
                        score.setTrongSo(rs.getBigDecimal("trong_so"));
                        gradebook.getDiemSoList().add(score);
                    }
                }
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
        return new ArrayList<>(sections.values());
    }

    public boolean saveOrUpdate(DiemSo diemSo) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT)) {
            ps.setInt(1, diemSo.getMaDanhsach());
            ps.setInt(2, diemSo.getMaThanhphan());
            ps.setBigDecimal(3, diemSo.getGiaTriDiem());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    private Integer lockOwnedSection(Connection conn, int teacherId, int sectionId)
            throws SQLException {
        String sql = "SELECT ma_monhoc FROM dbo.lophocphan WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE ma_lophocphan = ? AND ma_giangvien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private Map<Integer, Integer> loadRelations(Connection conn,
                                                String table,
                                                String idColumn,
                                                String relationColumn,
                                                Set<Integer> ids) throws SQLException {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        if (ids.isEmpty()) return result;
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(idColumn).append(", ").append(relationColumn)
                .append(" FROM dbo.").append(table).append(" WITH (UPDLOCK, HOLDLOCK) WHERE ")
                .append(idColumn).append(" IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(',');
            sql.append('?');
        }
        sql.append(')');
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Integer id : ids) ps.setInt(index++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(rs.getInt(1), rs.getInt(2));
            }
        }
        return result;
    }

    private void writeBatch(Connection conn, List<DiemSo> values) throws SQLException {
        try (PreparedStatement upsert = conn.prepareStatement(UPSERT);
             PreparedStatement delete = conn.prepareStatement(DELETE)) {
            boolean hasUpserts = false;
            boolean hasDeletes = false;
            for (DiemSo value : values) {
                if (value.getGiaTriDiem() == null) {
                    delete.setInt(1, value.getMaDanhsach());
                    delete.setInt(2, value.getMaThanhphan());
                    delete.addBatch();
                    hasDeletes = true;
                } else {
                    upsert.setInt(1, value.getMaDanhsach());
                    upsert.setInt(2, value.getMaThanhphan());
                    upsert.setBigDecimal(3, value.getGiaTriDiem());
                    upsert.addBatch();
                    hasUpserts = true;
                }
            }
            if (hasUpserts) upsert.executeBatch();
            if (hasDeletes) delete.executeBatch();
        }
    }

    private DiemSo map(ResultSet rs) throws SQLException {
        DiemSo diem = new DiemSo();
        diem.setMaDiem(rs.getInt("ma_diem"));
        diem.setMaDanhsach(rs.getInt("ma_danhsach"));
        diem.setMaThanhphan(rs.getInt("ma_thanhphan"));
        diem.setGiaTriDiem(rs.getBigDecimal("gia_tri_diem"));
        diem.setTenThanhPhan(rs.getString("ten_thanhphan"));
        diem.setTrongSo(rs.getBigDecimal("trong_so"));
        return diem;
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu điểm.", cause);
    }

    private void rollback(Connection connection, SQLException cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }
}
