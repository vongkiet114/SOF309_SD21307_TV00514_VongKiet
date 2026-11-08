package Servlet;

import Models.DiemDanh;
import Models.LichHoc;
import Models.LopHocPhan;
import Models.SinhVien;
import Models.TaiKhoan;
import Services.AcademicException;
import Services.DiemDanhService;
import Services.LichHocService;
import Services.LopHocPhanService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminDiemDanhServletTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-07-27T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void getAnBuoiQuaKhuVaSapTheoNgayRoiCa() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        Map<String, String[]> params = new HashMap<>();
        params.put("maLHP", new String[]{"8"});
        RequestCapture request = new RequestCapture(params, admin());

        fixture.servlet.doGet(request.proxy(), new ResponseCapture().proxy());

        @SuppressWarnings("unchecked")
        List<LichHoc> schedules = (List<LichHoc>) request.attributes.get("dsBuoi");
        assertEquals(Arrays.asList(10, 9, 11), schedules.stream()
                .map(LichHoc::getMaLichhoc)
                .collect(Collectors.toList()));
        assertFalse(schedules.stream()
                .anyMatch(value -> LocalDate.of(2026, 7, 26).equals(value.getNgayHoc())));
        assertEquals("/WEB-INF/admin/diemDanhBu.jsp", request.forwardPath);
    }

    @Test
    void postBuoiQuaKhuKhongGoiSave() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        Map<String, String[]> params = validParams(8);
        RequestCapture request = new RequestCapture(params, admin());
        ResponseCapture response = new ResponseCapture();

        fixture.servlet.doPost(request.proxy(), response.proxy());

        assertEquals(0, service.saveCalls);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.errorStatus);
        assertTrue(((String) request.attributes.get("error")).contains("đã qua"));
    }

    @Test
    void postHomNayVaTuongLaiDeuLuuVoiNgayTuClock() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);

        for (int scheduleId : new int[]{10, 11}) {
            ResponseCapture response = new ResponseCapture();
            fixture.servlet.doPost(
                    new RequestCapture(validParams(scheduleId), admin()).proxy(),
                    response.proxy());
            assertTrue(response.redirect.contains("maLichhoc=" + scheduleId));
        }

        assertEquals(Arrays.asList(10, 11), service.savedSchedules);
        assertEquals(LocalDate.of(2026, 7, 27), service.savedToday);
    }

    @Test
    void chuaDangNhapVeLoginVaSaiVaiTroBiForbidden() throws Exception {
        FakeDiemDanhService service = new FakeDiemDanhService();
        Fixture fixture = new Fixture(service);
        ResponseCapture anonymousResponse = new ResponseCapture();

        fixture.servlet.doGet(
                new RequestCapture(Collections.<String, String[]>emptyMap(), null).proxy(),
                anonymousResponse.proxy());

        assertEquals("/login", anonymousResponse.redirect);

        TaiKhoan teacher = admin();
        teacher.setTenVaiTro("TEACHER");
        ResponseCapture teacherResponse = new ResponseCapture();
        fixture.servlet.doPost(
                new RequestCapture(validParams(10), teacher).proxy(),
                teacherResponse.proxy());

        assertEquals(HttpServletResponse.SC_FORBIDDEN, teacherResponse.errorStatus);
        assertEquals(0, service.saveCalls);
    }

    private static Map<String, String[]> validParams(int scheduleId) {
        Map<String, String[]> values = new HashMap<>();
        values.put("maLHP", new String[]{"8"});
        values.put("maLichhoc", new String[]{Integer.toString(scheduleId)});
        values.put("maDanhSach", new String[]{"11", "12"});
        values.put("trangThai", new String[]{"CoMat", "Vang"});
        return values;
    }

    private static TaiKhoan admin() {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(1);
        value.setTenVaiTro("ADMIN");
        return value;
    }

    private static LopHocPhan section(int id) {
        LopHocPhan value = new LopHocPhan();
        value.setMaLophocphan(id);
        return value;
    }

    private static LichHoc schedule(int id,
                                    int sectionId,
                                    LocalDate date,
                                    String status,
                                    int shift) {
        LichHoc value = new LichHoc();
        value.setMaLichhoc(id);
        value.setMaLophocphan(sectionId);
        value.setNgayHoc(date);
        value.setTrangThai(status);
        value.setMaCa(shift);
        return value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == Void.TYPE) return null;
        return type.isPrimitive() ? Array.get(Array.newInstance(type, 1), 0) : null;
    }

    private static final class Fixture {
        private final FakeLopHocPhanService sections = new FakeLopHocPhanService();
        private final FakeLichHocService schedules = new FakeLichHocService();
        private final AdminDiemDanhServlet servlet;

        private Fixture(FakeDiemDanhService service) {
            sections.sections = Collections.singletonList(section(8));
            schedules.schedules = Arrays.asList(
                    schedule(8, 8, LocalDate.of(2026, 7, 26), "BinhThuong", 4),
                    schedule(9, 8, LocalDate.of(2026, 7, 27), "BinhThuong", 2),
                    schedule(10, 8, LocalDate.of(2026, 7, 27), "BinhThuong", 1),
                    schedule(11, 8, LocalDate.of(2026, 7, 28), "BinhThuong", 3),
                    schedule(12, 8, LocalDate.of(2026, 7, 27), "DaHuy", 0),
                    schedule(13, 9, LocalDate.of(2026, 7, 27), "BinhThuong", 0));
            servlet = new AdminDiemDanhServlet(
                    service, sections, schedules, FIXED_CLOCK);
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
            HttpSession session = AdminDiemDanhServletTest.proxy(
                    HttpSession.class, (value, method, args) ->
                            "getAttribute".equals(method.getName())
                                    && "user".equals(args[0])
                                    ? user : defaultValue(method.getReturnType()));
            return AdminDiemDanhServletTest.proxy(
                    HttpServletRequest.class, (value, method, args) -> {
                        if ("getSession".equals(method.getName())) return session;
                        if ("getParameter".equals(method.getName())) {
                            String[] values = params.get(args[0]);
                            return values == null || values.length == 0
                                    ? null : values[0];
                        }
                        if ("getParameterValues".equals(method.getName())) {
                            return params.get(args[0]);
                        }
                        if ("setAttribute".equals(method.getName())) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        }
                        if ("getContextPath".equals(method.getName())) return "";
                        if ("getRequestDispatcher".equals(method.getName())) {
                            String path = (String) args[0];
                            return AdminDiemDanhServletTest.proxy(
                                    RequestDispatcher.class,
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

        private HttpServletResponse proxy() {
            return AdminDiemDanhServletTest.proxy(
                    HttpServletResponse.class, (value, method, args) -> {
                        if ("sendRedirect".equals(method.getName())) {
                            redirect = (String) args[0];
                            return null;
                        }
                        if ("sendError".equals(method.getName())) {
                            errorStatus = (Integer) args[0];
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class FakeDiemDanhService extends DiemDanhService {
        private int saveCalls;
        private final List<Integer> savedSchedules = new ArrayList<>();
        private LocalDate savedToday;

        @Override
        public List<SinhVien> getBangDiemDanhAdmin(int section, int schedule)
                throws AcademicException {
            return Collections.emptyList();
        }

        @Override
        public void saveDiemDanhAdmin(int section,
                                      int schedule,
                                      List<DiemDanh> rows,
                                      LocalDate today)
                throws AcademicException {
            saveCalls++;
            savedSchedules.add(schedule);
            savedToday = today;
        }
    }

    private static final class FakeLopHocPhanService extends LopHocPhanService {
        private List<LopHocPhan> sections = Collections.emptyList();

        @Override
        public List<LopHocPhan> getAll(Integer term, Integer teacher) {
            return sections;
        }
    }

    private static final class FakeLichHocService extends LichHocService {
        private List<LichHoc> schedules = Collections.emptyList();

        @Override
        public List<LichHoc> findByLopHocPhan(int sectionId) {
            return schedules;
        }
    }
}
