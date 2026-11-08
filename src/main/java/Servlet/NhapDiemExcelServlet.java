package Servlet;

import Models.GradeImportPreview;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.GradeExcelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@WebServlet("/giangvien/nhapdiem/excel")
@MultipartConfig(fileSizeThreshold = 5 * 1024 * 1024,
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 5L * 1024 * 1024 + 256 * 1024)
public class NhapDiemExcelServlet extends HttpServlet {
    static final String PREVIEW_KEY = "gradeImportPreview";
    private static final String PREVIEW_JSP =
            "/WEB-INF/teacher/nhapDiemExcelPreview.jsp";
    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final GradeExcelService excelService;

    public NhapDiemExcelServlet() {
        this(new GradeExcelService());
    }

    NhapDiemExcelServlet(GradeExcelService excelService) {
        this.excelService = excelService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TeacherSession teacher = requireTeacher(req, resp);
        if (teacher == null) return;
        if (!"template".equals(req.getParameter("action"))) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Yêu cầu Excel không hợp lệ.");
            return;
        }
        Integer sectionId = positive(req.getParameter("maLHP"));
        if (sectionId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Lớp học phần không hợp lệ.");
            return;
        }

        try {
            String fileName = "bang-diem-lhp-" + sectionId + ".xlsx";
            resp.setContentType(XLSX_MIME);
            resp.setHeader("X-Content-Type-Options", "nosniff");
            resp.setHeader("Content-Disposition", "attachment; filename=\""
                    + fileName + "\"; filename*=UTF-8''" + fileName);
            excelService.writeTemplate(teacher.account.getMaNguoidung(), sectionId,
                    resp.getOutputStream());
        } catch (AcademicException e) {
            handleAcademicError(resp, e);
        } catch (RepositoryException e) {
            handleInternalError(resp, "Lỗi dữ liệu khi xuất mẫu điểm Excel", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TeacherSession teacher = requireTeacher(req, resp);
        if (teacher == null) return;
        String action = req.getParameter("action");
        if ("preview".equals(action)) {
            preview(req, resp, teacher);
        } else if ("confirm".equals(action)) {
            confirm(req, resp, teacher);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Yêu cầu Excel không hợp lệ.");
        }
    }

    private void preview(HttpServletRequest req,
                         HttpServletResponse resp,
                         TeacherSession teacher)
            throws ServletException, IOException {
        try {
            Part upload = req.getPart("file");
            if (upload == null) {
                throw invalid("Vui lòng chọn file Excel .xlsx.");
            }
            GradeImportPreview result;
            try (InputStream input = upload.getInputStream()) {
                result = excelService.preview(teacher.account.getMaNguoidung(),
                        upload.getSubmittedFileName(), upload.getSize(), input,
                        Instant.now());
            }
            req.setAttribute("preview", result);
            replacePreview(teacher.session, result);
            forwardPreview(req, resp);
        } catch (AcademicException e) {
            replacePreview(teacher.session, null);
            if (e.getCode() == AcademicException.Code.FORBIDDEN) {
                handleAcademicError(resp, e);
                return;
            }
            req.setAttribute("error", e.getMessage());
            forwardPreview(req, resp);
        } catch (IllegalStateException e) {
            replacePreview(teacher.session, null);
            req.setAttribute("error", "File Excel phải nhỏ hơn hoặc bằng 5 MB.");
            forwardPreview(req, resp);
        } catch (RepositoryException e) {
            replacePreview(teacher.session, null);
            handleInternalError(resp, "Lỗi dữ liệu khi xem trước điểm Excel", e);
        }
    }

    private void confirm(HttpServletRequest req,
                         HttpServletResponse resp,
                         TeacherSession teacher) throws IOException {
        synchronized (teacher.session) {
            Object stored = teacher.session.getAttribute(PREVIEW_KEY);
            if (!(stored instanceof GradeImportPreview)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Phiên xem trước không hợp lệ hoặc đã được sử dụng.");
                return;
            }
            GradeImportPreview preview = (GradeImportPreview) stored;
            String requestToken = req.getParameter("token");
            if (!sameToken(preview.getToken(), requestToken)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Phiên xem trước không hợp lệ hoặc đã được sử dụng.");
                return;
            }
            try {
                excelService.confirm(teacher.account.getMaNguoidung(), preview,
                        Instant.now());
                teacher.session.removeAttribute(PREVIEW_KEY);
                String message = URLEncoder.encode("Nhập điểm từ Excel thành công.",
                        StandardCharsets.UTF_8.toString());
                resp.sendRedirect(req.getContextPath()
                        + "/giangvien/nhapdiem?maLHP=" + preview.getSectionId()
                        + "&msg=" + message);
            } catch (AcademicException e) {
                handleAcademicError(resp, e);
            } catch (RepositoryException e) {
                handleInternalError(resp, "Lỗi dữ liệu khi xác nhận điểm Excel", e);
            }
        }
    }

    private void replacePreview(HttpSession session, GradeImportPreview preview) {
        synchronized (session) {
            session.removeAttribute(PREVIEW_KEY);
            if (preview != null && preview.getErrors().isEmpty()) {
                session.setAttribute(PREVIEW_KEY, preview);
            }
        }
    }

    private void forwardPreview(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("trang", "nhapdiem");
        req.setAttribute("tieuDeTrang", "Xem trước điểm Excel");
        req.getRequestDispatcher(PREVIEW_JSP).forward(req, resp);
    }

    private TeacherSession requireTeacher(HttpServletRequest req,
                                          HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        Object user = session == null ? null : session.getAttribute("user");
        if (!(user instanceof TaiKhoan)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        TaiKhoan account = (TaiKhoan) user;
        if (!"TEACHER".equalsIgnoreCase(account.getTenVaiTro())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng này chỉ dành cho giảng viên.");
            return null;
        }
        return new TeacherSession(account, session);
    }

    private void handleAcademicError(HttpServletResponse resp, AcademicException e)
            throws IOException {
        int status = e.getCode() == AcademicException.Code.FORBIDDEN
                ? HttpServletResponse.SC_FORBIDDEN
                : HttpServletResponse.SC_BAD_REQUEST;
        resp.sendError(status, e.getMessage());
    }

    private void handleInternalError(HttpServletResponse resp,
                                     String logMessage,
                                     RuntimeException error) throws IOException {
        ServletContext context = getServletContext();
        if (context != null) context.log(logMessage, error);
        if (!resp.isCommitted()) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Không thể xử lý yêu cầu lúc này.");
        }
    }

    private static boolean sameToken(String expected, String provided) {
        boolean present = expected != null && provided != null;
        byte[] expectedBytes = expected == null ? new byte[0]
                : expected.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = provided == null ? new byte[0]
                : provided.getBytes(StandardCharsets.UTF_8);
        boolean equal = MessageDigest.isEqual(expectedBytes, providedBytes);
        return present && equal;
    }

    private static Integer positive(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static AcademicException invalid(String message) {
        return new AcademicException(AcademicException.Code.INVALID_INPUT, message);
    }

    private static final class TeacherSession {
        private final TaiKhoan account;
        private final HttpSession session;

        private TeacherSession(TaiKhoan account, HttpSession session) {
            this.account = account;
            this.session = session;
        }
    }
}
