package Servlet;

import Models.TaiKhoan;
import Services.LopHocPhanService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/giangvien/lopgiangday")
public class LopGiangDayServlet extends HttpServlet {

    private final LopHocPhanService lhpService = new LopHocPhanService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan gv = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;

        if (gv == null || !"TEACHER".equalsIgnoreCase(gv.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("dsLop", lhpService.getAll(null, gv.getMaNguoidung()));
        req.setAttribute("trang", "lopgiangday_gv");
        req.setAttribute("tieuDeTrang", "Lớp đang giảng dạy");
        req.getRequestDispatcher("/WEB-INF/teacher/lopGiangDay.jsp").forward(req, resp);
    }
}
