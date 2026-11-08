package Servlet;

import Models.HocKy;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CatalogException;
import Services.HocKyService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/admin/hocky")
public class HocKyServlet extends HttpServlet {
    private final HocKyService hocKyService;

    public HocKyServlet() {
        this(new HocKyService());
    }

    HocKyServlet(HocKyService hocKyService) {
        this.hocKyService = hocKyService;
    }

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
        req.setAttribute("dsHocKy", hocKyService.getAll());
        forward(req, resp, "/WEB-INF/admin/hocKyList.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = parseInt(req.getParameter("id"));
        HocKy hocKy = id > 0 ? hocKyService.getById(id) : null;
        if (hocKy == null) {
            redirect(req, resp, "error", "Không tìm thấy học kỳ cần sửa.");
            return;
        }
        showForm(req, resp, hocKy);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, HocKy hocKy)
            throws ServletException, IOException {
        if (hocKy != null) {
            req.setAttribute("hocKy", hocKy);
        }
        forward(req, resp, "/WEB-INF/admin/hocKyForm.jsp");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = CatalogIdParser.parseSaveId(req.getParameter("maHocky"));
        if (id < 0) {
            redirect(req, resp, "error", "Mã học kỳ không hợp lệ.");
            return;
        }
        HocKy hocKy = new HocKy();
        hocKy.setMaHocky(id);
        hocKy.setTenHocky(req.getParameter("tenHocky"));
        hocKy.setBlock(parseInt(req.getParameter("block")));
        hocKy.setNamHoc(req.getParameter("namHoc"));
        try {
            hocKy.setNgayBatdau(parseDate(req.getParameter("ngayBatdau")));
            hocKy.setNgayKetthuc(parseDate(req.getParameter("ngayKetthuc")));
            hocKyService.save(hocKy);
            redirect(req, resp, "msg", "Lưu học kỳ thành công.");
        } catch (DateTimeParseException e) {
            req.setAttribute("error", "Ngày bắt đầu hoặc ngày kết thúc không hợp lệ.");
            showForm(req, resp, hocKy);
        } catch (CatalogException e) {
            req.setAttribute("error", e.getMessage());
            showForm(req, resp, hocKy);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            hocKyService.delete(parseInt(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa học kỳ thành công.");
        } catch (CatalogException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void setPageAttributes(HttpServletRequest req) {
        req.setAttribute("trang", "hocky");
        req.setAttribute("tieuDeTrang", "Quản lý Học kỳ");
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi danh mục học kỳ", e);
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

    private void forward(HttpServletRequest req, HttpServletResponse resp, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp,
                          String type, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/hocky?" + type + "=" + encoded);
    }

    private int parseInt(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseDate(String value) {
        return value == null || value.trim().isEmpty() ? null : LocalDate.parse(value.trim());
    }
}
