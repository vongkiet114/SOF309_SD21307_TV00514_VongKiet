package Servlet;

import Models.MonHocTienQuyet;
import Models.TaiKhoan;
import Services.MonHocService;
import Services.MonTienQuyetService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/montienquyet")
public class MonTienQuyetServlet extends HttpServlet {

    private final MonTienQuyetService mtqService = new MonTienQuyetService();
    private final MonHocService monHocService = new MonHocService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("dsTienQuyet", mtqService.getAll());
        req.setAttribute("dsMonHoc", monHocService.getAll());
        req.setAttribute("trang", "montienquyet");
        req.setAttribute("tieuDeTrang", "Quản lý Môn tiên quyết");
        forward(req, resp, "/WEB-INF/admin/tienQuyet.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        String action = req.getParameter("action");
        try {
            Integer maMonHoc = parseInt(req.getParameter("maMonhoc"));
            Integer maMonHocTienQuyet = parseInt(req.getParameter("maMonhocTienquyet"));

            if (maMonHoc == null || maMonHocTienQuyet == null) {
                throw new Exception("Vui lòng chọn đầy đủ môn học.");
            }

            if ("delete".equals(action)) {
                mtqService.delete(maMonHoc, maMonHocTienQuyet);
                redirect(req, resp, "msg", "Xóa ràng buộc thành công.");
            } else { // "create"
                MonHocTienQuyet mtq = new MonHocTienQuyet(maMonHoc, maMonHocTienQuyet);
                mtqService.create(mtq);
                redirect(req, resp, "msg", "Thêm ràng buộc thành công.");
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

    private void forward(HttpServletRequest r, HttpServletResponse p, String path) throws ServletException, IOException {
        r.getRequestDispatcher(path).forward(r, p);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String type, String message) throws IOException {
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/montienquyet?" + type + "=" + encodedMsg);
    }

    private static Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }
}
