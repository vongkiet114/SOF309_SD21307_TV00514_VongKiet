package Servlet;

import Models.BangDiemHocPhan;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.XemBangDiemService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XemBangDiemServletTest {
    @Test
    void chiLayBangDiemCuaSinhVienTrongSession() throws Exception {
        FakeService service = new FakeService();
        RequestCapture request = new RequestCapture(student(42));
        ResponseCapture response = new ResponseCapture();

        new XemBangDiemServlet(service).doGet(request.proxy(), response.proxy());

        assertEquals(42, service.studentId);
        assertEquals("/WEB-INF/student/bangDiem.jsp", request.forwardPath);
        assertEquals(service.result, request.attributes.get("bangDiem"));
        assertNull(response.redirect);
    }

    @Test
    void loiRepositoryKhongLamLoChiTietSql() throws Exception {
        FakeService service = new FakeService();
        service.repositoryFailure = true;
        RequestCapture request = new RequestCapture(student(42));
        ResponseCapture response = new ResponseCapture();
        LogCapture log = new LogCapture();
        XemBangDiemServlet servlet = new XemBangDiemServlet(service);
        servlet.init(config(log));

        servlet.doGet(request.proxy(), response.proxy());

        assertTrue(log.logged);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertFalse(response.errorMessage.contains("SQL secret"));
        assertNull(request.forwardPath);
    }

    private static TaiKhoan student(int id) {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(id);
        value.setTenVaiTro("STUDENT");
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

    private static final class RequestCapture {
        private final TaiKhoan user;
        private final Map<String, Object> attributes = new HashMap<>();
        private String forwardPath;

        private RequestCapture(TaiKhoan user) {
            this.user = user;
        }

        private HttpServletRequest proxy() {
            HttpSession session = XemBangDiemServletTest.proxy(HttpSession.class,
                    (value, method, args) -> "getAttribute".equals(method.getName())
                            && "user".equals(args[0])
                            ? user : defaultValue(method.getReturnType()));
            return XemBangDiemServletTest.proxy(HttpServletRequest.class, (value, method, args) -> {
                if ("getSession".equals(method.getName())) return session;
                if ("getParameter".equals(method.getName())
                        && "maNguoiDung".equals(args[0])) return "999";
                if ("setAttribute".equals(method.getName())) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                }
                if ("getContextPath".equals(method.getName())) return "";
                if ("getRequestDispatcher".equals(method.getName())) {
                    String path = (String) args[0];
                    return XemBangDiemServletTest.proxy(RequestDispatcher.class,
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
            return XemBangDiemServletTest.proxy(HttpServletResponse.class,
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

    private static final class FakeService extends XemBangDiemService {
        private final List<BangDiemHocPhan> result = Collections.singletonList(
                new BangDiemHocPhan());
        private int studentId;
        private boolean repositoryFailure;

        @Override
        public List<BangDiemHocPhan> getBangDiem(int id) {
            studentId = id;
            if (repositoryFailure) {
                throw new RepositoryException("SQL secret", new RuntimeException("test"));
            }
            return result;
        }
    }

    private static final class LogCapture {
        private boolean logged;
    }
}
