package Repository;

import Models.DiemDanh;
import Models.SinhVien;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class DiemDanhTransactionDatabaseTest {
    private static final LocalDate TODAY = LocalDate.of(2097, 5, 10);

    @Test
    void kiemTraQuyenTrangThaiQuanHeRollbackUpsertVaDocDungPhamVi() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DiemDanhRepository repository = new DiemDanhRepository();
            assertEquals(22, fixture.tableCount());

            assertEquals(DiemDanhRepository.BatchResult.SESSION_IN_PAST,
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.pastSchedule,
                            Arrays.asList(row(fixture.membershipA,
                                    fixture.pastSchedule, "CoMat")), TODAY));
            assertEquals(0, fixture.attendanceCount());
            assertEquals(DiemDanhRepository.BatchResult.SAVED,
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(row(fixture.membershipA,
                                    fixture.normalSchedule, "CoMat")), TODAY));
            assertEquals(DiemDanhRepository.BatchResult.SAVED,
                    repository.saveBatchAdmin(fixture.sectionA, fixture.futureSchedule,
                            Arrays.asList(row(fixture.membershipA,
                                    fixture.futureSchedule, "CoMat")), TODAY));
            assertEquals(2, fixture.attendanceCount());
            fixture.clearAttendance();

            assertEquals(DiemDanhRepository.BatchResult.SESSION_NOT_OWNED,
                    repository.saveBatch(fixture.teacherB, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(row(fixture.membershipA,
                                    fixture.normalSchedule, "CoMat"))));
            assertEquals(0, fixture.attendanceCount());

            for (int schedule : new int[]{fixture.leaveSchedule, fixture.cancelledSchedule}) {
                assertEquals(DiemDanhRepository.BatchResult.SESSION_NOT_NORMAL,
                        repository.saveBatch(fixture.teacherA, fixture.sectionA, schedule,
                                Arrays.asList(row(fixture.membershipA, schedule, "CoMat"))));
            }
            assertEquals(0, fixture.attendanceCount());

            assertEquals(DiemDanhRepository.BatchResult.RELATION_MISMATCH,
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(
                                    row(fixture.membershipA, fixture.normalSchedule, "CoMat"),
                                    row(fixture.foreignMembership,
                                            fixture.normalSchedule, "Vang"))));
            assertEquals(0, fixture.attendanceCount());

            assertEquals(DiemDanhRepository.BatchResult.UNKNOWN_MEMBERSHIP,
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(row(Integer.MAX_VALUE,
                                    fixture.normalSchedule, "CoMat"))));
            assertEquals(0, fixture.attendanceCount());

            assertEquals(DiemDanhRepository.BatchResult.RELATION_MISMATCH,
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(row(fixture.membershipA,
                                    fixture.leaveSchedule, "CoMat"))));
            assertEquals(0, fixture.attendanceCount());

            assertEquals(DiemDanhRepository.BatchResult.SAVED,
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(
                                    row(fixture.membershipA, fixture.normalSchedule, "CoMat"),
                                    row(fixture.membershipB, fixture.normalSchedule, "Vang"))));
            assertEquals("CoMat", fixture.state(fixture.membershipA));
            assertEquals("Vang", fixture.state(fixture.membershipB));

            assertEquals(DiemDanhRepository.BatchResult.SAVED,
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(
                                    row(fixture.membershipA, fixture.normalSchedule, "Vang"),
                                    row(fixture.membershipB, fixture.normalSchedule, "CoMat"))));
            assertEquals("Vang", fixture.state(fixture.membershipA));
            assertEquals("CoMat", fixture.state(fixture.membershipB));

            assertThrows(RepositoryException.class, () ->
                    repository.saveBatch(fixture.teacherA, fixture.sectionA,
                            fixture.normalSchedule,
                            Arrays.asList(
                                    row(fixture.membershipA, fixture.normalSchedule, "CoMat"),
                                    row(fixture.membershipB, fixture.normalSchedule, "Tre"))));
            assertEquals("Vang", fixture.state(fixture.membershipA));
            assertEquals("CoMat", fixture.state(fixture.membershipB));

            List<SinhVien> sheet = repository.findDiemDanhForBuoiHoc(
                    fixture.teacherA, fixture.sectionA, fixture.normalSchedule);
            assertEquals(2, sheet.size());
            assertEquals("Vang", stateFor(sheet, fixture.membershipA));
            assertEquals("CoMat", stateFor(sheet, fixture.membershipB));
            assertEquals(0, repository.findDiemDanhForBuoiHoc(
                    fixture.teacherB, fixture.sectionA, fixture.normalSchedule).size());
            assertEquals(0, repository.findDiemDanhForBuoiHoc(
                    fixture.teacherA, fixture.sectionB, fixture.normalSchedule).size());
            assertEquals(0, repository.findDiemDanhForBuoiHoc(
                    fixture.teacherA, fixture.sectionA, fixture.leaveSchedule).size());
        }
    }

    private static DiemDanh row(int membership, int schedule, String state) {
        DiemDanh value = new DiemDanh();
        value.setMaDanhsach(membership);
        value.setMaLichhoc(schedule);
        value.setTrangThai(state);
        return value;
    }

    private static String stateFor(List<SinhVien> rows, int membership) {
        for (SinhVien row : rows) {
            if (row.getMaDanhSach() == membership) return row.getTrangThaiDiemDanh();
        }
        return null;
    }

    private static final class Fixture implements AutoCloseable {
        private final String tag = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        private final String sectionPrefix = "T7-" + tag;
        private Connection connection;
        private int term;
        private int teacherA;
        private int teacherB;
        private int studentA;
        private int studentB;
        private int room;
        private int shift;
        private int subject;
        private int sectionA;
        private int sectionB;
        private int membershipA;
        private int membershipB;
        private int foreignMembership;
        private int normalSchedule;
        private int pastSchedule;
        private int futureSchedule;
        private int leaveSchedule;
        private int cancelledSchedule;

        private Fixture() throws Exception {
            try {
                connection = DBConnect.getConnection();
                connection.setAutoCommit(true);
                loadReferences();
                term = insert("INSERT INTO dbo.hocky "
                                + "(ten_hocky, block, nam_hoc, ngay_batdau, ngay_ketthuc) "
                                + "VALUES (?, 1, '2097', '2097-01-01', '2097-12-31')",
                        "DB attendance " + tag);
                sectionA = addSection(sectionPrefix + "-A", teacherA);
                sectionB = addSection(sectionPrefix + "-B", teacherB);
                membershipA = addMembership(sectionA, studentA);
                membershipB = addMembership(sectionA, studentB);
                foreignMembership = addMembership(sectionB, studentA);
                pastSchedule = addSchedule(sectionA, TODAY.minusDays(1),
                        "BinhThuong");
                normalSchedule = addSchedule(sectionA, TODAY,
                        "BinhThuong");
                futureSchedule = addSchedule(sectionA, TODAY.plusDays(1),
                        "BinhThuong");
                leaveSchedule = addSchedule(sectionA, LocalDate.of(2097, 5, 11),
                        "XinNghi");
                cancelledSchedule = addSchedule(sectionA, LocalDate.of(2097, 5, 12),
                        "DaHuy");
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
            subject = scalar("SELECT TOP 1 ma_monhoc FROM dbo.monhoc ORDER BY ma_monhoc");
            List<Integer> teachers = values("SELECT TOP 2 ma_nguoidung FROM dbo.giangvien "
                    + "ORDER BY ma_nguoidung");
            List<Integer> students = values("SELECT TOP 2 ma_nguoidung FROM dbo.sinhvien "
                    + "WHERE trang_thai = 'DangHoc' ORDER BY ma_nguoidung");
            if (teachers.size() < 2 || students.size() < 2) {
                throw new SQLException("DB contract cần ít nhất 2 giảng viên và 2 sinh viên.");
            }
            teacherA = teachers.get(0);
            teacherB = teachers.get(1);
            studentA = students.get(0);
            studentB = students.get(1);
            room = scalar("SELECT TOP 1 ma_phong FROM dbo.phonghoc ORDER BY ma_phong");
            shift = scalar("SELECT TOP 1 ma_ca FROM dbo.cahoc ORDER BY ma_ca");
        }

        private int addSection(String code, int teacher) throws SQLException {
            return insert("INSERT INTO dbo.lophocphan "
                            + "(ma_lhp, ma_monhoc, ma_giangvien, ma_hocky, trang_thai) "
                            + "VALUES (?, ?, ?, ?, 'MoLop')",
                    code, subject, teacher, term);
        }

        private int addMembership(int section, int student) throws SQLException {
            return insert("INSERT INTO dbo.danhsach_sv_lhp "
                            + "(ma_nguoidung, ma_lophocphan) VALUES (?, ?)",
                    student, section);
        }

        private int addSchedule(int section, LocalDate date, String status)
                throws SQLException {
            return insert("INSERT INTO dbo.lichhoc "
                            + "(ma_lophocphan, ma_phong, ma_ca, ngay_hoc, loai_lich, trang_thai) "
                            + "VALUES (?, ?, ?, ?, 'HocChinh', ?)",
                    section, room, shift, Date.valueOf(date), status);
        }

        private int tableCount() throws SQLException {
            return scalar("SELECT COUNT(*) FROM sys.tables WHERE is_ms_shipped = 0");
        }

        private int attendanceCount() throws SQLException {
            return scalar("SELECT COUNT(*) FROM dbo.diemdanh dd "
                    + "JOIN dbo.danhsach_sv_lhp roster "
                    + "ON roster.ma_danhsach = dd.ma_danhsach "
                    + "WHERE roster.ma_lophocphan IN (?, ?)", sectionA, sectionB);
        }

        private void clearAttendance() throws SQLException {
            execute("DELETE dd FROM dbo.diemdanh dd "
                            + "JOIN dbo.danhsach_sv_lhp roster "
                            + "ON roster.ma_danhsach = dd.ma_danhsach "
                            + "WHERE roster.ma_lophocphan IN (?, ?)",
                    sectionA, sectionB);
        }

        private String state(int membership) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT trang_thai FROM dbo.diemdanh "
                            + "WHERE ma_danhsach = ? AND ma_lichhoc = ?")) {
                statement.setInt(1, membership);
                statement.setInt(2, normalSchedule);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getString(1) : null;
                }
            }
        }

        private List<Integer> values(String sql) throws SQLException {
            java.util.ArrayList<Integer> result = new java.util.ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.add(rs.getInt(1));
            }
            return result;
        }

        private int scalar(String sql, Object... parameters) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Fixture không có dữ liệu tham chiếu.");
                    return rs.getInt(1);
                }
            }
        }

        private int insert(String sql, Object... parameters) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, parameters);
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Fixture không ghi đúng một bản ghi.");
                }
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Fixture không nhận identity.");
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

        private void bind(PreparedStatement statement, Object... parameters)
                throws SQLException {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
        }

        @Override
        public void close() throws SQLException {
            if (connection == null) return;
            SQLException failure = null;
            try {
                connection.setAutoCommit(true);
                execute("DELETE dd FROM dbo.diemdanh dd "
                                + "JOIN dbo.danhsach_sv_lhp roster "
                                + "ON roster.ma_danhsach = dd.ma_danhsach "
                                + "JOIN dbo.lophocphan section "
                                + "ON section.ma_lophocphan = roster.ma_lophocphan "
                                + "WHERE section.ma_lhp LIKE ?", sectionPrefix + "%");
                execute("DELETE FROM dbo.lichhoc WHERE ma_lophocphan IN "
                                + "(SELECT ma_lophocphan FROM dbo.lophocphan WHERE ma_lhp LIKE ?)",
                        sectionPrefix + "%");
                execute("DELETE FROM dbo.danhsach_sv_lhp WHERE ma_lophocphan IN "
                                + "(SELECT ma_lophocphan FROM dbo.lophocphan WHERE ma_lhp LIKE ?)",
                        sectionPrefix + "%");
                execute("DELETE FROM dbo.lophocphan WHERE ma_lhp LIKE ?", sectionPrefix + "%");
                if (term > 0) execute("DELETE FROM dbo.hocky WHERE ma_hocky = ?", term);
            } catch (SQLException cleanupError) {
                failure = cleanupError;
            }
            try {
                connection.close();
            } catch (SQLException closeError) {
                if (failure == null) failure = closeError;
                else failure.addSuppressed(closeError);
            }
            connection = null;
            if (failure != null) throw failure;
        }
    }
}
