package Servlet;

import Models.TaiKhoan;
import Models.TimetableFilter;
import Models.TimetableWeek;
import Services.TimetableService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet(urlPatterns = {"/sinhvien/lichhoc", "/giangvien/lichday"})
public class XemLichHocServlet extends HttpServlet {
    private static final String GENERIC_ERROR = "Không thể xử lý yêu cầu lúc này.";

    private final TimetableService timetableService;

    public XemLichHocServlet() {
        this(new TimetableService());
    }

    XemLichHocServlet(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan account = session == null ? null
                : (TaiKhoan) session.getAttribute("user");
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = account.getTenVaiTro();
        if (role == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Không xác định được vai trò.");
            return;
        }

        String viewPath;
        String basePath;
        if ("STUDENT".equalsIgnoreCase(role)) {
            req.setAttribute("trang", "lichhoc_sv");
            req.setAttribute("tieuDeTrang", "Lịch học cá nhân");
            viewPath = "/WEB-INF/student/lichHocCaNhan.jsp";
            basePath = "/sinhvien/lichhoc";
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            req.setAttribute("trang", "lichday_gv");
            req.setAttribute("tieuDeTrang", "Lịch dạy cá nhân");
            viewPath = "/WEB-INF/teacher/lichDayCaNhan.jsp";
            basePath = "/giangvien/lichday";
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            String rawWeek = req.getParameter("week");
            TimetableFilter filter = TimetableFilter.user(parseWeek(rawWeek, req),
                    account.getMaNguoidung(), role);
            TimetableWeek timetable = timetableService.loadClasses(filter,
                    rawWeek == null || rawWeek.trim().isEmpty());
            req.setAttribute("timetable", timetable);
            req.setAttribute("timetableFilter", filter);
            req.setAttribute("timetableMode", "CLASS");
            req.setAttribute("canManage", Boolean.FALSE);
            req.setAttribute("timetableBasePath", basePath);
            req.setAttribute("filterQuery", "");
            req.getRequestDispatcher(viewPath).forward(req, resp);
        } catch (RuntimeException error) {
            log("Không thể tải thời khóa biểu cá nhân.", error);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        GENERIC_ERROR);
            }
        }
    }

    private LocalDate parseWeek(String raw, HttpServletRequest req) {
        if (raw == null || raw.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(raw);
        } catch (RuntimeException invalid) {
            appendWarning(req,
                    "Tuần được chọn không hợp lệ; hệ thống đã quay về tuần hiện tại.");
            return LocalDate.now();
        }
    }

    private void appendWarning(HttpServletRequest req, String warning) {
        Object existing = req.getAttribute("timetableWarning");
        String prefix = existing == null ? "" : existing.toString().trim();
        req.setAttribute("timetableWarning",
                prefix.isEmpty() ? warning : prefix + " " + warning);
    }
}
