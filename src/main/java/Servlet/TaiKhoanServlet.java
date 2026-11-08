package Servlet;

import Models.TaiKhoan;
import Services.TaiKhoanService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/taikhoan")
public class TaiKhoanServlet extends HttpServlet {

    private final TaiKhoanService tkService = new TaiKhoanService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("dsTaiKhoan", tkService.dsTaiKhoan());
        req.setAttribute("trang", "taikhoan");
        req.setAttribute("tieuDeTrang", "Quản lý Tài khoản");
        req.getRequestDispatcher("/WEB-INF/admin/taiKhoanList.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        String action = req.getParameter("action");
        try {
            switch (action) {
                case "lock":
                    int lockId = requireInt(req, "id");
                    tkService.khoa(lockId);
                    redirect(req, resp, "msg", "Đã khóa tài khoản #" + lockId);
                    break;
                case "unlock":
                    int unlockId = requireInt(req, "id");
                    tkService.moKhoa(unlockId);
                    redirect(req, resp, "msg", "Đã mở khóa tài khoản #" + unlockId);
                    break;
                default:
                    redirect(req, resp, "error", "Hành động không hợp lệ.");
                    break;
            }
        } catch (Exception e) {
            redirect(req, resp, "error", e.getMessage());
        }
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

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String type, String message) throws IOException {
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/taikhoan?" + type + "=" + encodedMsg);
    }

    private static int requireInt(HttpServletRequest req, String name) throws Exception {
        String param = req.getParameter(name);
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            throw new Exception("Thiếu hoặc sai định dạng tham số: " + name);
        }
    }
}
