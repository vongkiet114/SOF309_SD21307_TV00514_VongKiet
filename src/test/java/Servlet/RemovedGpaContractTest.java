package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemovedGpaContractTest {

    @Test
    void khongConThanhPhanGpa() throws Exception {
        for (String path : Arrays.asList(
                "src/main/java/Models/GPA.java",
                "src/main/java/Repository/GPARepository.java",
                "src/main/java/Services/TinhGPAService.java",
                "src/main/java/Controllers/TinhGPAController.java",
                "src/main/java/Controllers/XemGPAController.java",
                "src/main/java/Servlet/TinhGPAServlet.java",
                "src/main/java/Servlet/XemGPAServlet.java",
                "src/main/webapp/WEB-INF/admin/gpaList.jsp",
                "src/main/webapp/WEB-INF/student/xemGPA.jsp",
                "src/main/webapp/assets/css/student.css")) {
            assertFalse(Files.exists(Paths.get(path)), path);
        }

        String dashboard = read("src/main/webapp/WEB-INF/student/dashboard.jsp");
        assertFalse(dashboard.toUpperCase(Locale.ROOT).contains("GPA"));
        assertFalse(dashboard.contains("Tổng tín chỉ tích lũy"));
        assertFalse(dashboard.contains("Xếp loại"));
    }

    @Test
    void dashboardCoBonTheDuLieuThatVaBonLoiTat() throws Exception {
        String dashboard = read("src/main/webapp/WEB-INF/student/dashboard.jsp");

        for (String label : Arrays.asList(
                "Học phần đang học",
                "Buổi học hôm nay",
                "Kỳ thi sắp tới",
                "Buổi vắng trong học kỳ")) {
            assertTrue(dashboard.contains(label), label);
        }
        for (String route : Arrays.asList(
                "/sinhvien/lichhoc",
                "/sinhvien/kythi",
                "/sinhvien/diemdanh",
                "/sinhvien/thongbao")) {
            assertTrue(dashboard.contains(route), route);
        }
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
