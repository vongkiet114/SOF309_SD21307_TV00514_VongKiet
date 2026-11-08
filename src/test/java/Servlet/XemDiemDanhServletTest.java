package Servlet;

import Models.LichSuDiemDanhItem;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.XemDiemDanhService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XemDiemDanhServletTest {
    @Test
    void loiTaiLichSuDuocLogVaTra503ChungKhongLoChiTiet() throws Exception {
        XemDiemDanhServlet servlet = new XemDiemDanhServlet(new FailingService());
        LogCapture log = new LogCapture();
        servlet.init(config(log));
        ResponseCapture response = new ResponseCapture();

        servlet.doGet(request(student(9)), response.proxy());

        assertTrue(log.logged);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể tải lịch sử điểm danh lúc này.", response.errorMessage);
        assertFalse(response.errorMessage.contains("secret"));
    }

    private static TaiKhoan student(int id) {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(id);
        value.setTenVaiTro("STUDENT");
        return value;
    }

    private static HttpServletRequest request(TaiKhoan user) {
        HttpSession session = proxy(HttpSession.class, (value, method, args) ->
                "getAttribute".equals(method.getName()) && "user".equals(args[0])
                        ? user : defaultValue(method.getReturnType()));
        return proxy(HttpServletRequest.class, (value, method, args) -> {
            if ("getSession".equals(method.getName())) return session;
            if ("getContextPath".equals(method.getName())) return "";
            return defaultValue(method.getReturnType());
        });
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

    private static final class FailingService extends XemDiemDanhService {
        @Override
        public List<Models.MonHoc> getMonHocDaDiemDanh(int maNguoiDung) {
            return java.util.Collections.emptyList();
        }

        @Override
        public List<LichSuDiemDanhItem> getLichSuDiemDanh(int maNguoiDung, Integer maMonhoc,
                                                          Integer nam, Integer thang) {
            throw new RepositoryException("jdbc password secret", new RuntimeException("test"));
        }
    }

    private static final class ResponseCapture {
        private int errorStatus;
        private String errorMessage;

        private HttpServletResponse proxy() {
            return XemDiemDanhServletTest.proxy(HttpServletResponse.class,
                    (value, method, args) -> {
                        if ("sendError".equals(method.getName())) {
                            errorStatus = (Integer) args[0];
                            errorMessage = args.length > 1 ? (String) args[1] : null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class LogCapture {
        private boolean logged;
    }
}
