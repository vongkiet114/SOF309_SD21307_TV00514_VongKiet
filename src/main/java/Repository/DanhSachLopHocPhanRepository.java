package Repository;

import Models.LopHocPhan;
import Models.SinhVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DanhSachLopHocPhanRepository {
    public enum AddResult {
        ADDED,
        SECTION_NOT_FOUND,
        SECTION_CLOSED,
        STUDENT_NOT_ACTIVE,
        WRONG_SPECIALIZATION,
        ALREADY_ENROLLED,
        DUPLICATE_SUBJECT,
        SCHEDULE_CONFLICT
    }

    public enum RemoveResult {
        REMOVED,
        MEMBERSHIP_NOT_FOUND,
        HAS_GRADES,
        HAS_ATTENDANCE
    }

    private static final String SELECT_SECTION =
            "SELECT lhp.ma_lophocphan, lhp.ma_lhp, lhp.ma_monhoc, " +
            "lhp.ma_giangvien, lhp.ma_hocky, lhp.trang_thai, " +
            "mh.ten_monhoc, mh.so_tinchi, nd.ho_ten AS ten_giangvien, " +
            "hk.ten_hocky, hk.nam_hoc, " +
            "(SELECT COUNT(*) FROM danhsach_sv_lhp dsl " +
            " WHERE dsl.ma_lophocphan = lhp.ma_lophocphan) AS so_luong_sv " +
            "FROM lophocphan lhp " +
            "JOIN monhoc mh ON mh.ma_monhoc = lhp.ma_monhoc " +
            "JOIN giangvien gv ON gv.ma_nguoidung = lhp.ma_giangvien " +
            "JOIN nguoidung nd ON nd.ma_nguoidung = gv.ma_nguoidung " +
            "JOIN hocky hk ON hk.ma_hocky = lhp.ma_hocky " +
            "WHERE lhp.ma_lophocphan = ?";

    private static final String SELECT_MEMBERS =
            "SELECT dsl.ma_danhsach, sv.ma_nguoidung, sv.ma_lop, sv.mssv, " +
            "sv.trang_thai, nd.ho_ten, nd.email, l.ten_lop " +
            "FROM danhsach_sv_lhp dsl " +
            "JOIN sinhvien sv ON sv.ma_nguoidung = dsl.ma_nguoidung " +
            "JOIN nguoidung nd ON nd.ma_nguoidung = sv.ma_nguoidung " +
            "JOIN lop l ON l.ma_lop = sv.ma_lop " +
            "WHERE dsl.ma_lophocphan = ? " +
            "ORDER BY sv.mssv";

    private static final String SELECT_CANDIDATES =
            "SELECT CAST(0 AS INT) AS ma_danhsach, sv.ma_nguoidung, sv.ma_lop, " +
            "sv.mssv, sv.trang_thai, nd.ho_ten, nd.email, l.ten_lop " +
            "FROM sinhvien sv " +
            "JOIN nguoidung nd ON nd.ma_nguoidung = sv.ma_nguoidung " +
            "JOIN lop l ON l.ma_lop = sv.ma_lop " +
            "JOIN lophocphan target_lhp ON target_lhp.ma_lophocphan = ? " +
            "JOIN monhoc target_subject ON target_subject.ma_monhoc = target_lhp.ma_monhoc " +
            "WHERE nd.hoat_dong = 1 AND sv.trang_thai = 'DangHoc' " +
            "AND l.ma_chuyennganh = target_subject.ma_chuyennganh " +
            "AND NOT EXISTS (SELECT 1 FROM danhsach_sv_lhp enrolled " +
            "                WHERE enrolled.ma_lophocphan = target_lhp.ma_lophocphan " +
            "                AND enrolled.ma_nguoidung = sv.ma_nguoidung) " +
            "AND (? = '' OR sv.mssv LIKE ? OR nd.ho_ten LIKE ?) " +
            "ORDER BY sv.mssv";

    private static final String LOCK_SECTION =
            "SELECT lhp.ma_monhoc, lhp.trang_thai, mh.ma_chuyennganh " +
            "FROM lophocphan lhp WITH (UPDLOCK, HOLDLOCK) " +
            "JOIN monhoc mh WITH (HOLDLOCK) ON mh.ma_monhoc = lhp.ma_monhoc " +
            "WHERE lhp.ma_lophocphan = ?";

    private static final String LOCK_STUDENT =
            "SELECT sv.trang_thai, nd.hoat_dong, l.ma_chuyennganh " +
            "FROM sinhvien sv WITH (UPDLOCK, HOLDLOCK) " +
            "JOIN nguoidung nd WITH (HOLDLOCK) ON nd.ma_nguoidung = sv.ma_nguoidung " +
            "JOIN lop l WITH (HOLDLOCK) ON l.ma_lop = sv.ma_lop " +
            "WHERE sv.ma_nguoidung = ?";

    private static final String ALREADY_ENROLLED =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM danhsach_sv_lhp WITH (UPDLOCK, HOLDLOCK) " +
            "WHERE ma_lophocphan = ? AND ma_nguoidung = ?) THEN 1 ELSE 0 END";

    private static final String DUPLICATE_SUBJECT =
            "SELECT CASE WHEN EXISTS (" +
            "SELECT 1 FROM danhsach_sv_lhp dsl WITH (UPDLOCK, HOLDLOCK) " +
            "JOIN lophocphan enrolled_lhp WITH (HOLDLOCK) " +
            "ON enrolled_lhp.ma_lophocphan = dsl.ma_lophocphan " +
            "WHERE dsl.ma_nguoidung = ? AND dsl.ma_lophocphan <> ? " +
            "AND enrolled_lhp.ma_monhoc = ?) THEN 1 ELSE 0 END";

    private static final String SCHEDULE_CONFLICT =
            "SELECT CASE WHEN EXISTS (" +
            "SELECT 1 FROM (" +
            "  SELECT target_class.ngay_hoc AS event_date, target_class.ma_ca " +
            "  FROM lichhoc target_class WITH (HOLDLOCK) " +
            "  WHERE target_class.ma_lophocphan = ? " +
            "    AND target_class.trang_thai = 'BinhThuong' " +
            "  UNION ALL " +
            "  SELECT target_exam.ngay_thi AS event_date, target_exam.ma_ca " +
            "  FROM kythi target_exam WITH (HOLDLOCK) " +
            "  WHERE target_exam.ma_lophocphan = ?" +
            ") target_event " +
            "JOIN (" +
            "  SELECT student_class.ngay_hoc AS event_date, student_class.ma_ca " +
            "  FROM danhsach_sv_lhp class_membership WITH (HOLDLOCK) " +
            "  JOIN lichhoc student_class WITH (HOLDLOCK) " +
            "    ON student_class.ma_lophocphan = class_membership.ma_lophocphan " +
            "  WHERE class_membership.ma_nguoidung = ? " +
            "    AND class_membership.ma_lophocphan <> ? " +
            "    AND student_class.trang_thai = 'BinhThuong' " +
            "  UNION ALL " +
            "  SELECT student_exam.ngay_thi AS event_date, student_exam.ma_ca " +
            "  FROM danhsach_sv_lhp exam_membership WITH (HOLDLOCK) " +
            "  JOIN kythi student_exam WITH (HOLDLOCK) " +
            "    ON student_exam.ma_lophocphan = exam_membership.ma_lophocphan " +
            "  WHERE exam_membership.ma_nguoidung = ? " +
            "    AND exam_membership.ma_lophocphan <> ?" +
            ") student_event ON student_event.event_date = target_event.event_date " +
            "AND student_event.ma_ca = target_event.ma_ca" +
            ") THEN 1 ELSE 0 END";

    private static final String INSERT_MEMBERSHIP =
            "INSERT INTO danhsach_sv_lhp (ma_nguoidung, ma_lophocphan) VALUES (?, ?)";

    private static final String LOCK_MEMBERSHIP =
            "SELECT ma_danhsach FROM danhsach_sv_lhp WITH (UPDLOCK, HOLDLOCK) " +
            "WHERE ma_danhsach = ? AND ma_lophocphan = ?";

    private static final String HAS_GRADES =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM diemso WITH (HOLDLOCK) " +
            "WHERE ma_danhsach = ?) THEN 1 ELSE 0 END";

    private static final String HAS_ATTENDANCE =
            "SELECT CASE WHEN EXISTS (SELECT 1 FROM diemdanh WITH (HOLDLOCK) " +
            "WHERE ma_danhsach = ?) THEN 1 ELSE 0 END";

    private static final String DELETE_MEMBERSHIP =
            "DELETE FROM danhsach_sv_lhp WHERE ma_danhsach = ? AND ma_lophocphan = ?";

    public LopHocPhan getSection(int sectionId) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_SECTION)) {
            statement.setInt(1, sectionId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapSection(rs) : null;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public List<SinhVien> getMembers(int sectionId) {
        return queryStudents(SELECT_MEMBERS, sectionId, null);
    }

    public List<SinhVien> findCandidates(int sectionId, String keyword) {
        return queryStudents(SELECT_CANDIDATES, sectionId, keyword == null ? "" : keyword);
    }

    public AddResult addAtomically(int sectionId, int studentId) {
        try (Connection conn = DBConnect.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            try {
                int subjectId;
                int specializationId;
                String sectionStatus;
                try (PreparedStatement statement = conn.prepareStatement(LOCK_SECTION)) {
                    statement.setInt(1, sectionId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            return rollbackAndReturn(conn, AddResult.SECTION_NOT_FOUND);
                        }
                        subjectId = rs.getInt("ma_monhoc");
                        specializationId = rs.getInt("ma_chuyennganh");
                        sectionStatus = rs.getString("trang_thai");
                    }
                }

                if (!"MoLop".equals(sectionStatus) && !"DangHoc".equals(sectionStatus)) {
                    return rollbackAndReturn(conn, AddResult.SECTION_CLOSED);
                }

                int studentSpecializationId;
                try (PreparedStatement statement = conn.prepareStatement(LOCK_STUDENT)) {
                    statement.setInt(1, studentId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()
                                || !rs.getBoolean("hoat_dong")
                                || !"DangHoc".equals(rs.getString("trang_thai"))) {
                            return rollbackAndReturn(conn, AddResult.STUDENT_NOT_ACTIVE);
                        }
                        studentSpecializationId = rs.getInt("ma_chuyennganh");
                    }
                }

                if (studentSpecializationId != specializationId) {
                    return rollbackAndReturn(conn, AddResult.WRONG_SPECIALIZATION);
                }
                if (exists(conn, ALREADY_ENROLLED, sectionId, studentId)) {
                    return rollbackAndReturn(conn, AddResult.ALREADY_ENROLLED);
                }
                if (exists(conn, DUPLICATE_SUBJECT, studentId, sectionId, subjectId)) {
                    return rollbackAndReturn(conn, AddResult.DUPLICATE_SUBJECT);
                }
                // Yeu cau nghiep vu: sinh vien duoc dang ky nhieu mon hoc trong cung
                // hoc ky, KHONG kiem tra/chan khi cac mon co lich hoc trung gio.
                // (Truoc day co kiem tra SCHEDULE_CONFLICT o day - da bo.)

                try (PreparedStatement statement = conn.prepareStatement(INSERT_MEMBERSHIP)) {
                    statement.setInt(1, studentId);
                    statement.setInt(2, sectionId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Không thêm được danh sách lớp học phần.");
                    }
                }
                conn.commit();
                return AddResult.ADDED;
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public RemoveResult removeAtomically(int sectionId, int membershipId) {
        try (Connection conn = DBConnect.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            try {
                if (!exists(conn, LOCK_MEMBERSHIP, membershipId, sectionId)) {
                    return rollbackAndReturn(conn, RemoveResult.MEMBERSHIP_NOT_FOUND);
                }
                if (exists(conn, HAS_GRADES, membershipId)) {
                    return rollbackAndReturn(conn, RemoveResult.HAS_GRADES);
                }
                if (exists(conn, HAS_ATTENDANCE, membershipId)) {
                    return rollbackAndReturn(conn, RemoveResult.HAS_ATTENDANCE);
                }

                try (PreparedStatement statement = conn.prepareStatement(DELETE_MEMBERSHIP)) {
                    statement.setInt(1, membershipId);
                    statement.setInt(2, sectionId);
                    if (statement.executeUpdate() != 1) {
                        return rollbackAndReturn(conn, RemoveResult.MEMBERSHIP_NOT_FOUND);
                    }
                }
                conn.commit();
                return RemoveResult.REMOVED;
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    private List<SinhVien> queryStudents(String sql, int sectionId, String keyword) {
        List<SinhVien> students = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, sectionId);
            if (keyword != null) {
                String pattern = "%" + keyword + "%";
                statement.setString(2, keyword);
                statement.setString(3, pattern);
                statement.setString(4, pattern);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    students.add(mapStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
        return students;
    }

    private LopHocPhan mapSection(ResultSet rs) throws SQLException {
        LopHocPhan section = new LopHocPhan();
        section.setMaLophocphan(rs.getInt("ma_lophocphan"));
        section.setMaLhp(rs.getString("ma_lhp"));
        section.setMaMonhoc(rs.getInt("ma_monhoc"));
        section.setMaGiangvien(rs.getInt("ma_giangvien"));
        section.setMaHocky(rs.getInt("ma_hocky"));
        section.setTrangThai(rs.getString("trang_thai"));
        section.setTenMonHoc(rs.getString("ten_monhoc"));
        section.setSoTinChi(rs.getInt("so_tinchi"));
        section.setTenGiangVien(rs.getString("ten_giangvien"));
        section.setTenHocKy(rs.getString("ten_hocky"));
        section.setNamHoc(rs.getString("nam_hoc"));
        section.setSoLuongSV(rs.getInt("so_luong_sv"));
        return section;
    }

    private SinhVien mapStudent(ResultSet rs) throws SQLException {
        SinhVien student = new SinhVien();
        student.setMaDanhSach(rs.getInt("ma_danhsach"));
        student.setMaNguoidung(rs.getInt("ma_nguoidung"));
        student.setMaLop(rs.getInt("ma_lop"));
        student.setMssv(rs.getString("mssv"));
        student.setTrangThai(rs.getString("trang_thai"));
        student.setHoTen(rs.getString("ho_ten"));
        student.setEmail(rs.getString("email"));
        student.setTenLop(rs.getString("ten_lop"));
        return student;
    }

    private boolean exists(Connection conn, String sql, int... parameters) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setInt(i + 1, parameters[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private <T> T rollbackAndReturn(Connection conn, T result) throws SQLException {
        conn.rollback();
        return result;
    }

    private void rollback(Connection conn, SQLException cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập danh sách lớp học phần.", cause);
    }
}
