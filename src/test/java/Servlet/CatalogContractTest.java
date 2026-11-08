package Servlet;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class CatalogContractTest {
    @Test void khoaChiDungCotThatVaDuAction() throws Exception {
        String repository = read("src/main/java/Repository/KhoaRepository.java");
        String servlet = read("src/main/java/Servlet/KhoaServlet.java");
        String form = read("src/main/webapp/WEB-INF/admin/khoaForm.jsp");
        assertTrue(repository.contains("INSERT INTO khoa (ten_khoa)"));
        assertFalse(Pattern.compile("\\bma_kh\\b").matcher(repository).find());
        assertFalse(repository.contains("sdt_vanphong"));
        assertTrue(servlet.contains("protected void doPost"));
        assertTrue(form.contains("name=\"tenKhoa\""));
        assertFalse(form.contains("name=\"maKh\""));
    }

    @Test void chuyenNganhDungDungFieldVaAction() throws Exception {
        String repository = read("src/main/java/Repository/ChuyenNganhRepository.java");
        String servlet = read("src/main/java/Servlet/ChuyenNganhServlet.java");
        String form = read("src/main/webapp/WEB-INF/admin/chuyenNganhForm.jsp");
        String sinhVienList = read("src/main/webapp/WEB-INF/admin/sinhVienList.jsp");
        assertTrue(repository.contains("ma_cn, ten_chuyennganh, ma_khoa"));
        assertTrue(form.contains("name=\"maCn\""));
        assertTrue(form.contains("name=\"tenChuyenNganh\""));
        assertTrue(form.contains("name=\"maKhoa\""));
        assertTrue(servlet.contains("protected void doPost"));
        assertTrue(sinhVienList.contains("getMaChuyenNganh()"));
        assertTrue(sinhVienList.contains("getTenChuyenNganh()"));
        assertFalse(sinhVienList.contains("getMaChuyennganh()"));
        assertFalse(sinhVienList.contains("getTenChuyennganh()"));
    }

    @Test void lopChiDungTenVaChuyenNganh() throws Exception {
        String form = read("src/main/webapp/WEB-INF/admin/lopHocForm.jsp");
        assertTrue(form.contains("name=\"tenLop\""));
        assertTrue(form.contains("name=\"maChuyenNganh\""));
        assertFalse(form.contains("maKhoahoc"));
        assertFalse(form.contains("khoaHocMoi"));
        assertFalse(form.contains("maGvcn"));
    }

    @Test void monHocDungBonFieldSchema() throws Exception {
        String repository = read("src/main/java/Repository/MonHocRepository.java");
        String form = read("src/main/webapp/WEB-INF/admin/monHocForm.jsp");
        assertTrue(repository.contains("ma_mon, ten_monhoc, so_tinchi, ma_chuyennganh"));
        assertTrue(form.contains("name=\"maMon\""));
        assertTrue(form.contains("name=\"soTinchi\""));
        assertFalse(form.contains("name=\"trangThai\""));
        assertFalse(repository.contains("tien_to"));
    }

    @Test void hocKyLuuDayDuBlockVaNgay() throws Exception {
        String repository = read("src/main/java/Repository/HocKyRepository.java");
        String form = read("src/main/webapp/WEB-INF/admin/hocKyForm.jsp");
        assertTrue(repository.contains("ten_hocky, block, nam_hoc, ngay_batdau, ngay_ketthuc"));
        assertTrue(form.contains("name=\"block\""));
        assertFalse(form.contains("name=\"trangThai\""));
    }

    @Test void phongHocChiDungTenPhong() throws Exception {
        String repository = read("src/main/java/Repository/PhongHocRepository.java");
        String servlet = read("src/main/java/Servlet/PhongHocServlet.java");
        String jsp = read("src/main/webapp/WEB-INF/admin/phongHocList.jsp");
        assertTrue(repository.contains("INSERT INTO phonghoc (ten_phong) VALUES (?)"));
        assertFalse(repository.contains("suc_chua"));
        assertFalse(repository.contains("loai_phong"));
        assertFalse(repository.contains("lichday_giangvien"));
        assertTrue(servlet.contains("\"them\""));
        assertTrue(servlet.contains("protected void doPost"));
        assertTrue(jsp.contains("action=them"));
        assertTrue(jsp.contains("name=\"tenPhong\""));
        assertFalse(jsp.contains("name=\"sucChua\""));
    }

    @Test void caHocChiLuuHaiCotGio() throws Exception {
        String repository = read("src/main/java/Repository/CaHocRepository.java");
        String servlet = read("src/main/java/Servlet/CaHocServlet.java");
        String jsp = read("src/main/webapp/WEB-INF/admin/caHocList.jsp");
        assertTrue(repository.contains("INSERT INTO cahoc (gio_batdau, gio_ketthuc) VALUES (?, ?)"));
        assertTrue(repository.contains("SELECT COUNT(*) FROM lichhoc WHERE ma_ca = ?"));
        assertTrue(repository.contains("SELECT COUNT(*) FROM kythi WHERE ma_ca = ?"));
        assertFalse(repository.contains("ten_ca"));
        assertTrue(servlet.contains("\"them\""));
        assertTrue(servlet.contains("\"sua\""));
        assertTrue(servlet.contains("\"luu\""));
        assertTrue(servlet.contains("\"xoa\""));
        assertTrue(servlet.contains("protected void doPost"));
        assertTrue(jsp.contains("action=them"));
        assertFalse(jsp.contains("name=\"tenCa\""));
        assertTrue(jsp.contains("name=\"gioBatdau\""));
        assertTrue(jsp.contains("name=\"gioKetthuc\""));
    }

    @Test void tatCaDanhMucCoDuActionVaFormPostDuocCsrfBaoPhu() throws Exception {
        String[] servlets = {
                "KhoaServlet.java", "ChuyenNganhServlet.java", "LopHocServlet.java",
                "MonHocServlet.java", "HocKyServlet.java", "PhongHocServlet.java",
                "CaHocServlet.java"
        };
        for (String file : servlets) {
            String path = "src/main/java/Servlet/" + file;
            String servlet = read(path);
            assertTrue(servlet.contains("protected void doPost"), path);
            assertTrue(servlet.contains("\"luu\""), path);
            assertTrue(servlet.contains("\"xoa\""), path);
        }

        String[] listPages = {
                "khoaList.jsp", "chuyenNganhList.jsp", "lopHocList.jsp", "monHocList.jsp",
                "hocKyList.jsp", "phongHocList.jsp", "caHocList.jsp"
        };
        for (String file : listPages) {
            String path = "src/main/webapp/WEB-INF/admin/" + file;
            String list = read(path);
            assertTrue(list.contains("action=them"), path);
            assertTrue(list.contains("action=sua&id="), path);
            assertTrue(list.contains("method=\"post\""), path);
            assertTrue(list.contains("name=\"action\" value=\"xoa\""), path);
        }

        String[] postPages = {
                "khoaList.jsp", "khoaForm.jsp",
                "chuyenNganhList.jsp", "chuyenNganhForm.jsp",
                "lopHocList.jsp", "lopHocForm.jsp",
                "monHocList.jsp", "monHocForm.jsp",
                "hocKyList.jsp", "hocKyForm.jsp",
                "phongHocList.jsp", "caHocList.jsp"
        };
        for (String file : postPages) {
            String path = "src/main/webapp/WEB-INF/admin/" + file;
            String page = read(path);
            assertTrue(page.contains("<form method=\"post\""), path);
            assertTrue(page.contains("<%@ include file=\"/WEB-INF/footer.jsp\" %>"), path);
        }

        String csrfFooter = read("src/main/webapp/WEB-INF/footer.jsp");
        assertTrue(csrfFooter.contains("input.name = '_csrf'"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
