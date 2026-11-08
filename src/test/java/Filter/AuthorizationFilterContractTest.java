package Filter;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationFilterContractTest {
    @Test
    void filterBaoVeDuBaKhuVucVaDungChinhSachChung() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(
                "src/main/java/Filter/AuthorizationFilter.java")), StandardCharsets.UTF_8);

        assertTrue(source.contains("\"/admin/*\""));
        assertTrue(source.contains("\"/giangvien/*\""));
        assertTrue(source.contains("\"/sinhvien/*\""));
        assertTrue(source.contains("RoleAccess.requiredRole"));
        assertTrue(source.contains("SC_FORBIDDEN"));
    }
}
