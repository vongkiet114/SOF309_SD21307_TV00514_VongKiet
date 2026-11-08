package Repository;

import Models.KyThi;
import Models.LichHoc;
import Models.ScheduleConflict;
import Models.ScheduleWriteResult;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class ScheduleCommandRepository {
    private static final String LOCK_SECTION_SQL =
            "SELECT lhp.ma_lhp, lhp.ma_giangvien, lhp.trang_thai, "
                    + "hk.ngay_batdau, hk.ngay_ketthuc "
                    + "FROM dbo.lophocphan lhp WITH (UPDLOCK, HOLDLOCK) "
                    + "JOIN dbo.hocky hk WITH (UPDLOCK, HOLDLOCK) "
                    + "ON hk.ma_hocky = lhp.ma_hocky "
                    + "WHERE lhp.ma_lophocphan = ?";

    private static final String FIND_CONFLICT_SQL =
            "WITH scheduled_event AS ("
                    + "SELECT CAST('CLASS' AS varchar(5)) AS existing_type, "
                    + "lh.ma_lichhoc AS existing_id, lh.ma_lophocphan AS existing_section, "
                    + "lh.ma_phong AS existing_room, existing_lhp.ma_giangvien AS existing_teacher, "
                    + "existing_lhp.ma_lhp AS existing_section_code "
                    + "FROM dbo.lichhoc lh WITH (UPDLOCK, HOLDLOCK) "
                    + "JOIN dbo.lophocphan existing_lhp WITH (HOLDLOCK) "
                    + "ON existing_lhp.ma_lophocphan = lh.ma_lophocphan "
                    + "WHERE lh.ngay_hoc = ? AND lh.ma_ca = ? "
                    + "AND lh.trang_thai = 'BinhThuong' "
                    + "AND (? <= 0 OR lh.ma_lichhoc <> ?) "
                    + "UNION ALL "
                    + "SELECT CAST('EXAM' AS varchar(5)), kt.ma_kythi, kt.ma_lophocphan, "
                    + "kt.ma_phong, existing_lhp.ma_giangvien, existing_lhp.ma_lhp "
                    + "FROM dbo.kythi kt WITH (UPDLOCK, HOLDLOCK) "
                    + "JOIN dbo.lophocphan existing_lhp WITH (HOLDLOCK) "
                    + "ON existing_lhp.ma_lophocphan = kt.ma_lophocphan "
                    + "WHERE kt.ngay_thi = ? AND kt.ma_ca = ? "
                    + "AND (? <= 0 OR kt.ma_kythi <> ?)"
                    + ") "
                    + "SELECT TOP (1) decision.conflict_kind, event.existing_type, "
                    + "event.existing_section_code "
                    + "FROM scheduled_event event "
                    + "CROSS APPLY (SELECT CASE "
                    + "WHEN event.existing_room = ? THEN 'ROOM' "
                    + "WHEN event.existing_teacher = ? THEN 'TEACHER' "
                    + "WHEN EXISTS (SELECT 1 "
                    + "FROM dbo.danhsach_sv_lhp candidate_roster WITH (HOLDLOCK) "
                    + "JOIN dbo.danhsach_sv_lhp existing_roster WITH (HOLDLOCK) "
                    + "ON existing_roster.ma_nguoidung = candidate_roster.ma_nguoidung "
                    + "WHERE candidate_roster.ma_lophocphan = ? "
                    + "AND existing_roster.ma_lophocphan = event.existing_section) "
                    + "THEN 'STUDENT' END AS conflict_kind) decision "
                    + "WHERE decision.conflict_kind IS NOT NULL "
                    + "ORDER BY CASE decision.conflict_kind "
                    + "WHEN 'ROOM' THEN 1 WHEN 'TEACHER' THEN 2 ELSE 3 END, "
                    + "event.existing_type, event.existing_id";

    private static final String FIND_SECTION_EXAMS_SQL =
            "SELECT loai_kythi, ngay_thi "
                    + "FROM dbo.kythi WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE ma_lophocphan = ? AND (? <= 0 OR ma_kythi <> ?)";

    public ScheduleWriteResult saveClass(LichHoc value) {
        try (Connection connection = DBConnect.getConnection()) {
            begin(connection);
            try {
                SectionContext section = lockSection(connection, value.getMaLophocphan(),
                        value.getNgayHoc());
                if ("BinhThuong".equals(value.getTrangThai())) {
                    ScheduleConflict conflict = findConflict(connection,
                            value.getNgayHoc(), value.getMaCa(), value.getMaPhong(),
                            section.teacherId, value.getMaLophocphan(),
                            value.getMaLichhoc(), 0);
                    if (conflict != null) {
                        connection.rollback();
                        return ScheduleWriteResult.rejected(conflict);
                    }
                }

                int id = writeClass(connection, value);
                connection.commit();
                return ScheduleWriteResult.saved(id);
            } catch (SQLException e) {
                rollback(connection, e);
                throw dataAccessError("Không thể lưu lịch học.", e);
            } catch (RepositoryException e) {
                rollback(connection, e);
                throw e;
            } catch (RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        } catch (SQLException e) {
            throw dataAccessError("Không thể mở kết nối để lưu lịch học.", e);
        }
    }

    public ScheduleWriteResult saveExam(KyThi value) {
        try (Connection connection = DBConnect.getConnection()) {
            begin(connection);
            try {
                SectionContext section = lockSection(connection, value.getMaLophocphan(),
                        value.getNgayThi());
                ScheduleConflict examRule = findExamRuleViolation(connection, value, section);
                if (examRule != null) {
                    connection.rollback();
                    return ScheduleWriteResult.rejected(examRule);
                }
                ScheduleConflict conflict = findConflict(connection,
                        value.getNgayThi(), value.getMaCa(), value.getMaPhong(),
                        section.teacherId, value.getMaLophocphan(),
                        0, value.getMaKythi());
                if (conflict != null) {
                    connection.rollback();
                    return ScheduleWriteResult.rejected(conflict);
                }

                int id = writeExam(connection, value);
                connection.commit();
                return ScheduleWriteResult.saved(id);
            } catch (SQLException e) {
                rollback(connection, e);
                throw dataAccessError("Không thể lưu lịch thi.", e);
            } catch (RepositoryException e) {
                rollback(connection, e);
                throw e;
            } catch (RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        } catch (SQLException e) {
            throw dataAccessError("Không thể mở kết nối để lưu lịch thi.", e);
        }
    }

    private void begin(Connection connection) throws SQLException {
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        connection.setAutoCommit(false);
    }

    private SectionContext lockSection(Connection connection, int sectionId, LocalDate date)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_SECTION_SQL)) {
            statement.setInt(1, sectionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new RepositoryException(
                            "Không thể lưu lịch vì lớp học phần không tồn tại.", null);
                }
                String status = rs.getString("trang_thai");
                if ("HoanThanh".equals(status) || "Huy".equals(status)) {
                    throw new RepositoryException(
                            "Không thể lưu lịch cho lớp học phần đã đóng.", null);
                }
                LocalDate start = rs.getDate("ngay_batdau").toLocalDate();
                LocalDate end = rs.getDate("ngay_ketthuc").toLocalDate();
                if (date == null || date.isBefore(start) || date.isAfter(end)) {
                    throw new RepositoryException(
                            "Ngày xếp lịch phải nằm trong học kỳ của lớp học phần.", null);
                }
                return new SectionContext(rs.getString("ma_lhp"),
                        rs.getInt("ma_giangvien"));
            }
        }
    }

    private ScheduleConflict findConflict(Connection connection, LocalDate date, int shiftId,
                                          int roomId, int teacherId, int sectionId,
                                          int excludedClassId, int excludedExamId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_CONFLICT_SQL)) {
            int parameter = 1;
            statement.setDate(parameter++, Date.valueOf(date));
            statement.setInt(parameter++, shiftId);
            statement.setInt(parameter++, excludedClassId);
            statement.setInt(parameter++, excludedClassId);
            statement.setDate(parameter++, Date.valueOf(date));
            statement.setInt(parameter++, shiftId);
            statement.setInt(parameter++, excludedExamId);
            statement.setInt(parameter++, excludedExamId);
            statement.setInt(parameter++, roomId);
            statement.setInt(parameter++, teacherId);
            statement.setInt(parameter, sectionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ScheduleConflict(
                        ScheduleConflict.Kind.valueOf(rs.getString("conflict_kind")),
                        ScheduleConflict.ExistingType.valueOf(rs.getString("existing_type")),
                        rs.getString("existing_section_code"));
            }
        }
    }

    private ScheduleConflict findExamRuleViolation(Connection connection, KyThi candidate,
                                                   SectionContext section) throws SQLException {
        boolean duplicate = false;
        boolean invalidOrder = false;
        try (PreparedStatement statement = connection.prepareStatement(FIND_SECTION_EXAMS_SQL)) {
            statement.setInt(1, candidate.getMaLophocphan());
            statement.setInt(2, candidate.getMaKythi());
            statement.setInt(3, candidate.getMaKythi());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String existingType = rs.getString("loai_kythi");
                    LocalDate existingDate = rs.getDate("ngay_thi").toLocalDate();
                    if (candidate.getLoaiKythi().equals(existingType)) {
                        duplicate = true;
                    }
                    if ("GiuaKy".equals(candidate.getLoaiKythi())
                            && "CuoiKy".equals(existingType)
                            && !candidate.getNgayThi().isBefore(existingDate)) {
                        invalidOrder = true;
                    }
                    if ("CuoiKy".equals(candidate.getLoaiKythi())
                            && "GiuaKy".equals(existingType)
                            && !existingDate.isBefore(candidate.getNgayThi())) {
                        invalidOrder = true;
                    }
                }
            }
        }
        if (duplicate) {
            return new ScheduleConflict(ScheduleConflict.Kind.DUPLICATE_EXAM,
                    ScheduleConflict.ExistingType.EXAM, section.sectionCode);
        }
        if (invalidOrder) {
            return new ScheduleConflict(ScheduleConflict.Kind.EXAM_ORDER,
                    ScheduleConflict.ExistingType.EXAM, section.sectionCode);
        }
        return null;
    }

    private int writeClass(Connection connection, LichHoc value) throws SQLException {
        if (value.getMaLichhoc() > 0) {
            String sql = "UPDATE dbo.lichhoc SET ma_lophocphan = ?, ma_phong = ?, "
                    + "ma_ca = ?, ngay_hoc = ?, loai_lich = ?, trang_thai = ? "
                    + "WHERE ma_lichhoc = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindClass(statement, value);
                statement.setInt(7, value.getMaLichhoc());
                if (statement.executeUpdate() != 1) {
                    throw new RepositoryException(
                            "Không tìm thấy lịch học cần cập nhật.", null);
                }
                return value.getMaLichhoc();
            }
        }
        String sql = "INSERT INTO dbo.lichhoc "
                + "(ma_lophocphan, ma_phong, ma_ca, ngay_hoc, loai_lich, trang_thai) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bindClass(statement, value);
            if (statement.executeUpdate() != 1) {
                throw new RepositoryException("Không thể tạo lịch học.", null);
            }
            return generatedId(statement, "lịch học");
        }
    }

    private void bindClass(PreparedStatement statement, LichHoc value) throws SQLException {
        statement.setInt(1, value.getMaLophocphan());
        statement.setInt(2, value.getMaPhong());
        statement.setInt(3, value.getMaCa());
        statement.setDate(4, Date.valueOf(value.getNgayHoc()));
        statement.setString(5, value.getLoaiLich());
        statement.setString(6, value.getTrangThai());
    }

    private int writeExam(Connection connection, KyThi value) throws SQLException {
        if (value.getMaKythi() > 0) {
            String sql = "UPDATE dbo.kythi SET ma_lophocphan = ?, ma_phong = ?, "
                    + "ma_ca = ?, ngay_thi = ?, loai_kythi = ? WHERE ma_kythi = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindExam(statement, value);
                statement.setInt(6, value.getMaKythi());
                if (statement.executeUpdate() != 1) {
                    throw new RepositoryException("Không tìm thấy lịch thi cần cập nhật.", null);
                }
                return value.getMaKythi();
            }
        }
        String sql = "INSERT INTO dbo.kythi "
                + "(ma_lophocphan, ma_phong, ma_ca, ngay_thi, loai_kythi) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bindExam(statement, value);
            if (statement.executeUpdate() != 1) {
                throw new RepositoryException("Không thể tạo lịch thi.", null);
            }
            return generatedId(statement, "lịch thi");
        }
    }

    private void bindExam(PreparedStatement statement, KyThi value) throws SQLException {
        statement.setInt(1, value.getMaLophocphan());
        statement.setInt(2, value.getMaPhong());
        statement.setInt(3, value.getMaCa());
        statement.setDate(4, Date.valueOf(value.getNgayThi()));
        statement.setString(5, value.getLoaiKythi());
    }

    private int generatedId(PreparedStatement statement, String recordName) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new RepositoryException(
                        "Không nhận được mã " + recordName + " vừa tạo.", null);
            }
            return keys.getInt(1);
        }
    }

    private void rollback(Connection connection, Throwable cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }

    private RepositoryException dataAccessError(String message, SQLException cause) {
        return new RepositoryException(message, cause);
    }

    private static final class SectionContext {
        private final String sectionCode;
        private final int teacherId;

        private SectionContext(String sectionCode, int teacherId) {
            this.sectionCode = sectionCode;
            this.teacherId = teacherId;
        }
    }
}
