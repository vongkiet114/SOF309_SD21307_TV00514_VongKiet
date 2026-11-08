package Servlet;

import Models.LichHoc;
import Models.LopHocPhan;
import Models.PhongHoc;
import Models.TaiKhoan;
import Models.TimetableFilter;
import Models.TimetableWeek;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.CaHocService;
import Services.LichHocService;
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

@WebServlet("/admin/lichhoc")
public class LichHocServlet extends HttpServlet {
    private static final String LIST_VIEW = "/WEB-INF/admin/lichHocList.jsp";
    private static final String FORM_VIEW = "/WEB-INF/admin/lichHocForm.jsp";
    private static final String GENERIC_ERROR = "Không thể xử lý yêu cầu lúc này.";

    private final LichHocService lichHocService;
    private final LopHocPhanService lhpService;
    private final PhongHocService phongHocService;
    private final CaHocService caHocService;
    private final TimetableService timetableService;

    public LichHocServlet() {
        this(new LichHocService(), new LopHocPhanService(),
                new PhongHocService(), new CaHocService());
    }

    LichHocServlet(LichHocService lichHocService, LopHocPhanService lhpService,
                   PhongHocService phongHocService, CaHocService caHocService) {
        this(lichHocService, lhpService, phongHocService, caHocService,
                new TimetableService());
    }

    LichHocServlet(LichHocService lichHocService, LopHocPhanService lhpService,
                   PhongHocService phongHocService, CaHocService caHocService,
                   TimetableService timetableService) {
        this.lichHocService = lichHocService;
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
            logInternal("Không thể tải màn hình lịch học.", e);
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
        LichHoc submitted = null;
        try {
            if ("create".equals(action)) {
                createMultiple(req, resp);
            } else if ("update".equals(action)) {
                submitted = fromRequest(req);
                submitted.setMaLichhoc(requireInt(req, "id", "Mã lịch học không hợp lệ."));
                lichHocService.update(submitted);
                redirectMsg(req, resp, "Cập nhật lịch học thành công.");
            } else if ("delete".equals(action)) {
                lichHocService.delete(requireInt(req, "id", "Mã lịch học không hợp lệ."));
                redirectMsg(req, resp, "Xóa lịch học thành công.");
            } else {
                redirectErr(req, resp, "Hành động không hợp lệ.");
            }
        } catch (AcademicException e) {
            if ("create".equals(action)) {
                safelyRetainAndForward(req, resp, null, false, e.getMessage());
            } else if ("update".equals(action) && submitted != null) {
                safelyRetainAndForward(req, resp, submitted, true, e.getMessage());
            } else {
                redirectErr(req, resp, e.getMessage());
            }
        } catch (RepositoryException e) {
            logInternal("Không thể ghi lịch học.", e);
            redirectErr(req, resp, GENERIC_ERROR);
        } catch (RuntimeException e) {
            logInternal("Lỗi ngoài dự kiến khi xử lý lịch học.", e);
            redirectErr(req, resp, GENERIC_ERROR);
        }
    }

    private void createMultiple(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, AcademicException, RepositoryException {
        LocalDate tuNgay = dateOrNull(req.getParameter("tuNgay"));
        LocalDate denNgay = dateOrNull(req.getParameter("denNgay"));
        String[] dsMaCaThu = req.getParameterValues("maCa_Thu");

        if (tuNgay == null || denNgay == null) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT, "Vui lòng chọn Từ ngày và Đến ngày.");
        }
        if (tuNgay.isAfter(denNgay)) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT, "Từ ngày không được lớn hơn Đến ngày.");
        }
        if (dsMaCaThu == null || dsMaCaThu.length == 0) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT, "Vui lòng chọn ít nhất một Ca/Thứ trong bảng ma trận đặt lịch.");
        }

        int maLophocphan = intOrZero(req.getParameter("maLophocphan"));
        if (maLophocphan == 0) throw new AcademicException(AcademicException.Code.INVALID_INPUT, "Vui lòng chọn lớp học phần.");
        int maPhong = intOrZero(req.getParameter("maPhong"));
        if (maPhong == 0) throw new AcademicException(AcademicException.Code.INVALID_INPUT, "Vui lòng chọn phòng học.");
        String loaiLich = req.getParameter("loaiLich");
        String trangThai = req.getParameter("trangThai");

        int count = 0;
        for (LocalDate date = tuNgay; !date.isAfter(denNgay); date = date.plusDays(1)) {
            int dayOfWeekValue = date.getDayOfWeek().getValue();
            int thu = dayOfWeekValue == 7 ? 8 : dayOfWeekValue + 1;

            for (String caThu : dsMaCaThu) {
                String[] parts = caThu.split("_");
                if (parts.length == 2) {
                    int ca = intOrZero(parts[0]);
                    int t = intOrZero(parts[1]);
                    if (t == thu) {
                        LichHoc lh = new LichHoc();
                        lh.setMaLophocphan(maLophocphan);
                        lh.setMaPhong(maPhong);
                        lh.setMaCa(ca);
                        lh.setNgayHoc(date);
                        lh.setLoaiLich(loaiLich);
                        lh.setTrangThai(trangThai);
                        lichHocService.create(lh);
                        count++;
                    }
                }
            }
        }
        
        if (count == 0) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT, "Không có ngày nào trong khoảng thời gian khớp với Thứ đã chọn trong ma trận.");
        }
        redirectMsg(req, resp, "Đã xếp lịch thành công " + count + " buổi học.");
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
                    "Mã lịch học không hợp lệ.");
            return false;
        }
        LichHoc value = lichHocService.findById(id);
        if (value == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy buổi học cần sửa.");
            return false;
        }
        req.setAttribute("lichHoc", value);
        req.setAttribute("isEdit", true);
        loadFormOptions(req);
        return true;
    }

    private void retainAndForward(HttpServletRequest req, HttpServletResponse resp,
                                  LichHoc value, boolean edit, String error)
            throws ServletException, IOException {
        req.setAttribute("lichHoc", value);
        req.setAttribute("isEdit", edit);
        req.setAttribute("error", error);
        loadFormOptions(req);
        forward(req, resp, FORM_VIEW);
    }

    private void safelyRetainAndForward(HttpServletRequest req, HttpServletResponse resp,
                                        LichHoc value, boolean edit, String error)
            throws ServletException, IOException {
        try {
            retainAndForward(req, resp, value, edit, error);
        } catch (RuntimeException reloadError) {
            logInternal("Không thể tải lại biểu mẫu lịch học.", reloadError);
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
        TimetableWeek timetable = timetableService.loadClasses(filter,
                rawWeek == null || rawWeek.trim().isEmpty());
        req.setAttribute("timetable", timetable);
        req.setAttribute("timetableFilter", filter);
        req.setAttribute("timetableMode", "CLASS");
        req.setAttribute("canManage", Boolean.TRUE);
        req.setAttribute("timetableBasePath", "/admin/lichhoc");
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

    private LichHoc fromRequest(HttpServletRequest req) {
        LichHoc value = new LichHoc();
        value.setMaLophocphan(intOrZero(req.getParameter("maLophocphan")));
        value.setMaPhong(intOrZero(req.getParameter("maPhong")));
        value.setMaCa(intOrZero(req.getParameter("maCa")));
        value.setNgayHoc(dateOrNull(req.getParameter("ngayHoc")));
        value.setLoaiLich(req.getParameter("loaiLich"));
        value.setTrangThai(req.getParameter("trangThai"));
        return value;
    }

    private void setPageMetadata(HttpServletRequest req) {
        req.setAttribute("trang", "lichhoc");
        req.setAttribute("tieuDeTrang", "Quản lý Lịch học");
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

    private void redirectMsg(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/lichhoc?msg=" + encode(message));
    }

    private void redirectErr(HttpServletRequest req, HttpServletResponse resp, String error)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/lichhoc?error=" + encode(error));
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value,
                    StandardCharsets.UTF_8.toString());
        } catch (Exception ignored) {
            return "";
        }
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
