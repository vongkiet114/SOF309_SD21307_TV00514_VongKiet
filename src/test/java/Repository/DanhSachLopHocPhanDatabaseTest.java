package Repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class DanhSachLopHocPhanDatabaseTest {
    @Test
    void themSinhVienTuChoiTheoDungThuTuRollbackVaChiGhiKhiHopLe() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DanhSachLopHocPhanRepository repository = new DanhSachLopHocPhanRepository();
            int sameSubjectMembership = fixture.addMembership(fixture.sameSubjectSection);
            fixture.addMembership(fixture.otherSubjectSection);
            LocalDate conflictDate = LocalDate.of(2098, 3, 10);
            fixture.addClass(fixture.targetSection, conflictDate);
            fixture.addClass(fixture.otherSubjectSection, conflictDate);
            fixture.setSectionStatus("Huy");
            fixture.setStudentActive(false);
            fixture.moveStudent(fixture.wrongClass);

            assertEquals(DanhSachLopHocPhanRepository.AddResult.SECTION_NOT_FOUND,
                    repository.addAtomically(Integer.MAX_VALUE, fixture.student));
            assertNoTargetMembership(fixture);

            assertEquals(DanhSachLopHocPhanRepository.AddResult.SECTION_CLOSED,
                    repository.addAtomically(fixture.targetSection, fixture.student));
            assertNoTargetMembership(fixture);

            fixture.setSectionStatus("MoLop");
            assertEquals(DanhSachLopHocPhanRepository.AddResult.STUDENT_NOT_ACTIVE,
                    repository.addAtomically(fixture.targetSection, fixture.student));
            assertNoTargetMembership(fixture);

            fixture.setStudentActive(true);
            assertEquals(DanhSachLopHocPhanRepository.AddResult.WRONG_SPECIALIZATION,
                    repository.addAtomically(fixture.targetSection, fixture.student));
            assertNoTargetMembership(fixture);

            fixture.moveStudent(fixture.correctClass);
            int targetMembership = fixture.addMembership(fixture.targetSection);
            assertEquals(DanhSachLopHocPhanRepository.AddResult.ALREADY_ENROLLED,
                    repository.addAtomically(fixture.targetSection, fixture.student));
            assertEquals(1, fixture.membershipCount(fixture.targetSection));

            fixture.deleteMembership(targetMembership);
            assertEquals(DanhSachLopHocPhanRepository.AddResult.DUPLICATE_SUBJECT,
                    repository.addAtomically(fixture.targetSection, fixture.student));
            assertNoTargetMembership(fixture);

            fixture.deleteMembership(sameSubjectMembership);
            assertEquals(DanhSachLopHocPhanRepository.AddResult.SCHEDULE_CONFLICT,
                    repository.addAtomically(fixture.targetSection, fixture.student));
            assertNoTargetMembership(fixture);

            fixture.clearEvents();
            assertEquals(DanhSachLopHocPhanRepository.AddResult.ADDED,
                    repository.addAtomically(fixture.targetSection, fixture.student));
            assertEquals(1, fixture.membershipCount(fixture.targetSection));
        }
    }

    @Test
    void phatHienDuBonToHopXungDotHocVaThiMaKhongGhiDoDang() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DanhSachLopHocPhanRepository repository = new DanhSachLopHocPhanRepository();
            fixture.addMembership(fixture.otherSubjectSection);
            assertConflict(repository, fixture, EventType.CLASS, EventType.CLASS,
                    LocalDate.of(2098, 4, 1));
            assertConflict(repository, fixture, EventType.CLASS, EventType.EXAM,
                    LocalDate.of(2098, 4, 2));
            assertConflict(repository, fixture, EventType.EXAM, EventType.CLASS,
                    LocalDate.of(2098, 4, 3));
            assertConflict(repository, fixture, EventType.EXAM, EventType.EXAM,
                    LocalDate.of(2098, 4, 4));
        }
    }

    @Test
    void goSinhVienRollbackKhiCoDiemHoacDiemDanhVaXoaKhiSach() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DanhSachLopHocPhanRepository repository = new DanhSachLopHocPhanRepository();
            int membership = fixture.addMembership(fixture.targetSection);
            int schedule = fixture.addClass(fixture.targetSection, LocalDate.of(2098, 5, 1));
            int grade = fixture.addGrade(membership);
            int attendance = fixture.addAttendance(membership, schedule);

            assertEquals(DanhSachLopHocPhanRepository.RemoveResult.HAS_GRADES,
                    repository.removeAtomically(fixture.targetSection, membership));
            assertEquals(1, fixture.membershipCount(fixture.targetSection));
            assertEquals(1, fixture.countById("dbo.diemso", "ma_diem", grade));
            assertEquals(1, fixture.countById("dbo.diemdanh", "ma_diemdanh", attendance));

            fixture.deleteById("dbo.diemso", "ma_diem", grade);
            assertEquals(DanhSachLopHocPhanRepository.RemoveResult.HAS_ATTENDANCE,
                    repository.removeAtomically(fixture.targetSection, membership));
            assertEquals(1, fixture.membershipCount(fixture.targetSection));
            assertEquals(1, fixture.countById("dbo.diemdanh", "ma_diemdanh", attendance));

            fixture.deleteById("dbo.diemdanh", "ma_diemdanh", attendance);
            assertEquals(DanhSachLopHocPhanRepository.RemoveResult.REMOVED,
                    repository.removeAtomically(fixture.targetSection, membership));
            assertNoTargetMembership(fixture);
            assertEquals(DanhSachLopHocPhanRepository.RemoveResult.MEMBERSHIP_NOT_FOUND,
                    repository.removeAtomically(fixture.targetSection, membership));
        }
    }

    private static void assertConflict(DanhSachLopHocPhanRepository repository,
                                       Fixture fixture, EventType targetType,
                                       EventType existingType, LocalDate date) throws Exception {
        fixture.clearEvents();
        fixture.addEvent(targetType, fixture.targetSection, date);
        fixture.addEvent(existingType, fixture.otherSubjectSection, date);

        assertEquals(DanhSachLopHocPhanRepository.AddResult.SCHEDULE_CONFLICT,
                repository.addAtomically(fixture.targetSection, fixture.student),
                targetType + " - " + existingType);
        assertNoTargetMembership(fixture);
    }

    private static void assertNoTargetMembership(Fixture fixture) throws SQLException {
        assertEquals(0, fixture.membershipCount(fixture.targetSection));
    }

    private enum EventType {
        CLASS,
        EXAM
    }

    private static final class Fixture implements AutoCloseable {
        private final String tag = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
        private final String email = "task3-" + tag.toLowerCase(Locale.ROOT) + "@test.invalid";
        private final String sectionPrefix = "T3-" + tag;
        private Connection connection;
        private int student;
        private int correctClass;
        private int wrongClass;
        private int targetSection;
        private int sameSubjectSection;
        private int otherSubjectSection;
        private int subject;
        private int otherSubject;
        private int teacher;
        private int term;
        private int room;
        private int shift;
        private int gradeComponent;

        Fixture() throws Exception {
            try {
                connection = DBConnect.getConnection();
                connection.setAutoCommit(true);
                loadReferences();
                createStudent();
                targetSection = addSection(sectionPrefix + "-T", subject);
                sameSubjectSection = addSection(sectionPrefix + "-S", subject);
                otherSubjectSection = addSection(sectionPrefix + "-O", otherSubject);
            } catch (Exception setupError) {
                try {
                    close();
                } catch (Exception cleanupError) {
                    setupError.addSuppressed(cleanupError);
                }
                throw setupError;
            }
        }

        private void loadReferences() throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT TOP 1 a.ma_monhoc, b.ma_monhoc, a.ma_chuyennganh "
                            + "FROM dbo.monhoc a JOIN dbo.monhoc b "
                            + "ON b.ma_chuyennganh = a.ma_chuyennganh "
                            + "AND b.ma_monhoc > a.ma_monhoc ORDER BY a.ma_monhoc, b.ma_monhoc");
                 ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Cần ít nhất hai môn cùng chuyên ngành cho DB contract test.");
                }
                subject = rs.getInt(1);
                otherSubject = rs.getInt(2);
                int specialization = rs.getInt(3);
                correctClass = scalar("SELECT TOP 1 ma_lop FROM dbo.lop "
                        + "WHERE ma_chuyennganh = ? ORDER BY ma_lop", specialization);
                wrongClass = scalar("SELECT TOP 1 ma_lop FROM dbo.lop "
                        + "WHERE ma_chuyennganh <> ? ORDER BY ma_lop", specialization);
                teacher = scalar("SELECT TOP 1 ma_nguoidung FROM dbo.giangvien "
                        + "WHERE ma_chuyennganh = ? ORDER BY ma_nguoidung", specialization);
            }
            term = scalar("SELECT TOP 1 ma_hocky FROM dbo.hocky ORDER BY ma_hocky");
            room = scalar("SELECT TOP 1 ma_phong FROM dbo.phonghoc ORDER BY ma_phong");
            shift = scalar("SELECT TOP 1 ma_ca FROM dbo.cahoc ORDER BY ma_ca");
            gradeComponent = scalar("SELECT TOP 1 ma_thanhphan FROM dbo.thanhphan_diem "
                    + "WHERE ma_monhoc = ? ORDER BY ma_thanhphan", subject);
        }

        private void createStudent() throws SQLException {
            int studentRole = scalar("SELECT ma_vaitro FROM dbo.vaitro "
                    + "WHERE UPPER(ten_vaitro) = 'STUDENT'");
            student = insert("INSERT INTO dbo.nguoidung "
                            + "(email, mat_khau, ho_ten, ma_vaitro, hoat_dong) "
                            + "VALUES (?, 'test-only', N'Sinh viên DB contract', ?, 1)",
                    email, studentRole);
            execute("INSERT INTO dbo.sinhvien "
                            + "(ma_nguoidung, ma_lop, mssv, trang_thai) "
                            + "VALUES (?, ?, ?, 'DangHoc')",
                    student, correctClass, "T3" + tag);
        }

        private int addSection(String code, int subjectId) throws SQLException {
            return insert("INSERT INTO dbo.lophocphan "
                            + "(ma_lhp, ma_monhoc, ma_giangvien, ma_hocky, trang_thai) "
                            + "VALUES (?, ?, ?, ?, 'MoLop')",
                    code, subjectId, teacher, term);
        }

        private int addMembership(int sectionId) throws SQLException {
            return insert("INSERT INTO dbo.danhsach_sv_lhp "
                    + "(ma_nguoidung, ma_lophocphan) VALUES (?, ?)", student, sectionId);
        }

        private int addClass(int sectionId, LocalDate date) throws SQLException {
            return insert("INSERT INTO dbo.lichhoc "
                            + "(ma_lophocphan, ma_phong, ma_ca, ngay_hoc, loai_lich, trang_thai) "
                            + "VALUES (?, ?, ?, ?, 'HocChinh', 'BinhThuong')",
                    sectionId, room, shift, Date.valueOf(date));
        }

        private int addExam(int sectionId, LocalDate date) throws SQLException {
            return insert("INSERT INTO dbo.kythi "
                            + "(ma_lophocphan, ma_phong, ma_ca, ngay_thi, loai_kythi) "
                            + "VALUES (?, ?, ?, ?, 'GiuaKy')",
                    sectionId, room, shift, Date.valueOf(date));
        }

        private void addEvent(EventType type, int sectionId, LocalDate date) throws SQLException {
            if (type == EventType.CLASS) {
                addClass(sectionId, date);
            } else {
                addExam(sectionId, date);
            }
        }

        private int addGrade(int membership) throws SQLException {
            return insert("INSERT INTO dbo.diemso "
                            + "(ma_danhsach, ma_thanhphan, gia_tri_diem) VALUES (?, ?, ?)",
                    membership, gradeComponent, new BigDecimal("8.25"));
        }

        private int addAttendance(int membership, int schedule) throws SQLException {
            return insert("INSERT INTO dbo.diemdanh "
                            + "(ma_danhsach, ma_lichhoc, trang_thai) VALUES (?, ?, 'CoMat')",
                    membership, schedule);
        }

        private void setSectionStatus(String status) throws SQLException {
            execute("UPDATE dbo.lophocphan SET trang_thai = ? WHERE ma_lophocphan = ?",
                    status, targetSection);
        }

        private void setStudentActive(boolean active) throws SQLException {
            execute("UPDATE dbo.nguoidung SET hoat_dong = ? WHERE ma_nguoidung = ?",
                    active, student);
        }

        private void moveStudent(int classId) throws SQLException {
            execute("UPDATE dbo.sinhvien SET ma_lop = ? WHERE ma_nguoidung = ?",
                    classId, student);
        }

        private void clearEvents() throws SQLException {
            execute("DELETE FROM dbo.kythi WHERE ma_lophocphan IN (?, ?)",
                    targetSection, otherSubjectSection);
            execute("DELETE FROM dbo.lichhoc WHERE ma_lophocphan IN (?, ?)",
                    targetSection, otherSubjectSection);
        }

        private void deleteMembership(int membership) throws SQLException {
            deleteById("dbo.danhsach_sv_lhp", "ma_danhsach", membership);
        }

        private void deleteById(String table, String column, int id) throws SQLException {
            execute("DELETE FROM " + table + " WHERE " + column + " = ?", id);
        }

        private int countById(String table, String column, int id) throws SQLException {
            return scalar("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?", id);
        }

        private int membershipCount(int sectionId) throws SQLException {
            return scalar("SELECT COUNT(*) FROM dbo.danhsach_sv_lhp "
                            + "WHERE ma_nguoidung = ? AND ma_lophocphan = ?",
                    student, sectionId);
        }

        private int scalar(String sql, Object... parameters) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Truy vấn fixture không trả dữ liệu: " + sql);
                    }
                    return rs.getInt(1);
                }
            }
        }

        private int insert(String sql, Object... parameters) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, parameters);
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Fixture không ghi đúng một bản ghi: " + sql);
                }
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không nhận được identity cho fixture.");
                    }
                    return keys.getInt(1);
                }
            }
        }

        private void execute(String sql, Object... parameters) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                statement.executeUpdate();
            }
        }

        private void bind(PreparedStatement statement, Object... parameters) throws SQLException {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
        }

        @Override
        public void close() throws SQLException {
            if (connection == null) {
                return;
            }
            SQLException failure = null;
            try {
                connection.setAutoCommit(true);
                execute("DELETE roster FROM dbo.danhsach_sv_lhp roster "
                                + "LEFT JOIN dbo.nguoidung account "
                                + "ON account.ma_nguoidung = roster.ma_nguoidung "
                                + "LEFT JOIN dbo.lophocphan section "
                                + "ON section.ma_lophocphan = roster.ma_lophocphan "
                                + "WHERE account.email = ? OR section.ma_lhp LIKE ?",
                        email, sectionPrefix + "%");
                execute("DELETE FROM dbo.lophocphan WHERE ma_lhp LIKE ?",
                        sectionPrefix + "%");
                execute("DELETE FROM dbo.sinhvien WHERE ma_nguoidung IN "
                        + "(SELECT ma_nguoidung FROM dbo.nguoidung WHERE email = ?)", email);
                execute("DELETE FROM dbo.nguoidung WHERE email = ?", email);
            } catch (SQLException cleanupError) {
                failure = cleanupError;
            }
            try {
                connection.close();
            } catch (SQLException closeError) {
                if (failure == null) {
                    failure = closeError;
                } else {
                    failure.addSuppressed(closeError);
                }
            }
            connection = null;
            if (failure != null) {
                throw failure;
            }
        }
    }
}
