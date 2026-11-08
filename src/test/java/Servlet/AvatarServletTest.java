package Servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AvatarServletTest {
    Path tempRoot;

    @BeforeEach
    void setUpWorkspaceTemp() throws Exception {
        Path workspaceTemp = Paths.get("target", "avatar-test-tmp")
                .toAbsolutePath().normalize();
        Files.createDirectories(workspaceTemp);
        tempRoot = Files.createTempDirectory(workspaceTemp, "serving-");
    }

    @Test
    void phucVuAnhBenNgoaiWarVoiMimeVaHeaderAnToan() throws Exception {
        Path uploadRoot = tempRoot.resolve("uploads");
        byte[] content = new byte[]{1, 2, 3, 4};
        Files.createDirectories(uploadRoot.resolve("sinhvien"));
        Files.write(uploadRoot.resolve("sinhvien").resolve("avatar.png"), content);
        ServletContext context = context(uploadRoot, null, null);
        ResponseCapture response = new ResponseCapture();

        new AvatarServlet().doGet(request("/sinhvien/avatar.png", context),
                response.proxy());

        assertEquals("image/png", response.contentType);
        assertEquals("nosniff", response.headers.get("X-Content-Type-Options"));
        assertEquals("public, max-age=86400", response.headers.get("Cache-Control"));
        assertEquals(content.length, response.contentLength);
        assertArrayEquals(content, response.body.toByteArray());
        assertEquals(0, response.errorStatus);
    }

    @Test
    void fallbackSangAnhDemoDongGoiMaKhongCanRealPath() throws Exception {
        Path uploadRoot = tempRoot.resolve("uploads");
        byte[] bundled = new byte[]{9, 8, 7};
        ServletContext context = context(
                uploadRoot, "/assets/img/giangvien/demo.jpg", bundled);
        ResponseCapture response = new ResponseCapture();

        new AvatarServlet().doGet(request("/giangvien/demo.jpg", context),
                response.proxy());

        assertEquals("image/jpeg", response.contentType);
        assertArrayEquals(bundled, response.body.toByteArray());
        assertEquals(0, response.errorStatus);
    }

    @Test
    void tuChoiTraversalVaKhongDocTaiNguyenBundledNgoaiThuMucChoPhep()
            throws Exception {
        Path uploadRoot = tempRoot.resolve("uploads");
        boolean[] resourceRead = {false};
        ServletContext context = proxy(ServletContext.class, (value, method, args) -> {
            if ("getInitParameter".equals(method.getName())) return uploadRoot.toString();
            if ("getRealPath".equals(method.getName())) return null;
            if ("getResourceAsStream".equals(method.getName())) {
                resourceRead[0] = true;
                return new ByteArrayInputStream(new byte[]{5});
            }
            return defaultValue(method.getReturnType());
        });
        ResponseCapture response = new ResponseCapture();

        new AvatarServlet().doGet(request("/sinhvien/../secret.png", context),
                response.proxy());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.errorStatus);
        assertFalse(resourceRead[0]);
        assertEquals(0, response.body.size());
    }

    private static HttpServletRequest request(String pathInfo, ServletContext context) {
        return proxy(HttpServletRequest.class, (value, method, args) -> {
            if ("getPathInfo".equals(method.getName())) return pathInfo;
            if ("getServletContext".equals(method.getName())) return context;
            return defaultValue(method.getReturnType());
        });
    }

    private static ServletContext context(Path uploadRoot,
                                          String bundledPath,
                                          byte[] bundled) {
        return proxy(ServletContext.class, (value, method, args) -> {
            if ("getInitParameter".equals(method.getName())) return uploadRoot.toString();
            if ("getRealPath".equals(method.getName())) return null;
            if ("getResourceAsStream".equals(method.getName())
                    && bundledPath != null && bundledPath.equals(args[0])) {
                return new ByteArrayInputStream(bundled);
            }
            return defaultValue(method.getReturnType());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == Void.TYPE) return null;
        return type.isPrimitive()
                ? Array.get(Array.newInstance(type, 1), 0) : null;
    }

    private static final class ResponseCapture {
        final Map<String, String> headers = new HashMap<>();
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        String contentType;
        long contentLength;
        int errorStatus;

        HttpServletResponse proxy() {
            ServletOutputStream output = new ServletOutputStream() {
                @Override public void write(int value) {
                    body.write(value);
                }

                @Override public boolean isReady() {
                    return true;
                }

                @Override public void setWriteListener(WriteListener listener) {
                }
            };
            return AvatarServletTest.proxy(HttpServletResponse.class,
                    (value, method, args) -> {
                        switch (method.getName()) {
                            case "setContentType":
                                contentType = (String) args[0];
                                return null;
                            case "setHeader":
                                headers.put((String) args[0], (String) args[1]);
                                return null;
                            case "setContentLengthLong":
                                contentLength = (Long) args[0];
                                return null;
                            case "getOutputStream":
                                return output;
                            case "sendError":
                                errorStatus = (Integer) args[0];
                                return null;
                            default:
                                return defaultValue(method.getReturnType());
                        }
                    });
        }
    }
}
