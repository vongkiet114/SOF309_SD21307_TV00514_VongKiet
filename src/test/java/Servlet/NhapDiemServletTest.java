package Servlet;

import Models.DiemSo;
import Models.LopHocPhan;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.LopHocPhanService;
import Services.NhapDiemService;
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
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NhapDiemServletTest {
    @Test
    void postLayGiangVienTuSessionVaGiuOTrongThanhLenhXoa() throws Exception {
        FakeNhapDiemService service = new FakeNhapDiemService();
        RequestCapture request = new RequestCapture(validParams(), teacher(7));
        ResponseCapture response = new ResponseCapture();

        new NhapDiemServlet(service, new FakeLopHocPhanService())
                .doPost(request.proxy(), response.proxy());

        assertEquals(7, service.teacherId);
        assertEquals(8, service.sectionId);
        assertEquals(2, service.saved.size());
        assertEquals(new BigDecimal("8.25"), service.saved.get(0).getGiaTriDiem());
        assertNull(service.saved.get(1).getGiaTriDiem());
        assertTrue(decode(response.redirect).contains("Lưu điểm thành công"));
    }

    @Test
    void diemSaiForwardLaiBangVaGiuGiaTriNguoiDungNhap() throws Exception {
        FakeNhapDiemService service = new FakeNhapDiemService();
        Map<String, String[]> params = validParams();
        params.put("diem", new String[]{"7.123", ""});
        RequestCapture request = new RequestCapture(params, teacher(7));
        ResponseCapture response = new ResponseCapture();

        new NhapDiemServlet(service, new FakeLopHocPhanService())
                .doPost(request.proxy(), response.proxy());

        assertFalse(service.saveCalled);
        assertEquals(7, service.readTeacherId);
        assertEquals(8, service.readSectionId);
        assertEquals("/WEB-INF/teacher/nhapDiem.jsp", request.forwardPath);
        assertNull(response.redirect);
        assertTrue(((String) request.attributes.get("error")).contains("tối đa hai"));
        @SuppressWarnings("unchecked")
        Map<String, String> submitted = (Map<String, String>)
                request.attributes.get("submittedScores");
        assertEquals("7.123", submitted.get("11:21"));
        assertEquals("", submitted.get("12:22"));
    }

    @Test
    void baMangKhacDoDaiBiTuChoiTruocKhiGhi() throws Exception {
        FakeNhapDiemService service = new FakeNhapDiemService();
        Map<String, String[]> params = validParams();
        params.put("maThanhPhan", new String[]{"21"});
        RequestCapture request = new RequestCapture(params, teacher(7));

        new NhapDiemServlet(service, new FakeLopHocPhanService())
                .doPost(request.proxy(), new ResponseCapture().proxy());

        assertFalse(service.saveCalled);
        assertTrue(((String) request.attributes.get("error")).contains("không hợp lệ"));
    }

    @Test
    void getVaPostKhongTinIdGiangVienTuRequest() throws Exception {
        FakeNhapDiemService service = new FakeNhapDiemService();
        Map<String, String[]> params = new HashMap<>();
        params.put("maLHP", new String[]{"8"});
        params.put("maGiangVien", new String[]{"999"});
        RequestCapture request = new RequestCapture(params, teacher(7));

        new NhapDiemServlet(service, new FakeLopHocPhanService())
                .doGet(request.proxy(), new ResponseCapture().proxy());

        assertEquals(7, service.readTeacherId);
        assertEquals(8, service.readSectionId);
    }

    @Test
    void loiRepositoryChiLogNoiBoVaTraThongBaoChung() throws Exception {
        FakeNhapDiemService service = new FakeNhapDiemService();
        service.repositoryFailure = true;
        RequestCapture request = new RequestCapture(validParams(), teacher(7));
        ResponseCapture response = new ResponseCapture();
        LogCapture log = new LogCapture();
        NhapDiemServlet servlet = new NhapDiemServlet(service, new FakeLopHocPhanService());
        servlet.init(config(log));

        servlet.doPost(request.proxy(), response.proxy());

        assertTrue(log.logged);
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertFalse(response.errorMessage.contains("SQL secret"));
    }

    @Test
    void giaoDienGuiMoiOVaChoPhepHaiChuSoThapPhan() throws Exception {
        String jsp = new String(Files.readAllBytes(
                Paths.get("src/main/webapp/WEB-INF/teacher/nhapDiem.jsp")),
                StandardCharsets.UTF_8);

        assertTrue(jsp.contains("name=\"maDanhSach\""));
        assertTrue(jsp.contains("name=\"maThanhPhan\""));
        assertTrue(jsp.contains("name=\"diem\""));
        assertTrue(jsp.contains("step=\"0.01\""));
        assertTrue(jsp.contains("submittedScores"));
    }

    private static Map<String, String[]> validParams() {
        Map<String, String[]> values = new HashMap<>();
        values.put("maLHP", new String[]{"8"});
        values.put("maGiangVien", new String[]{"999"});
        values.put("maDanhSach", new String[]{"11", "12"});
        values.put("maThanhPhan", new String[]{"21", "22"});
        values.put("diem", new String[]{"8.25", ""});
        return values;
    }

    private static TaiKhoan teacher(int id) {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(id);
        value.setTenVaiTro("TEACHER");
        return value;
    }

    private static String decode(String value) throws Exception {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
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
        private final Map<String, String[]> params;
        private final TaiKhoan user;
        private final Map<String, Object> attributes = new HashMap<>();
        private String forwardPath;

        private RequestCapture(Map<String, String[]> params, TaiKhoan user) {
            this.params = params;
            this.user = user;
        }

        private HttpServletRequest proxy() {
            HttpSession session = NhapDiemServletTest.proxy(HttpSession.class,
                    (value, method, args) -> "getAttribute".equals(method.getName())
                            && "user".equals(args[0])
                            ? user : defaultValue(method.getReturnType()));
            return NhapDiemServletTest.proxy(HttpServletRequest.class, (value, method, args) -> {
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
                    return NhapDiemServletTest.proxy(RequestDispatcher.class,
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
            return NhapDiemServletTest.proxy(HttpServletResponse.class, (value, method, args) -> {
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

    private static final class FakeNhapDiemService extends NhapDiemService {
        private int teacherId;
        private int sectionId;
        private int readTeacherId;
        private int readSectionId;
        private List<DiemSo> saved;
        private boolean saveCalled;
        private boolean repositoryFailure;

        @Override
        public NhapDiemData getNhapDiemData(int teacher, int section)
                throws AcademicException {
            readTeacherId = teacher;
            readSectionId = section;
            return new NhapDiemData(Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyMap());
        }

        @Override
        public void saveDiem(int teacher, int section, List<DiemSo> values)
                throws AcademicException {
            saveCalled = true;
            teacherId = teacher;
            sectionId = section;
            saved = values;
            if (repositoryFailure) {
                throw new RepositoryException("SQL secret", new RuntimeException("test"));
            }
        }
    }

    private static final class FakeLopHocPhanService extends LopHocPhanService {
        @Override
        public List<LopHocPhan> getAll(Integer term, Integer teacher) {
            return Collections.emptyList();
        }
    }
}
