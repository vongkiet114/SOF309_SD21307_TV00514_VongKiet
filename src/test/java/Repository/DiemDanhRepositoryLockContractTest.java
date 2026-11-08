package Repository;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiemDanhRepositoryLockContractTest {
    @Test
    void teacherSessionCheckLocksScheduleAndOwningSectionRows() throws Exception {
        String repository = new String(Files.readAllBytes(
                Paths.get("src/main/java/Repository/DiemDanhRepository.java")),
                StandardCharsets.UTF_8);
        int start = repository.indexOf("private BatchResult lockAndCheckSession(");
        int end = repository.indexOf(
                "private BatchResult lockAndCheckMemberships", start);

        assertTrue(start >= 0 && end > start,
                "Không tìm thấy phạm vi query khóa buổi học của giảng viên.");
        String lockQuery = repository.substring(start, end);
        assertTrue(lockQuery.contains(
                "FROM dbo.lichhoc lh WITH (UPDLOCK, HOLDLOCK)"));
        assertTrue(lockQuery.contains(
                "JOIN dbo.lophocphan lhp WITH (UPDLOCK, HOLDLOCK)"),
                "Phải khóa hàng lophocphan chứa ma_giangvien trước khi kiểm tra quyền.");
    }
}
