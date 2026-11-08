package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaiKhoanJspContractTest {
    @Test
    void formDungActionMaTaiKhoanServletHoTro() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/admin/taiKhoanList.jsp");

        assertTrue(jsp.contains("name=\"action\" value=\"lock\""));
        assertTrue(jsp.contains("name=\"action\" value=\"unlock\""));
        assertTrue(jsp.contains("equalsIgnoreCase(\"ADMIN\")"));
        assertTrue(jsp.contains("equalsIgnoreCase(\"TEACHER\")"));
        assertTrue(jsp.contains("equalsIgnoreCase(\"STUDENT\")"));
    }

    @Test
    void khongTaoTaiKhoanStudentRoiKhongCoHoSo() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/admin/taiKhoanList.jsp");
        String servlet = read("src/main/java/Servlet/TaiKhoanServlet.java");
        String service = read("src/main/java/Services/TaiKhoanService.java");

        assertFalse(jsp.contains("name=\"action\" value=\"create\""));
        assertFalse(servlet.contains("case \"create\":"));
        assertFalse(service.contains("themTaiKhoan(String email, String hoTen)"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
