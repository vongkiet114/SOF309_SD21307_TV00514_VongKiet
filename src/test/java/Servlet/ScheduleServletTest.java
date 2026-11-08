package Servlet;

import Models.CaHoc;
import Models.KyThi;
import Models.LichHoc;
import Models.LopHocPhan;
import Models.PhongHoc;
import Models.TaiKhoan;
import Models.TimetableFilter;
import Models.TimetableWeek;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.CaHocService;
import Services.KyThiService;
import Services.LichHocService;
import Services.LopHocPhanService;
import Services.PhongHocService;
import Services.TimetableService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleServletTest {
    @Test
    void sinhVienChiTaiLichHocTheoIdTrongSession() throws Exception {
        CapturingTimetableService service = new CapturingTimetableService();
        XemLichHocServlet servlet = new XemLichHocServlet(service);
        RequestCapture request = RequestCapture.withUser(
                account(101, "STUDENT"), personalParams("2026-09-07"));

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertPersonalTimetable(request, service, TimetableOperation.CLASS,
                101, "STUDENT", "/WEB-INF/student/lichHocCaNhan.jsp",
                "/sinhvien/lichhoc");
        assertFalse(service.lastAutoAdvance);
    }

    @Test
    void sinhVienChiTaiLichThiTheoIdTrongSession() throws Exception {
        CapturingTimetableService service = new CapturingTimetableService();
        XemKyThiServlet servlet = new XemKyThiServlet(service);
        RequestCapture request = RequestCapture.withUser(
                account(102, "STUDENT"), personalParams("2026-10-19"));

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertPersonalTimetable(request, service, TimetableOperation.EXAM,
                102, "STUDENT", "/WEB-INF/student/xemKyThi.jsp",
                "/sinhvien/kythi");
        assertFalse(service.lastAutoAdvance);
    }

    @Test
    void giangVienChiTaiLichThiTheoIdTrongSession() throws Exception {
        CapturingTimetableService service = new CapturingTimetableService();
        XemKyThiServlet servlet = new XemKyThiServlet(service);
        RequestCapture request = RequestCapture.withUser(
                account(202, "TEACHER"), personalParams("2026-10-19"));

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertPersonalTimetable(request, service, TimetableOperation.EXAM,
                202, "TEACHER", "/WEB-INF/teacher/xemKyThi.jsp",
                "/giangvien/kythi");
        assertFalse(service.lastAutoAdvance);
    }

    @Test
    void giangVienChiTaiLichDayTheoIdTrongSession() throws Exception {
        CapturingTimetableService service = new CapturingTimetableService();
        XemLichHocServlet servlet = new XemLichHocServlet(service);
        RequestCapture request = RequestCapture.withUser(
                account(201, "TEACHER"), personalParams("2026-09-07"));

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertPersonalTimetable(request, service, TimetableOperation.CLASS,
                201, "TEACHER", "/WEB-INF/teacher/lichDayCaNhan.jsp",
                "/giangvien/lichday");
        assertFalse(service.lastAutoAdvance);
    }

    @Test
    void lichCaNhanThieuWeekTuDongTimLichGanNhat() throws Exception {
        CapturingTimetableService service = new CapturingTimetableService();
        XemLichHocServlet servlet = new XemLichHocServlet(service);
        RequestCapture request = RequestCapture.withUser(
                account(101, "STUDENT"), Collections.emptyMap());

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertTrue(service.lastAutoAdvance);
    }

    @Test
    void lichCaNhanWeekSaiQuayVeTuanHienTaiKhongTuDongVaCanhBao() throws Exception {
        CapturingTimetableService service = new CapturingTimetableService();
        XemKyThiServlet servlet = new XemKyThiServlet(service);
        RequestCapture request = RequestCapture.withUser(
                account(202, "TEACHER"), params("week", "not-a-date"));

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertEquals(TimetableWeek.mondayOf(LocalDate.now()),
                service.lastFilter.getWeekStart());
        assertFalse(service.lastAutoAdvance);
        assertEquals("Tuần được chọn không hợp lệ; hệ thống đã quay về tuần hiện tại.",
                request.attributes.get("timetableWarning"));
    }

    @Test
    void adminVaRoleKhacKhongDuocDungRouteLichCaNhan() throws Exception {
        XemLichHocServlet classServlet = new XemLichHocServlet(
                new CapturingTimetableService());
        XemKyThiServlet examServlet = new XemKyThiServlet(
                new CapturingTimetableService());

        RequestCapture adminRequest = RequestCapture.withUser(
                account(1, "ADMIN"), Collections.emptyMap());
        ResponseCapture adminResponse = new ResponseCapture();
        classServlet.doGet(adminRequest.proxy(), adminResponse.proxy());

        RequestCapture otherRequest = RequestCapture.withUser(
                account(2, "STAFF"), Collections.emptyMap());
        ResponseCapture otherResponse = new ResponseCapture();
        examServlet.doGet(otherRequest.proxy(), otherResponse.proxy());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, adminResponse.errorStatus);
        assertNull(adminRequest.forwardPath);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, otherResponse.errorStatus);
        assertNull(otherRequest.forwardPath);
    }

    @Test
    void lichCaNhanKhongXacThucHoacThieuVaiTroGiuHanhViCu() throws Exception {
        XemLichHocServlet servlet = new XemLichHocServlet(
                new CapturingTimetableService());
        RequestCapture anonymousRequest = RequestCapture.withUser(null,
                Collections.emptyMap());
        ResponseCapture anonymousResponse = new ResponseCapture();

        servlet.doGet(anonymousRequest.proxy(), anonymousResponse.proxy());

        assertEquals("/login", anonymousResponse.redirect);

        RequestCapture noRoleRequest = RequestCapture.withUser(account(101, null),
                Collections.emptyMap());
        ResponseCapture noRoleResponse = new ResponseCapture();
        servlet.doGet(noRoleRequest.proxy(), noRoleResponse.proxy());

        assertEquals(HttpServletResponse.SC_FORBIDDEN, noRoleResponse.errorStatus);
        assertNull(noRoleRequest.forwardPath);
    }

    @Test
    void lichCaNhanLoiDuLieuTra503KhongLoChiTietChoCaHaiLoaiLich() throws Exception {
        XemLichHocServlet servlet = new XemLichHocServlet(
                new FailingTimetableService(TimetableOperation.CLASS,
                        "SQL class detail"));
        RequestCapture request = RequestCapture.withUser(
                account(101, "STUDENT"), Collections.emptyMap());
        ResponseCapture response = new ResponseCapture();
        LogCapture log = new LogCapture();
        servlet.init(config(log));

        servlet.doGet(request.proxy(), response.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertFalse(response.errorMessage.contains("SQL class detail"));
        assertNull(request.forwardPath);
        assertTrue(log.logged);

        XemKyThiServlet examServlet = new XemKyThiServlet(
                new FailingTimetableService(TimetableOperation.EXAM,
                        "SQL exam detail"));
        RequestCapture examRequest = RequestCapture.withUser(
                account(202, "TEACHER"), Collections.emptyMap());
        ResponseCapture examResponse = new ResponseCapture();
        LogCapture examLog = new LogCapture();
        examServlet.init(config(examLog));

        examServlet.doGet(examRequest.proxy(), examResponse.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                examResponse.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", examResponse.errorMessage);
        assertFalse(examResponse.errorMessage.contains("SQL exam detail"));
        assertNull(examRequest.forwardPath);
        assertTrue(examLog.logged);
    }

    @Test
    void adminLichHocTaiDungTuanVaBonBoLoc() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("week", "2026-09-09");
        params.put("maHocKy", "2");
        params.put("maLhp", "3");
        params.put("maGiangVien", "4");
        params.put("maPhong", "5");
        RequestCapture request = new RequestCapture(params);
        ResponseCapture response = new ResponseCapture();
        CapturingTimetableService timetable = new CapturingTimetableService();
        LichHocServlet servlet = new LichHocServlet(
                new StubClassReadService(null),
                new FilterSectionService(section(3, 4, 2)),
                new FilterRoomService(room(5)),
                new EmptyShiftService(), timetable);

        servlet.doGet(request.proxy(), response.proxy());

        assertEquals("/WEB-INF/admin/lichHocList.jsp", request.forwardPath);
        assertEquals("CLASS", request.attributes.get("timetableMode"));
        assertEquals(TimetableOperation.CLASS, timetable.lastOperation);
        assertEquals(Boolean.TRUE, request.attributes.get("canManage"));
        assertEquals("/admin/lichhoc", request.attributes.get("timetableBasePath"));
        assertEquals(LocalDate.of(2026, 9, 7), timetable.lastFilter.getWeekStart());
        assertEquals(2, timetable.lastFilter.getTermId());
        assertEquals(3, timetable.lastFilter.getSectionId());
        assertEquals(4, timetable.lastFilter.getTeacherId());
        assertEquals(5, timetable.lastFilter.getRoomId());
        assertFalse(timetable.lastAutoAdvance);
    }

    @Test
    void adminLichThiThieuWeekTuDongTimLichGanNhat() throws Exception {
        RequestCapture request = new RequestCapture(Collections.emptyMap());
        CapturingTimetableService timetable = new CapturingTimetableService();
        KyThiServlet servlet = new KyThiServlet(
                new StubExamReadService(null), new EmptySectionService(),
                new EmptyRoomService(), new EmptyShiftService(), timetable);

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertEquals("EXAM", request.attributes.get("timetableMode"));
        assertEquals(TimetableOperation.EXAM, timetable.lastOperation);
        assertEquals(Boolean.TRUE, request.attributes.get("canManage"));
        assertTrue(timetable.lastAutoAdvance);
    }

    @Test
    void adminBoQuaBoLocSaiVaCanhBaoNguoiDung() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("week", "not-a-date");
        params.put("maHocKy", "abc");
        params.put("maLhp", "999");
        RequestCapture request = new RequestCapture(params);
        CapturingTimetableService timetable = new CapturingTimetableService();
        LichHocServlet servlet = new LichHocServlet(
                new StubClassReadService(null),
                new FilterSectionService(section(3, 4, 2)),
                new FilterRoomService(room(5)),
                new EmptyShiftService(), timetable);

        servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertNull(timetable.lastFilter.getTermId());
        assertNull(timetable.lastFilter.getSectionId());
        assertEquals(TimetableWeek.mondayOf(LocalDate.now()),
                timetable.lastFilter.getWeekStart());
        assertFalse(timetable.lastAutoAdvance);
        assertEquals("Tuần được chọn không hợp lệ; hệ thống đã quay về tuần hiện tại. "
                        + "Học kỳ được chọn không hợp lệ; bộ lọc đã được bỏ qua. "
                        + "Lớp học phần được chọn không hợp lệ; bộ lọc đã được bỏ qua.",
                request.attributes.get("timetableWarning"));
    }

    @Test
    void getLichHocVaKyThiKhiTangDocLoiTra503AnToanKhongRedirectLap() throws Exception {
        RequestCapture classRequest = new RequestCapture(Collections.emptyMap());
        ResponseCapture classResponse = new ResponseCapture();
        LogCapture classLog = new LogCapture();
        LichHocServlet classServlet = new LichHocServlet(
                new RuntimeFailingClassReadService(), new EmptySectionService(),
                new EmptyRoomService(), new EmptyShiftService(),
                new RuntimeFailingTimetableService());
        classServlet.init(config(classLog));

        classServlet.doGet(classRequest.proxy(), classResponse.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, classResponse.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", classResponse.errorMessage);
        assertNull(classResponse.redirect);
        assertNull(classRequest.forwardPath);
        assertTrue(classLog.logged);
        assertTrue(!classResponse.errorMessage.contains("SQL read detail"));

        RequestCapture examRequest = new RequestCapture(Collections.emptyMap());
        ResponseCapture examResponse = new ResponseCapture();
        LogCapture examLog = new LogCapture();
        KyThiServlet examServlet = new KyThiServlet(
                new RuntimeFailingExamReadService(), new EmptySectionService(),
                new EmptyRoomService(), new EmptyShiftService(),
                new RuntimeFailingTimetableService());
        examServlet.init(config(examLog));

        examServlet.doGet(examRequest.proxy(), examResponse.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, examResponse.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", examResponse.errorMessage);
        assertNull(examResponse.redirect);
        assertNull(examRequest.forwardPath);
        assertTrue(examLog.logged);
        assertTrue(!examResponse.errorMessage.contains("SQL read detail"));
    }

    @Test
    void editLichHocVaKyThiIdSaiTra400KhongForwardModelNull() throws Exception {
        for (String invalidId : new String[]{null, "abc", "0", "-1"}) {
            Map<String, String> classParams = new HashMap<>();
            classParams.put("action", "edit");
            classParams.put("id", invalidId);
            RequestCapture classRequest = new RequestCapture(classParams);
            ResponseCapture classResponse = new ResponseCapture();

            new LichHocServlet(new StubClassReadService(null), new EmptySectionService(),
                    new EmptyRoomService(), new EmptyShiftService())
                    .doGet(classRequest.proxy(), classResponse.proxy());

            assertEquals(HttpServletResponse.SC_BAD_REQUEST, classResponse.errorStatus);
            assertNull(classRequest.forwardPath);
            assertNull(classRequest.attributes.get("isEdit"));
            assertNull(classRequest.attributes.get("lichHoc"));

            Map<String, String> examParams = new HashMap<>();
            examParams.put("action", "edit");
            examParams.put("id", invalidId);
            RequestCapture examRequest = new RequestCapture(examParams);
            ResponseCapture examResponse = new ResponseCapture();

            new KyThiServlet(new StubExamReadService(null), new EmptySectionService(),
                    new EmptyRoomService(), new EmptyShiftService())
                    .doGet(examRequest.proxy(), examResponse.proxy());

            assertEquals(HttpServletResponse.SC_BAD_REQUEST, examResponse.errorStatus);
            assertNull(examRequest.forwardPath);
            assertNull(examRequest.attributes.get("isEdit"));
            assertNull(examRequest.attributes.get("kyThi"));
        }
    }

    @Test
    void editLichHocVaKyThiKhongTonTaiTra404KhongForwardModelNull() throws Exception {
        Map<String, String> classParams = new HashMap<>();
        classParams.put("action", "edit");
        classParams.put("id", "999999");
        RequestCapture classRequest = new RequestCapture(classParams);
        ResponseCapture classResponse = new ResponseCapture();

        new LichHocServlet(new StubClassReadService(null), new EmptySectionService(),
                new EmptyRoomService(), new EmptyShiftService())
                .doGet(classRequest.proxy(), classResponse.proxy());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, classResponse.errorStatus);
        assertNull(classRequest.forwardPath);
        assertNull(classRequest.attributes.get("isEdit"));
        assertNull(classRequest.attributes.get("lichHoc"));

        Map<String, String> examParams = new HashMap<>();
        examParams.put("action", "edit");
        examParams.put("id", "999999");
        RequestCapture examRequest = new RequestCapture(examParams);
        ResponseCapture examResponse = new ResponseCapture();

        new KyThiServlet(new StubExamReadService(null), new EmptySectionService(),
                new EmptyRoomService(), new EmptyShiftService())
                .doGet(examRequest.proxy(), examResponse.proxy());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, examResponse.errorStatus);
        assertNull(examRequest.forwardPath);
        assertNull(examRequest.attributes.get("isEdit"));
        assertNull(examRequest.attributes.get("kyThi"));
    }

    @Test
    void editLichHocVaKyThiHopLeForwardDungModel() throws Exception {
        LichHoc existingClass = new LichHoc(31, 2, 3, 4,
                java.time.LocalDate.of(2026, 9, 10), "HocChinh", "BinhThuong");
        Map<String, String> classParams = new HashMap<>();
        classParams.put("action", "edit");
        classParams.put("id", "31");
        RequestCapture classRequest = new RequestCapture(classParams);
        ResponseCapture classResponse = new ResponseCapture();

        new LichHocServlet(new StubClassReadService(existingClass), new EmptySectionService(),
                new EmptyRoomService(), new EmptyShiftService())
                .doGet(classRequest.proxy(), classResponse.proxy());

        assertEquals("/WEB-INF/admin/lichHocForm.jsp", classRequest.forwardPath);
        assertEquals(existingClass, classRequest.attributes.get("lichHoc"));
        assertEquals(Boolean.TRUE, classRequest.attributes.get("isEdit"));
        assertEquals(0, classResponse.errorStatus);
        assertNull(classResponse.redirect);

        KyThi existingExam = new KyThi(41, 2, 3, 4,
                java.time.LocalDate.of(2026, 9, 20), "CuoiKy");
        Map<String, String> examParams = new HashMap<>();
        examParams.put("action", "edit");
        examParams.put("id", "41");
        RequestCapture examRequest = new RequestCapture(examParams);
        ResponseCapture examResponse = new ResponseCapture();

        new KyThiServlet(new StubExamReadService(existingExam), new EmptySectionService(),
                new EmptyRoomService(), new EmptyShiftService())
                .doGet(examRequest.proxy(), examResponse.proxy());

        assertEquals("/WEB-INF/admin/kyThiForm.jsp", examRequest.forwardPath);
        assertEquals(existingExam, examRequest.attributes.get("kyThi"));
        assertEquals(Boolean.TRUE, examRequest.attributes.get("isEdit"));
        assertEquals(0, examResponse.errorStatus);
        assertNull(examResponse.redirect);
    }

    @Test
    void loiXungDotLichHocForwardFormVaGiuDuLieu() throws Exception {
        RequestCapture request = new RequestCapture(classParams());
        ResponseCapture response = new ResponseCapture();
        LichHocServlet servlet = new LichHocServlet(new FailingClassService(false),
                new EmptySectionService(), new EmptyRoomService(), new EmptyShiftService());

        servlet.doPost(request.proxy(), response.proxy());

        assertEquals("/WEB-INF/admin/lichHocForm.jsp", request.forwardPath);
        assertNull(response.redirect);
        assertEquals("Phòng đã có lịch với lớp WEB-LHP-009.",
                request.attributes.get("error"));
        LichHoc retained = (LichHoc) request.attributes.get("lichHoc");
        assertNull(retained);
        assertEquals(Boolean.FALSE, request.attributes.get("isEdit"));
        assertEquals("2", request.params.get("maLophocphan"));
        assertEquals("3", request.params.get("maPhong"));
        assertEquals("2026-09-10", request.params.get("tuNgay"));
        assertEquals("4_5", request.params.get("maCa_Thu"));
        assertNotNull(request.attributes.get("dsLHP"));
    }

    @Test
    void loiXungDotKyThiForwardFormVaGiuIdKhiSua() throws Exception {
        Map<String, String> params = examParams();
        params.put("action", "update");
        params.put("id", "17");
        RequestCapture request = new RequestCapture(params);
        ResponseCapture response = new ResponseCapture();
        KyThiServlet servlet = new KyThiServlet(new FailingExamService(false),
                new EmptySectionService(), new EmptyRoomService(), new EmptyShiftService());

        servlet.doPost(request.proxy(), response.proxy());

        assertEquals("/WEB-INF/admin/kyThiForm.jsp", request.forwardPath);
        assertNull(response.redirect);
        KyThi retained = (KyThi) request.attributes.get("kyThi");
        assertNotNull(retained);
        assertEquals(17, retained.getMaKythi());
        assertEquals("CuoiKy", retained.getLoaiKythi());
        assertEquals("Giảng viên đã có lịch với lớp UDPM-LHP-003.",
                request.attributes.get("error"));
    }

    @Test
    void loiXungDotLichHocNhungTaiLaiFormLoiTra503AnToan() throws Exception {
        RequestCapture request = new RequestCapture(classParams());
        ResponseCapture response = new ResponseCapture();
        LogCapture log = new LogCapture();
        LichHocServlet servlet = new LichHocServlet(new FailingClassService(false),
                new RepositoryFailingSectionService(), new EmptyRoomService(),
                new EmptyShiftService());
        servlet.init(config(log));

        servlet.doPost(request.proxy(), response.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertNull(response.redirect);
        assertNull(request.forwardPath);
        assertTrue(log.logged);
        assertTrue(!response.errorMessage.contains("JDBC reload detail"));
    }

    @Test
    void loiXungDotKyThiNhungTaiLaiFormLoiTra503AnToan() throws Exception {
        RequestCapture request = new RequestCapture(examParams());
        ResponseCapture response = new ResponseCapture();
        LogCapture log = new LogCapture();
        KyThiServlet servlet = new KyThiServlet(new FailingExamService(false),
                new EmptySectionService(), new RuntimeFailingRoomService(),
                new EmptyShiftService());
        servlet.init(config(log));

        servlet.doPost(request.proxy(), response.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertNull(response.redirect);
        assertNull(request.forwardPath);
        assertTrue(log.logged);
        assertTrue(!response.errorMessage.contains("SQL reload detail"));
    }

    @Test
    void duLieuLichHocSaiVanForwardFormThayViMatDuLieu() throws Exception {
        Map<String, String> params = classParams();
        params.put("maPhong", "");
        RequestCapture request = new RequestCapture(params);
        ResponseCapture response = new ResponseCapture();
        LichHocServlet servlet = new LichHocServlet(new FailingClassService(false),
                new EmptySectionService(), new EmptyRoomService(), new EmptyShiftService());

        servlet.doPost(request.proxy(), response.proxy());

        assertEquals("/WEB-INF/admin/lichHocForm.jsp", request.forwardPath);
        assertNull(response.redirect);
        LichHoc retained = (LichHoc) request.attributes.get("lichHoc");
        assertNull(retained);
        assertEquals(Boolean.FALSE, request.attributes.get("isEdit"));
        assertEquals("2", request.params.get("maLophocphan"));
        assertEquals("", request.params.get("maPhong"));
        assertEquals("DayBu", request.params.get("loaiLich"));
    }

    @Test
    void duLieuKyThiSaiVanForwardFormThayViMatDuLieu() throws Exception {
        Map<String, String> params = examParams();
        params.put("ngayThi", "khong-phai-ngay");
        RequestCapture request = new RequestCapture(params);
        ResponseCapture response = new ResponseCapture();
        KyThiServlet servlet = new KyThiServlet(new FailingExamService(false),
                new EmptySectionService(), new EmptyRoomService(), new EmptyShiftService());

        servlet.doPost(request.proxy(), response.proxy());

        assertEquals("/WEB-INF/admin/kyThiForm.jsp", request.forwardPath);
        assertNull(response.redirect);
        KyThi retained = (KyThi) request.attributes.get("kyThi");
        assertNotNull(retained);
        assertEquals(2, retained.getMaLophocphan());
        assertNull(retained.getNgayThi());
        assertEquals("CuoiKy", retained.getLoaiKythi());
    }

    @Test
    void loiRepositoryChiLogNoiBoVaTraThongBaoChung() throws Exception {
        RequestCapture classRequest = new RequestCapture(classParams());
        ResponseCapture classResponse = new ResponseCapture();
        LogCapture classLog = new LogCapture();
        LichHocServlet classServlet = new LichHocServlet(new FailingClassService(true),
                new EmptySectionService(), new EmptyRoomService(), new EmptyShiftService());
        classServlet.init(config(classLog));

        classServlet.doPost(classRequest.proxy(), classResponse.proxy());

        assertTrue(decode(classResponse.redirect)
                .endsWith("error=Không thể xử lý yêu cầu lúc này."));
        assertTrue(classLog.logged);
        assertTrue(!decode(classResponse.redirect).contains("SQL test detail"));

        RequestCapture examRequest = new RequestCapture(examParams());
        ResponseCapture examResponse = new ResponseCapture();
        LogCapture examLog = new LogCapture();
        KyThiServlet examServlet = new KyThiServlet(new FailingExamService(true),
                new EmptySectionService(), new EmptyRoomService(), new EmptyShiftService());
        examServlet.init(config(examLog));

        examServlet.doPost(examRequest.proxy(), examResponse.proxy());

        assertTrue(decode(examResponse.redirect)
                .endsWith("error=Không thể xử lý yêu cầu lúc này."));
        assertTrue(examLog.logged);
        assertTrue(!decode(examResponse.redirect).contains("SQL test detail"));
    }

    @Test
    void formEscapeLoiNhanLopCoGiangVienVaLichNghiCoKieuThe() throws Exception {
        String classForm = read("src/main/webapp/WEB-INF/admin/lichHocForm.jsp");
        String examForm = read("src/main/webapp/WEB-INF/admin/kyThiForm.jsp");
        String timetable = read("src/main/webapp/WEB-INF/common/timetableWeek.jspf");
        String timetableCss = read("src/main/webapp/assets/css/timetable.css");

        assertTrue(classForm.contains("request.getAttribute(\"error\")"));
        assertTrue(classForm.contains("Html.escape(error)"));
        assertTrue(classForm.contains("lhp.getTenGiangVien()"));
        assertTrue(classForm.contains("request.getParameter(\"tuNgay\")"));
        assertTrue(classForm.contains("request.getParameter(\"denNgay\")"));
        assertTrue(classForm.contains("request.getParameterValues(\"maCa_Thu\")"));
        assertTrue(classForm.contains("caThuDaChon.contains"));
        assertTrue(examForm.contains("request.getAttribute(\"error\")"));
        assertTrue(examForm.contains("Html.escape(error)"));
        assertTrue(examForm.contains("lhp.getTenGiangVien()"));
        assertTrue(timetable.contains(
                "timetable-swatch event-leave\"></span>Xin nghỉ"));
        assertTrue(timetable.contains(
                "timetable-swatch event-cancelled\"></span>Đã hủy"));
        assertTrue(timetable.contains("event.getCssClass()"));
        assertTrue(timetableCss.contains(".event-leave"));
        assertTrue(timetableCss.contains(".event-cancelled"));
    }

    @Test
    void theLichXinNghiVaDaHuyBiLamMoVaGachNgang() throws Exception {
        String css = read("src/main/webapp/assets/css/timetable.css")
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ");
        String targetedSelector = ".timetable-event.event-leave, "
                + ".timetable-event.event-cancelled {";
        int ruleStart = css.indexOf(targetedSelector);

        assertTrue(ruleStart >= 0,
                "Kiểu làm mờ chỉ được áp dụng cho thẻ lịch, không áp dụng chú giải");
        int ruleEnd = css.indexOf('}', ruleStart);
        assertTrue(ruleEnd > ruleStart);
        String rule = css.substring(ruleStart, ruleEnd);
        assertTrue(rule.contains("background: rgba("));
        assertTrue(rule.contains("border-color: rgba("));
        assertTrue(rule.contains("text-decoration: line-through"));
        assertFalse(rule.contains("opacity:"),
                "Không làm mờ toàn bộ nút vì sẽ giảm độ tương phản chữ và focus");
    }

    private static Map<String, String> classParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "create");
        params.put("maLophocphan", "2");
        params.put("maPhong", "3");
        params.put("tuNgay", "2026-09-10");
        params.put("denNgay", "2026-09-10");
        params.put("maCa_Thu", "4_5");
        params.put("loaiLich", "DayBu");
        params.put("trangThai", "BinhThuong");
        return params;
    }

    private static Map<String, String> examParams() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "create");
        params.put("maLophocphan", "2");
        params.put("maPhong", "3");
        params.put("maCa", "4");
        params.put("ngayThi", "2026-09-20");
        params.put("loaiKythi", "CuoiKy");
        return params;
    }

    private static Map<String, String> params(String name, String value) {
        Map<String, String> result = new HashMap<>();
        result.put(name, value);
        return result;
    }

    private static Map<String, String> personalParams(String week) {
        Map<String, String> result = params("week", week);
        result.put("maHocKy", "99");
        result.put("maLhp", "98");
        result.put("maGiangVien", "97");
        result.put("maPhong", "96");
        return result;
    }

    private static TaiKhoan account(int id, String role) {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(id);
        value.setTenVaiTro(role);
        return value;
    }

    private static void assertPersonalTimetable(RequestCapture request,
            CapturingTimetableService service, TimetableOperation operation,
            int userId, String role, String viewPath, String basePath) {
        assertEquals(operation, service.lastOperation);
        assertEquals(viewPath, request.forwardPath);
        assertEquals(userId, service.lastFilter.getUserId());
        assertEquals(role, service.lastFilter.getRole());
        assertNull(service.lastFilter.getTermId());
        assertNull(service.lastFilter.getSectionId());
        assertNull(service.lastFilter.getTeacherId());
        assertNull(service.lastFilter.getRoomId());
        assertEquals(operation.name(), request.attributes.get("timetableMode"));
        assertEquals(Boolean.FALSE, request.attributes.get("canManage"));
        assertEquals(basePath, request.attributes.get("timetableBasePath"));
        assertEquals("", request.attributes.get("filterQuery"));
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static String decode(String value) throws Exception {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    }

    private static ServletConfig config(LogCapture capture) {
        ServletContext context = proxy(ServletContext.class, (value, method, args) -> {
            if ("log".equals(method.getName())) {
                capture.logged = true;
            }
            return defaultValue(method.getReturnType());
        });
        return proxy(ServletConfig.class, (value, method, args) -> {
            if ("getServletContext".equals(method.getName())) {
                return context;
            }
            return defaultValue(method.getReturnType());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == Void.TYPE) {
            return null;
        }
        return type.isPrimitive() ? Array.get(Array.newInstance(type, 1), 0) : null;
    }

    private static final class RequestCapture {
        private final Map<String, String> params;
        private final TaiKhoan user;
        private final Map<String, Object> attributes = new HashMap<>();
        private String forwardPath;

        private RequestCapture(Map<String, String> params) {
            this(params, account(1, "ADMIN"));
        }

        private RequestCapture(Map<String, String> params, TaiKhoan user) {
            this.params = params;
            this.user = user;
        }

        private static RequestCapture withUser(TaiKhoan user,
                Map<String, String> params) {
            return new RequestCapture(params, user);
        }

        private HttpServletRequest proxy() {
            HttpSession session = ScheduleServletTest.proxy(HttpSession.class,
                    (value, method, args) -> {
                        if ("getAttribute".equals(method.getName())
                                && "user".equals(args[0])) {
                            return user;
                        }
                        return defaultValue(method.getReturnType());
                    });
            return ScheduleServletTest.proxy(HttpServletRequest.class,
                    (value, method, args) -> {
                        if ("getSession".equals(method.getName())) {
                            return session;
                        }
                        if ("getParameter".equals(method.getName())) {
                            return params.get(args[0]);
                        }
                        if ("getParameterValues".equals(method.getName())) {
                            String raw = params.get(args[0]);
                            return raw == null ? null : raw.split(",");
                        }
                        if ("setAttribute".equals(method.getName())) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        }
                        if ("getAttribute".equals(method.getName())) {
                            return attributes.get(args[0]);
                        }
                        if ("getContextPath".equals(method.getName())) {
                            return "";
                        }
                        if ("getRequestDispatcher".equals(method.getName())) {
                            String path = (String) args[0];
                            return ScheduleServletTest.proxy(RequestDispatcher.class,
                                    (dispatcher, dispatchMethod, dispatchArgs) -> {
                                        if ("forward".equals(dispatchMethod.getName())) {
                                            forwardPath = path;
                                        }
                                        return defaultValue(dispatchMethod.getReturnType());
                                    });
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class ResponseCapture {
        private String redirect;
        private int errorStatus;
        private String errorMessage;
        private boolean committed;

        private HttpServletResponse proxy() {
            return ScheduleServletTest.proxy(HttpServletResponse.class,
                    (value, method, args) -> {
                        if ("sendRedirect".equals(method.getName())) {
                            redirect = (String) args[0];
                            committed = true;
                            return null;
                        }
                        if ("sendError".equals(method.getName())) {
                            errorStatus = (Integer) args[0];
                            errorMessage = args.length > 1 ? (String) args[1] : null;
                            committed = true;
                            return null;
                        }
                        if ("isCommitted".equals(method.getName())) {
                            return committed;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class LogCapture {
        private boolean logged;
    }

    private static final class FailingClassService extends LichHocService {
        private final boolean repositoryFailure;

        private FailingClassService(boolean repositoryFailure) {
            this.repositoryFailure = repositoryFailure;
        }

        @Override
        public int create(LichHoc value) throws AcademicException {
            fail();
            return 0;
        }

        @Override
        public void update(LichHoc value) throws AcademicException {
            fail();
        }

        private void fail() throws AcademicException {
            if (repositoryFailure) {
                throw new RepositoryException("SQL test detail", new SQLExceptionForTest());
            }
            throw new AcademicException(AcademicException.Code.CONFLICT_ROOM,
                    "Phòng đã có lịch với lớp WEB-LHP-009.");
        }
    }

    private static final class FailingExamService extends KyThiService {
        private final boolean repositoryFailure;

        private FailingExamService(boolean repositoryFailure) {
            this.repositoryFailure = repositoryFailure;
        }

        @Override
        public int create(KyThi value) throws AcademicException {
            fail();
            return 0;
        }

        @Override
        public void update(KyThi value) throws AcademicException {
            fail();
        }

        private void fail() throws AcademicException {
            if (repositoryFailure) {
                throw new RepositoryException("SQL test detail", new SQLExceptionForTest());
            }
            throw new AcademicException(AcademicException.Code.CONFLICT_TEACHER,
                    "Giảng viên đã có lịch với lớp UDPM-LHP-003.");
        }
    }

    private static final class SQLExceptionForTest extends RuntimeException {
    }

    private static LopHocPhan section(int id, int teacherId, int termId) {
        return new LopHocPhan(id, 0, teacherId, termId, "DangMo");
    }

    private static PhongHoc room(int id) {
        PhongHoc value = new PhongHoc();
        value.setMaPhong(id);
        return value;
    }

    private static final class CapturingTimetableService extends TimetableService {
        private TimetableFilter lastFilter;
        private boolean lastAutoAdvance;
        private TimetableOperation lastOperation;

        @Override
        public TimetableWeek loadClasses(TimetableFilter filter, boolean autoAdvance) {
            return capture(TimetableOperation.CLASS, filter, autoAdvance);
        }

        @Override
        public TimetableWeek loadExams(TimetableFilter filter, boolean autoAdvance) {
            return capture(TimetableOperation.EXAM, filter, autoAdvance);
        }

        private TimetableWeek capture(TimetableOperation operation,
                TimetableFilter filter, boolean autoAdvance) {
            lastOperation = operation;
            lastFilter = filter;
            lastAutoAdvance = autoAdvance;
            return emptyWeek(filter);
        }
    }

    private static final class FilterSectionService extends LopHocPhanService {
        private final LopHocPhan option;

        private FilterSectionService(LopHocPhan option) {
            this.option = option;
        }

        @Override
        public List<LopHocPhan> getAll(Integer term, Integer teacher) {
            return Collections.singletonList(option);
        }
    }

    private static final class FilterRoomService extends PhongHocService {
        private final PhongHoc option;

        private FilterRoomService(PhongHoc option) {
            this.option = option;
        }

        @Override
        public List<PhongHoc> getAll() {
            return Collections.singletonList(option);
        }
    }

    private static final class EmptySectionService extends LopHocPhanService {
        @Override
        public List<LopHocPhan> getAll(Integer term, Integer teacher) {
            return Collections.emptyList();
        }
    }

    private static final class RepositoryFailingSectionService extends LopHocPhanService {
        @Override
        public List<LopHocPhan> getAll(Integer term, Integer teacher) {
            throw new RepositoryException("JDBC reload detail", new SQLExceptionForTest());
        }
    }

    private static final class EmptyRoomService extends PhongHocService {
        @Override
        public List<PhongHoc> getAll() {
            return Collections.emptyList();
        }
    }

    private static final class RuntimeFailingRoomService extends PhongHocService {
        @Override
        public List<PhongHoc> getAll() {
            throw new RuntimeException("SQL reload detail");
        }
    }

    private static final class EmptyShiftService extends CaHocService {
        @Override
        public List<CaHoc> getAll() {
            return Collections.emptyList();
        }
    }

    private static final class RuntimeFailingClassReadService extends LichHocService {
        @Override
        public List<LichHoc> getAll() {
            throw new RuntimeException("SQL read detail");
        }
    }

    private static final class RuntimeFailingExamReadService extends KyThiService {
        @Override
        public List<KyThi> getAll() {
            throw new RuntimeException("SQL read detail");
        }
    }

    private static final class RuntimeFailingTimetableService extends TimetableService {
        @Override
        public TimetableWeek loadClasses(TimetableFilter filter, boolean autoAdvance) {
            throw new RuntimeException("SQL read detail");
        }

        @Override
        public TimetableWeek loadExams(TimetableFilter filter, boolean autoAdvance) {
            throw new RuntimeException("SQL read detail");
        }
    }

    private static final class FailingTimetableService extends TimetableService {
        private final TimetableOperation failingOperation;
        private final String detail;

        private FailingTimetableService(TimetableOperation failingOperation,
                String detail) {
            this.failingOperation = failingOperation;
            this.detail = detail;
        }

        @Override
        public TimetableWeek loadClasses(TimetableFilter filter, boolean autoAdvance) {
            return failOrReturn(TimetableOperation.CLASS, filter);
        }

        @Override
        public TimetableWeek loadExams(TimetableFilter filter, boolean autoAdvance) {
            return failOrReturn(TimetableOperation.EXAM, filter);
        }

        private TimetableWeek failOrReturn(TimetableOperation operation,
                TimetableFilter filter) {
            if (failingOperation == operation) {
                throw new RuntimeException(detail);
            }
            return emptyWeek(filter);
        }
    }

    private enum TimetableOperation {
        CLASS, EXAM
    }

    private static TimetableWeek emptyWeek(TimetableFilter filter) {
        return TimetableWeek.of(filter.getWeekStart(), Collections.emptyList(),
                Collections.emptyList(), false, null);
    }

    private static final class StubClassReadService extends LichHocService {
        private final LichHoc found;

        private StubClassReadService(LichHoc found) {
            this.found = found;
        }

        @Override
        public LichHoc findById(int id) {
            return found;
        }
    }

    private static final class StubExamReadService extends KyThiService {
        private final KyThi found;

        private StubExamReadService(KyThi found) {
            this.found = found;
        }

        @Override
        public KyThi findById(int id) {
            return found;
        }
    }
}
