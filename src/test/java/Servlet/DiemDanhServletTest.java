package Servlet;

import Models.DiemDanh;
import Models.LichHoc;
import Models.LopHocPhan;
import Models.SinhVien;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.DiemDanhService;
import Services.LichHocService;
import Services.LopHocPhanService;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiemDanhServletTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-07-27T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void postLayGiangVienTuSessionLuuTheoLoVaPrgGiuLopBuoi() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        RequestCapture request = new RequestCapture(validParams(), teacher(7));
        ResponseCapture response = new ResponseCapture();

        fixture.servlet.doPost(request.proxy(), response.proxy());

        assertEquals(7, service.savedTeacher);
        assertEquals(8, service.savedSection);
        assertEquals(9, service.savedSchedule);
        assertEquals(2, service.savedRows.size());
        assertEquals(11, service.savedRows.get(0).getMaDanhsach());
        assertEquals("CoMat", service.savedRows.get(0).getTrangThai());
        String redirect = decode(response.redirect);
        assertTrue(redirect.contains("maLHP=8"));
        assertTrue(redirect.contains("maLichhoc=9"));
        assertTrue(redirect.contains("Lưu điểm danh thành công"));
    }

    @Test
    void haiMangKhacDoDaiVaIdKhongHopLeDeuReloadFormKhongGhi() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        Map<String, String[]> differentLengths = validParams();
        differentLengths.put("trangThai", new String[]{"CoMat"});
        RequestCapture first = new RequestCapture(differentLengths, teacher(7));

        fixture.servlet.doPost(first.proxy(), new ResponseCapture().proxy());

        assertFalse(service.saveCalled);
        assertEquals("/WEB-INF/teacher/diemDanh.jsp", first.forwardPath);
        assertTrue(((String) first.attributes.get("error")).contains("không hợp lệ"));

        Map<String, String[]> invalidId = validParams();
        invalidId.put("maDanhSach", new String[]{"11", "SQL-secret"});
        RequestCapture second = new RequestCapture(invalidId, teacher(7));
        fixture.servlet.doPost(second.proxy(), new ResponseCapture().proxy());

        assertFalse(service.saveCalled);
        assertEquals("/WEB-INF/teacher/diemDanh.jsp", second.forwardPath);
        assertTrue(((String) second.attributes.get("error")).contains("không hợp lệ"));
    }

    @Test
    void giaTriIdNullCungDuocXuLyNhuLoiValidation() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        Map<String, String[]> params = validParams();
        params.put("maDanhSach", new String[]{"11", null});
        RequestCapture request = new RequestCapture(params, teacher(7));

        fixture.servlet.doPost(request.proxy(), new ResponseCapture().proxy());

        assertFalse(service.saveCalled);
        assertEquals("/WEB-INF/teacher/diemDanh.jsp", request.forwardPath);
        assertTrue(((String) request.attributes.get("error")).contains("không hợp lệ"));
    }

    @Test
    void getChiTaiLopCuaGiangVienBuoiBinhThuongVaDocKemIdSession() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        Map<String, String[]> params = new HashMap<>();
        params.put("maLHP", new String[]{"8"});
        params.put("maLichhoc", new String[]{"9"});
        params.put("maGiangVien", new String[]{"999"});
        RequestCapture request = new RequestCapture(params, teacher(7));

        fixture.servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        assertEquals(7, fixture.sections.requestedTeacher);
        assertEquals(8, fixture.schedules.requestedSection);
        assertEquals(7, service.readTeacher);
        assertEquals(8, service.readSection);
        assertEquals(9, service.readSchedule);
        @SuppressWarnings("unchecked")
        List<LichHoc> visibleSchedules = (List<LichHoc>) request.attributes.get("dsBuoi");
        assertEquals(1, visibleSchedules.size());
        assertEquals("BinhThuong", visibleSchedules.get(0).getTrangThai());
        assertEquals("/WEB-INF/teacher/diemDanh.jsp", request.forwardPath);
    }

    @Test
    void getKhongHienBuoiQuaKhuVaPostKhongGoiSaveChoBuoiDo() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        Map<String, String[]> getParams = new HashMap<>();
        getParams.put("maLHP", new String[]{"8"});
        RequestCapture getRequest = new RequestCapture(getParams, teacher(7));

        fixture.servlet.doGet(getRequest.proxy(), new ResponseCapture().proxy());

        @SuppressWarnings("unchecked")
        List<LichHoc> visibleSchedules =
                (List<LichHoc>) getRequest.attributes.get("dsBuoi");
        assertFalse(visibleSchedules.stream()
                .anyMatch(value -> LocalDate.of(2026, 7, 26).equals(value.getNgayHoc())));

        Map<String, String[]> postParams = validParams();
        postParams.put("maLichhoc", new String[]{"8"});
        RequestCapture postRequest = new RequestCapture(postParams, teacher(7));

        fixture.servlet.doPost(postRequest.proxy(), new ResponseCapture().proxy());

        assertFalse(service.saveCalled);
    }

    @Test
    void getTuChoiLopKhacVaBuoiXinNghiTruocKhiDocBang() throws Exception {
        FakeDiemDanhService foreignService = new FakeDiemDanhService();
        Fixture foreignFixture = new Fixture(foreignService);
        Map<String, String[]> foreignParams = new HashMap<>();
        foreignParams.put("maLHP", new String[]{"999"});
        RequestCapture foreign = new RequestCapture(foreignParams, teacher(7));
        ResponseCapture forbidden = new ResponseCapture();

        foreignFixture.servlet.doGet(foreign.proxy(), forbidden.proxy());

        assertEquals(HttpServletResponse.SC_FORBIDDEN, forbidden.errorStatus);
        assertEquals(0, foreignFixture.schedules.calls);
        assertEquals(0, foreignService.readCalls);

        FakeDiemDanhService cancelledService = new FakeDiemDanhService();
        Fixture cancelledFixture = new Fixture(cancelledService);
        Map<String, String[]> cancelledParams = new HashMap<>();
        cancelledParams.put("maLHP", new String[]{"8"});
        cancelledParams.put("maLichhoc", new String[]{"10"});
        ResponseCapture badRequest = new ResponseCapture();
        cancelledFixture.servlet.doGet(
                new RequestCapture(cancelledParams, teacher(7)).proxy(), badRequest.proxy());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, badRequest.errorStatus);
        assertEquals(0, cancelledService.readCalls);
    }

    @Test
    void chiTeacherDuocPhepTruyCap() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        TaiKhoan student = teacher(5);
        student.setTenVaiTro("STUDENT");
        ResponseCapture response = new ResponseCapture();

        fixture.servlet.doPost(
                new RequestCapture(validParams(), student).proxy(), response.proxy());

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.errorStatus);
        assertFalse(service.saveCalled);
    }

    @Test
    void loiValidationReloadThatBaiDuocLogVaTra503Chung() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        service.saveFailure = new AcademicException(
                AcademicException.Code.INVALID_INPUT, "Sai trạng thái");
        service.readFailure = new RepositoryException(
                "SQL credential secret", new RuntimeException("test"));
        Fixture fixture = new Fixture(service);
        LogCapture log = new LogCapture();
        fixture.servlet.init(config(log));
        ResponseCapture response = new ResponseCapture();

        fixture.servlet.doPost(
                new RequestCapture(validParams(), teacher(7)).proxy(), response.proxy());

        assertTrue(log.logged);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertFalse(response.errorMessage.contains("secret"));
    }

    @Test
    void moiRuntimeLoiDuLieuDeuKhongLoChiTiet() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        service.runtimeSaveFailure = new IllegalStateException("raw JDBC URL secret");
        Fixture fixture = new Fixture(service);
        LogCapture log = new LogCapture();
        fixture.servlet.init(config(log));
        ResponseCapture response = new ResponseCapture();

        fixture.servlet.doPost(
                new RequestCapture(validParams(), teacher(7)).proxy(), response.proxy());

        assertTrue(log.logged);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
    }

    @Test
    void jspCoMotFormChoCaBangHaiTrangThaiVaNutDanhDauHangLoat() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/teacher/diemDanh.jsp");

        assertTrue(jsp.contains("name=\"maDanhSach\""));
        assertTrue(jsp.contains("data-attendance-state"));
        assertTrue(jsp.contains("\"CoMat\".equals"));
        assertTrue(jsp.contains("\"Vang\".equals"));
        assertTrue(jsp.contains("Chưa điểm danh"));
        assertTrue(jsp.contains("Đánh dấu tất cả Có mặt"));
        assertTrue(jsp.contains("Đánh dấu tất cả Vắng"));
        assertTrue(jsp.contains("Lưu điểm danh"));
        assertFalse(jsp.contains("value=\"Tre\""));
        assertFalse(jsp.contains("VangCoPhep"));
        assertFalse(jsp.contains("name=\"ghiChu\""));
        assertEquals(1, occurrences(jsp, "method=\"post\""));

        String history = read("src/main/webapp/WEB-INF/student/lichSuDiemDanh.jsp");
        assertTrue(history.contains("\"CoMat\".equals"));
        assertTrue(history.contains("\"Vang\".equals"));
        assertFalse(history.contains("\"Tre\".equals"));
        assertFalse(history.contains("VangCoPhep"));
        assertFalse(history.contains("Thời gian điểm danh"));
        assertFalse(history.contains("Object[]"));
        assertTrue(history.contains("List<LichSuDiemDanhItem>"));
        // Lich su diem danh da toi gian con 3 cot (Ngay hoc, Mon hoc, Trang thai) + bo loc.
        assertTrue(history.contains("colspan=\"3\""));
    }

    private static Map<String, String[]> validParams() {
        Map<String, String[]> values = new HashMap<>();
        values.put("maLHP", new String[]{"8"});
        values.put("maLichhoc", new String[]{"9"});
        values.put("maGiangVien", new String[]{"999"});
        values.put("maDanhSach", new String[]{"11", "12"});
        values.put("trangThai", new String[]{"CoMat", "Vang"});
        return values;
    }

    private static TaiKhoan teacher(int id) {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(id);
        value.setTenVaiTro("TEACHER");
        return value;
    }

    private static LopHocPhan section(int id) {
        LopHocPhan value = new LopHocPhan();
        value.setMaLophocphan(id);
        return value;
    }

    private static LichHoc schedule(int id, String status) {
        return schedule(id, LocalDate.of(2097, 1, id), status);
    }

    private static LichHoc schedule(int id, LocalDate date, String status) {
        LichHoc value = new LichHoc();
        value.setMaLichhoc(id);
        value.setMaLophocphan(8);
        value.setNgayHoc(date);
        value.setTrangThai(status);
        return value;
    }

    private static String decode(String value) throws Exception {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static int occurrences(String value, String target) {
        int count = 0;
        int position = 0;
        while ((position = value.indexOf(target, position)) >= 0) {
            count++;
            position += target.length();
        }
        return count;
    }

    private static ServletConfig config(LogCapture capture) {
        ServletContext context = proxy(ServletContext.class, (value, method, args) -> {
            if ("log".equals(method.getName())) capture.logged = true;
            return defaultValue(method.getReturnType());
        });
        return proxy(ServletConfig.class, (value, method, args) ->
                "getServletContext".equals(method.getName())
                        ? context : defaultValue(method.getReturnType()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == Void.TYPE) return null;
        return type.isPrimitive() ? Array.get(Array.newInstance(type, 1), 0) : null;
    }

    private static final class Fixture {
        private final FakeLopHocPhanService sections = new FakeLopHocPhanService();
        private final FakeLichHocService schedules = new FakeLichHocService();
        private final DiemDanhServlet servlet;

        private Fixture(FakeDiemDanhService service) {
            sections.sections = Collections.singletonList(section(8));
            schedules.schedules = Arrays.asList(
                    schedule(8, LocalDate.of(2026, 7, 26), "BinhThuong"),
                    schedule(9, "BinhThuong"),
                    schedule(10, "XinNghi"),
                    schedule(11, "DaHuy"));
            servlet = new DiemDanhServlet(service, sections, schedules, FIXED_CLOCK);
        }
    }

    private static final class RequestCapture {
        private final Map<String, String[]> params;
        private final TaiKhoan user;
        private final Map<String, Object> attributes = new HashMap<>();
        private String forwardPath;

        private RequestCapture(Map<String, String[]> params, TaiKhoan user) {
            this.params = params;
            this.user = user;
        }

        private HttpServletRequest proxy() {
            HttpSession session = DiemDanhServletTest.proxy(HttpSession.class,
                    (value, method, args) -> "getAttribute".equals(method.getName())
                            && "user".equals(args[0])
                            ? user : defaultValue(method.getReturnType()));
            return DiemDanhServletTest.proxy(HttpServletRequest.class, (value, method, args) -> {
                if ("getSession".equals(method.getName())) return session;
                if ("getParameter".equals(method.getName())) {
                    String[] values = params.get(args[0]);
                    return values == null || values.length == 0 ? null : values[0];
                }
                if ("getParameterValues".equals(method.getName())) return params.get(args[0]);
                if ("setAttribute".equals(method.getName())) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                }
                if ("getContextPath".equals(method.getName())) return "";
                if ("getRequestDispatcher".equals(method.getName())) {
                    String path = (String) args[0];
                    return DiemDanhServletTest.proxy(RequestDispatcher.class,
                            (dispatcher, dispatchMethod, dispatchArgs) -> {
                                if ("forward".equals(dispatchMethod.getName())) forwardPath = path;
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

        private HttpServletResponse proxy() {
            return DiemDanhServletTest.proxy(HttpServletResponse.class,
                    (value, method, args) -> {
                        if ("sendRedirect".equals(method.getName())) {
                            redirect = (String) args[0];
                            return null;
                        }
                        if ("sendError".equals(method.getName())) {
                            errorStatus = (Integer) args[0];
                            errorMessage = args.length > 1 ? (String) args[1] : null;
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class LogCapture {
        private boolean logged;
    }

    private static final class FakeDiemDanhService extends DiemDanhService {
        private int savedTeacher;
        private int savedSection;
        private int savedSchedule;
        private int readTeacher;
        private int readSection;
        private int readSchedule;
        private int readCalls;
        private boolean saveCalled;
        private List<DiemDanh> savedRows = new ArrayList<>();
        private AcademicException saveFailure;
        private RuntimeException readFailure;
        private RuntimeException runtimeSaveFailure;

        @Override
        public List<SinhVien> getBangDiemDanh(int teacher, int section, int schedule)
                throws AcademicException {
            readCalls++;
            readTeacher = teacher;
            readSection = section;
            readSchedule = schedule;
            if (readFailure != null) throw readFailure;
            return Collections.emptyList();
        }

        @Override
        public void saveDiemDanh(int teacher, int section, int schedule,
                                 List<DiemDanh> rows) throws AcademicException {
            saveCalled = true;
            savedTeacher = teacher;
            savedSection = section;
            savedSchedule = schedule;
            savedRows = rows;
            if (saveFailure != null) throw saveFailure;
            if (runtimeSaveFailure != null) throw runtimeSaveFailure;
        }

        @Override
        public void saveDiemDanh(int teacher, int section, int schedule,
                                 List<DiemDanh> rows, LocalDate today)
                throws AcademicException {
            saveDiemDanh(teacher, section, schedule, rows);
        }
    }

    private static final class FakeLopHocPhanService extends LopHocPhanService {
        private List<LopHocPhan> sections = Collections.emptyList();
        private int requestedTeacher;

        @Override
        public List<LopHocPhan> getAll(Integer term, Integer teacher) {
            requestedTeacher = teacher == null ? 0 : teacher;
            return sections;
        }
    }

    private static final class FakeLichHocService extends LichHocService {
        private List<LichHoc> schedules = Collections.emptyList();
        private int requestedSection;
        private int calls;

        @Override
        public List<LichHoc> findByLopHocPhan(int section) {
            calls++;
            requestedSection = section;
            return schedules;
        }
    }
}
