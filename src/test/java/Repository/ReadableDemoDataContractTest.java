package Repository;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadableDemoDataContractTest {
    private static final Path V002 = Paths.get(
            "database", "migrations", "V002__readable_class_and_section_codes.sql");
    private static final Path V003 = Paths.get(
            "database", "migrations", "V003__coherent_demo_data.sql");
    private static final Path V006 = Paths.get(
            "database", "migrations", "V006__align_demo_term_dates.sql");
    private static final Path VERIFICATION = Paths.get(
            "database", "verification", "coherent_demo_data_verification.sql");
    private static final Path SCHEMA = Paths.get("database", "db_qlsv.sql");
    private static final Path SEED = Paths.get("database", "dulieumau.sql");
    private static final Path LOP_REPOSITORY = Paths.get(
            "src", "main", "java", "Repository", "LopHocRepository.java");
    private static final Path LHP_REPOSITORY = Paths.get(
            "src", "main", "java", "Repository", "LopHocPhanRepository.java");
    private static final Path LOP_LIST = Paths.get(
            "src", "main", "webapp", "WEB-INF", "admin", "lopHocList.jsp");
    private static final Path LHP_LIST = Paths.get(
            "src", "main", "webapp", "WEB-INF", "admin", "lhpList.jsp");

    @Test
    void coDuMigrationVaScriptXacMinhChoDuLieuDeDoc() {
        assertTrue(Files.isRegularFile(V002), "Thiếu migration V002 tạo mã hiển thị");
        assertTrue(Files.isRegularFile(V003), "Thiếu migration V003 chuẩn hóa dữ liệu demo");
        assertTrue(Files.isRegularFile(VERIFICATION), "Thiếu script xác minh dữ liệu demo");
    }

    @Test
    void migrationCanBienHocKyChiSuaHaiHocKyDemoVaTuXacMinhHauDieuKien() throws Exception {
        String sql = normalized(read(V006));
        String blockOneGuard = "if (select count(*) from dbo.hocky "
                + "where nam_hoc = '2026' and block = 1) <> 1";
        String blockTwoGuard = "if (select count(*) from dbo.hocky "
                + "where nam_hoc = '2026' and block = 2) <> 1";
        int updateIndex = sql.indexOf("update dbo.hocky");

        assertTransaction(sql);
        assertTrue(sql.contains("if db_name() <> n'ql_daotao'"),
                "Migration phải từ chối database khác QL_DaoTao");
        assertTrue(sql.contains("throw 51600"));
        assertTrue(sql.indexOf(blockOneGuard) >= 0
                        && sql.indexOf(blockOneGuard) < updateIndex,
                "Phải có đúng một học kỳ demo block 1 trước khi UPDATE");
        assertTrue(sql.indexOf(blockTwoGuard) >= 0
                        && sql.indexOf(blockTwoGuard) < updateIndex,
                "Phải có đúng một học kỳ demo block 2 trước khi UPDATE");
        assertEquals(1, occurrences(sql, blockOneGuard));
        assertEquals(1, occurrences(sql, blockTwoGuard));
        assertTrue(sql.contains("throw 51604"));
        assertTrue(sql.contains("throw 51605"));
        assertTrue(sql.contains("set ngay_ketthuc = case block"));
        assertTrue(sql.contains("when 1 then convert(date, '2026-12-31')"));
        assertTrue(sql.contains("when 2 then convert(date, '2027-02-28')"));
        assertTrue(sql.contains("where nam_hoc = '2026' and block in (1, 2)"),
                "Migration chỉ được cập nhật hai học kỳ demo năm 2026");
        assertTrue(sql.contains("if @@rowcount <> 2"));
        assertTrue(sql.contains("throw 51603"));
        assertEquals(1, occurrences(sql, "update dbo.hocky"),
                "Migration chỉ được có một lệnh UPDATE học kỳ có khóa phạm vi");
        assertEquals(2, occurrences(sql, "not between hk.ngay_batdau and hk.ngay_ketthuc"),
                "Migration phải kiểm tra lại cả lịch học và lịch thi sau khi cập nhật");
        assertTrue(sql.contains("from dbo.lichhoc lh"));
        assertTrue(sql.contains("from dbo.kythi kt"));
        assertTrue(sql.contains("throw 51601"));
        assertTrue(sql.contains("throw 51602"));
    }

    @Test
    void migrationMaHienThiCoTransactionCotBatBuocUniqueVaSequence() throws Exception {
        String sql = normalized(read(V002));

        assertTransaction(sql);
        assertTrue(sql.contains("ma_lop_hienthi"));
        assertTrue(sql.contains("ma_lhp"));
        assertTrue(sql.contains("alter column ma_lop_hienthi varchar(20) not null"));
        assertTrue(sql.contains("alter column ma_lhp varchar(30) not null"));
        assertTrue(sql.contains("create unique index ux_lop_ma_lop_hienthi"));
        assertTrue(sql.contains("create unique index ux_lophocphan_ma_lhp"));
        assertTrue(sql.contains("create sequence dbo.seq_lop_hienthi"));
        assertTrue(sql.contains("create sequence dbo.seq_lhp_hienthi"));
        assertTrue(sql.contains("object_id(n'dbo.seq_lop_hienthi', n'so')"));
        assertTrue(sql.contains("object_id(n'dbo.seq_lhp_hienthi', n'so')"));
    }

    @Test
    void migrationDuLieuBaoToanTaiKhoanVaNhatKy() throws Exception {
        String sql = normalized(read(V003));

        assertTransaction(sql);
        assertTrue(sql.contains("bài tập thực hành"));
        assertTrue(sql.contains("dự án cuối môn"));
        assertTrue(sql.contains("web101"));
        assertTrue(sql.contains("mkt301"));
        assertTrue(sql.contains("tkdh201"));
        assertTrue(sql.contains("udpm201"));
        assertTrue(sql.contains("hakhoa@fpt.edu.vn"),
                "Migration phải nhận diện và giữ tài khoản test đã phát sinh");
        assertTrue(sql.contains("udpm-015"),
                "Migration phải chuẩn hóa lớp test SD2001 thay vì xóa âm thầm");
        assertTrue(sql.contains("cntt1"),
                "Migration phải nhận diện mã môn test đã phát sinh ở MON1");
        assertTrue(sql.contains("rollback transaction"));
        assertFalse(sql.contains("delete from dbo.nguoidung"));
        assertFalse(sql.contains("delete from nguoidung"));
        assertFalse(sql.contains("delete from dbo.nhatky_hoatdong"));
        assertFalse(sql.contains("delete from nhatky_hoatdong"));
        assertFalse(sql.contains("rand("), "Dữ liệu demo phải sinh xác định, không dùng RAND");
        assertFalse(sql.contains("newid("), "Dữ liệu demo phải sinh xác định, không dùng NEWID");
    }

    @Test
    void schemaTaoMoiVaDuLieuMauCungDungMaOnDinh() throws Exception {
        String schema = normalized(read(SCHEMA));
        String seed = normalized(read(SEED));

        assertTrue(schema.contains("ma_lop_hienthi varchar(20) not null"));
        assertTrue(schema.contains("ma_lhp varchar(30) not null"));
        assertTrue(schema.contains("create sequence dbo.seq_lop_hienthi"));
        assertTrue(schema.contains("create sequence dbo.seq_lhp_hienthi"));

        assertTrue(seed.contains("web-001"));
        assertTrue(seed.contains("mkt-lhp-001"));
        assertTrue(seed.contains("nguyễn minh anh"));
        assertTrue(seed.contains("nhập môn thiết kế web"));
        assertFalse(seed.contains("'lop_'"));
        assertFalse(seed.contains("'mon' +"));
        assertFalse(seed.contains("rand("));
        assertFalse(seed.contains("newid("));
    }

    @Test
    void duLieuMauCanBienDungHaiHocKyDemo() throws Exception {
        String seed = normalized(read(SEED));

        // Mốc thời gian học kỳ hiện tại được neo ĐỘNG theo hôm nay (dịch từ mốc gốc 2026-09-01)
        assertTrue(seed.contains("declare @shiftdays int"),
                "Seed phải neo mốc thời gian học kỳ theo ngày hiện tại");
        assertTrue(seed.contains("dateadd(day, @shiftdays, '2026-09-01')"),
                "Học kỳ hiện tại phải dịch từ mốc gốc theo @shiftDays");
        assertTrue(seed.contains("n'học kỳ hiện tại - block 1'"));
        assertTrue(seed.contains("n'học kỳ hiện tại - block 2'"));

        // Có thêm một học kỳ TRƯỚC đã hoàn thành (toàn bộ trong quá khứ) để tạo lịch sử
        assertTrue(seed.contains("declare @prevshift int"),
                "Seed phải tạo học kỳ trước nằm hoàn toàn trong quá khứ");
        assertTrue(seed.contains("n'học kỳ trước - block 1'"));
        assertTrue(seed.contains("n'học kỳ trước - block 2'"));
    }

    @Test
    void duLieuMauChiSinhDiemDanhCoMatHoacVang() throws Exception {
        String seed = normalized(read(SEED));

        assertTrue(seed.contains("when 0 then 'vang'"));
        assertTrue(seed.contains("else 'comat'"));
        assertFalse(seed.contains("then 'tre'"));
        assertFalse(seed.contains("vangcophep"));
    }

    @Test
    void repositoryDocMaThatVaGiaoDienKhongHienThiIdThayMa() throws Exception {
        String lopRepo = normalized(read(LOP_REPOSITORY));
        String lhpRepo = normalized(read(LHP_REPOSITORY));
        String lopList = read(LOP_LIST);
        String lhpList = read(LHP_LIST);

        assertTrue(lopRepo.contains("ma_lop_hienthi"));
        assertTrue(lopRepo.contains("next value for dbo.seq_lop_hienthi"));
        assertTrue(lhpRepo.contains("lhp.ma_lhp"));
        assertTrue(lhpRepo.contains("next value for dbo.seq_lhp_hienthi"));
        assertFalse(lhpRepo.contains("rs.getstring(\"ma_mon\") +"));
        assertTrue(lopList.contains("getMaLopHienThi()"));
        assertTrue(lhpList.contains("getMaLhp()"));
    }

    private static void assertTransaction(String sql) {
        assertTrue(sql.contains("set xact_abort on"));
        assertTrue(sql.contains("begin try"));
        assertTrue(sql.contains("begin transaction"));
        assertTrue(sql.contains("commit transaction"));
        assertTrue(sql.contains("begin catch"));
        assertTrue(sql.contains("throw"));
    }

    private static String read(Path path) throws Exception {
        assertTrue(Files.isRegularFile(path), "Thiếu tệp " + path);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String normalized(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ");
    }

    private static int occurrences(String value, String token) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
