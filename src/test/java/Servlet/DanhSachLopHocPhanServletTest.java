package Servlet;

import Models.LopHocPhan;
import Models.SinhVien;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.DanhSachLopHocPhanService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DanhSachLopHocPhanServletTest {
    @Test
    void taiKhoanKhongPhaiAdminBiChanTruocKhiDocHoacGhiDuLieu() throws Exception {
        FakeService service = new FakeService();
        DanhSachLopHocPhanServlet servlet = servlet(service);

        ResponseCapture getResponse = new ResponseCapture();
        servlet.doGet(request(params("7", null, null), "TEACHER").proxy(),
                getResponse.proxy());

        ResponseCapture postResponse = new ResponseCapture();
        servlet.doPost(request(params("7", "add", "9"), "STUDENT").proxy(),
                postResponse.proxy());

        assertEquals(HttpServletResponse.SC_FORBIDDEN, getResponse.errorStatus);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, postResponse.errorStatus);
        assertEquals(0, service.calls);
        assertNull(getResponse.redirect);
        assertNull(postResponse.redirect);
    }

    @Test
    void loiRepositoryKhiGetTra503ChungVaKhongRedirectVeChinhNo() throws Exception {
        FakeService service = new FakeService();
        service.repositoryFailureOnRead = true;
        DanhSachLopHocPhanServlet servlet = servlet(service);
        ResponseCapture response = new ResponseCapture();

        servlet.doGet(request(params("7", null, null), "ADMIN").proxy(), response.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertNull(response.redirect);
        assertFalse(response.errorMessage.contains("JDBC"));
    }

    @Test
    void loiRepositoryKhiPostTra503ChungVaKhongPrgSangTrangDangLoi() throws Exception {
        FakeService service = new FakeService();
        service.repositoryFailureOnWrite = true;
        DanhSachLopHocPhanServlet servlet = servlet(service);
        ResponseCapture response = new ResponseCapture();

        servlet.doPost(request(params("7", "add", "9"), "ADMIN").proxy(), response.proxy());

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.errorStatus);
        assertEquals("Không thể xử lý yêu cầu lúc này.", response.errorMessage);
        assertNull(response.redirect);
    }

    @Test
    void postThemThanhCongDungPostRedirectGetVeDungLop() throws Exception {
        FakeService service = new FakeService();
        DanhSachLopHocPhanServlet servlet = servlet(service);
        ResponseCapture response = new ResponseCapture();

        servlet.doPost(request(params("7", "add", "9"), "ADMIN").proxy(), response.proxy());

        assertEquals(7, service.sectionId);
        assertEquals(9, service.studentId);
        assertEquals("/QLSV/admin/lophocphan/sinhvien?maLHP=7&msg="
                        + "Thêm sinh viên vào lớp học phần thành công.",
                URLDecoder.decode(response.redirect, StandardCharsets.UTF_8.name()));
        assertEquals(0, response.errorStatus);
    }

    private static DanhSachLopHocPhanServlet servlet(FakeService service) throws Exception {
        DanhSachLopHocPhanServlet servlet = new DanhSachLopHocPhanServlet(service);
        servlet.init(config());
        return servlet;
    }

    private static RequestCapture request(Map<String, String> parameters, String role) {
        return new RequestCapture(parameters, role);
    }

    private static Map<String, String> params(String sectionId, String action, String relatedId) {
        Map<String, String> values = new HashMap<>();
        values.put("maLHP", sectionId);
        values.put("action", action);
        values.put("maNguoidung", relatedId);
        values.put("maDanhsach", relatedId);
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
        private final Map<String, String> parameters;
        private final String role;

        RequestCapture(Map<String, String> parameters, String role) {
            this.parameters = parameters;
            this.role = role;
        }

        HttpServletRequest proxy() {
            TaiKhoan account = new TaiKhoan();
            account.setTenVaiTro(role);
            HttpSession session = DanhSachLopHocPhanServletTest.proxy(HttpSession.class,
                    (value, method, args) -> {
                        if ("getAttribute".equals(method.getName()) && "user".equals(args[0])) {
                            return account;
                        }
                        return defaultValue(method.getReturnType());
                    });
            return DanhSachLopHocPhanServletTest.proxy(HttpServletRequest.class,
                    (value, method, args) -> {
                        if ("getSession".equals(method.getName())) {
                            return session;
                        }
                        if ("getParameter".equals(method.getName())) {
                            return parameters.get(args[0]);
                        }
                        if ("getContextPath".equals(method.getName())) {
                            return "/QLSV";
                        }
                        if ("getRequestDispatcher".equals(method.getName())) {
                            return DanhSachLopHocPhanServletTest.proxy(RequestDispatcher.class,
                                    (dispatcher, dispatchMethod, dispatchArgs) ->
                                            defaultValue(dispatchMethod.getReturnType()));
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static class ResponseCapture {
        int errorStatus;
        String errorMessage;
        String redirect;

        HttpServletResponse proxy() {
            return DanhSachLopHocPhanServletTest.proxy(HttpServletResponse.class,
                    (value, method, args) -> {
                        if ("sendError".equals(method.getName())) {
                            errorStatus = (Integer) args[0];
                            errorMessage = args.length > 1 ? (String) args[1] : null;
                            return null;
                        }
                        if ("sendRedirect".equals(method.getName())) {
                            redirect = (String) args[0];
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static class FakeService extends DanhSachLopHocPhanService {
        int calls;
        int sectionId;
        int studentId;
        boolean repositoryFailureOnRead;
        boolean repositoryFailureOnWrite;

        @Override
        public LopHocPhan getSection(int sectionId) {
            calls++;
            if (repositoryFailureOnRead) {
                throw new RepositoryException("JDBC test detail", new RuntimeException("test"));
            }
            LopHocPhan section = new LopHocPhan();
            section.setMaLophocphan(sectionId);
            return section;
        }

        @Override
        public List<SinhVien> getMembers(int sectionId) {
            calls++;
            return Collections.emptyList();
        }

        @Override
        public List<SinhVien> findCandidates(int sectionId, String keyword) {
            calls++;
            return Collections.emptyList();
        }

        @Override
        public void addStudent(int sectionId, int studentId) throws AcademicException {
            calls++;
            if (repositoryFailureOnWrite) {
                throw new RepositoryException("JDBC test detail", new RuntimeException("test"));
            }
            this.sectionId = sectionId;
            this.studentId = studentId;
        }
    }
}
