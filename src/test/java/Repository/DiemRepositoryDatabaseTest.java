package Repository;

import Models.DiemSo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class DiemRepositoryDatabaseTest {
    @Test
    void batchKiemTraQuyenQuanHeRollbackUpsertVaXoaOTrong() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DiemRepository repository = new DiemRepository();

            assertEquals(DiemRepository.BatchResult.SECTION_NOT_OWNED,
                    repository.saveBatch(Integer.MAX_VALUE, fixture.section,
                            Arrays.asList(cell(fixture.membershipA, fixture.componentA, "7"))));
            assertEquals(0, fixture.scoreCount());

            assertEquals(DiemRepository.BatchResult.UNKNOWN_MEMBERSHIP,
                    repository.saveBatch(fixture.teacher, fixture.section,
                            Arrays.asList(
                                    cell(fixture.membershipA, fixture.componentA, "7"),
                                    cell(Integer.MAX_VALUE, fixture.componentB, "8"))));
            assertEquals(0, fixture.scoreCount());

            assertEquals(DiemRepository.BatchResult.RELATION_MISMATCH,
                    repository.saveBatch(fixture.teacher, fixture.section,
                            Arrays.asList(cell(fixture.foreignMembership,
                                    fixture.componentA, "7"))));
            assertEquals(0, fixture.scoreCount());

            assertEquals(DiemRepository.BatchResult.SAVED,
                    repository.saveBatch(fixture.teacher, fixture.section,
                            Arrays.asList(
                                    cell(fixture.membershipA, fixture.componentA, "7.25"),
                                    cell(fixture.membershipB, fixture.componentB, "8.50"))));
            assertEquals(new BigDecimal("7.25"),
                    fixture.score(fixture.membershipA, fixture.componentA));
            assertEquals(new BigDecimal("8.50"),
                    fixture.score(fixture.membershipB, fixture.componentB));

            assertThrows(RepositoryException.class,
                    () -> repository.saveBatch(fixture.teacher, fixture.section,
                            Arrays.asList(
                                    cell(fixture.membershipA, fixture.componentA, "9.00"),
                                    cell(fixture.membershipB, fixture.componentB, "10.01"))));
            assertEquals(new BigDecimal("7.25"),
                    fixture.score(fixture.membershipA, fixture.componentA));
            assertEquals(new BigDecimal("8.50"),
                    fixture.score(fixture.membershipB, fixture.componentB));

            DiemSo blank = cell(fixture.membershipA, fixture.componentA, null);
            assertEquals(DiemRepository.BatchResult.SAVED,
                    repository.saveBatch(fixture.teacher, fixture.section,
                            Arrays.asList(blank,
                                    cell(fixture.membershipB, fixture.componentB, "9.75"))));
            assertNull(fixture.score(fixture.membershipA, fixture.componentA));
            assertEquals(new BigDecimal("9.75"),
                    fixture.score(fixture.membershipB, fixture.componentB));
        }
    }

    private static DiemSo cell(int membership, int component, String value) {
        DiemSo cell = new DiemSo();
        cell.setMaDanhsach(membership);
        cell.setMaThanhphan(component);
        cell.setGiaTriDiem(value == null ? null : new BigDecimal(value));
        return cell;
    }

    private static final class Fixture implements AutoCloseable {
        private final String tag = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        private final String sectionPrefix = "T5-" + tag;
        private final String subjectCode = "T5" + tag.substring(0, 6);
        private Connection connection;
        private int subject;
        private int term;
        private int teacher;
        private int studentA;
        private int studentB;
        private int section;
        private int foreignSection;
        private int membershipA;
        private int membershipB;
        private int foreignMembership;
        private int componentA;
        private int componentB;

        private Fixture() throws Exception {
            try {
                connection = DBConnect.getConnection();
                connection.setAutoCommit(true);
                teacher = scalar("SELECT TOP 1 ma_nguoidung FROM dbo.giangvien ORDER BY ma_nguoidung");
                studentA = scalar("SELECT TOP 1 ma_nguoidung FROM dbo.sinhvien ORDER BY ma_nguoidung");
                studentB = scalar("SELECT TOP 1 ma_nguoidung FROM dbo.sinhvien "
                        + "WHERE ma_nguoidung <> ? ORDER BY ma_nguoidung", studentA);
                int major = scalar("SELECT TOP 1 ma_chuyennganh FROM dbo.chuyennganh "
                        + "ORDER BY ma_chuyennganh");
                subject = insert("INSERT INTO dbo.monhoc "
                                + "(ma_mon, ten_monhoc, so_tinchi, ma_chuyennganh) "
                                + "VALUES (?, ?, 3, ?)",
                        subjectCode, "DB grade " + tag, major);
                componentA = insert("INSERT INTO dbo.thanhphan_diem "
                                + "(ten_thanhphan, trong_so, ma_monhoc) VALUES (?, 0.40, ?)",
                        "Quá trình " + tag, subject);
                componentB = insert("INSERT INTO dbo.thanhphan_diem "
                                + "(ten_thanhphan, trong_so, ma_monhoc) VALUES (?, 0.60, ?)",
                        "Cuối kỳ " + tag, subject);
                term = insert("INSERT INTO dbo.hocky "
                                + "(ten_hocky, block, nam_hoc, ngay_batdau, ngay_ketthuc) "
                                + "VALUES (?, 1, '2097', '2097-01-01', '2097-12-31')",
                        "DB grade " + tag);
                section = addSection(sectionPrefix + "-A");
                foreignSection = addSection(sectionPrefix + "-B");
                membershipA = addMembership(section, studentA);
                membershipB = addMembership(section, studentB);
                foreignMembership = addMembership(foreignSection, studentA);
            } catch (Exception setupError) {
                try {
                    close();
                } catch (Exception cleanupError) {
                    setupError.addSuppressed(cleanupError);
                }
                throw setupError;
            }
        }

        private int addSection(String code) throws SQLException {
            return insert("INSERT INTO dbo.lophocphan "
                            + "(ma_lhp, ma_monhoc, ma_giangvien, ma_hocky, trang_thai) "
                            + "VALUES (?, ?, ?, ?, 'MoLop')",
                    code, subject, teacher, term);
        }

        private int addMembership(int sectionId, int studentId) throws SQLException {
            return insert("INSERT INTO dbo.danhsach_sv_lhp "
                            + "(ma_nguoidung, ma_lophocphan) VALUES (?, ?)",
                    studentId, sectionId);
        }

        private int scoreCount() throws SQLException {
            return scalar("SELECT COUNT(*) FROM dbo.diemso ds "
                    + "JOIN dbo.danhsach_sv_lhp roster ON roster.ma_danhsach = ds.ma_danhsach "
                    + "WHERE roster.ma_lophocphan = ?", section);
        }

        private BigDecimal score(int membership, int component) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT gia_tri_diem FROM dbo.diemso "
                            + "WHERE ma_danhsach = ? AND ma_thanhphan = ?")) {
                statement.setInt(1, membership);
                statement.setInt(2, component);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getBigDecimal(1) : null;
                }
            }
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

        private void bind(PreparedStatement statement, Object... parameters) throws SQLException {
            for (int i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
        }

        @Override
        public void close() throws SQLException {
            if (connection == null) return;
            SQLException failure = null;
            try {
                connection.setAutoCommit(true);
                execute("DELETE ds FROM dbo.diemso ds "
                                + "JOIN dbo.danhsach_sv_lhp roster "
                                + "ON roster.ma_danhsach = ds.ma_danhsach "
                                + "JOIN dbo.lophocphan section "
                                + "ON section.ma_lophocphan = roster.ma_lophocphan "
                                + "WHERE section.ma_lhp LIKE ?", sectionPrefix + "%");
                execute("DELETE roster FROM dbo.danhsach_sv_lhp roster "
                                + "JOIN dbo.lophocphan section "
                                + "ON section.ma_lophocphan = roster.ma_lophocphan "
                                + "WHERE section.ma_lhp LIKE ?", sectionPrefix + "%");
                execute("DELETE FROM dbo.lophocphan WHERE ma_lhp LIKE ?", sectionPrefix + "%");
                if (subject > 0) {
                    execute("DELETE FROM dbo.thanhphan_diem WHERE ma_monhoc = ?", subject);
                    execute("DELETE FROM dbo.monhoc WHERE ma_monhoc = ?", subject);
                }
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
