package Servlet;

import Models.AdminDashboardView;
import Models.GiangVienDashboardView;
import Models.SinhVienDashboardView;
import Models.TaiKhoan;
import Services.DashboardService;
import Services.GiangVienDashboardService;
import Services.SinhVienDashboardService;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleDashboardServletTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-07-27T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void sinhVienDashboardKiemTraVaiTroForwardVaCheLoiDuLieu() throws Exception {
        FakeDashboardService service = new FakeDashboardService();
        Fixture fixture = new Fixture(service);

        ResponseCapture anonymousResponse = new ResponseCapture();
        fixture.servlet.doGet(new RequestCapture(null).proxy(), anonymousResponse.proxy());
        assertEquals("/login", anonymousResponse.redirect);
        assertEquals(0, service.calls);

        TaiKhoan teacher = student();
        teacher.setTenVaiTro("TEACHER");
        ResponseCapture teacherResponse = new ResponseCapture();
        fixture.servlet.doGet(new RequestCapture(teacher).proxy(), teacherResponse.proxy());
        assertEquals("/login", teacherResponse.redirect);
        assertEquals(0, service.calls);

        RequestCapture studentRequest = new RequestCapture(student());
        fixture.servlet.doGet(studentRequest.proxy(), new ResponseCapture().proxy());
        assertEquals("/WEB-INF/student/dashboard.jsp", studentRequest.forwardPath);
        assertEquals(Collections.singleton("dashboard"), studentRequest.attributes.keySet());
        assertSame(service.result, studentRequest.attributes.get("dashboard"));
        assertEquals(LocalDate.of(2026, 7, 27), service.today);

        service.failure = new RuntimeException("database-secret");
        RequestCapture failingRequest = new RequestCapture(student());
        ResponseCapture failingResponse = new ResponseCapture();
        fixture.servlet.doGet(failingRequest.proxy(), failingResponse.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, failingResponse.errorStatus);
        assertEquals("Không thể tải dữ liệu dashboard lúc này.", failingResponse.errorMessage);
        assertFalse(failingResponse.errorMessage.contains("database-secret"));
        assertSame(service.failure, fixture.loggedFailure);
        assertTrue(failingRequest.attributes.isEmpty());
    }

    @Test
    void giangVienDashboardKiemTraVaiTroForwardVaCheLoiDuLieu() throws Exception {
        FakeTeacherDashboardService service = new FakeTeacherDashboardService();
        TeacherFixture fixture = new TeacherFixture(service);

        ResponseCapture anonymousResponse = new ResponseCapture();
        fixture.servlet.doGet(new RequestCapture(null).proxy(), anonymousResponse.proxy());
        assertEquals("/login", anonymousResponse.redirect);
        assertEquals(0, service.calls);

        TaiKhoan student = student();
        ResponseCapture studentResponse = new ResponseCapture();
        fixture.servlet.doGet(new RequestCapture(student).proxy(), studentResponse.proxy());
        assertEquals("/login", studentResponse.redirect);
        assertEquals(0, service.calls);

        RequestCapture teacherRequest = new RequestCapture(teacher());
        fixture.servlet.doGet(teacherRequest.proxy(), new ResponseCapture().proxy());
        assertEquals("/WEB-INF/teacher/dashboard.jsp", teacherRequest.forwardPath);
        assertEquals(Collections.singleton("dashboard"), teacherRequest.attributes.keySet());
        assertSame(service.result, teacherRequest.attributes.get("dashboard"));
        assertEquals(LocalDate.of(2026, 7, 27), service.today);

        service.failure = new RuntimeException("database-secret");
        RequestCapture failingRequest = new RequestCapture(teacher());
        ResponseCapture failingResponse = new ResponseCapture();
        fixture.servlet.doGet(failingRequest.proxy(), failingResponse.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, failingResponse.errorStatus);
        assertEquals("Không thể tải dữ liệu dashboard lúc này.", failingResponse.errorMessage);
        assertFalse(failingResponse.errorMessage.contains("database-secret"));
        assertSame(service.failure, fixture.loggedFailure);
        assertTrue(failingRequest.attributes.isEmpty());
    }

    @Test
    void giangVienDashboardCoiSessionSaiKieuLaChuaDangNhap() throws Exception {
        FakeTeacherDashboardService service = new FakeTeacherDashboardService();
        TeacherFixture fixture = new TeacherFixture(service);
        ResponseCapture response = new ResponseCapture();

        fixture.servlet.doGet(
                new RequestCapture("stale-session").proxy(), response.proxy());

        assertEquals("/login", response.redirect);
        assertEquals(0, service.calls);
    }

    @Test
    void adminDashboardKiemTraVaiTroForwardVaCheLoiDuLieu() throws Exception {
        FakeAdminDashboardService service = new FakeAdminDashboardService();
        AdminFixture fixture = new AdminFixture(service);

        ResponseCapture anonymousResponse = new ResponseCapture();
        fixture.servlet.doGet(
                new RequestCapture(null).proxy(), anonymousResponse.proxy());
        assertEquals("/login", anonymousResponse.redirect);
        assertEquals(0, service.calls);

        ResponseCapture studentResponse = new ResponseCapture();
        fixture.servlet.doGet(
                new RequestCapture(student()).proxy(), studentResponse.proxy());
        assertEquals("/sinhvien/dashboard", studentResponse.redirect);
        assertEquals(0, service.calls);

        ResponseCapture malformedResponse = new ResponseCapture();
        fixture.servlet.doGet(
                new RequestCapture("stale-session").proxy(),
                malformedResponse.proxy());
        assertEquals("/login", malformedResponse.redirect);
        assertEquals(0, service.calls);

        RequestCapture adminRequest = new RequestCapture(admin());
        fixture.servlet.doGet(
                adminRequest.proxy(), new ResponseCapture().proxy());
        assertEquals("/WEB-INF/admin/dashboard.jsp", adminRequest.forwardPath);
        assertEquals(Collections.singleton("dashboard"),
                adminRequest.attributes.keySet());
        assertSame(service.result, adminRequest.attributes.get("dashboard"));
        assertEquals(LocalDate.of(2026, 7, 27), service.today);

        service.failure = new RuntimeException("database-secret");
        RequestCapture failingRequest = new RequestCapture(admin());
        ResponseCapture failingResponse = new ResponseCapture();
        fixture.servlet.doGet(
                failingRequest.proxy(), failingResponse.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                failingResponse.errorStatus);
        assertEquals("Không thể tải dữ liệu dashboard lúc này.",
                failingResponse.errorMessage);
        assertFalse(failingResponse.errorMessage.contains("database-secret"));
        assertSame(service.failure, fixture.loggedFailure);
        assertTrue(failingRequest.attributes.isEmpty());
    }

    private static TaiKhoan student() {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(11);
        value.setTenVaiTro("STUDENT");
        return value;
    }

    private static TaiKhoan teacher() {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(7);
        value.setTenVaiTro("TEACHER");
        return value;
    }

    private static TaiKhoan admin() {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(1);
        value.setTenVaiTro("ADMIN");
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
        private final SinhVienDashboardServlet servlet;
        private Throwable loggedFailure;

        private Fixture(FakeDashboardService service) throws Exception {
            servlet = new SinhVienDashboardServlet(service, FIXED_CLOCK);
            ServletContext context = proxy(ServletContext.class, (value, method, args) -> {
                if ("log".equals(method.getName())
                        && args != null
                        && args.length == 2
                        && args[1] instanceof Throwable) {
                    loggedFailure = (Throwable) args[1];
                }
                return defaultValue(method.getReturnType());
            });
            ServletConfig config = proxy(ServletConfig.class, (value, method, args) ->
                    "getServletContext".equals(method.getName())
                            ? context : defaultValue(method.getReturnType()));
            servlet.init(config);
        }
    }

    private static final class TeacherFixture {
        private final GiangVienDashboardServlet servlet;
        private Throwable loggedFailure;

        private TeacherFixture(FakeTeacherDashboardService service) throws Exception {
            servlet = new GiangVienDashboardServlet(service, FIXED_CLOCK);
            ServletContext context = proxy(ServletContext.class, (value, method, args) -> {
                if ("log".equals(method.getName())
                        && args != null
                        && args.length == 2
                        && args[1] instanceof Throwable) {
                    loggedFailure = (Throwable) args[1];
                }
                return defaultValue(method.getReturnType());
            });
            ServletConfig config = proxy(ServletConfig.class, (value, method, args) ->
                    "getServletContext".equals(method.getName())
                            ? context : defaultValue(method.getReturnType()));
            servlet.init(config);
        }
    }

    private static final class AdminFixture {
        private final DashboardServlet servlet;
        private Throwable loggedFailure;

        private AdminFixture(FakeAdminDashboardService service) throws Exception {
            servlet = new DashboardServlet(service, FIXED_CLOCK);
            ServletContext context = proxy(
                    ServletContext.class, (value, method, args) -> {
                        if ("log".equals(method.getName())
                                && args != null
                                && args.length == 2
                                && args[1] instanceof Throwable) {
                            loggedFailure = (Throwable) args[1];
                        }
                        return defaultValue(method.getReturnType());
                    });
            ServletConfig config = proxy(
                    ServletConfig.class, (value, method, args) ->
                            "getServletContext".equals(method.getName())
                                    ? context
                                    : defaultValue(method.getReturnType()));
            servlet.init(config);
        }
    }

    private static final class RequestCapture {
        private final Object user;
        private final Map<String, Object> attributes = new HashMap<>();
        private String forwardPath;

        private RequestCapture(Object user) {
            this.user = user;
        }

        private HttpServletRequest proxy() {
            HttpSession session = user == null ? null : RoleDashboardServletTest.proxy(
                    HttpSession.class, (value, method, args) ->
                            "getAttribute".equals(method.getName())
                                    && "user".equals(args[0])
                                    ? user : defaultValue(method.getReturnType()));
            return RoleDashboardServletTest.proxy(
                    HttpServletRequest.class, (value, method, args) -> {
                        if ("getSession".equals(method.getName())) return session;
                        if ("setAttribute".equals(method.getName())) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        }
                        if ("getContextPath".equals(method.getName())) return "";
                        if ("getRequestDispatcher".equals(method.getName())) {
                            String path = (String) args[0];
                            return RoleDashboardServletTest.proxy(
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
        private String errorMessage;

        private HttpServletResponse proxy() {
            return RoleDashboardServletTest.proxy(
                    HttpServletResponse.class, (value, method, args) -> {
                        if ("sendRedirect".equals(method.getName())) {
                            redirect = (String) args[0];
                            return null;
                        }
                        if ("sendError".equals(method.getName())) {
                            errorStatus = (Integer) args[0];
                            if (args.length > 1) errorMessage = (String) args[1];
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class FakeDashboardService extends SinhVienDashboardService {
        private final SinhVienDashboardView result = new SinhVienDashboardView(
                0, 0, 0, 0, false, false,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        private int calls;
        private LocalDate today;
        private RuntimeException failure;

        @Override
        public SinhVienDashboardView build(int userId, LocalDate value) {
            calls++;
            today = value;
            if (failure != null) throw failure;
            return result;
        }
    }

    private static final class FakeTeacherDashboardService extends GiangVienDashboardService {
        private final GiangVienDashboardView result = new GiangVienDashboardView(
                0, 0, 0, 0,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        private int calls;
        private LocalDate today;
        private RuntimeException failure;

        @Override
        public GiangVienDashboardView build(int userId, LocalDate value) {
            calls++;
            today = value;
            if (failure != null) throw failure;
            return result;
        }
    }

    private static final class FakeAdminDashboardService extends DashboardService {
        private final AdminDashboardView result = new AdminDashboardView(
                0, 0, 0, 0,
                Collections.emptyList(), Collections.emptyList());
        private int calls;
        private LocalDate today;
        private RuntimeException failure;

        @Override
        public AdminDashboardView build(LocalDate value) {
            calls++;
            today = value;
            if (failure != null) throw failure;
            return result;
        }
    }
}
