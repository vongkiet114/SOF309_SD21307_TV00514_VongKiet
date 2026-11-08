package Servlet;

import Models.MonHoc;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CatalogException;
import Services.ChuyenNganhService;
import Services.MonHocService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/monhoc")
public class MonHocServlet extends HttpServlet {
    private final MonHocService monHocService = new MonHocService();
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
        String q = req.getParameter("q");
        req.setAttribute("dsMonHoc", monHocService.getAll(q));
        req.setAttribute("searchQuery", q != null ? q : "");
        forward(req, resp, "/WEB-INF/admin/monHocList.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseId(req.getParameter("id"));
        MonHoc mon = id > 0 ? monHocService.getById(id) : null;
        if (mon == null) {
            redirect(req, resp, "error", "Không tìm thấy môn học cần sửa.");
            return;
        }
        showForm(req, resp, mon);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, MonHoc mon)
            throws ServletException, IOException {
        if (mon != null) {
            req.setAttribute("monHoc", mon);
        }
        req.setAttribute("dsChuyenNganh", chuyenNganhService.getAll());
        forward(req, resp, "/WEB-INF/admin/monHocForm.jsp");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        MonHoc mon = new MonHoc();
        mon.setMaMonhoc(CatalogIdParser.parseSaveId(req.getParameter("id")));
        mon.setMaMon(req.getParameter("maMon"));
        mon.setTenMonhoc(req.getParameter("tenMonhoc"));
        mon.setSoTinchi(parseId(req.getParameter("soTinchi")));
        mon.setMaChuyenNganh(parseId(req.getParameter("maChuyenNganh")));
        try {
            if (mon.getMaMonhoc() < 0) {
                throw new CatalogException("Mã môn học không hợp lệ.");
            }
            monHocService.save(mon);
            redirect(req, resp, "msg", "Lưu môn học thành công.");
        } catch (CatalogException e) {
            req.setAttribute("error", e.getMessage());
            showForm(req, resp, mon);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            monHocService.delete(parseId(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa môn học thành công.");
        } catch (CatalogException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void setPageAttributes(HttpServletRequest req) {
        req.setAttribute("trang", "monhoc");
        req.setAttribute("tieuDeTrang", "Quản lý Môn học");
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi danh mục môn học", e);
        redirect(req, resp, "error", "Không thể xử lý dữ liệu môn học.");
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
        resp.sendRedirect(req.getContextPath() + "/admin/monhoc?" + type + "=" + encoded);
    }

    private int parseId(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
