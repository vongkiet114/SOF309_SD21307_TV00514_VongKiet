package Servlet;

import Models.KyThi;
import Models.LichHoc;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadableSchedulePresentationTest {
    private static final Path LICH_REPOSITORY = Paths.get(
            "src", "main", "java", "Repository", "LichHocRepository.java");
    private static final Path THI_REPOSITORY = Paths.get(
            "src", "main", "java", "Repository", "KyThiRepository.java");
    private static final Path LICH_LIST = Paths.get(
            "src", "main", "webapp", "WEB-INF", "admin", "lichHocList.jsp");
    private static final Path LICH_FORM = Paths.get(
            "src", "main", "webapp", "WEB-INF", "admin", "lichHocForm.jsp");
    private static final Path THI_LIST = Paths.get(
            "src", "main", "webapp", "WEB-INF", "admin", "kyThiList.jsp");
    private static final Path THI_FORM = Paths.get(
            "src", "main", "webapp", "WEB-INF", "admin", "kyThiForm.jsp");
    private static final Path SINH_VIEN_LIST = Paths.get(
            "src", "main", "webapp", "WEB-INF", "admin", "sinhVienList.jsp");
    private static final Path GIANG_VIEN_THI = Paths.get(
            "src", "main", "webapp", "WEB-INF", "teacher", "xemKyThi.jsp");
    private static final Path SINH_VIEN_THI = Paths.get(
            "src", "main", "webapp", "WEB-INF", "student", "xemKyThi.jsp");
    private static final Path SINH_VIEN_LICH = Paths.get(
            "src", "main", "webapp", "WEB-INF", "student", "lichHocCaNhan.jsp");
    private static final Path GIANG_VIEN_LICH = Paths.get(
            "src", "main", "webapp", "WEB-INF", "teacher", "lichDayCaNhan.jsp");
    private static final Path GIANG_VIEN_DASHBOARD = Paths.get(
            "src", "main", "webapp", "WEB-INF", "teacher", "dashboard.jsp");
    private static final Path DASHBOARD_REPOSITORY = Paths.get(
            "src", "main", "java", "Repository", "DashboardRepository.java");
    private static final Path GIANG_VIEN_DIEM_DANH = Paths.get(
            "src", "main", "webapp", "WEB-INF", "teacher", "diemDanh.jsp");
    private static final Path GIANG_VIEN_NHAP_DIEM = Paths.get(
            "src", "main", "webapp", "WEB-INF", "teacher", "nhapDiem.jsp");
    private static final Path SINH_VIEN_HO_SO = Paths.get(
            "src", "main", "webapp", "WEB-INF", "student", "thongTinCaNhan.jsp");
    private static final Path GIANG_VIEN_THONG_BAO = Paths.get(
            "src", "main", "webapp", "WEB-INF", "teacher", "thongBao.jsp");

    @Test
    void lichHocVaKyThiMangTheoMaLhpOnDinhTuDatabase() throws Exception {
        assertReadableCodeProperty(LichHoc.class, new LichHoc());
        assertReadableCodeProperty(KyThi.class, new KyThi());

        String lichRepo = normalized(read(LICH_REPOSITORY));
        String thiRepo = normalized(read(THI_REPOSITORY));
        assertTrue(lichRepo.contains("lhp.ma_lhp"));
        assertTrue(lichRepo.contains("setmalhp(rs.getstring(\"ma_lhp\"))"));
        assertTrue(lichRepo.contains("datetimeformatter.ofpattern(\"hh:mm\")"));
        assertTrue(thiRepo.contains("lhp.ma_lhp"));
        assertTrue(thiRepo.contains("setmalhp(rs.getstring(\"ma_lhp\"))"));
        assertTrue(thiRepo.contains("datetimeformatter.ofpattern(\"hh:mm\")"));
    }

    @Test
    void bieuMauKyThiDungDropdownDeDocVaDungTenThamSoServlet() throws Exception {
        String jsp = read(THI_FORM);

        assertTrue(jsp.contains("List<LopHocPhan>"));
        assertTrue(jsp.contains("name=\"maLophocphan\""));
        assertTrue(jsp.contains("getMaLhp()"));
        assertTrue(jsp.contains("getTenMonHoc()"));
        assertTrue(jsp.contains("getTenHocKy()"));
        assertTrue(jsp.contains("name=\"maPhong\""));
        assertTrue(jsp.contains("name=\"maCa\""));
        assertTrue(jsp.contains("name=\"id\""));
        assertFalse(jsp.contains("name=\"maLHP\""));
        assertFalse(jsp.contains("name=\"maKythi\""));
    }

    @Test
    void danhSachLichHienMaOnDinhNgayVietNamVaNhanTiengViet() throws Exception {
        String lichList = read(LICH_LIST);
        String thiList = read(THI_LIST);
        String lichForm = read(LICH_FORM);
        String sinhVienList = read(SINH_VIEN_LIST);

        assertTrue(lichList.contains("/WEB-INF/common/timetableWeek.jspf"));
        assertTrue(lichList.contains("name=\"maHocKy\""));
        assertTrue(thiList.contains("/WEB-INF/common/timetableWeek.jspf"));
        assertTrue(thiList.contains("name=\"maHocKy\""));
        assertTrue(lichForm.contains("getTenMonHoc()"));
        assertTrue(lichForm.contains("getTenHocKy()"));
        assertTrue(sinhVienList.contains("TrangThaiTheoNgay.nhan"));
        assertFalse(lichList.contains("IT001_HK1_N01"));
        assertFalse(thiList.contains("IT001_HK1_N01"));
    }

    @Test
    void giangVienCoManHinhLichThiHopLe() {
        assertTrue(Files.isRegularFile(GIANG_VIEN_THI),
                "Servlet lịch thi đang chuyển giảng viên tới một JSP không tồn tại");
    }

    @Test
    void congSinhVienVaGiangVienDungDuLieuDaJoinDeHienThiDeDoc() throws Exception {
        String sinhVienThi = read(SINH_VIEN_THI);
        String sinhVienLich = read(SINH_VIEN_LICH);
        String giangVienLich = read(GIANG_VIEN_LICH);
        String dashboard = read(GIANG_VIEN_DASHBOARD);
        String diemDanh = read(GIANG_VIEN_DIEM_DANH);
        String nhapDiem = read(GIANG_VIEN_NHAP_DIEM);
        String hoSo = read(SINH_VIEN_HO_SO);

        assertTrue(sinhVienThi.contains("Lịch thi của tôi"));
        assertTrue(sinhVienThi.contains("/WEB-INF/common/timetableWeek.jspf"));
        assertFalse(sinhVienThi.contains("request.getAttribute(\"dsLHP\")"));

        assertTrue(sinhVienLich.contains("Lịch học của tôi"));
        assertTrue(sinhVienLich.contains("/WEB-INF/common/timetableWeek.jspf"));
        assertTrue(giangVienLich.contains("Lịch dạy của tôi"));
        assertTrue(giangVienLich.contains("/WEB-INF/common/timetableWeek.jspf"));
        assertTrue(dashboard.contains("b.getMaLhp()"));
        assertFalse(dashboard.contains("\"00:00\""));
        assertTrue(diemDanh.contains("DateTimeFormatter.ofPattern(\"dd/MM/yyyy\")"));
        assertTrue(nhapDiem.contains("getMaLhp()"));
        assertTrue(nhapDiem.contains("getTenHocKy()"));
        assertTrue(hoSo.contains("TrangThaiTheoNgay.nhan"));
    }

    @Test
    void thongBaoGiangVienDinhDangDungKieuLocalDateTime() throws Exception {
        String jsp = read(GIANG_VIEN_THONG_BAO);

        assertTrue(jsp.contains("DateTimeFormatter.ofPattern(\"dd/MM/yyyy HH:mm\")"));
        assertTrue(jsp.contains("tb.getNgayTao().format"));
        assertFalse(jsp.contains("SimpleDateFormat"));
    }

    @Test
    void dashboardGiangVienTapTrungVaoCongViecHomNayVaHanhDongTrucTiep() throws Exception {
        String jsp = read(GIANG_VIEN_DASHBOARD);

        assertTrue(jsp.contains("Lớp đang dạy"));
        assertTrue(jsp.contains("Sinh viên phụ trách"));
        assertTrue(jsp.contains("Buổi dạy hôm nay"));
        assertTrue(jsp.contains("Buổi cần điểm danh"));
        assertTrue(jsp.contains("b.getMaLophocphan()"));
        assertTrue(jsp.contains("b.getMaLichhoc()"));
        assertTrue(jsp.contains("/giangvien/lopgiangday"));
        assertTrue(jsp.contains("/giangvien/diemdanh"));
        assertTrue(jsp.contains("/giangvien/nhapdiem"));
        assertTrue(jsp.contains("/giangvien/lichday"));
        assertFalse(jsp.contains("Biểu đồ sĩ số"));
        assertFalse(jsp.contains("&#128276;"));
    }

    @Test
    void dashboardGiangVienDemSinhVienDuyNhatVaBuoiChuaDiemDanhBangSqlCoThamSo()
            throws Exception {
        String repository = normalized(read(DASHBOARD_REPOSITORY));

        assertTrue(repository.contains("count(distinct ds.ma_nguoidung)"));
        assertTrue(repository.contains("lhp.trang_thai = 'danghoc'"));
        assertTrue(repository.contains("lh.trang_thai = 'binhthuong'"));
        assertTrue(repository.contains("not exists"));
        assertTrue(repository.contains("dd.ma_lichhoc = lh.ma_lichhoc"));
        assertTrue(repository.contains("preparedstatement"));
        assertTrue(repository.contains("date.valueof(today)"));
        assertTrue(repository.contains("new repositoryexception("));
        assertTrue(repository.contains(
                "\"không thể tải số liệu dashboard giảng viên.\", e)"));
    }

    @Test
    void mauDayBuLaTimVaLichThiLaDoCamTheoBangMauDaDuyet() throws Exception {
        String css = normalized(read(Paths.get(
                "src", "main", "webapp", "assets", "css", "timetable.css")));

        assertTrue(css.contains(
                ".event-makeup { background: #f3e8ff; color: #7e22ce; }"));
        assertTrue(css.contains(
                ".event-exam { background: #ffedd5; color: #9a3412; }"));
    }

    private static void assertReadableCodeProperty(Class<?> type, Object instance) throws Exception {
        Method setter = assertDoesNotThrow(() -> type.getMethod("setMaLhp", String.class));
        Method getter = assertDoesNotThrow(() -> type.getMethod("getMaLhp"));
        setter.invoke(instance, "WEB-LHP-004");
        assertEquals("WEB-LHP-004", getter.invoke(instance));
    }

    private static String read(Path path) throws Exception {
        assertTrue(Files.isRegularFile(path), "Thiếu tệp " + path);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String normalized(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ");
    }
}
