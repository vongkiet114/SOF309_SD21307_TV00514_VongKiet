package Servlet;

import Models.CaHoc;
import Models.TaiKhoan;
import Services.CaHocService;
import Services.CatalogException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaHocServletTest {
    @Test void idSuaKhongHopLeKhongBiLuuThanhThem() throws Exception {
        for (String invalidId : new String[]{"abc", "-2"}) {
            FakeCaHocService service = new FakeCaHocService();
            RequestCapture request = new RequestCapture(params(invalidId, "07:15", "09:15"));
            ResponseCapture response = new ResponseCapture();

            new CaHocServlet(service).doPost(request.proxy(), response.proxy());

            assertFalse(service.saved, String.valueOf(invalidId));
            assertTrue(decode(response.redirect).endsWith("error=Mã ca học không hợp lệ."),
                    String.valueOf(invalidId));
        }
    }

    @Test void idRongHoacBangKhongVanDuocLuuThanhCaHocMoi() throws Exception {
        for (String createId : new String[]{null, "", "0"}) {
            FakeCaHocService service = new FakeCaHocService();
            RequestCapture request = new RequestCapture(params(createId, "07:15", "09:15"));
            ResponseCapture response = new ResponseCapture();

            new CaHocServlet(service).doPost(request.proxy(), response.proxy());

            assertTrue(service.saved, String.valueOf(createId));
            assertEquals(0, service.savedValue.getMaCa(), String.valueOf(createId));
            assertTrue(decode(response.redirect).endsWith("msg=Lưu ca học thành công."),
                    String.valueOf(createId));
        }
    }

    @Test void gioKhongHopLeForwardLaiDanhSachVoiDuDuLieuLoi() throws Exception {
        FakeCaHocService service = new FakeCaHocService();
        RequestCapture request = new RequestCapture(params("", "khong-phai-gio", "09:15"));
        ResponseCapture response = new ResponseCapture();

        new CaHocServlet(service).doPost(request.proxy(), response.proxy());

        assertFalse(service.saved);
        assertEquals("/WEB-INF/admin/caHocList.jsp", request.forwardedPath);
        assertNotNull(request.attributes.get("dsCaHoc"));
        assertNotNull(request.attributes.get("caHoc"));
        assertEquals("Giờ bắt đầu hoặc giờ kết thúc không hợp lệ.",
                request.attributes.get("error"));
    }

    private static Map<String, String> params(String id, String batDau, String ketThuc) {
        Map<String, String> values = new HashMap<>();
        values.put("action", "luu");
        values.put("id", id);
        values.put("gioBatdau", batDau);
        values.put("gioKetthuc", ketThuc);
        return values;
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
        final Map<String, String> parameters;
        final Map<String, Object> attributes = new HashMap<>();
        String forwardedPath;

        RequestCapture(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        HttpServletRequest proxy() {
            TaiKhoan admin = new TaiKhoan();
            admin.setTenVaiTro("ADMIN");
            HttpSession session = CaHocServletTest.proxy(HttpSession.class, (value, method, args) -> {
                if ("getAttribute".equals(method.getName()) && "user".equals(args[0])) {
                    return admin;
                }
                return defaultValue(method.getReturnType());
            });
            return CaHocServletTest.proxy(HttpServletRequest.class, (value, method, args) -> {
                if ("getSession".equals(method.getName())) {
                    return session;
                }
                if ("getParameter".equals(method.getName())) {
                    return parameters.get(args[0]);
                }
                if ("setAttribute".equals(method.getName())) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                }
                if ("getAttribute".equals(method.getName())) {
                    return attributes.get(args[0]);
                }
                if ("getRequestDispatcher".equals(method.getName())) {
                    forwardedPath = (String) args[0];
                    return CaHocServletTest.proxy(RequestDispatcher.class,
                            (dispatcher, dispatcherMethod, dispatcherArgs) ->
                                    defaultValue(dispatcherMethod.getReturnType()));
                }
                if ("getContextPath".equals(method.getName())) {
                    return "";
                }
                return defaultValue(method.getReturnType());
            });
        }
    }

    private static class ResponseCapture {
        String redirect;

        HttpServletResponse proxy() {
            return CaHocServletTest.proxy(HttpServletResponse.class, (value, method, args) -> {
                if ("sendRedirect".equals(method.getName())) {
                    redirect = (String) args[0];
                    return null;
                }
                return defaultValue(method.getReturnType());
            });
        }
    }

    private static class FakeCaHocService extends CaHocService {
        boolean saved;
        CaHoc savedValue;

        @Override public void save(CaHoc value) throws CatalogException {
            saved = true;
            savedValue = value;
        }

        @Override public java.util.List<CaHoc> getAll() {
            return Collections.emptyList();
        }
    }
}
