package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminDiemDanhContractTest {
    @Test
    void endpointChiChoAdminVaDungServiceDiemDanhBu() throws Exception {
        String servlet = read("src/main/java/Servlet/AdminDiemDanhServlet.java");

        assertTrue(servlet.contains("@WebServlet(\"/admin/diemdanhbu\")"));
        assertTrue(servlet.contains("\"ADMIN\".equalsIgnoreCase"));
        assertTrue(servlet.contains("getBangDiemDanhAdmin"));
        assertTrue(servlet.contains("saveDiemDanhAdmin"));
        assertTrue(servlet.contains("Clock.systemDefaultZone()"));
        assertTrue(servlet.contains("LocalDate.now(clock)"));
        assertTrue(servlet.contains("AttendanceSessionPolicy.filterSelectable"));
        assertTrue(servlet.contains("SC_SERVICE_UNAVAILABLE"));
        assertFalse(servlet.contains("e.printStackTrace"));
    }

    @Test
    void giaoDienChiCoHaiTrangThaiVaCoTrongSidebarAdmin() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/admin/diemDanhBu.jsp");
        String sidebar = read("src/main/webapp/WEB-INF/sidebar.jsp");

        assertTrue(jsp.contains("\"CoMat\".equals"));
        assertTrue(jsp.contains("\"Vang\".equals"));
        assertTrue(jsp.contains("Chưa điểm danh"));
        assertTrue(jsp.contains("Đánh dấu tất cả Có mặt"));
        assertTrue(jsp.contains("Đánh dấu tất cả Vắng"));
        assertFalse(jsp.contains("value=\"Tre\""));
        assertTrue(sidebar.contains("/admin/diemdanhbu"));
        assertTrue(jsp.contains("request.setAttribute(\"trang\", \"diemdanhbu\")"));
        assertTrue(jsp.contains("data-filter-table=\"attendance-table\""));
        assertTrue(jsp.contains("data-filter-text"));
        assertFalse(jsp.contains("data-table-filter"));
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
