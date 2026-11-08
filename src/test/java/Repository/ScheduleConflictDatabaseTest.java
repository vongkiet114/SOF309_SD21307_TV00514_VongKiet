package Repository;

import Models.KyThi;
import Models.LichHoc;
import Models.ScheduleConflict;
import Models.ScheduleWriteResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class ScheduleConflictDatabaseTest {
    @Test
    void phatHienPhongGiangVienSinhVienChoHocHocThiThiVaHaiChieuCheo() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ScheduleCommandRepository repository = new ScheduleCommandRepository();
            for (ScheduleConflict.Kind kind : new ScheduleConflict.Kind[]{
                    ScheduleConflict.Kind.ROOM,
                    ScheduleConflict.Kind.TEACHER,
                    ScheduleConflict.Kind.STUDENT}) {
                fixture.assertConflict(repository, EventType.CLASS, EventType.CLASS, kind);
                fixture.assertConflict(repository, EventType.EXAM, EventType.EXAM, kind);
                fixture.assertConflict(repository, EventType.CLASS, EventType.EXAM, kind);
                fixture.assertConflict(repository, EventType.EXAM, EventType.CLASS, kind);
            }
        }
    }

    @Test
    void capNhatLoaiChinhBanGhiHienTaiVaLichNghiKhongGiuTaiNguyen() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ScheduleCommandRepository repository = new ScheduleCommandRepository();
            LocalDate date = LocalDate.of(2098, 5, 10);

            LichHoc savedClass = fixture.classCandidate(date, "BinhThuong");
            ScheduleWriteResult insertedClass = repository.saveClass(savedClass);
            assertTrue(insertedClass.isSaved());
            savedClass.setMaLichhoc(insertedClass.getId());
            assertEquals(insertedClass.getId(), repository.saveClass(savedClass).getId());
            assertEquals(1, fixture.countCandidateClasses());

            fixture.clearEvents();
            KyThi savedExam = fixture.examCandidate(date, "BaoVe");
            ScheduleWriteResult insertedExam = repository.saveExam(savedExam);
            assertTrue(insertedExam.isSaved());
            savedExam.setMaKythi(insertedExam.getId());
            assertEquals(insertedExam.getId(), repository.saveExam(savedExam).getId());
            assertEquals(1, fixture.countCandidateExams());

            fixture.clearEvents();
            fixture.addExam(fixture.roomSection, date, fixture.roomA, "BaoVe");
            assertTrue(repository.saveClass(fixture.classCandidate(date, "XinNghi")).isSaved(),
                    "Lịch XinNghi mới không được giữ tài nguyên");

            fixture.clearEvents();
            fixture.addClass(fixture.roomSection, date, fixture.roomA, "XinNghi");
            assertTrue(repository.saveExam(fixture.examCandidate(date, "BaoVe")).isSaved(),
                    "Lịch XinNghi hiện hữu không được giữ tài nguyên");

            fixture.clearEvents();
            fixture.addClass(fixture.roomSection, date, fixture.roomA, "DaHuy");
            assertTrue(repository.saveClass(fixture.classCandidate(date, "BinhThuong")).isSaved(),
                    "Lịch DaHuy hiện hữu không được giữ tài nguyên");
        }
    }

    @Test
    void tuChoiTrungLoaiKyThiVaThuTuGiuaKyCuoiKy() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ScheduleCommandRepository repository = new ScheduleCommandRepository();

            fixture.addExam(fixture.candidateSection, LocalDate.of(2098, 6, 1),
                    fixture.roomB, "BaoVe");
            ScheduleWriteResult duplicate = repository.saveExam(
                    fixture.examCandidate(LocalDate.of(2098, 6, 2), "BaoVe"));
            assertFalse(duplicate.isSaved());
            assertEquals(ScheduleConflict.Kind.DUPLICATE_EXAM,
                    duplicate.getConflict().getKind());
            assertEquals(1, fixture.countCandidateExams());

            fixture.clearEvents();
            fixture.addExam(fixture.candidateSection, LocalDate.of(2098, 7, 10),
                    fixture.roomB, "CuoiKy");
            ScheduleWriteResult middleAfterFinal = repository.saveExam(
                    fixture.examCandidate(LocalDate.of(2098, 7, 10), "GiuaKy"));
            assertFalse(middleAfterFinal.isSaved());
            assertEquals(ScheduleConflict.Kind.EXAM_ORDER,
                    middleAfterFinal.getConflict().getKind());

            fixture.clearEvents();
            fixture.addExam(fixture.candidateSection, LocalDate.of(2098, 8, 10),
                    fixture.roomB, "GiuaKy");
            ScheduleWriteResult finalBeforeMiddle = repository.saveExam(
                    fixture.examCandidate(LocalDate.of(2098, 8, 10), "CuoiKy"));
            assertFalse(finalBeforeMiddle.isSaved());
            assertEquals(ScheduleConflict.Kind.EXAM_ORDER,
                    finalBeforeMiddle.getConflict().getKind());
        }
    }

    @Test
    void tuChoiLopKhongTonTaiLopDaDongVaNgayNgoaiHocKyMaKhongGhiDoDang() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ScheduleCommandRepository repository = new ScheduleCommandRepository();
            LichHoc missing = fixture.classCandidate(LocalDate.of(2098, 9, 1), "BinhThuong");
            missing.setMaLophocphan(Integer.MAX_VALUE);
            assertThrows(RepositoryException.class, () -> repository.saveClass(missing));
            assertEquals(0, fixture.countCandidateClasses());

            fixture.setCandidateStatus("Huy");
            assertThrows(RepositoryException.class, () -> repository.saveClass(
                    fixture.classCandidate(LocalDate.of(2098, 9, 1), "BinhThuong")));
            assertEquals(0, fixture.countCandidateClasses());

            fixture.setCandidateStatus("MoLop");
            assertThrows(RepositoryException.class, () -> repository.saveExam(
                    fixture.examCandidate(LocalDate.of(2099, 1, 1), "BaoVe")));
            assertEquals(0, fixture.countCandidateExams());
        }
    }

    private enum EventType {
        CLASS,
        EXAM
    }

    private static final class Fixture implements AutoCloseable {
        private final String tag = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        private final String sectionPrefix = "T4-" + tag;
        private Connection connection;
        private int term;
        private int subject;
        private int teacherA;
        private int teacherB;
        private int studentA;
        private int studentB;
        private int roomA;
        private int roomB;
        private int shift;
        private int candidateSection;
        private int roomSection;
        private int teacherSection;
        private int studentSection;

        Fixture() throws Exception {
            try {
                connection = DBConnect.getConnection();
                connection.setAutoCommit(true);
                loadReferences();
                term = insert("INSERT INTO dbo.hocky "
                                + "(ten_hocky, block, nam_hoc, ngay_batdau, ngay_ketthuc) "
                                + "VALUES (?, 1, '2098', '2098-01-01', '2098-12-31')",
                        "DB contract " + tag);
                candidateSection = addSection(sectionPrefix + "-C", teacherA);
                roomSection = addSection(sectionPrefix + "-R", teacherB);
                teacherSection = addSection(sectionPrefix + "-T", teacherA);
                studentSection = addSection(sectionPrefix + "-S", teacherB);
                addMembership(candidateSection, studentA);
                addMembership(roomSection, studentB);
                addMembership(teacherSection, studentB);
                addMembership(studentSection, studentA);
            } catch (Exception setupError) {
                try {
                    close();
                } catch (Exception cleanupError) {
                    setupError.addSuppressed(cleanupError);
                }
                throw setupError;
            }
        }

        private void assertConflict(ScheduleCommandRepository repository,
                                    EventType candidateType,
                                    EventType existingType,
                                    ScheduleConflict.Kind kind) throws Exception {
            clearEvents();
            int existingSection = sectionFor(kind);
            int existingRoom = kind == ScheduleConflict.Kind.ROOM ? roomA : roomB;
            LocalDate date = LocalDate.of(2098, 4, 10);
            addEvent(existingType, existingSection, date, existingRoom, "BinhThuong");

            ScheduleWriteResult result = candidateType == EventType.CLASS
                    ? repository.saveClass(classCandidate(date, "BinhThuong"))
                    : repository.saveExam(examCandidate(date, "BaoVe"));

            assertFalse(result.isSaved(), candidateType + " - " + existingType + " - " + kind);
            assertEquals(kind, result.getConflict().getKind());
            assertEquals(existingType == EventType.CLASS
                            ? ScheduleConflict.ExistingType.CLASS
                            : ScheduleConflict.ExistingType.EXAM,
                    result.getConflict().getExistingType());
            assertEquals(sectionCode(existingSection),
                    result.getConflict().getExistingSectionCode());
            if (candidateType == EventType.CLASS) {
                assertEquals(0, countCandidateClasses());
            } else {
                assertEquals(0, countCandidateExams());
            }
        }

        private int sectionFor(ScheduleConflict.Kind kind) {
            switch (kind) {
                case ROOM:
                    return roomSection;
                case TEACHER:
                    return teacherSection;
                default:
                    return studentSection;
            }
        }

        private LichHoc classCandidate(LocalDate date, String status) {
            return new LichHoc(0, candidateSection, roomA, shift, date,
                    "HocChinh", status);
        }

        private KyThi examCandidate(LocalDate date, String type) {
            return new KyThi(0, candidateSection, roomA, shift, date, type);
        }

        private void loadReferences() throws SQLException {
            subject = scalar("SELECT TOP 1 ma_monhoc FROM dbo.monhoc ORDER BY ma_monhoc");
            List<Integer> teachers = values("SELECT TOP 2 ma_nguoidung FROM dbo.giangvien "
                    + "ORDER BY ma_nguoidung");
            List<Integer> students = values("SELECT TOP 2 ma_nguoidung FROM dbo.sinhvien "
                    + "WHERE trang_thai = 'DangHoc' ORDER BY ma_nguoidung");
            List<Integer> rooms = values("SELECT TOP 2 ma_phong FROM dbo.phonghoc ORDER BY ma_phong");
            if (teachers.size() < 2 || students.size() < 2 || rooms.size() < 2) {
                throw new SQLException("DB contract cần ít nhất 2 giảng viên, sinh viên và phòng.");
            }
            teacherA = teachers.get(0);
            teacherB = teachers.get(1);
            studentA = students.get(0);
            studentB = students.get(1);
            roomA = rooms.get(0);
            roomB = rooms.get(1);
            shift = scalar("SELECT TOP 1 ma_ca FROM dbo.cahoc ORDER BY ma_ca");
        }

        private int addSection(String code, int teacher) throws SQLException {
            return insert("INSERT INTO dbo.lophocphan "
                            + "(ma_lhp, ma_monhoc, ma_giangvien, ma_hocky, trang_thai) "
                            + "VALUES (?, ?, ?, ?, 'MoLop')",
                    code, subject, teacher, term);
        }

        private void addMembership(int section, int student) throws SQLException {
            insert("INSERT INTO dbo.danhsach_sv_lhp "
                    + "(ma_nguoidung, ma_lophocphan) VALUES (?, ?)", student, section);
        }

        private int addClass(int section, LocalDate date, int room, String status)
                throws SQLException {
            return insert("INSERT INTO dbo.lichhoc "
                            + "(ma_lophocphan, ma_phong, ma_ca, ngay_hoc, loai_lich, trang_thai) "
                            + "VALUES (?, ?, ?, ?, 'HocChinh', ?)",
                    section, room, shift, Date.valueOf(date), status);
        }

        private int addExam(int section, LocalDate date, int room, String type)
                throws SQLException {
            return insert("INSERT INTO dbo.kythi "
                            + "(ma_lophocphan, ma_phong, ma_ca, ngay_thi, loai_kythi) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    section, room, shift, Date.valueOf(date), type);
        }

        private void addEvent(EventType type, int section, LocalDate date,
                              int room, String classStatus) throws SQLException {
            if (type == EventType.CLASS) {
                addClass(section, date, room, classStatus);
            } else {
                addExam(section, date, room, "BaoVe");
            }
        }

        private int countCandidateClasses() throws SQLException {
            return scalar("SELECT COUNT(*) FROM dbo.lichhoc WHERE ma_lophocphan = ?",
                    candidateSection);
        }

        private int countCandidateExams() throws SQLException {
            return scalar("SELECT COUNT(*) FROM dbo.kythi WHERE ma_lophocphan = ?",
                    candidateSection);
        }

        private String sectionCode(int section) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT ma_lhp FROM dbo.lophocphan WHERE ma_lophocphan = ?")) {
                statement.setInt(1, section);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy lớp test.");
                    }
                    return rs.getString(1);
                }
            }
        }

        private void setCandidateStatus(String status) throws SQLException {
            execute("UPDATE dbo.lophocphan SET trang_thai = ? WHERE ma_lophocphan = ?",
                    status, candidateSection);
        }

        private void clearEvents() throws SQLException {
            execute("DELETE FROM dbo.kythi WHERE ma_lophocphan IN (?, ?, ?, ?)",
                    candidateSection, roomSection, teacherSection, studentSection);
            execute("DELETE FROM dbo.lichhoc WHERE ma_lophocphan IN (?, ?, ?, ?)",
                    candidateSection, roomSection, teacherSection, studentSection);
        }

        private List<Integer> values(String sql) throws SQLException {
            List<Integer> result = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
            return result;
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
                execute("DELETE FROM dbo.kythi WHERE ma_lophocphan IN "
                        + "(SELECT ma_lophocphan FROM dbo.lophocphan WHERE ma_lhp LIKE ?)",
                        sectionPrefix + "%");
                execute("DELETE FROM dbo.lichhoc WHERE ma_lophocphan IN "
                        + "(SELECT ma_lophocphan FROM dbo.lophocphan WHERE ma_lhp LIKE ?)",
                        sectionPrefix + "%");
                execute("DELETE FROM dbo.danhsach_sv_lhp WHERE ma_lophocphan IN "
                        + "(SELECT ma_lophocphan FROM dbo.lophocphan WHERE ma_lhp LIKE ?)",
                        sectionPrefix + "%");
                execute("DELETE FROM dbo.lophocphan WHERE ma_lhp LIKE ?", sectionPrefix + "%");
                if (term > 0) {
                    execute("DELETE FROM dbo.hocky WHERE ma_hocky = ?", term);
                }
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
