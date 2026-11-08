package Servlet;

import Models.LopHoc;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CatalogException;
import Services.ChuyenNganhService;
import Services.LopHocService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/lophoc")
public class LopHocServlet extends HttpServlet {
    private final LopHocService lopService = new LopHocService();
    private final ChuyenNganhService chuyenNganhService = new ChuyenNganhService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
        req.setAttribute("dsLop", lopService.getAll());
        forward(req, resp, "/WEB-INF/admin/lopHocList.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        LopHoc lop = id > 0 ? lopService.getById(id) : null;
        if (lop == null) {
            redirect(req, resp, "error", "Không tìm thấy lớp cần sửa.");
            return;
        }
        showForm(req, resp, lop);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, LopHoc lop)
            throws ServletException, IOException {
        if (lop != null) {
            req.setAttribute("lop", lop);
        }
        req.setAttribute("dsChuyenNganh", chuyenNganhService.getAll());
        forward(req, resp, "/WEB-INF/admin/lopHocForm.jsp");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LopHoc lop = new LopHoc();
        lop.setMaLop(CatalogIdParser.parseSaveId(req.getParameter("id")));
        lop.setTenLop(req.getParameter("tenLop"));
        lop.setMaChuyenNganh(parseId(req.getParameter("maChuyenNganh")));
        try {
            if (lop.getMaLop() < 0) {
                throw new CatalogException("Mã lớp không hợp lệ.");
            }
            lopService.save(lop);
            redirect(req, resp, "msg", "Lưu lớp thành công.");
        } catch (CatalogException e) {
            req.setAttribute("error", e.getMessage());
            showForm(req, resp, lop);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            lopService.delete(parseId(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa lớp thành công.");
        } catch (CatalogException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void setPageAttributes(HttpServletRequest req) {
        req.setAttribute("trang", "lophoc");
        req.setAttribute("tieuDeTrang", "Quản lý Lớp");
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi danh mục lớp", e);
        redirect(req, resp, "error", "Không thể xử lý dữ liệu lớp.");
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan tk = session == null ? null : (TaiKhoan) session.getAttribute("user");
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
        resp.sendRedirect(req.getContextPath() + "/admin/lophoc?" + type + "=" + encoded);
    }

    private int parseId(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
