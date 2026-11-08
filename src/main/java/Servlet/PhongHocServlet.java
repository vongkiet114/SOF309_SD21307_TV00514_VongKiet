package Servlet;

import Models.PhongHoc;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CatalogException;
import Services.PhongHocService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/phonghoc")
public class PhongHocServlet extends HttpServlet {
    private final PhongHocService phongHocService = new PhongHocService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        setPageAttributes(req);
        try {
            String action = req.getParameter("action");
            if ("them".equals(action)) {
                showList(req, resp, null);
            } else if ("sua".equals(action)) {
                showEditForm(req, resp);
            } else {
                showList(req, resp, null);
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

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        PhongHoc phongHoc = id > 0 ? phongHocService.getById(id) : null;
        if (phongHoc == null) {
            redirect(req, resp, "error", "Không tìm thấy phòng học cần sửa.");
            return;
        }
        showList(req, resp, phongHoc);
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp, PhongHoc phongHoc)
            throws ServletException, IOException {
        req.setAttribute("dsPhongHoc", phongHocService.getAll());
        if (phongHoc != null) {
            req.setAttribute("phongHoc", phongHoc);
        }
        req.getRequestDispatcher("/WEB-INF/admin/phongHocList.jsp").forward(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PhongHoc phongHoc = new PhongHoc();
        phongHoc.setMaPhong(CatalogIdParser.parseSaveId(req.getParameter("id")));
        phongHoc.setTenPhong(req.getParameter("tenPhong"));
        try {
            if (phongHoc.getMaPhong() < 0) {
                throw new CatalogException("Mã phòng học không hợp lệ.");
            }
            phongHocService.save(phongHoc);
            redirect(req, resp, "msg", "Lưu phòng học thành công.");
        } catch (CatalogException e) {
            req.setAttribute("error", e.getMessage());
            showList(req, resp, phongHoc);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            phongHocService.delete(parseId(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa phòng học thành công.");
        } catch (CatalogException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void setPageAttributes(HttpServletRequest req) {
        req.setAttribute("trang", "phonghoc");
        req.setAttribute("tieuDeTrang", "Quản lý Phòng học");
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi danh mục phòng học", e);
        redirect(req, resp, "error", "Không thể xử lý yêu cầu lúc này.");
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

    private void redirect(HttpServletRequest req, HttpServletResponse resp,
                          String type, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/phonghoc?" + type + "=" + encoded);
    }

    private int parseId(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
