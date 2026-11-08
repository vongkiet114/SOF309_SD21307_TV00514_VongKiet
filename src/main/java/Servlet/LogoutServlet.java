package Servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// servlet dang xuat: huy bo phien dang nhap hien tai roi quay ve trang login.
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession phien = req.getSession(false);
        if (phien != null) {
            // huy toan bo du lieu trong phien (xoa thong tin "user" da luu khi dang nhap)
            phien.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
