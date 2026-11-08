package Servlet;

import Controllers.DangNhapController;
import Models.TaiKhoan;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginGetServlet extends HttpServlet {

    private final DangNhapController dangNhapController = new DangNhapController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/loginget.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String matKhau = req.getParameter("matKhau");

        TaiKhoan taiKhoan = dangNhapController.dangNhap(email, matKhau);

        if (taiKhoan == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=1");
            return;
        }

        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = req.getSession(true);
        newSession.setAttribute("user", taiKhoan);

        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
