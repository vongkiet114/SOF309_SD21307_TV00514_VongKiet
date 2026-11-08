package Servlet;

import Models.CaHoc;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.CaHocService;
import Services.CatalogException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@WebServlet("/admin/cahoc")
public class CaHocServlet extends HttpServlet {
    private final CaHocService caHocService;

    public CaHocServlet() {
        this(new CaHocService());
    }

    CaHocServlet(CaHocService caHocService) {
        this.caHocService = caHocService;
    }

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
        CaHoc caHoc = id > 0 ? caHocService.getById(id) : null;
        if (caHoc == null) {
            redirect(req, resp, "error", "Không tìm thấy ca học cần sửa.");
            return;
        }
        showList(req, resp, caHoc);
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp, CaHoc caHoc)
            throws ServletException, IOException {
        req.setAttribute("dsCaHoc", caHocService.getAll());
        if (caHoc != null) {
            req.setAttribute("caHoc", caHoc);
        }
        req.getRequestDispatcher("/WEB-INF/admin/caHocList.jsp").forward(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = CatalogIdParser.parseSaveId(req.getParameter("id"));
        if (id < 0) {
            redirect(req, resp, "error", "Mã ca học không hợp lệ.");
            return;
        }

        CaHoc caHoc = new CaHoc();
        caHoc.setMaCa(id);
        try {
            caHoc.setGioBatdau(parseTime(req.getParameter("gioBatdau")));
            caHoc.setGioKetthuc(parseTime(req.getParameter("gioKetthuc")));
            caHocService.save(caHoc);
            redirect(req, resp, "msg", "Lưu ca học thành công.");
        } catch (DateTimeParseException e) {
            req.setAttribute("error", "Giờ bắt đầu hoặc giờ kết thúc không hợp lệ.");
            showList(req, resp, caHoc);
        } catch (CatalogException e) {
            req.setAttribute("error", e.getMessage());
            showList(req, resp, caHoc);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            caHocService.delete(parseId(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa ca học thành công.");
        } catch (CatalogException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void setPageAttributes(HttpServletRequest req) {
        req.setAttribute("trang", "cahoc");
        req.setAttribute("tieuDeTrang", "Quản lý Ca học");
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi danh mục ca học", e);
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
        resp.sendRedirect(req.getContextPath() + "/admin/cahoc?" + type + "=" + encoded);
    }

    private int parseId(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalTime parseTime(String value) {
        return value == null || value.trim().isEmpty() ? null : LocalTime.parse(value.trim());
    }
}
