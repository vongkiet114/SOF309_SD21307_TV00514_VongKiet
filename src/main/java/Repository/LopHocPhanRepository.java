package Repository;

import Models.LopHocPhan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LopHocPhanRepository {

    private static final String SELECT_QUERY =
            "SELECT lhp.ma_lophocphan, lhp.ma_lhp, lhp.ma_monhoc, lhp.ma_giangvien, " +
            "lhp.ma_hocky, lhp.trang_thai, mh.ten_monhoc, mh.so_tinchi, mh.ma_mon, " +
            "nd.ho_ten AS ten_giangvien, hk.ten_hocky, hk.nam_hoc, " +
            "(SELECT COUNT(*) FROM danhsach_sv_lhp dssv WHERE dssv.ma_lophocphan = lhp.ma_lophocphan) AS so_luong_sv " +
            "FROM lophocphan lhp " +
            "JOIN monhoc mh ON lhp.ma_monhoc = mh.ma_monhoc " +
            "JOIN giangvien gv ON lhp.ma_giangvien = gv.ma_nguoidung " +
            "JOIN nguoidung nd ON gv.ma_nguoidung = nd.ma_nguoidung " +
            "JOIN hocky hk ON lhp.ma_hocky = hk.ma_hocky ";
    private static final String NEXT_CODE =
            "SELECT UPPER(cn.ma_cn) AS ma_cn, " +
            "NEXT VALUE FOR dbo.seq_lhp_hienthi AS next_value " +
            "FROM dbo.monhoc mh " +
            "JOIN dbo.chuyennganh cn ON cn.ma_chuyennganh = mh.ma_chuyennganh " +
            "WHERE mh.ma_monhoc = ?";
    private static final String INSERT =
            "INSERT INTO lophocphan " +
            "(ma_lhp, ma_monhoc, ma_giangvien, ma_hocky, trang_thai) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE lhp SET lhp.ma_monhoc = mh.ma_monhoc, lhp.ma_giangvien = ?, " +
            "lhp.ma_hocky = ?, lhp.trang_thai = ?, " +
            "lhp.ma_lhp = UPPER(cn.ma_cn) + '-LHP-' " +
            "+ RIGHT(lhp.ma_lhp, CHARINDEX('-', REVERSE(lhp.ma_lhp)) - 1) " +
            "FROM dbo.lophocphan lhp " +
            "JOIN dbo.monhoc mh ON mh.ma_monhoc = ? " +
            "JOIN dbo.chuyennganh cn ON cn.ma_chuyennganh = mh.ma_chuyennganh " +
            "WHERE lhp.ma_lophocphan = ?";
    private static final String TERM_EXISTS =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM hocky WHERE ma_hocky = ?) THEN 1 ELSE 0 END";
    private static final String TEACHER_ELIGIBLE =
            "SELECT COUNT(*) FROM giangvien gv " +
            "JOIN nguoidung nd ON nd.ma_nguoidung = gv.ma_nguoidung " +
            "JOIN monhoc mh ON mh.ma_monhoc = ? " +
            "WHERE gv.ma_nguoidung = ? AND nd.hoat_dong = 1 " +
            "AND gv.ma_chuyennganh = mh.ma_chuyennganh";
    private static final String HAS_STUDENTS =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM danhsach_sv_lhp " +
            "WHERE ma_lophocphan = ?) THEN 1 ELSE 0 END";
    private static final String HAS_SCORES =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM diemso ds " +
            "JOIN danhsach_sv_lhp dsl ON dsl.ma_danhsach = ds.ma_danhsach " +
            "WHERE dsl.ma_lophocphan = ?) THEN 1 ELSE 0 END";
    private static final String HAS_SCHEDULES =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM lichhoc " +
            "WHERE ma_lophocphan = ?) THEN 1 ELSE 0 END";
    private static final String HAS_EXAMS =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM kythi " +
            "WHERE ma_lophocphan = ?) THEN 1 ELSE 0 END";
    private static final String HAS_ATTENDANCE =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM diemdanh dd " +
            "JOIN danhsach_sv_lhp dsl ON dsl.ma_danhsach = dd.ma_danhsach " +
            "WHERE dsl.ma_lophocphan = ?) THEN 1 ELSE 0 END";
    private static final String TEACHER_CHANGE_CONFLICT =
            "SELECT CASE WHEN EXISTS (" +
            "SELECT 1 FROM lichhoc current_schedule " +
            "WHERE current_schedule.ma_lophocphan = ? " +
            "AND current_schedule.trang_thai = 'BinhThuong' " +
            "AND (EXISTS (" +
            "SELECT 1 FROM lichhoc teacher_schedule " +
            "JOIN lophocphan teacher_lhp " +
            "ON teacher_lhp.ma_lophocphan = teacher_schedule.ma_lophocphan " +
            "WHERE teacher_lhp.ma_giangvien = ? " +
            "AND teacher_schedule.ma_lophocphan <> ? " +
            "AND teacher_schedule.trang_thai = 'BinhThuong' " +
            "AND teacher_schedule.ngay_hoc = current_schedule.ngay_hoc " +
            "AND teacher_schedule.ma_ca = current_schedule.ma_ca" +
            ") OR EXISTS (" +
            "SELECT 1 FROM kythi teacher_exam " +
            "JOIN lophocphan teacher_exam_lhp " +
            "ON teacher_exam_lhp.ma_lophocphan = teacher_exam.ma_lophocphan " +
            "WHERE teacher_exam_lhp.ma_giangvien = ? " +
            "AND teacher_exam.ngay_thi = current_schedule.ngay_hoc " +
            "AND teacher_exam.ma_ca = current_schedule.ma_ca" +
            "))) THEN 1 ELSE 0 END";

    public List<LopHocPhan> findByStudent(int maNguoiDung) {
        List<LopHocPhan> list = new ArrayList<>();
        String sql = SELECT_QUERY + "JOIN danhsach_sv_lhp dssv ON lhp.ma_lophocphan = dssv.ma_lophocphan " +
                     "WHERE dssv.ma_nguoidung = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
        return list;
    }
    
    // ... (rest of the existing methods)

    public List<LopHocPhan> getAll(Integer maHocKy, Integer maGiangVien) {
        List<LopHocPhan> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_QUERY);
        List<Object> params = new ArrayList<>();

        if (maHocKy != null && maHocKy > 0) {
            sql.append("WHERE lhp.ma_hocky = ? ");
            params.add(maHocKy);
        }
        if (maGiangVien != null && maGiangVien > 0) {
            sql.append(params.isEmpty() ? "WHERE " : "AND ");
            sql.append("lhp.ma_giangvien = ? ");
            params.add(maGiangVien);
        }
        sql.append("ORDER BY lhp.ma_lophocphan");

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
        return list;
    }

    public LopHocPhan findById(int id) {
        String sql = SELECT_QUERY + "WHERE lhp.ma_lophocphan = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean termExists(int id) {
        return exists(TERM_EXISTS, id);
    }

    public boolean isTeacherEligible(int teacherId, int subjectId) {
        return exists(TEACHER_ELIGIBLE, subjectId, teacherId);
    }

    public boolean hasStudents(int id) {
        return exists(HAS_STUDENTS, id);
    }

    public boolean hasScores(int id) {
        return exists(HAS_SCORES, id);
    }

    public boolean hasSchedules(int id) {
        return exists(HAS_SCHEDULES, id);
    }

    public boolean hasExams(int id) {
        return exists(HAS_EXAMS, id);
    }

    public boolean hasAttendance(int id) {
        return exists(HAS_ATTENDANCE, id);
    }

    public boolean teacherChangeConflicts(int id, int teacherId) {
        return exists(TEACHER_CHANGE_CONFLICT, id, teacherId, id, teacherId);
    }

    public String getDeleteConstraint(int id) {
        if (hasStudents(id)) {
            return "đang có sinh viên đăng ký";
        }
        if (hasSchedules(id)) {
            return "đang có lịch học";
        }
        if (hasExams(id)) {
            return "đang có lịch thi";
        }
        if (hasScores(id)) {
            return "đang có điểm";
        }
        if (hasAttendance(id)) {
            return "đang có điểm danh";
        }
        return null;
    }

    public int insert(LopHocPhan lhp) {
        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement codeStatement = conn.prepareStatement(NEXT_CODE)) {
                    codeStatement.setInt(1, lhp.getMaMonhoc());
                    try (ResultSet rs = codeStatement.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return 0;
                        }
                        lhp.setMaLhp(rs.getString("ma_cn").toUpperCase(Locale.ROOT)
                                + "-LHP-" + String.format("%03d", rs.getInt("next_value")));
                    }
                }

                int id;
                try (PreparedStatement ps = conn.prepareStatement(
                        INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, lhp.getMaLhp());
                    ps.setInt(2, lhp.getMaMonhoc());
                    ps.setInt(3, lhp.getMaGiangvien());
                    ps.setInt(4, lhp.getMaHocky());
                    ps.setString(5, lhp.getTrangThai());
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return 0;
                    }
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        id = rs.next() ? rs.getInt(1) : 0;
                    }
                }

                if (id <= 0) {
                    conn.rollback();
                    return 0;
                }
                conn.commit();
                return id;
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(LopHocPhan lhp) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setInt(1, lhp.getMaGiangvien());
            ps.setInt(2, lhp.getMaHocky());
            ps.setString(3, lhp.getTrangThai());
            ps.setInt(4, lhp.getMaMonhoc());
            ps.setInt(5, lhp.getMaLophocphan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM lophocphan WHERE ma_lophocphan = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SqlServerDeleteExceptionClassifier.classify(
                    "Không thể truy cập dữ liệu lớp học phần.", e);
        }
    }

    private LopHocPhan map(ResultSet rs) throws SQLException {
        LopHocPhan lhp = new LopHocPhan();
        lhp.setMaLophocphan(rs.getInt("ma_lophocphan"));
        lhp.setMaMonhoc(rs.getInt("ma_monhoc"));
        lhp.setMaGiangvien(rs.getInt("ma_giangvien"));
        lhp.setMaHocky(rs.getInt("ma_hocky"));
        lhp.setTrangThai(rs.getString("trang_thai"));

        lhp.setTenMonHoc(rs.getString("ten_monhoc"));
        lhp.setSoTinChi(rs.getInt("so_tinchi"));
        lhp.setTenGiangVien(rs.getString("ten_giangvien"));
        lhp.setTenHocKy(rs.getString("ten_hocky"));
        lhp.setSoLuongSV(rs.getInt("so_luong_sv"));
        lhp.setNamHoc(rs.getString("nam_hoc"));
        lhp.setMaLhp(rs.getString("ma_lhp"));

        return lhp;
    }

    private boolean exists(String sql, int... parameters) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                ps.setInt(i + 1, parameters[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu lớp học phần.", cause);
    }

    private void rollback(Connection connection, SQLException cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }
}
