package Repository;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMigrationContractTest {
    private static final Path MIGRATION = Paths.get(
            "database", "migrations", "V001__plan_c0_database_hardening.sql");
    private static final Path VERIFICATION = Paths.get(
            "database", "verification", "plan_c0_database_hardening_verification.sql");
    private static final Path CANONICAL_SCHEMA = Paths.get("database", "db_qlsv.sql");

    private static final List<String> TARGET_INDEXES = Arrays.asList(
            "ix_nguoidung_vaitro_hoatdong",
            "ix_nhatky_hoatdong_nguoidung_thoigian",
            "ix_thongbao_nguoinhan_nguoidung_dadoc",
            "ix_chuyennganh_khoa",
            "ix_lop_chuyennganh",
            "ix_monhoc_chuyennganh",
            "ix_monhoc_tienquyet_reverse",
            "ix_giangvien_chuyennganh",
            "ix_sinhvien_lop",
            "ix_lophocphan_monhoc",
            "ix_lophocphan_giangvien_hocky",
            "ix_lophocphan_hocky",
            "ix_danhsach_sv_lhp_lophocphan",
            "ix_thanhphan_diem_monhoc",
            "ix_diemso_thanhphan",
            "ix_diemdanh_lichhoc",
            "ix_lichhoc_lophocphan_ngay",
            "ix_lichhoc_ngay_ca_phong",
            "ix_kythi_lophocphan_ngay",
            "ix_kythi_ngay_ca_phong"
    );

    @Test
    void coDuMigrationVaScriptXacMinhPlanC0() {
        assertTrue(Files.isRegularFile(MIGRATION), "Thiếu migration V001 của Plan C0");
        assertTrue(Files.isRegularFile(VERIFICATION), "Thiếu script verification của Plan C0");
    }

    @Test
    void migrationCoTransactionVaKhongSuaDuLieuNghiepVu() throws Exception {
        String sql = normalized(readRequired(MIGRATION));

        assertTrue(sql.contains("set xact_abort on"));
        assertTrue(sql.contains("begin try"));
        assertTrue(sql.contains("begin transaction"));
        assertTrue(sql.contains("commit transaction"));
        assertTrue(sql.contains("rollback transaction"));
        assertTrue(sql.contains("throw"));

        String executable = withoutCommentsAndStrings(sql);
        Pattern destructive = Pattern.compile(
                "\\b(create\\s+table|drop\\s+table|drop\\s+column|truncate|delete|update|insert|merge)\\b",
                Pattern.CASE_INSENSITIVE);
        assertFalse(destructive.matcher(executable).find(),
                "Migration V001 không được tạo bảng, phá schema hoặc sửa dữ liệu nghiệp vụ");
    }

    @Test
    void migrationVaSchemaTaoMoiCungCoDungHaiMuoiIndex() throws Exception {
        String migration = readRequired(MIGRATION).toLowerCase(Locale.ROOT);
        String schema = readRequired(CANONICAL_SCHEMA).toLowerCase(Locale.ROOT);

        for (String index : TARGET_INDEXES) {
            assertTrue(migration.contains("create index " + index),
                    "Migration thiếu index " + index);
            assertTrue(schema.contains("create index " + index),
                    "Schema tạo mới thiếu index " + index);
            assertGuardedIndex(migration, index);
        }
    }

    @Test
    void migrationVaSchemaTaoMoiCungSietTamCotNotNull() throws Exception {
        String migration = normalized(readRequired(MIGRATION));
        String schema = normalized(readRequired(CANONICAL_SCHEMA));

        assertTrue(migration.contains("alter table dbo.nguoidung alter column hoat_dong bit not null"));
        assertTrue(migration.contains("alter table dbo.nhatky_hoatdong alter column thoi_gian datetime not null"));
        assertTrue(migration.contains("alter table dbo.thongbao alter column ngay_tao datetime not null"));
        assertTrue(migration.contains("alter table dbo.thongbao_nguoinhan alter column da_doc bit not null"));
        assertTrue(migration.contains("alter table dbo.sinhvien alter column trang_thai varchar(20) not null"));
        assertTrue(migration.contains("alter table dbo.lophocphan alter column trang_thai varchar(20) not null"));
        assertTrue(migration.contains("alter table dbo.lichhoc alter column loai_lich varchar(20) not null"));
        assertTrue(migration.contains("alter table dbo.lichhoc alter column trang_thai varchar(20) not null"));

        assertTrue(schema.contains("hoat_dong bit not null"));
        assertTrue(schema.contains("thoi_gian datetime not null"));
        assertTrue(schema.contains("ngay_tao datetime not null"));
        assertTrue(schema.contains("da_doc bit not null"));
        assertTrue(schema.contains("trang_thai varchar(20) not null"));
        assertTrue(schema.contains("loai_lich varchar(20) not null"));
    }

    @Test
    void migrationXacMinhChinhXacIndexVaDefaultTruocKhiCommit() throws Exception {
        String sql = normalized(readRequired(MIGRATION));
        int commitPosition = sql.indexOf("commit transaction");

        assertTrue(commitPosition > 0, "Migration phải có COMMIT");
        String beforeCommit = sql.substring(0, commitPosition);
        assertTrue(beforeCommit.contains("sys.index_columns"),
                "Hậu điều kiện phải đọc metadata cột khóa của index");
        assertTrue(beforeCommit.contains("key_ordinal"),
                "Hậu điều kiện phải kiểm tra đúng thứ tự cột khóa");
        assertTrue(beforeCommit.contains("is_descending_key"),
                "Hậu điều kiện phải kiểm tra chiều ASC/DESC của cột khóa");
        assertTrue(beforeCommit.contains("dc.definition"),
                "Hậu điều kiện phải kiểm tra definition của default hiện hữu");
        assertTrue(beforeCommit.contains("default_definition"),
                "Migration phải đối chiếu default với giá trị mong đợi");
    }

    @Test
    void verificationChiDocVaKhongThayDoiDatabase() throws Exception {
        String sql = normalized(readRequired(VERIFICATION));
        String executable = withoutCommentsAndStrings(sql);

        assertTrue(sql.contains("db_name()"));
        assertTrue(sql.contains("count_big"));
        assertTrue(sql.contains("sys.indexes"));
        assertTrue(sql.contains("sys.columns"));

        Pattern mutating = Pattern.compile(
                "\\b(create|alter|drop|truncate|delete|update|insert|merge)\\b",
                Pattern.CASE_INSENSITIVE);
        assertFalse(mutating.matcher(executable).find(),
                "Script verification phải hoàn toàn chỉ đọc");
    }

    private static String readRequired(Path path) throws IOException {
        assertTrue(Files.isRegularFile(path), "Không tìm thấy " + path);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String normalized(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private static String withoutCommentsAndStrings(String value) {
        String withoutComments = value.replaceAll("(?m)--.*$", " ");
        return withoutComments.replaceAll("'(?:''|[^'])*'", "''");
    }

    private static void assertGuardedIndex(String migration, String index) {
        String marker = "create index " + index;
        int createPosition = migration.indexOf(marker);
        int start = Math.max(0, createPosition - 500);
        String guard = migration.substring(start, createPosition);
        assertTrue(guard.contains("if not exists") && guard.contains(index),
                "Index " + index + " phải có IF NOT EXISTS riêng");
    }
}
