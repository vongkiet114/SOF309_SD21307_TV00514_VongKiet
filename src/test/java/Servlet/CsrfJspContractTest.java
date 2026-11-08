package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfJspContractTest {
    @Test
    void layoutGanTokenChoTatCaFormPost() throws Exception {
        String header = read("src/main/webapp/WEB-INF/header.jsp");
        String footer = read("src/main/webapp/WEB-INF/footer.jsp");

        assertTrue(header.contains("name=\"csrf-token\""));
        assertTrue(footer.contains("input.name = '_csrf'"));
    }

    @Test
    void formDangNhapCoTokenServerSide() throws Exception {
        String login = read("src/main/webapp/loginget.jsp");

        assertTrue(login.contains("name=\"_csrf\""));
        assertTrue(login.contains("requestScope.csrfToken"));
    }

    @Test
    void trangGiangVienDungLayoutChungCoGanToken() throws Exception {
        String header = read("src/main/webapp/WEB-INF/header.jsp");
        String footer = read("src/main/webapp/WEB-INF/footer.jsp");
        String diemDanh = read("src/main/webapp/WEB-INF/teacher/diemDanh.jsp");
        String nhapDiem = read("src/main/webapp/WEB-INF/teacher/nhapDiem.jsp");

        assertTrue(header.contains("name=\"csrf-token\""));
        assertTrue(footer.contains("input.name = '_csrf'"));
        assertTrue(diemDanh.contains("/WEB-INF/header.jsp"));
        assertTrue(diemDanh.contains("/WEB-INF/footer.jsp"));
        assertTrue(nhapDiem.contains("/WEB-INF/header.jsp"));
        assertTrue(nhapDiem.contains("/WEB-INF/footer.jsp"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
