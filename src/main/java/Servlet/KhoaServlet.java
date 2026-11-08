package Servlet;

import Models.Khoa;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CatalogException;
import Services.KhoaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/khoa")
public class KhoaServlet extends HttpServlet {

    private final KhoaService khoaService = new KhoaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("trang", "khoa");
        req.setAttribute("tieuDeTrang", "Quản lý Khoa");
        try {
            String action = req.getParameter("action");
            if ("them".equals(action)) {
                forward(req, resp, "/WEB-INF/admin/khoaForm.jsp");
            } else if ("sua".equals(action)) {
                showEditForm(req, resp);
            } else {
                showList(req, resp);
            }
        } catch (RepositoryException e) {
            handleRepositoryError(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("trang", "khoa");
        req.setAttribute("tieuDeTrang", "Quản lý Khoa");
        try {
            String action = req.getParameter("action");
            if ("luu".equals(action)) {
                save(req, resp);
            } else if ("xoa".equals(action)) {
                delete(req, resp);
            } else {
                redirect(req, resp, "error", "Hành động không hợp lệ.");
            }
        } catch (RepositoryException e) {
            handleRepositoryError(req, resp, e);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("dsKhoa", khoaService.getAll());
        forward(req, resp, "/WEB-INF/admin/khoaList.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        Khoa khoa = id > 0 ? khoaService.getById(id) : null;
        if (khoa == null) {
            redirect(req, resp, "error", "Không tìm thấy khoa cần sửa.");
            return;
        }
        req.setAttribute("khoa", khoa);
        forward(req, resp, "/WEB-INF/admin/khoaForm.jsp");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Khoa khoa = new Khoa();
        khoa.setMaKhoa(CatalogIdParser.parseSaveId(req.getParameter("id")));
        khoa.setTenKhoa(req.getParameter("tenKhoa"));
        try {
            if (khoa.getMaKhoa() < 0) {
                throw new CatalogException("Mã khoa không hợp lệ.");
            }
            khoaService.save(khoa);
            redirect(req, resp, "msg", "Lưu khoa thành công.");
        } catch (CatalogException e) {
            req.setAttribute("khoa", khoa);
            req.setAttribute("error", e.getMessage());
            forward(req, resp, "/WEB-INF/admin/khoaForm.jsp");
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            khoaService.delete(parseId(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa khoa thành công.");
        } catch (CatalogException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi danh mục khoa", e);
        redirect(req, resp, "error", "Không thể xử lý dữ liệu khoa.");
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

    private void forward(HttpServletRequest req, HttpServletResponse resp, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp,
                          String type, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/khoa?" + type + "=" + encoded);
    }

    private int parseId(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
