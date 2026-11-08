package Servlet;

import Models.GiangVien;
import Models.SinhVien;
import Models.TaiKhoan;
import Services.GiangVienService;
import Services.SinhVienService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class XemThongTinServletTest {
    @Test
    void giangVienThongTinForwardToiJspTonTaiVaHoSoCuChuyenHuong() throws Exception {
        Fixture fixture = new Fixture();
        RequestCapture request = new RequestCapture(teacher(7));

        fixture.xemThongTin.doGet(request.proxy(), new ResponseCapture().proxy());

        assertEquals("/WEB-INF/teacher/thongTinCaNhan.jsp", request.forwardPath);
        assertEquals("thongtin_gv", request.attributes.get("trang"));

        ResponseCapture legacy = new ResponseCapture();
        fixture.hoSo.doGet(new RequestCapture(teacher(7)).proxy(), legacy.proxy());
        assertEquals("/giangvien/thongtin", legacy.redirect);
    }

    @Test
    void jspHienThiDuTruongVaCoTrangThaiRong() throws Exception {
        Path studentPath = Paths.get("src/main/webapp/WEB-INF/student/thongTinCaNhan.jsp");
        Path teacherPath = Paths.get("src/main/webapp/WEB-INF/teacher/thongTinCaNhan.jsp");
        assertTrue(Files.isRegularFile(studentPath));
        assertTrue(Files.isRegularFile(teacherPath));
        String student = read(studentPath);
        String teacher = read(teacherPath);

        for (String label : Arrays.asList("Mã số sinh viên", "Email", "Lớp",
                "Chuyên ngành", "Giới tính", "Ngày sinh", "Địa chỉ",
                "Căn cước công dân", "Dân tộc", "Khóa học",
                "Ngày nhập học", "Trạng thái học tập")) {
            assertTrue(student.contains(label), label);
        }
        for (String label : Arrays.asList("Email", "Học vị", "Khoa",
                "Chuyên ngành", "Giới tính", "Ngày sinh", "Địa chỉ")) {
            assertTrue(teacher.contains(label), label);
        }
        assertTrue(student.contains("Chưa cập nhật"));
        assertTrue(teacher.contains("Chưa cập nhật"));
        assertFalse(teacher.contains("mật khẩu"));
    }

    @Test
    void bangHoSoKhongBiPhanTrangDeHienThiTatCaChiTiet() throws Exception {
        String student = read(Paths.get(
                "src/main/webapp/WEB-INF/student/thongTinCaNhan.jsp"));
        String teacher = read(Paths.get(
                "src/main/webapp/WEB-INF/teacher/thongTinCaNhan.jsp"));

        assertTrue(student.contains("<table data-no-paginate>"));
        assertTrue(teacher.contains("<table data-no-paginate>"));
    }

    @Test
    void hoSoKhongTonTaiVanForwardVaHienThiTrangThaiRong() throws Exception {
        Fixture fixture = new Fixture();
        fixture.teacherProfile = null;
        RequestCapture request = new RequestCapture(teacher(7));

        fixture.xemThongTin.doGet(request.proxy(), new ResponseCapture().proxy());

        assertEquals("/WEB-INF/teacher/thongTinCaNhan.jsp", request.forwardPath);
        assertNull(request.attributes.get("thongTin"));
    }

    @Test
    void loiDuLieuDuocLogVaKhongLoChiTiet() throws Exception {
        Fixture fixture = new Fixture();
        fixture.teacherFailure = new RuntimeException("jdbc password secret");
        ResponseCapture response = new ResponseCapture();
        LogCapture log = new LogCapture();
        fixture.xemThongTin.init(config(log));

        fixture.xemThongTin.doGet(
                new RequestCapture(teacher(7)).proxy(), response.proxy());

        assertTrue(log.logged);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể tải thông tin cá nhân lúc này.", response.errorMessage);
        assertFalse(response.errorMessage.contains("secret"));
    }

    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static TaiKhoan teacher(int id) {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(id);
        value.setTenVaiTro("TEACHER");
        return value;
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
        private SinhVien studentProfile = new SinhVien();
        private GiangVien teacherProfile = new GiangVien();
        private RuntimeException teacherFailure;
        private final FakeSinhVienService students = new FakeSinhVienService(this);
        private final FakeGiangVienService teachers = new FakeGiangVienService(this);
        private final XemThongTinServlet xemThongTin = servlet(students, teachers);
        private final HoSoGVServlet hoSo = new HoSoGVServlet();

        private static XemThongTinServlet servlet(SinhVienService students,
                                                  GiangVienService teachers) {
            try {
                Constructor<XemThongTinServlet> constructor = XemThongTinServlet.class
                        .getDeclaredConstructor(SinhVienService.class, GiangVienService.class);
                constructor.setAccessible(true);
                return constructor.newInstance(students, teachers);
            } catch (ReflectiveOperationException e) {
                fail("XemThongTinServlet phải cho phép tiêm service để kiểm thử tải hồ sơ");
                throw new AssertionError(e);
            }
        }
    }

    private static final class FakeSinhVienService extends SinhVienService {
        private final Fixture fixture;

        private FakeSinhVienService(Fixture fixture) {
            this.fixture = fixture;
        }

        @Override
        public SinhVien findByMaNguoidung(int maNguoidung) {
            return fixture.studentProfile;
        }
    }

    private static final class FakeGiangVienService extends GiangVienService {
        private final Fixture fixture;

        private FakeGiangVienService(Fixture fixture) {
            this.fixture = fixture;
        }

        @Override
        public GiangVien findByMaNguoidung(int maNguoidung) {
            if (fixture.teacherFailure != null) throw fixture.teacherFailure;
            return fixture.teacherProfile;
        }
    }

    private static final class RequestCapture {
        private final TaiKhoan user;
        private final Map<String, Object> attributes = new HashMap<>();
        private String forwardPath;

        private RequestCapture(TaiKhoan user) {
            this.user = user;
        }

        private HttpServletRequest proxy() {
            HttpSession session = XemThongTinServletTest.proxy(HttpSession.class,
                    (value, method, args) -> "getAttribute".equals(method.getName())
                            && "user".equals(args[0])
                            ? user : defaultValue(method.getReturnType()));
            return XemThongTinServletTest.proxy(HttpServletRequest.class,
                    (value, method, args) -> {
                        if ("getSession".equals(method.getName())) return session;
                        if ("setAttribute".equals(method.getName())) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        }
                        if ("getContextPath".equals(method.getName())) return "";
                        if ("getRequestDispatcher".equals(method.getName())) {
                            String path = (String) args[0];
                            return XemThongTinServletTest.proxy(RequestDispatcher.class,
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
            return XemThongTinServletTest.proxy(HttpServletResponse.class,
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
}
