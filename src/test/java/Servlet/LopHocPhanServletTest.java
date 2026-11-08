package Servlet;

import Models.GiangVien;
import Models.HocKy;
import Models.LopHocPhan;
import Models.MonHoc;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.GiangVienService;
import Services.HocKyService;
import Services.LopHocPhanService;
import Services.MonHocService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LopHocPhanServletTest {
    @Test void loiNghiepVuKhiTaoForwardLaiFormVaGiuLuaChon() throws Exception {
        FakeLopHocPhanService service = new FakeLopHocPhanService();
        RequestCapture request = new RequestCapture(params("create", null));
        ResponseCapture response = new ResponseCapture();

        servlet(service).doPost(request.proxy(), response.proxy());

        assertEquals("/WEB-INF/admin/lhpForm.jsp", request.forwardPath);
        assertNull(response.redirect);
        assertEquals("Giảng viên không phù hợp.", request.attributes.get("error"));
        LopHocPhan retained = (LopHocPhan) request.attributes.get("lhp");
        assertEquals(2, retained.getMaMonhoc());
        assertEquals(7, retained.getMaGiangvien());
        assertEquals(4, retained.getMaHocky());
        assertEquals("DangHoc", retained.getTrangThai());
        assertEquals(Boolean.FALSE, request.attributes.get("isEdit"));
    }

    @Test void loiNghiepVuKhiSuaForwardLaiFormVaGiuId() throws Exception {
        FakeLopHocPhanService service = new FakeLopHocPhanService();
        RequestCapture request = new RequestCapture(params("update", "9"));
        ResponseCapture response = new ResponseCapture();

        servlet(service).doPost(request.proxy(), response.proxy());

        assertEquals("/WEB-INF/admin/lhpForm.jsp", request.forwardPath);
        assertNull(response.redirect);
        LopHocPhan retained = (LopHocPhan) request.attributes.get("lhp");
        assertEquals(9, retained.getMaLophocphan());
        assertEquals(Boolean.TRUE, request.attributes.get("isEdit"));
    }

    @Test void loiRepositoryChiTraThongBaoChung() throws Exception {
        FakeLopHocPhanService service = new FakeLopHocPhanService();
        service.repositoryFailure = true;
        RequestCapture request = new RequestCapture(params("create", null));
        ResponseCapture response = new ResponseCapture();
        LopHocPhanServlet servlet = servlet(service);
        servlet.init(config());

        servlet.doPost(request.proxy(), response.proxy());

        assertTrue(decode(response.redirect)
                .endsWith("error=Không thể xử lý yêu cầu lúc này."));
        assertNull(request.forwardPath);
    }

    @Test void formHienThiLoiVaChuyenNganhGiangVien() throws Exception {
        String jsp = new String(Files.readAllBytes(
                Paths.get("src/main/webapp/WEB-INF/admin/lhpForm.jsp")), StandardCharsets.UTF_8);

        assertTrue(jsp.contains("request.getAttribute(\"error\")"));
        assertTrue(jsp.contains("Html.escape(error)"));
        assertTrue(jsp.contains("gv.getTenChuyenNganh()"));
    }

    private static LopHocPhanServlet servlet(FakeLopHocPhanService service) {
        return new LopHocPhanServlet(service, new FakeMonHocService(),
                new FakeGiangVienService(), new FakeHocKyService());
    }

    private static Map<String, String> params(String action, String id) {
        Map<String, String> values = new HashMap<>();
        values.put("action", action);
        values.put("id", id);
        values.put("maMonhoc", "2");
        values.put("maGiangvien", "7");
        values.put("maHocky", "4");
        values.put("trangThai", "DangHoc");
        return values;
    }

    private static ServletConfig config() {
        ServletContext context = proxy(ServletContext.class,
                (value, method, args) -> defaultValue(method.getReturnType()));
        return proxy(ServletConfig.class, (value, method, args) -> {
            if ("getServletContext".equals(method.getName())) {
                return context;
            }
            return defaultValue(method.getReturnType());
        });
    }

    private static String decode(String value) throws Exception {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
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

    private static class RequestCapture {
        final Map<String, String> params;
        final Map<String, Object> attributes = new HashMap<>();
        String forwardPath;

        RequestCapture(Map<String, String> params) {
            this.params = params;
        }

        HttpServletRequest proxy() {
            TaiKhoan admin = new TaiKhoan();
            admin.setTenVaiTro("ADMIN");
            HttpSession session = LopHocPhanServletTest.proxy(HttpSession.class, (value, method, args) -> {
                if ("getAttribute".equals(method.getName()) && "user".equals(args[0])) {
                    return admin;
                }
                return defaultValue(method.getReturnType());
            });
            return LopHocPhanServletTest.proxy(HttpServletRequest.class, (value, method, args) -> {
                if ("getSession".equals(method.getName())) {
                    return session;
                }
                if ("getParameter".equals(method.getName())) {
                    return params.get(args[0]);
                }
                if ("getParameterValues".equals(method.getName())) {
                    String param = params.get(args[0]);
                    return param != null ? new String[]{param} : null;
                }
                if ("setAttribute".equals(method.getName())) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                }
                if ("getContextPath".equals(method.getName())) {
                    return "";
                }
                if ("getRequestDispatcher".equals(method.getName())) {
                    String path = (String) args[0];
                    return LopHocPhanServletTest.proxy(RequestDispatcher.class,
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

    private static class ResponseCapture {
        String redirect;

        HttpServletResponse proxy() {
            return LopHocPhanServletTest.proxy(HttpServletResponse.class, (value, method, args) -> {
                if ("sendRedirect".equals(method.getName())) {
                    redirect = (String) args[0];
                    return null;
                }
                return defaultValue(method.getReturnType());
            });
        }
    }

    private static class FakeLopHocPhanService extends LopHocPhanService {
        boolean repositoryFailure;

        @Override public int create(LopHocPhan value) throws AcademicException {
            fail();
            return 0;
        }

        @Override public void update(LopHocPhan value) throws AcademicException {
            fail();
        }

        private void fail() throws AcademicException {
            if (repositoryFailure) {
                throw new RepositoryException("SQL test detail", new RuntimeException("test"));
            }
            throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                    "Giảng viên không phù hợp.");
        }
    }

    private static class FakeMonHocService extends MonHocService {
        @Override public List<MonHoc> getAll() { return Collections.emptyList(); }
    }

    private static class FakeGiangVienService extends GiangVienService {
        @Override public List<GiangVien> dsGiangVien() { return Collections.emptyList(); }
    }

    private static class FakeHocKyService extends HocKyService {
        @Override public List<HocKy> getAll() { return Collections.emptyList(); }
    }
}
