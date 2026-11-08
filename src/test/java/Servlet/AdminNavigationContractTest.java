package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminNavigationContractTest {
    @Test
    void quanLyThongBaoCoDanhSachFormVaGuiNguoiNhanTrongCungGiaoDich() throws Exception {
        String servlet = read("src/main/java/Servlet/ThongBaoServlet.java");
        String repository = read("src/main/java/Repository/ThongBaoRepository.java");
        String form = read("src/main/webapp/WEB-INF/admin/thongBaoForm.jsp");

        assertTrue(servlet.contains("protected void doGet"));
        assertTrue(servlet.contains("protected void doPost"));
        assertTrue(repository.contains("thongbao_nguoinhan"));
        assertTrue(repository.contains("setAutoCommit(false)"));
        assertTrue(repository.contains("conn.commit()"));
        assertFalse(servlet.contains("class ThongBaoRepository"));
        assertFalse(servlet.contains("class ThongBaoService"));
        assertFalse(servlet.contains("// ... (existing code)"));
        assertTrue(form.contains("value=\"ToanTruong\""));
        assertFalse(form.contains("value=\"Khoa\""));
        assertFalse(form.contains("value=\"Lop\""));
        assertFalse(form.contains("value=\"CaNhan\""));
    }

    @Test
    void dashboardCoBonThaoTacNhanhDungFormVaKhongConLienKetTrung() throws Exception {
        String dashboard = read("src/main/webapp/WEB-INF/admin/dashboard.jsp");

        assertTrue(dashboard.contains("/admin/sinhvien?action=add\">Thêm sinh viên"));
        assertTrue(dashboard.contains("/admin/giangvien?action=add\">Thêm giảng viên"));
        assertTrue(dashboard.contains("/admin/lophocphan?action=add\">Thêm lớp học phần"));
        assertTrue(dashboard.contains("/admin/lichhoc?action=add\">Thêm lịch học"));
        assertFalse(dashboard.contains("Tìm kiếm sinh viên"));
        assertFalse(dashboard.contains("Môn học đang mở"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
