package Servlet;

import Models.KyThi;
import Models.LopHocPhan;
import Models.PhongHoc;
import Models.TaiKhoan;
import Models.TimetableFilter;
import Models.TimetableWeek;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.CaHocService;
import Services.KyThiService;
import Services.LopHocPhanService;
import Services.PhongHocService;
import Services.TimetableService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/admin/kythi")
public class KyThiServlet extends HttpServlet {
    private static final String LIST_VIEW = "/WEB-INF/admin/kyThiList.jsp";
    private static final String FORM_VIEW = "/WEB-INF/admin/kyThiForm.jsp";
    private static final String GENERIC_ERROR = "Không thể xử lý yêu cầu lúc này.";

    private final KyThiService kyThiService;
    private final LopHocPhanService lhpService;
    private final PhongHocService phongHocService;
    private final CaHocService caHocService;
    private final TimetableService timetableService;

    public KyThiServlet() {
        this(new KyThiService(), new LopHocPhanService(),
                new PhongHocService(), new CaHocService());
    }

    KyThiServlet(KyThiService kyThiService, LopHocPhanService lhpService,
                 PhongHocService phongHocService, CaHocService caHocService) {
        this(kyThiService, lhpService, phongHocService, caHocService,
                new TimetableService());
    }

    KyThiServlet(KyThiService kyThiService, LopHocPhanService lhpService,
                 PhongHocService phongHocService, CaHocService caHocService,
                 TimetableService timetableService) {
        this.kyThiService = kyThiService;
        this.lhpService = lhpService;
        this.phongHocService = phongHocService;
        this.caHocService = caHocService;
        this.timetableService = timetableService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }
        setPageMetadata(req);
        String action = req.getParameter("action");
        try {
            if ("add".equals(action)) {
                prepareAddForm(req);
                forward(req, resp, FORM_VIEW);
            } else if ("edit".equals(action)) {
                if (!prepareEditForm(req, resp)) {
                    return;
                }
                forward(req, resp, FORM_VIEW);
            } else {
                loadTimetable(req);
                forward(req, resp, LIST_VIEW);
            }
        } catch (RuntimeException e) {
            logInternal("Không thể tải màn hình lịch thi.", e);
            sendUnavailable(resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }
        setPageMetadata(req);
        String action = req.getParameter("action");
        KyThi submitted = null;
        try {
            if ("create".equals(action)) {
                submitted = fromRequest(req);
                kyThiService.create(submitted);
                redirectMessage(req, resp, "Tạo lịch thi thành công.");
            } else if ("update".equals(action)) {
                submitted = fromRequest(req);
                submitted.setMaKythi(requireInt(req, "id", "Mã lịch thi không hợp lệ."));
                kyThiService.update(submitted);
                redirectMessage(req, resp, "Cập nhật lịch thi thành công.");
            } else if ("delete".equals(action)) {
                kyThiService.delete(requireInt(req, "id", "Mã lịch thi không hợp lệ."));
                redirectMessage(req, resp, "Xóa lịch thi thành công.");
            } else {
                redirectError(req, resp, "Hành động không hợp lệ.");
            }
        } catch (AcademicException e) {
            if (submitted != null && ("create".equals(action) || "update".equals(action))) {
                safelyRetainAndForward(req, resp, submitted,
                        "update".equals(action), e.getMessage());
            } else {
                redirectError(req, resp, e.getMessage());
            }
        } catch (RepositoryException e) {
            logInternal("Không thể ghi lịch thi.", e);
            redirectError(req, resp, GENERIC_ERROR);
        } catch (RuntimeException e) {
            logInternal("Lỗi ngoài dự kiến khi xử lý lịch thi.", e);
            redirectError(req, resp, GENERIC_ERROR);
        }
    }

    private void prepareAddForm(HttpServletRequest req) {
        req.setAttribute("isEdit", false);
        loadFormOptions(req);
    }

    private boolean prepareEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Integer id = parseInt(req.getParameter("id"));
        if (id == null || id <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Mã lịch thi không hợp lệ.");
            return false;
        }
        KyThi value = kyThiService.findById(id);
        if (value == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy kỳ thi cần sửa.");
            return false;
        }
        req.setAttribute("kyThi", value);
        req.setAttribute("isEdit", true);
        loadFormOptions(req);
        return true;
    }

    private void retainAndForward(HttpServletRequest req, HttpServletResponse resp,
                                  KyThi value, boolean edit, String error)
            throws ServletException, IOException {
        req.setAttribute("kyThi", value);
        req.setAttribute("isEdit", edit);
        req.setAttribute("error", error);
        loadFormOptions(req);
        forward(req, resp, FORM_VIEW);
    }

    private void safelyRetainAndForward(HttpServletRequest req, HttpServletResponse resp,
                                        KyThi value, boolean edit, String error)
            throws ServletException, IOException {
        try {
            retainAndForward(req, resp, value, edit, error);
        } catch (RuntimeException reloadError) {
            logInternal("Không thể tải lại biểu mẫu lịch thi.", reloadError);
            sendUnavailable(resp);
        }
    }

    private void loadFormOptions(HttpServletRequest req) {
        req.setAttribute("dsLHP", lhpService.getAll(null, null));
        req.setAttribute("dsPhong", phongHocService.getAll());
        req.setAttribute("dsCa", caHocService.getAll());
    }

    private void loadTimetable(HttpServletRequest req) {
        List<LopHocPhan> dsLHP = lhpService.getAll(null, null);
        List<PhongHoc> dsPhong = phongHocService.getAll();
        Set<Integer> termIds = dsLHP.stream()
                .map(LopHocPhan::getMaHocky).collect(Collectors.toSet());
        Set<Integer> sectionIds = dsLHP.stream()
                .map(LopHocPhan::getMaLophocphan).collect(Collectors.toSet());
        Set<Integer> teacherIds = dsLHP.stream()
                .map(LopHocPhan::getMaGiangvien).collect(Collectors.toSet());
        Set<Integer> roomIds = dsPhong.stream()
                .map(PhongHoc::getMaPhong).collect(Collectors.toSet());

        String rawWeek = req.getParameter("week");
        TimetableFilter filter = TimetableFilter.admin(
                parseWeek(rawWeek, req),
                allowedId(req.getParameter("maHocKy"), termIds, "Học kỳ", req),
                allowedId(req.getParameter("maLhp"), sectionIds, "Lớp học phần", req),
                allowedId(req.getParameter("maGiangVien"), teacherIds, "Giảng viên", req),
                allowedId(req.getParameter("maPhong"), roomIds, "Phòng", req));
        TimetableWeek timetable = timetableService.loadExams(filter,
                rawWeek == null || rawWeek.trim().isEmpty());
        req.setAttribute("timetable", timetable);
        req.setAttribute("timetableFilter", filter);
        req.setAttribute("timetableMode", "EXAM");
        req.setAttribute("canManage", Boolean.TRUE);
        req.setAttribute("timetableBasePath", "/admin/kythi");
        req.setAttribute("filterQuery", filterQuery(filter));
        req.setAttribute("dsLHP", dsLHP);
        req.setAttribute("dsPhong", dsPhong);
    }

    private LocalDate parseWeek(String raw, HttpServletRequest req) {
        if (raw == null || raw.trim().isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(raw);
        } catch (RuntimeException invalid) {
            appendWarning(req,
                    "Tuần được chọn không hợp lệ; hệ thống đã quay về tuần hiện tại.");
            return LocalDate.now();
        }
    }

    private Integer allowedId(String raw, Set<Integer> allowed, String label,
                              HttpServletRequest req) {
        if (raw == null || raw.trim().isEmpty()) return null;
        Integer value = parseInt(raw);
        if (value != null && value > 0 && allowed.contains(value)) return value;
        appendWarning(req, label + " được chọn không hợp lệ; bộ lọc đã được bỏ qua.");
        return null;
    }

    private void appendWarning(HttpServletRequest req, String warning) {
        Object existing = req.getAttribute("timetableWarning");
        String prefix = existing == null ? "" : existing.toString().trim();
        req.setAttribute("timetableWarning",
                prefix.isEmpty() ? warning : prefix + " " + warning);
    }

    private String filterQuery(TimetableFilter filter) {
        StringBuilder query = new StringBuilder();
        appendFilter(query, "maHocKy", filter.getTermId());
        appendFilter(query, "maLhp", filter.getSectionId());
        appendFilter(query, "maGiangVien", filter.getTeacherId());
        appendFilter(query, "maPhong", filter.getRoomId());
        return query.toString();
    }

    private void appendFilter(StringBuilder query, String name, Integer value) {
        if (value != null) query.append('&').append(name).append('=').append(value);
    }

    private KyThi fromRequest(HttpServletRequest req) {
        KyThi value = new KyThi();
        value.setMaLophocphan(intOrZero(req.getParameter("maLophocphan")));
        value.setMaPhong(intOrZero(req.getParameter("maPhong")));
        value.setMaCa(intOrZero(req.getParameter("maCa")));
        value.setNgayThi(dateOrNull(req.getParameter("ngayThi")));
        value.setLoaiKythi(req.getParameter("loaiKythi"));
        return value;
    }

    private void setPageMetadata(HttpServletRequest req) {
        req.setAttribute("trang", "kythi");
        req.setAttribute("tieuDeTrang", "Quản lý Lịch thi");
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan account = session == null ? null : (TaiKhoan) session.getAttribute("user");
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!"ADMIN".equalsIgnoreCase(account.getTenVaiTro())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng này chỉ dành cho Admin.");
            return false;
        }
        return true;
    }

    private void logInternal(String message, Throwable error) {
        if (getServletContext() != null) {
            getServletContext().log(message, error);
        }
    }

    private void sendUnavailable(HttpServletResponse resp) throws IOException {
        if (!resp.isCommitted()) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, GENERIC_ERROR);
        }
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    private void redirectMessage(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {
        redirect(req, resp, "msg", message);
    }

    private void redirectError(HttpServletRequest req, HttpServletResponse resp, String error)
            throws IOException {
        redirect(req, resp, "error", error);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp,
                          String type, String message) throws IOException {
        String encoded = URLEncoder.encode(message == null ? "" : message,
                StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/kythi?" + type + "=" + encoded);
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int intOrZero(String value) {
        Integer parsed = parseInt(value);
        return parsed == null ? 0 : parsed;
    }

    private static LocalDate dateOrNull(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int requireInt(HttpServletRequest req, String name, String message)
            throws AcademicException {
        Integer value = parseInt(req.getParameter(name));
        if (value == null || value <= 0) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT, message);
        }
        return value;
    }

}
