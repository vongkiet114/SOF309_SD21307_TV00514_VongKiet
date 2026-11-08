package Servlet;

import Models.DiemDanh;
import Models.LichHoc;
import Models.LopHocPhan;
import Models.TaiKhoan;
import Services.AcademicException;
import Services.AttendanceSessionPolicy;
import Services.DiemDanhService;
import Services.LichHocService;
import Services.LopHocPhanService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/diemdanhbu")
public class AdminDiemDanhServlet extends HttpServlet {
    private final DiemDanhService diemDanhService;
    private final LopHocPhanService lhpService;
    private final LichHocService lichHocService;
    private final Clock clock;

    public AdminDiemDanhServlet() {
        this(new DiemDanhService(), new LopHocPhanService(),
                new LichHocService(), Clock.systemDefaultZone());
    }

    AdminDiemDanhServlet(DiemDanhService diemDanhService,
                    LopHocPhanService lhpService,
                    LichHocService lichHocService,
                    Clock clock) {
        this.diemDanhService = diemDanhService;
        this.lhpService = lhpService;
        this.lichHocService = lichHocService;
        this.clock = clock;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TaiKhoan admin = getAdmin(req, resp);
        if (admin == null) return;
        Integer sectionId = parsePositive(req.getParameter("maLHP"));
        Integer scheduleId = parsePositive(req.getParameter("maLichhoc"));
        try {
            showPage(req, resp, sectionId, scheduleId);
        } catch (AcademicException e) {
            handleAcademicError(resp, e);
        } catch (RuntimeException e) {
            handleRuntimeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TaiKhoan admin = getAdmin(req, resp);
        if (admin == null) return;

        Integer sectionId = parsePositive(req.getParameter("maLHP"));
        Integer scheduleId = parsePositive(req.getParameter("maLichhoc"));
        Map<String, String> submitted = submittedAttendance(req);
        try {
            if (sectionId == null || scheduleId == null) {
                throw invalid("Dữ liệu điểm danh không hợp lệ.");
            }
            List<DiemDanh> rows = parseRows(req, scheduleId);
            LocalDate today = LocalDate.now(clock);
            List<LichHoc> selectable = AttendanceSessionPolicy.filterSelectable(
                    lichHocService.findByLopHocPhan(sectionId), sectionId, today);
            if (!containsSchedule(selectable, scheduleId)) {
                throw invalid("Buổi học không thuộc lớp, đã qua hoặc không ở trạng thái bình thường.");
            }
            diemDanhService.saveDiemDanhAdmin(sectionId, scheduleId, rows, today);
            redirect(req, resp, sectionId, scheduleId, "msg", "Lưu điểm danh thành công.");
        } catch (AcademicException e) {
            if (e.getCode() == AcademicException.Code.FORBIDDEN) {
                handleAcademicError(resp, e);
                return;
            }
            req.setAttribute("error", e.getMessage());
            req.setAttribute("submittedAttendance", submitted);
            try {
                showPage(req, resp, sectionId, scheduleId);
            } catch (AcademicException reloadError) {
                handleAcademicError(resp, reloadError);
            } catch (RuntimeException reloadError) {
                handleRuntimeError(resp, reloadError);
            }
        } catch (RuntimeException e) {
            handleRuntimeError(resp, e);
        }
    }

    private void showPage(HttpServletRequest req,
                          HttpServletResponse resp,
                          Integer sectionId,
                          Integer scheduleId)
            throws ServletException, IOException, AcademicException {
        req.setAttribute("trang", "diemdanhbu");
        req.setAttribute("tieuDeTrang", "Điểm danh bù (Admin)");

        List<LopHocPhan> sections = lhpService.getAll(null, null); // Get ALL sections
        if (sections == null) sections = Collections.emptyList();
        req.setAttribute("dsLHP", sections);
        if (sectionId == null) {
            req.getRequestDispatcher("/WEB-INF/admin/diemDanhBu.jsp").forward(req, resp);
            return;
        }
        if (!containsSection(sections, sectionId)) {
            throw new AcademicException(AcademicException.Code.FORBIDDEN,
                    "Bạn không có quyền truy cập lớp học phần này.");
        }

        req.setAttribute("maLHP", sectionId);
        List<LichHoc> selectableSchedules = AttendanceSessionPolicy.filterSelectable(
                lichHocService.findByLopHocPhan(sectionId),
                sectionId, LocalDate.now(clock));
        req.setAttribute("dsBuoi", selectableSchedules);
        if (scheduleId != null) {
            if (!containsSchedule(selectableSchedules, scheduleId)) {
                throw invalid("Buổi học không thuộc lớp, đã qua hoặc không ở trạng thái bình thường.");
            }
            req.setAttribute("maLichhoc", scheduleId);
            req.setAttribute("bangDiemDanh", diemDanhService.getBangDiemDanhAdmin(sectionId, scheduleId));
        }
        req.getRequestDispatcher("/WEB-INF/admin/diemDanhBu.jsp").forward(req, resp);
    }

    private List<DiemDanh> parseRows(HttpServletRequest req, int scheduleId)
            throws AcademicException {
        String[] memberships = req.getParameterValues("maDanhSach");
        String[] states = req.getParameterValues("trangThai");
        if (memberships == null || states == null || memberships.length == 0
                || memberships.length != states.length) {
            throw invalid("Dữ liệu điểm danh không hợp lệ.");
        }
        List<DiemDanh> result = new ArrayList<>();
        for (int index = 0; index < memberships.length; index++) {
            Integer membership = parsePositive(memberships[index]);
            if (membership == null) {
                throw invalid("Dữ liệu điểm danh không hợp lệ.");
            }
            DiemDanh row = new DiemDanh();
            row.setMaDanhsach(membership);
            row.setMaLichhoc(scheduleId);
            row.setTrangThai(states[index]);
            result.add(row);
        }
        return result;
    }

    private Map<String, String> submittedAttendance(HttpServletRequest req) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] memberships = req.getParameterValues("maDanhSach");
        String[] states = req.getParameterValues("trangThai");
        if (memberships == null || states == null) return result;
        int length = Math.min(memberships.length, states.length);
        for (int index = 0; index < length; index++) {
            if (memberships[index] != null) {
                result.put(memberships[index].trim(), states[index]);
            }
        }
        return result;
    }

    private boolean containsSection(List<LopHocPhan> sections, int sectionId) {
        for (LopHocPhan section : sections) {
            if (section != null && section.getMaLophocphan() == sectionId) return true;
        }
        return false;
    }

    private boolean containsSchedule(List<LichHoc> schedules, int scheduleId) {
        for (LichHoc schedule : schedules) {
            if (schedule.getMaLichhoc() == scheduleId) return true;
        }
        return false;
    }

    private TaiKhoan getAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        Object user = session == null ? null : session.getAttribute("user");
        if (!(user instanceof TaiKhoan)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        TaiKhoan account = (TaiKhoan) user;
        if (!"ADMIN".equalsIgnoreCase(account.getTenVaiTro())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng này chỉ dành cho quản trị viên.");
            return null;
        }
        return account;
    }

    private void handleAcademicError(HttpServletResponse resp, AcademicException e)
            throws IOException {
        if (e.getCode() == AcademicException.Code.FORBIDDEN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập lớp học phần này.");
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleRuntimeError(HttpServletResponse resp, RuntimeException e)
            throws IOException {
        getServletContext().log("Lỗi dữ liệu điểm danh bù", e);
        resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Không thể xử lý yêu cầu lúc này.");
    }

    private void redirect(HttpServletRequest req,
                          HttpServletResponse resp,
                          int sectionId,
                          int scheduleId,
                          String type,
                          String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/diemdanhbu?maLHP="
                + sectionId + "&maLichhoc=" + scheduleId + "&" + type + "=" + encoded);
    }

    private static Integer parsePositive(String value) {
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
}
