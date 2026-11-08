package Servlet;

import Models.ChuyenNganh;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CatalogException;
import Services.ChuyenNganhService;
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

@WebServlet("/admin/chuyennganh")
public class ChuyenNganhServlet extends HttpServlet {
    private final ChuyenNganhService cnService = new ChuyenNganhService();
    private final KhoaService khoaService = new KhoaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        setPageAttributes(req);
        try {
            String action = req.getParameter("action");
            if ("them".equals(action)) {
                showForm(req, resp, null);
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

        setPageAttributes(req);
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
        String keyword = req.getParameter("keyword");
        Integer maKhoa = parseNullableId(req.getParameter("maKhoa"));
        req.setAttribute("dsChuyenNganh", cnService.search(keyword, maKhoa));
        req.setAttribute("dsKhoa", khoaService.getAll());
        req.setAttribute("keyword", keyword);
        req.setAttribute("maKhoaDaChon", maKhoa);
        forward(req, resp, "/WEB-INF/admin/chuyenNganhList.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        ChuyenNganh cn = id > 0 ? cnService.getById(id) : null;
        if (cn == null) {
            redirect(req, resp, "error", "Không tìm thấy chuyên ngành cần sửa.");
            return;
        }
        showForm(req, resp, cn);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, ChuyenNganh cn)
            throws ServletException, IOException {
        if (cn != null) {
            req.setAttribute("chuyenNganh", cn);
        }
        req.setAttribute("dsKhoa", khoaService.getAll());
        forward(req, resp, "/WEB-INF/admin/chuyenNganhForm.jsp");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ChuyenNganh cn = new ChuyenNganh();
        cn.setMaChuyenNganh(CatalogIdParser.parseSaveId(req.getParameter("id")));
        cn.setMaCn(req.getParameter("maCn"));
        cn.setTenChuyenNganh(req.getParameter("tenChuyenNganh"));
        cn.setMaKhoa(parseId(req.getParameter("maKhoa")));
        try {
            if (cn.getMaChuyenNganh() < 0) {
                throw new CatalogException("Mã chuyên ngành không hợp lệ.");
            }
            cnService.save(cn);
            redirect(req, resp, "msg", "Lưu chuyên ngành thành công.");
        } catch (CatalogException e) {
            req.setAttribute("chuyenNganh", cn);
            req.setAttribute("dsKhoa", khoaService.getAll());
            req.setAttribute("error", e.getMessage());
            forward(req, resp, "/WEB-INF/admin/chuyenNganhForm.jsp");
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            cnService.delete(parseId(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa chuyên ngành thành công.");
        } catch (CatalogException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void setPageAttributes(HttpServletRequest req) {
        req.setAttribute("trang", "chuyennganh");
        req.setAttribute("tieuDeTrang", "Quản lý Chuyên ngành");
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi danh mục chuyên ngành", e);
        redirect(req, resp, "error", "Không thể xử lý yêu cầu lúc này.");
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
        resp.sendRedirect(req.getContextPath() + "/admin/chuyennganh?" + type + "=" + encoded);
    }

    private int parseId(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer parseNullableId(String value) {
        int parsed = parseId(value);
        return parsed > 0 ? parsed : null;
    }
}
