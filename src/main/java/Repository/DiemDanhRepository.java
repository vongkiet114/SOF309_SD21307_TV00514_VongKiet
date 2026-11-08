package Repository;

import Models.DiemDanh;
import Models.LichSuDiemDanhItem;
import Models.SinhVien;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DiemDanhRepository {
    public enum BatchResult {
        SAVED,
        SESSION_NOT_OWNED,
        SESSION_NOT_NORMAL,
        SESSION_IN_PAST,
        UNKNOWN_MEMBERSHIP,
        RELATION_MISMATCH
    }

    public List<DiemDanh> findByMaNguoiDung(int maNguoiDung) {
        List<DiemDanh> list = new ArrayList<>();
        String sql = "SELECT dd.* FROM diemdanh dd "
                + "JOIN danhsach_sv_lhp dssv ON dd.ma_danhsach = dssv.ma_danhsach "
                + "WHERE dssv.ma_nguoidung = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DiemDanh dd = new DiemDanh();
                    dd.setMaDiemdanh(rs.getInt("ma_diemdanh"));
                    dd.setMaDanhsach(rs.getInt("ma_danhsach"));
                    dd.setMaLichhoc(rs.getInt("ma_lichhoc"));
                    dd.setTrangThai(rs.getString("trang_thai"));
                    list.add(dd);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải dữ liệu điểm danh.", e);
        }
        return list;
    }

    public List<LichSuDiemDanhItem> findLichSuByMaNguoiDung(int maNguoiDung) {
        List<LichSuDiemDanhItem> list = new ArrayList<>();
        String sql = "SELECT lh.ngay_hoc, mh.ten_monhoc, k.ten_khoa, dd.trang_thai "
                + "FROM dbo.diemdanh dd "
                + "JOIN dbo.danhsach_sv_lhp dssv ON dd.ma_danhsach = dssv.ma_danhsach "
                + "JOIN dbo.lichhoc lh ON dd.ma_lichhoc = lh.ma_lichhoc "
                + "JOIN dbo.lophocphan lhp ON lh.ma_lophocphan = lhp.ma_lophocphan "
                + "JOIN dbo.monhoc mh ON lhp.ma_monhoc = mh.ma_monhoc "
                + "JOIN dbo.chuyennganh cn ON mh.ma_chuyennganh = cn.ma_chuyennganh "
                + "JOIN dbo.khoa k ON cn.ma_khoa = k.ma_khoa "
                + "WHERE dssv.ma_nguoidung = ? "
                + "ORDER BY lh.ngay_hoc DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date date = rs.getDate("ngay_hoc");
                    list.add(new LichSuDiemDanhItem(
                            date == null ? null : date.toLocalDate(),
                            rs.getString("ten_monhoc"),
                            rs.getString("ten_khoa"),
                            rs.getString("trang_thai")));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải lịch sử điểm danh.", e);
        }
        return list;
    }

    // Lich su diem danh co loc theo mon hoc va theo thang (nam + thang).
    // maMonhoc == null -> tat ca mon; nam/thang == null -> khong loc theo thang.
    public List<LichSuDiemDanhItem> findLichSu(int maNguoiDung, Integer maMonhoc,
                                               Integer nam, Integer thang) {
        List<LichSuDiemDanhItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT lh.ngay_hoc, mh.ma_monhoc, mh.ten_monhoc, k.ten_khoa, dd.trang_thai "
                + "FROM dbo.diemdanh dd "
                + "JOIN dbo.danhsach_sv_lhp dssv ON dd.ma_danhsach = dssv.ma_danhsach "
                + "JOIN dbo.lichhoc lh ON dd.ma_lichhoc = lh.ma_lichhoc "
                + "JOIN dbo.lophocphan lhp ON lh.ma_lophocphan = lhp.ma_lophocphan "
                + "JOIN dbo.monhoc mh ON lhp.ma_monhoc = mh.ma_monhoc "
                + "JOIN dbo.chuyennganh cn ON mh.ma_chuyennganh = cn.ma_chuyennganh "
                + "JOIN dbo.khoa k ON cn.ma_khoa = k.ma_khoa "
                + "WHERE dssv.ma_nguoidung = ? ");
        List<Object> params = new ArrayList<>();
        params.add(maNguoiDung);
        if (maMonhoc != null && maMonhoc > 0) {
            sql.append("AND mh.ma_monhoc = ? ");
            params.add(maMonhoc);
        }
        if (nam != null && thang != null && thang >= 1 && thang <= 12) {
            sql.append("AND YEAR(lh.ngay_hoc) = ? AND MONTH(lh.ngay_hoc) = ? ");
            params.add(nam);
            params.add(thang);
        }
        sql.append("ORDER BY lh.ngay_hoc DESC");
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date date = rs.getDate("ngay_hoc");
                    list.add(new LichSuDiemDanhItem(
                            date == null ? null : date.toLocalDate(),
                            rs.getString("ten_monhoc"),
                            rs.getString("ten_khoa"),
                            rs.getString("trang_thai"),
                            rs.getInt("ma_monhoc")));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải lịch sử điểm danh.", e);
        }
        return list;
    }

    // Danh sach cac mon hoc ma sinh vien da tung duoc diem danh (dung cho bo loc).
    public List<Models.MonHoc> findMonHocDaDiemDanh(int maNguoiDung) {
        List<Models.MonHoc> list = new ArrayList<>();
        String sql = "SELECT DISTINCT mh.ma_monhoc, mh.ten_monhoc "
                + "FROM dbo.diemdanh dd "
                + "JOIN dbo.danhsach_sv_lhp dssv ON dd.ma_danhsach = dssv.ma_danhsach "
                + "JOIN dbo.lichhoc lh ON dd.ma_lichhoc = lh.ma_lichhoc "
                + "JOIN dbo.lophocphan lhp ON lh.ma_lophocphan = lhp.ma_lophocphan "
                + "JOIN dbo.monhoc mh ON lhp.ma_monhoc = mh.ma_monhoc "
                + "WHERE dssv.ma_nguoidung = ? "
                + "ORDER BY mh.ten_monhoc";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Models.MonHoc mh = new Models.MonHoc();
                    mh.setMaMonhoc(rs.getInt("ma_monhoc"));
                    mh.setTenMonhoc(rs.getString("ten_monhoc"));
                    list.add(mh);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải danh sách môn học.", e);
        }
        return list;
    }

    public List<SinhVien> findDiemDanhForBuoiHoc(int teacherId,
                                                  int sectionId,
                                                  int scheduleId) {
        List<SinhVien> result = new ArrayList<>();
        String sql = "SELECT sv.ma_nguoidung, sv.ma_lop, sv.mssv, "
                + "sv.trang_thai AS sinhvien_trang_thai, nd.ho_ten, nd.email, "
                + "l.ten_lop, roster.ma_danhsach, "
                + "dd.trang_thai AS diemdanh_trang_thai "
                + "FROM dbo.lichhoc lh "
                + "JOIN dbo.lophocphan lhp "
                + "ON lhp.ma_lophocphan = lh.ma_lophocphan "
                + "JOIN dbo.danhsach_sv_lhp roster "
                + "ON roster.ma_lophocphan = lhp.ma_lophocphan "
                + "JOIN dbo.sinhvien sv ON sv.ma_nguoidung = roster.ma_nguoidung "
                + "JOIN dbo.nguoidung nd ON nd.ma_nguoidung = sv.ma_nguoidung "
                + "JOIN dbo.lop l ON l.ma_lop = sv.ma_lop "
                + "LEFT JOIN dbo.diemdanh dd "
                + "ON dd.ma_danhsach = roster.ma_danhsach "
                + "AND dd.ma_lichhoc = lh.ma_lichhoc "
                + "WHERE lh.ma_lophocphan = ? AND lh.ma_lichhoc = ? "
                + "AND lhp.ma_giangvien = ? AND lh.trang_thai = 'BinhThuong' "
                + "ORDER BY sv.mssv";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, scheduleId);
            ps.setInt(3, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải bảng điểm danh.", e);
        }
        return result;
    }

    public BatchResult saveBatch(int teacherId,
                                 int sectionId,
                                 int scheduleId,
                                 List<DiemDanh> rows) {
        return saveBatch(teacherId, sectionId, scheduleId, rows,
                LocalDate.now(Clock.systemDefaultZone()));
    }

    public BatchResult saveBatch(int teacherId,
                                 int sectionId,
                                 int scheduleId,
                                 List<DiemDanh> rows,
                                 LocalDate today) {
        if (rows == null || rows.isEmpty()) {
            return BatchResult.RELATION_MISMATCH;
        }
        if (today == null) return BatchResult.SESSION_IN_PAST;
        try (Connection conn = DBConnect.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                BatchResult sessionResult = lockAndCheckSession(
                        conn, teacherId, sectionId, scheduleId, today);
                if (sessionResult != BatchResult.SAVED) {
                    conn.rollback();
                    return sessionResult;
                }

                for (DiemDanh row : rows) {
                    if (row == null || row.getMaLichhoc() != scheduleId) {
                        conn.rollback();
                        return BatchResult.RELATION_MISMATCH;
                    }
                }

                BatchResult membershipResult = lockAndCheckMemberships(
                        conn, sectionId, rows);
                if (membershipResult != BatchResult.SAVED) {
                    conn.rollback();
                    return membershipResult;
                }

                mergeRows(conn, rows);
                conn.commit();
                return BatchResult.SAVED;
            } catch (SQLException e) {
                rollback(conn, e);
                throw new RepositoryException("Không thể lưu điểm danh.", e);
            } finally {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                    // Connection is about to close; the original error remains authoritative.
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể mở giao dịch điểm danh.", e);
        }
    }

    private BatchResult lockAndCheckSession(Connection conn,
                                            int teacherId,
                                            int sectionId,
                                            int scheduleId,
                                            LocalDate today) throws SQLException {
        String sql = "SELECT lh.trang_thai, lh.ngay_hoc, lhp.ma_giangvien "
                + "FROM dbo.lichhoc lh WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN dbo.lophocphan lhp WITH (UPDLOCK, HOLDLOCK) "
                + "ON lhp.ma_lophocphan = lh.ma_lophocphan "
                + "WHERE lh.ma_lichhoc = ? AND lh.ma_lophocphan = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, scheduleId);
            statement.setInt(2, sectionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next() || rs.getInt("ma_giangvien") != teacherId) {
                    return BatchResult.SESSION_NOT_OWNED;
                }
                if (!"BinhThuong".equals(rs.getString("trang_thai"))) {
                    return BatchResult.SESSION_NOT_NORMAL;
                }
                Date scheduleDate = rs.getDate("ngay_hoc");
                if (scheduleDate == null || scheduleDate.toLocalDate().isBefore(today)) {
                    return BatchResult.SESSION_IN_PAST;
                }
                return BatchResult.SAVED;
            }
        }
    }

    private BatchResult lockAndCheckMemberships(Connection conn,
                                                int sectionId,
                                                List<DiemDanh> rows)
            throws SQLException {
        Set<Integer> requested = new LinkedHashSet<>();
        for (DiemDanh row : rows) requested.add(row.getMaDanhsach());

        StringBuilder sql = new StringBuilder(
                "SELECT ma_danhsach, ma_lophocphan "
                        + "FROM dbo.danhsach_sv_lhp WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE ma_danhsach IN (");
        for (int index = 0; index < requested.size(); index++) {
            if (index > 0) sql.append(',');
            sql.append('?');
        }
        sql.append(')');

        Set<Integer> found = new LinkedHashSet<>();
        boolean relationMismatch = false;
        try (PreparedStatement statement = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Integer membership : requested) statement.setInt(index++, membership);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    found.add(rs.getInt("ma_danhsach"));
                    if (rs.getInt("ma_lophocphan") != sectionId) relationMismatch = true;
                }
            }
        }
        if (found.size() != requested.size()) return BatchResult.UNKNOWN_MEMBERSHIP;
        return relationMismatch ? BatchResult.RELATION_MISMATCH : BatchResult.SAVED;
    }

    private void mergeRows(Connection conn, List<DiemDanh> rows) throws SQLException {
        String sql = "MERGE INTO dbo.diemdanh WITH (HOLDLOCK) AS target "
                + "USING (VALUES (?, ?, ?)) "
                + "AS source (ma_danhsach, ma_lichhoc, trang_thai) "
                + "ON target.ma_danhsach = source.ma_danhsach "
                + "AND target.ma_lichhoc = source.ma_lichhoc "
                + "WHEN MATCHED THEN UPDATE SET trang_thai = source.trang_thai "
                + "WHEN NOT MATCHED THEN INSERT "
                + "(ma_danhsach, ma_lichhoc, trang_thai) VALUES "
                + "(source.ma_danhsach, source.ma_lichhoc, source.trang_thai);";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (DiemDanh row : rows) {
                statement.setInt(1, row.getMaDanhsach());
                statement.setInt(2, row.getMaLichhoc());
                statement.setString(3, row.getTrangThai());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void rollback(Connection conn, SQLException original) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private SinhVien mapStudent(ResultSet rs) throws SQLException {
        SinhVien student = new SinhVien();
        student.setMaNguoidung(rs.getInt("ma_nguoidung"));
        student.setMaLop(rs.getInt("ma_lop"));
        student.setMssv(rs.getString("mssv"));
        student.setTrangThai(rs.getString("sinhvien_trang_thai"));
        student.setHoTen(rs.getString("ho_ten"));
        student.setEmail(rs.getString("email"));
        student.setTenLop(rs.getString("ten_lop"));
        student.setMaDanhSach(rs.getInt("ma_danhsach"));
        student.setTrangThaiDiemDanh(rs.getString("diemdanh_trang_thai"));
        return student;
    }
    public List<SinhVien> findDiemDanhForBuoiHocAdmin(int sectionId, int scheduleId) {
        List<SinhVien> result = new ArrayList<>();
        String sql = "SELECT sv.ma_nguoidung, sv.ma_lop, sv.mssv, "
                + "sv.trang_thai AS sinhvien_trang_thai, nd.ho_ten, nd.email, "
                + "l.ten_lop, roster.ma_danhsach, "
                + "dd.trang_thai AS diemdanh_trang_thai "
                + "FROM dbo.lichhoc lh "
                + "JOIN dbo.lophocphan lhp "
                + "ON lhp.ma_lophocphan = lh.ma_lophocphan "
                + "JOIN dbo.danhsach_sv_lhp roster "
                + "ON roster.ma_lophocphan = lhp.ma_lophocphan "
                + "JOIN dbo.sinhvien sv ON sv.ma_nguoidung = roster.ma_nguoidung "
                + "JOIN dbo.nguoidung nd ON nd.ma_nguoidung = sv.ma_nguoidung "
                + "JOIN dbo.lop l ON l.ma_lop = sv.ma_lop "
                + "LEFT JOIN dbo.diemdanh dd "
                + "ON dd.ma_danhsach = roster.ma_danhsach "
                + "AND dd.ma_lichhoc = lh.ma_lichhoc "
                + "WHERE lh.ma_lophocphan = ? AND lh.ma_lichhoc = ? "
                + "AND lh.trang_thai = 'BinhThuong' "
                + "ORDER BY sv.mssv";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải bảng điểm danh.", e);
        }
        return result;
    }

    public BatchResult saveBatchAdmin(int sectionId, int scheduleId, List<DiemDanh> rows) {
        return saveBatchAdmin(sectionId, scheduleId, rows,
                LocalDate.now(Clock.systemDefaultZone()));
    }

    public BatchResult saveBatchAdmin(int sectionId,
                                      int scheduleId,
                                      List<DiemDanh> rows,
                                      LocalDate today) {
        if (rows == null || rows.isEmpty()) return BatchResult.RELATION_MISMATCH;
        if (today == null) return BatchResult.SESSION_IN_PAST;
        try (Connection conn = DBConnect.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                BatchResult sessionResult =
                        lockAndCheckSessionAdmin(conn, sectionId, scheduleId, today);
                if (sessionResult != BatchResult.SAVED) {
                    conn.rollback();
                    return sessionResult;
                }
                for (DiemDanh row : rows) {
                    if (row == null || row.getMaLichhoc() != scheduleId) {
                        conn.rollback();
                        return BatchResult.RELATION_MISMATCH;
                    }
                }
                BatchResult membershipResult = lockAndCheckMemberships(conn, sectionId, rows);
                if (membershipResult != BatchResult.SAVED) {
                    conn.rollback();
                    return membershipResult;
                }
                mergeRows(conn, rows);
                conn.commit();
                return BatchResult.SAVED;
            } catch (SQLException e) {
                rollback(conn, e);
                throw new RepositoryException("Không thể lưu điểm danh.", e);
            } finally {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể mở giao dịch điểm danh.", e);
        }
    }

    private BatchResult lockAndCheckSessionAdmin(Connection conn,
                                                 int sectionId,
                                                 int scheduleId,
                                                 LocalDate today)
            throws SQLException {
        String sql = "SELECT lh.trang_thai, lh.ngay_hoc "
                + "FROM dbo.lichhoc lh WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE lh.ma_lichhoc = ? AND lh.ma_lophocphan = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, scheduleId);
            statement.setInt(2, sectionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return BatchResult.SESSION_NOT_OWNED;
                if (!"BinhThuong".equals(rs.getString("trang_thai"))) {
                    return BatchResult.SESSION_NOT_NORMAL;
                }
                Date scheduleDate = rs.getDate("ngay_hoc");
                if (scheduleDate == null || scheduleDate.toLocalDate().isBefore(today)) {
                    return BatchResult.SESSION_IN_PAST;
                }
                return BatchResult.SAVED;
            }
        }
    }
}
