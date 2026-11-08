package Repository;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanCAttendanceDatabaseContractTest {
    @Test
    void migrationVaSchemaChiChoCoMatHoacVang() throws Exception {
        String migration = read("database/migrations/V004__attendance_two_states.sql");
        String schema = read("database/db_qlsv.sql");
        String seed = read("database/dulieumau.sql");

        assertTrue(migration.contains("set xact_abort on"));
        assertTrue(migration.contains("begin transaction"));
        assertTrue(migration.contains(
                "update dbo.diemdanh set trang_thai = 'comat' where trang_thai = 'tre'"));
        assertTrue(migration.contains(
                "constraint ck_diemdanh_trang_thai check (trang_thai in ('comat', 'vang'))"));
        assertTrue(migration.contains("exec sys.sp_executesql @drop_check_sql"));
        assertFalse(migration.contains(
                "exec(n'alter table dbo.diemdanh drop constraint ' + quotename(@old_check))"));
        assertTrue(schema.contains(
                "constraint ck_diemdanh_trang_thai check (trang_thai in ('comat', 'vang'))"));
        assertFalse(schema.contains("'tre'"));
        assertFalse(seed.contains("then 'tre'"));
        assertFalse(read("src/main/webapp/WEB-INF/teacher/diemDanh.jsp").contains("vangcophep"));
        assertFalse(read("src/main/webapp/WEB-INF/teacher/diemDanh.jsp").contains("tre"));
        assertFalse(read("src/main/webapp/WEB-INF/student/lichSuDiemDanh.jsp").contains("vangcophep"));
    }

    @Test
    void verificationLaReadOnlyVaKiemTraDu22Bang() throws Exception {
        String sql = read("database/verification/plan_c_attendance_verification.sql");
        assertTrue(sql.contains("sys.check_constraints"));
        assertTrue(sql.contains("count(*) from sys.tables"));
        assertFalse(sql.matches("(?s).*\\b(update|delete|insert|merge|alter|drop)\\b.*"));
    }

    private static String read(String path) throws Exception {
        assertTrue(Files.isRegularFile(Paths.get(path)), "Không tìm thấy " + path);
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8)
                .toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
