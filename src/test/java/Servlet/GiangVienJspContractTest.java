package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GiangVienJspContractTest {
    @Test
    void servletDungCungRequestAttributeVaKhongDoiMaNguoiDungTuForm() throws Exception {
        String source = read("src/main/java/Servlet/GiangVienServlet.java");

        assertTrue(source.contains("setAttribute(\"giangVien\""));
        assertFalse(source.contains("requireInt(req, \"maNguoidung\")"));
    }

    @Test
    void danhSachDungActionMaServletHoTro() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/admin/giangVienList.jsp");

        assertTrue(jsp.contains("action=add"));
        assertTrue(jsp.contains("action=edit&id="));
        assertTrue(jsp.contains("name=\"action\" value=\"delete\""));
    }

    @Test
    void formTaoTaiKhoanGiangVienTrucTiep() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/admin/giangVienForm.jsp");

        assertTrue(jsp.contains("name=\"hoTen\""));
        assertTrue(jsp.contains("name=\"email\""));
        assertTrue(jsp.contains("name=\"maChuyenNganh\""));
        assertFalse(jsp.contains("name=\"maNguoidung\""));
        assertFalse(jsp.contains("name=\"trangThai\""));
    }

    @Test
    void danhSachLopGiangDayKhaiBaoKieuDanhSachJspSuDung() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/teacher/lopGiangDay.jsp");

        assertTrue(jsp.contains("<%@ page import=\"java.util.List\" %>"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
