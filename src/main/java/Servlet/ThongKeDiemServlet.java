package Servlet;

import Models.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/giangvien/thongkediem")
public class ThongKeDiemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan gv = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;

        if (gv == null || !"TEACHER".equalsIgnoreCase(gv.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("trang", "thongkediem");
        req.setAttribute("tieuDeTrang", "Thống kê điểm");
        req.getRequestDispatcher("/WEB-INF/teacher/thongKeDiem.jsp").forward(req, resp);
    }
}
