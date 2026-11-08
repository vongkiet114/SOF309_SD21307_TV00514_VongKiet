package Servlet;

import Models.ThongBao;
import Models.TaiKhoan;
import Services.ThongBaoService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@WebServlet("/admin/thongbao")
public class ThongBaoServlet extends HttpServlet {
    private final ThongBaoService thongBaoService = new ThongBaoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        setPageAttributes(req);
        String action = req.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            showList(req, resp);
        } else if ("them".equals(action)) {
            showForm(req, resp);
        } else {
            redirect(req, resp, "error", "Hành động không hợp lệ.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        setPageAttributes(req);
        String action = req.getParameter("action");
        if ("luu".equals(action)) {
            save(req, resp);
        } else if ("xoa".equals(action)) {
            delete(req, resp);
        } else {
            redirect(req, resp, "error", "Hành động không hợp lệ.");
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("dsThongBao", thongBaoService.getAll());
        } catch (RuntimeException e) {
            getServletContext().log("Không thể tải danh sách thông báo", e);
            req.setAttribute("dsThongBao", new ArrayList<ThongBao>());
            req.setAttribute("error", "Không thể tải danh sách thông báo.");
        }
        forward(req, resp, "/WEB-INF/admin/thongBaoList.jsp");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        forward(req, resp, "/WEB-INF/admin/thongBaoForm.jsp");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ThongBao thongBao = new ThongBao();
        thongBao.setTieuDe(req.getParameter("tieuDe"));
        thongBao.setNoiDung(req.getParameter("noiDung"));
        thongBao.setPhamVi(req.getParameter("phamVi"));

        try {
            thongBaoService.create(thongBao);
            redirect(req, resp, "msg", "Gửi thông báo thành công.");
        } catch (IllegalArgumentException e) {
            req.setAttribute("thongBao", thongBao);
            req.setAttribute("error", e.getMessage());
            showForm(req, resp);
        } catch (RuntimeException e) {
            getServletContext().log("Không thể gửi thông báo", e);
            req.setAttribute("thongBao", thongBao);
            req.setAttribute("error", "Không thể gửi thông báo. Vui lòng thử lại.");
            showForm(req, resp);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = parseId(req.getParameter("id"));
        if (id <= 0) {
            redirect(req, resp, "error", "Thông báo cần xóa không hợp lệ.");
            return;
        }

        try {
            thongBaoService.delete(id);
            redirect(req, resp, "msg", "Xóa thông báo thành công.");
        } catch (RuntimeException e) {
            getServletContext().log("Không thể xóa thông báo", e);
            redirect(req, resp, "error", "Không thể xóa thông báo.");
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan taiKhoan = session == null ? null : (TaiKhoan) session.getAttribute("user");
        if (taiKhoan == null || !"ADMIN".equalsIgnoreCase(taiKhoan.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    private void setPageAttributes(HttpServletRequest req) {
        req.setAttribute("trang", "thongbao");
        req.setAttribute("tieuDeTrang", "Quản lý thông báo");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp,
                          String type, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
        resp.sendRedirect(req.getContextPath() + "/admin/thongbao?" + type + "=" + encoded);
    }

    private int parseId(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
