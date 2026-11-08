package Servlet;

import Models.TaiKhoan;
import Services.DashboardService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final String LOI_DASHBOARD =
            "Không thể tải dữ liệu dashboard lúc này.";

    private final DashboardService dashboardService;
    private final Clock clock;

    public DashboardServlet() {
        this(new DashboardService(), Clock.systemDefaultZone());
    }

    DashboardServlet(DashboardService dashboardService, Clock clock) {
        this.dashboardService = dashboardService;
        this.clock = clock;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Object sessionUser =
                session != null ? session.getAttribute("user") : null;

        if (!(sessionUser instanceof TaiKhoan)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        TaiKhoan tk = (TaiKhoan) sessionUser;
        String vaiTro = tk.getTenVaiTro();

        if (vaiTro == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        switch (vaiTro.toUpperCase()) {
            case "ADMIN":
                try {
                    req.setAttribute("dashboard",
                            dashboardService.build(LocalDate.now(clock)));
                    req.getRequestDispatcher("/WEB-INF/admin/dashboard.jsp")
                            .forward(req, resp);
                } catch (RuntimeException failure) {
                    getServletContext().log(
                            "Không thể tải dashboard quản trị.", failure);
                    resp.sendError(
                            HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                            LOI_DASHBOARD);
                }
                break;
            case "TEACHER":
                resp.sendRedirect(req.getContextPath() + "/giangvien/dashboard");
                break;
            case "STUDENT":
                resp.sendRedirect(req.getContextPath() + "/sinhvien/dashboard");
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/login");
                break;
        }
    }
}
