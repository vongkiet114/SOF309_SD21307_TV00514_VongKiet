package Servlet;

import Models.TaiKhoan;
import Services.ThongKeGiangDayService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/giangvien/thongke")
public class ThongKeGiangDayServlet extends HttpServlet {

    private final ThongKeGiangDayService service;

    public ThongKeGiangDayServlet() {
        this(new ThongKeGiangDayService());
    }

    ThongKeGiangDayServlet(ThongKeGiangDayService service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan gv = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;

        if (gv == null || !"TEACHER".equalsIgnoreCase(gv.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            req.setAttribute("thongKe", service.thongKe(gv.getMaNguoidung()));
        } catch (RuntimeException e) {
            getServletContext().log("Không thể tải thống kê giảng dạy", e);
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Không thể tải thống kê giảng dạy lúc này.");
            return;
        }
        req.setAttribute("trang", "thongke_gv");
        req.setAttribute("tieuDeTrang", "Thống kê giảng dạy");
        req.getRequestDispatcher("/WEB-INF/teacher/thongKeGiangDay.jsp").forward(req, resp);
    }
}
