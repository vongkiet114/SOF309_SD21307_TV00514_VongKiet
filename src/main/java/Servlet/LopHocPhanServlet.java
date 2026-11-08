package Servlet;

import Models.LopHocPhan;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.GiangVienService;
import Services.HocKyService;
import Services.LopHocPhanService;
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

@WebServlet("/admin/lophocphan")
public class LopHocPhanServlet extends HttpServlet {

    private final LopHocPhanService lhpService;
    private final MonHocService monHocService;
    private final GiangVienService gvService;
    private final HocKyService hocKyService;

    public LopHocPhanServlet() {
        this(new LopHocPhanService(), new MonHocService(),
                new GiangVienService(), new HocKyService());
    }

    LopHocPhanServlet(LopHocPhanService lhpService,
                      MonHocService monHocService,
                      GiangVienService gvService,
                      HocKyService hocKyService) {
        this.lhpService = lhpService;
        this.monHocService = monHocService;
        this.gvService = gvService;
        this.hocKyService = hocKyService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("trang", "lophocphan");
        req.setAttribute("tieuDeTrang", "Quản lý Lớp học phần");

        try {
            String action = req.getParameter("action");
            if ("add".equals(action) || "edit".equals(action)) {
                prepareForm(req, resp, action);
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

        req.setAttribute("trang", "lophocphan");
        req.setAttribute("tieuDeTrang", "Quản lý Lớp học phần");
        try {
            String action = req.getParameter("action");
            if ("create".equals(action)) {
                create(req, resp);
            } else if ("update".equals(action)) {
                update(req, resp);
            } else if ("delete".equals(action)) {
                delete(req, resp);
            } else {
                redirect(req, resp, "error", "Hành động không hợp lệ.");
            }
        } catch (RepositoryException e) {
            handleRepositoryError(req, resp, e);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer maHocKy = parseInt(req.getParameter("maHocKy"));
        Integer maGiangVien = parseInt(req.getParameter("maGiangVien"));
        req.setAttribute("dsLHP", lhpService.getAll(maHocKy, maGiangVien));
        // For filter dropdowns
        req.setAttribute("dsHocKy", hocKyService.getAll());
        req.setAttribute("dsGiangVien", gvService.dsGiangVien());
        forward(req, resp, "/WEB-INF/admin/lhpList.jsp");
    }

    private void prepareForm(HttpServletRequest req, HttpServletResponse resp, String action) throws ServletException, IOException {
        LopHocPhan lhp = null;
        boolean isEdit = "edit".equals(action);
        if ("edit".equals(action)) {
            Integer id = parseInt(req.getParameter("id"));
            lhp = id == null ? null : lhpService.findById(id);
            if (lhp == null) {
                redirect(req, resp, "error", "Không tìm thấy lớp học phần cần sửa.");
                return;
            }
        }
        showForm(req, resp, lhp, isEdit);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp,
                          LopHocPhan lhp, boolean isEdit) throws ServletException, IOException {
        if (lhp != null) {
            req.setAttribute("lhp", lhp);
        }
        req.setAttribute("isEdit", isEdit);
        req.setAttribute("dsMonHoc", monHocService.getAll());
        req.setAttribute("dsGiangVien", gvService.dsGiangVien());
        req.setAttribute("dsHocKy", hocKyService.getAll());
        forward(req, resp, "/WEB-INF/admin/lhpForm.jsp");
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] maMonhocs = req.getParameterValues("maMonhoc");
        if (maMonhocs == null || maMonhocs.length == 0) {
            req.setAttribute("error", "Vui lòng chọn ít nhất một môn học.");
            showForm(req, resp, null, false);
            return;
        }

        int maGiangvien = intValue(req.getParameter("maGiangvien"));
        int maHocky = intValue(req.getParameter("maHocky"));
        String trangThai = req.getParameter("trangThai");

        try {
            int count = 0;
            for (String maMonStr : maMonhocs) {
                LopHocPhan lhp = new LopHocPhan();
                lhp.setMaMonhoc(intValue(maMonStr));
                lhp.setMaGiangvien(maGiangvien);
                lhp.setMaHocky(maHocky);
                lhp.setTrangThai(trangThai);
                lhpService.create(lhp);
                count++;
            }
            redirect(req, resp, "msg", "Tạo thành công " + count + " lớp học phần.");
        } catch (AcademicException e) {
            req.setAttribute("error", e.getMessage());
            LopHocPhan retained = new LopHocPhan();
            if (maMonhocs != null && maMonhocs.length > 0) {
                retained.setMaMonhoc(intValue(maMonhocs[0]));
            }
            retained.setMaGiangvien(maGiangvien);
            retained.setMaHocky(maHocky);
            retained.setTrangThai(trangThai);
            showForm(req, resp, retained, false);
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LopHocPhan lhp = fromRequest(req);
        lhp.setMaLophocphan(intValue(req.getParameter("id")));
        try {
            lhpService.update(lhp);
            redirect(req, resp, "msg", "Cập nhật lớp học phần thành công.");
        } catch (AcademicException e) {
            req.setAttribute("error", e.getMessage());
            showForm(req, resp, lhp, true);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            lhpService.delete(intValue(req.getParameter("id")));
            redirect(req, resp, "msg", "Xóa lớp học phần thành công.");
        } catch (AcademicException e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private LopHocPhan fromRequest(HttpServletRequest req) {
        LopHocPhan lhp = new LopHocPhan();
        lhp.setMaMonhoc(intValue(req.getParameter("maMonhoc")));
        lhp.setMaGiangvien(intValue(req.getParameter("maGiangvien")));
        lhp.setMaHocky(intValue(req.getParameter("maHocky")));
        lhp.setTrangThai(req.getParameter("trangThai"));
        return lhp;
    }

    private void handleRepositoryError(HttpServletRequest req, HttpServletResponse resp,
                                       RepositoryException e) throws IOException {
        getServletContext().log("Lỗi dữ liệu lớp học phần", e);
        redirect(req, resp, "error", "Không thể xử lý yêu cầu lúc này.");
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan tk = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;
        if (tk == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!"ADMIN".equalsIgnoreCase(tk.getTenVaiTro())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chức năng này chỉ dành cho Admin.");
            return false;
        }
        return true;
    }

    private void forward(HttpServletRequest r, HttpServletResponse p, String path) throws ServletException, IOException {
        r.getRequestDispatcher(path).forward(r, p);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String type, String message) throws IOException {
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/lophocphan?" + type + "=" + encodedMsg);
    }
    
    private static Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private static int intValue(String value) {
        Integer parsed = parseInt(value);
        return parsed == null ? 0 : parsed;
    }
}
