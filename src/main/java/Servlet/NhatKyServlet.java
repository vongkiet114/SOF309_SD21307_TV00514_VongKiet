package Servlet;

import Models.TaiKhoan;
import Services.NhatKyService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/admin/nhatky")
public class NhatKyServlet extends HttpServlet {

    private final NhatKyService nhatKyService = new NhatKyService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("dsNhatKy", nhatKyService.getAll());
        req.setAttribute("trang", "nhatky");
        req.setAttribute("tieuDeTrang", "Nhật ký hoạt động");
        req.getRequestDispatcher("/WEB-INF/admin/nhatKyList.jsp").forward(req, resp);
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan tk = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;
        if (tk == null || !"ADMIN".equalsIgnoreCase(tk.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
