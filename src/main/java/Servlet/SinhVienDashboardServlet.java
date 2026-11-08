package Servlet;

import Models.TaiKhoan;
import Services.SinhVienDashboardService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;

@WebServlet("/sinhvien/dashboard")
public class SinhVienDashboardServlet extends HttpServlet {

    private static final String LOI_DASHBOARD =
            "Không thể tải dữ liệu dashboard lúc này.";

    private final SinhVienDashboardService service;
    private final Clock clock;

    public SinhVienDashboardServlet() {
        this(new SinhVienDashboardService(), Clock.systemDefaultZone());
    }

    SinhVienDashboardServlet(SinhVienDashboardService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan sv = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;

        if (sv == null || !"STUDENT".equalsIgnoreCase(sv.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            req.setAttribute("dashboard",
                    service.build(sv.getMaNguoidung(), LocalDate.now(clock)));
            req.getRequestDispatcher("/WEB-INF/student/dashboard.jsp").forward(req, resp);
        } catch (RuntimeException failure) {
            getServletContext().log("Không thể tải dashboard sinh viên.", failure);
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, LOI_DASHBOARD);
        }
    }
}
