package Servlet;

import Models.TaiKhoan;
import Services.GiangVienDashboardService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;

@WebServlet("/giangvien/dashboard")
public class GiangVienDashboardServlet extends HttpServlet {

    private static final String LOI_DASHBOARD =
            "Không thể tải dữ liệu dashboard lúc này.";

    private final GiangVienDashboardService service;
    private final Clock clock;

    public GiangVienDashboardServlet() {
        this(new GiangVienDashboardService(), Clock.systemDefaultZone());
    }

    GiangVienDashboardServlet(GiangVienDashboardService service, Clock clock) {
        this.service = service;
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

        TaiKhoan gv = (TaiKhoan) sessionUser;
        if (!"TEACHER".equalsIgnoreCase(gv.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            req.setAttribute("dashboard",
                    service.build(gv.getMaNguoidung(), LocalDate.now(clock)));
            req.getRequestDispatcher("/WEB-INF/teacher/dashboard.jsp")
                    .forward(req, resp);
        } catch (RuntimeException failure) {
            getServletContext().log("Không thể tải dashboard giảng viên.", failure);
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, LOI_DASHBOARD);
        }
    }
}
