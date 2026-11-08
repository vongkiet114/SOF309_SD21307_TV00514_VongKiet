package Servlet;

import Models.HocKy;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CatalogException;
import Services.HocKyService;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HocKyServletTest {
    @Test void idSuaKhongHopLeKhongBiLuuThanhThem() throws Exception {
        for (String invalidId : new String[]{"abc", "-2"}) {
            FakeHocKyService service = new FakeHocKyService();
            ResponseCapture response = new ResponseCapture();

            new HocKyServlet(service).doPost(request(params(invalidId)), response.proxy());

            assertFalse(service.saved, invalidId);
            assertTrue(decode(response.redirect).endsWith("error=Mã học kỳ không hợp lệ."), invalidId);
        }
    }

    @Test void idRongHoacBangKhongVanDuocLuuThanhHocKyMoi() throws Exception {
        for (String createId : new String[]{null, "", "0"}) {
            FakeHocKyService service = new FakeHocKyService();
            ResponseCapture response = new ResponseCapture();

            new HocKyServlet(service).doPost(request(params(createId)), response.proxy());

            assertTrue(service.saved, String.valueOf(createId));
            assertEquals(0, service.savedValue.getMaHocky(), String.valueOf(createId));
            assertTrue(decode(response.redirect).endsWith("msg=Lưu học kỳ thành công."),
                    String.valueOf(createId));
        }
    }

    @Test void loiRepositoryDungThongBaoChung() throws Exception {
        FakeHocKyService service = new FakeHocKyService();
        service.repositoryFailure = true;
        ResponseCapture response = new ResponseCapture();
        HocKyServlet servlet = new HocKyServlet(service);
        servlet.init(config());

        servlet.doPost(request(params("")), response.proxy());

        assertTrue(decode(response.redirect)
                .endsWith("error=Không thể xử lý yêu cầu lúc này."));
    }

    private static Map<String, String> params(String id) {
        Map<String, String> values = new HashMap<>();
        values.put("action", "luu");
        values.put("maHocky", id);
        values.put("tenHocky", "Fall 2026");
        values.put("block", "1");
        values.put("namHoc", "2026");
        values.put("ngayBatdau", "2026-09-01");
        values.put("ngayKetthuc", "2026-10-30");
        return values;
    }

    private static HttpServletRequest request(Map<String, String> params) {
        TaiKhoan admin = new TaiKhoan();
        admin.setTenVaiTro("ADMIN");
        HttpSession session = proxy(HttpSession.class, (value, method, args) -> {
            if ("getAttribute".equals(method.getName()) && "user".equals(args[0])) {
                return admin;
            }
            return defaultValue(method.getReturnType());
        });
        return proxy(HttpServletRequest.class, (value, method, args) -> {
            if ("getSession".equals(method.getName())) {
                return session;
            }
            if ("getParameter".equals(method.getName())) {
                return params.get(args[0]);
            }
            if ("getContextPath".equals(method.getName())) {
                return "";
            }
            return defaultValue(method.getReturnType());
        });
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

    private static class ResponseCapture {
        String redirect;

        HttpServletResponse proxy() {
            return HocKyServletTest.proxy(HttpServletResponse.class, (value, method, args) -> {
                if ("sendRedirect".equals(method.getName())) {
                    redirect = (String) args[0];
                    return null;
                }
                return defaultValue(method.getReturnType());
            });
        }
    }

    private static class FakeHocKyService extends HocKyService {
        boolean saved;
        boolean repositoryFailure;
        HocKy savedValue;

        @Override public void save(HocKy value) throws CatalogException {
            if (repositoryFailure) {
                throw new RepositoryException("test", new RuntimeException("test"));
            }
            saved = true;
            savedValue = value;
        }
    }
}
