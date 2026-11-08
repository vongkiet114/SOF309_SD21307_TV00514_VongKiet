package Servlet;

import Models.GiangVien;
import Models.TaiKhoan;
import Services.ChuyenNganhService;
import Services.GiangVienService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/giangvien")
@MultipartConfig(
    fileSizeThreshold = 5 * 1024 * 1024,
    maxFileSize = 5L * 1024 * 1024,
    maxRequestSize = 6L * 1024 * 1024
)
public class GiangVienServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER =
            Logger.getLogger(GiangVienServlet.class.getName());

    private final GiangVienService gvService;
    private final ChuyenNganhService cnService;

    public GiangVienServlet() {
        this(new GiangVienService(), new ChuyenNganhService());
    }

    GiangVienServlet(GiangVienService gvService) {
        this(gvService, new ChuyenNganhService());
    }

    GiangVienServlet(GiangVienService gvService,
                     ChuyenNganhService cnService) {
        this.gvService = gvService;
        this.cnService = cnService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("trang", "giangvien");
        req.setAttribute("tieuDeTrang", "Quản lý Giảng viên");

        String action = req.getParameter("action");

        if ("add".equals(action) || "edit".equals(action)) {
            prepareForm(req, resp, action);
        } else if ("detail".equals(action)) {
            showDetail(req, resp);
        } else {
            showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        String action = req.getParameter("action");
        try {
            switch (action) {
                case "create":
                    create(req);
                    redirect(req, resp, "msg", "Tạo giảng viên thành công.");
                    break;
                case "update":
                    update(req);
                    redirect(req, resp, "msg", "Cập nhật giảng viên thành công.");
                    break;
                case "delete":
                    delete(req);
                    redirect(req, resp, "msg", "Xóa giảng viên thành công.");
                    break;
                default:
                    redirect(req, resp, "error", "Hành động không hợp lệ.");
                    break;
            }
        } catch (Exception e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        Integer maChuyenNganh = parseInt(req.getParameter("maChuyenNganh"));
        String hocVi = req.getParameter("hocVi");
        req.setAttribute("dsGiangVien", gvService.search(keyword, maChuyenNganh, hocVi));
        req.setAttribute("dsChuyenNganh", cnService.getAll());
        req.setAttribute("keyword", keyword);
        req.setAttribute("maChuyenNganhDaChon", maChuyenNganh);
        req.setAttribute("hocViDaChon", hocVi);
        forward(req, resp, "/WEB-INF/admin/giangVienList.jsp");
    }

    private void prepareForm(HttpServletRequest req, HttpServletResponse resp, String action) throws ServletException, IOException {
        if ("edit".equals(action)) {
            Integer id = parseInt(req.getParameter("id"));
            if (id != null) {
                req.setAttribute("giangVien", gvService.findByMaNguoidung(id));
            }
        }
        req.setAttribute("dsChuyenNganh", cnService.getAll());
        forward(req, resp, "/WEB-INF/admin/giangVienForm.jsp");
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = parseInt(req.getParameter("id"));
        if (id == null) {
            redirect(req, resp, "error", "Thiếu mã giảng viên.");
            return;
        }
        GiangVien giangVien = gvService.findByMaNguoidung(id);
        if (giangVien == null) {
            redirect(req, resp, "error", "Không tìm thấy giảng viên.");
            return;
        }
        req.setAttribute("giangVien", giangVien);
        forward(req, resp, "/WEB-INF/admin/giangVienDetail.jsp");
    }

    private void create(HttpServletRequest req) throws Exception {
        GiangVien gv = fromRequest(req);
        String uploaded = null;
        try {
            uploaded = Utils.FileUploadUtils.uploadImage(
                    req, "hinhAnhFile", "giangvien");
            gv.setHinhAnh(uploaded);
            gvService.create(gv);
        } catch (Exception e) {
            deleteImageQuietly(req, uploaded, "ảnh giảng viên vừa tải lên");
            throw e;
        }
    }

    private void update(HttpServletRequest req) throws Exception {
        int id = requireInt(req, "id");
        GiangVien existing = gvService.findByMaNguoidung(id);
        if (existing == null) {
            throw new Exception("Không tìm thấy giảng viên cần cập nhật.");
        }
        GiangVien gv = fromRequest(req);
        gv.setMaNguoidung(id);

        String uploaded = null;
        try {
            uploaded = Utils.FileUploadUtils.uploadImage(
                    req, "hinhAnhFile", "giangvien");
            gv.setHinhAnh(uploaded == null
                    ? existing.getHinhAnh() : uploaded);
            gvService.update(gv);
        } catch (Exception e) {
            deleteImageQuietly(req, uploaded, "ảnh giảng viên vừa tải lên");
            throw e;
        }
        if (uploaded != null
                && !Objects.equals(uploaded, existing.getHinhAnh())) {
            deleteImageQuietly(req, existing.getHinhAnh(), "ảnh giảng viên cũ");
        }
    }

    private void delete(HttpServletRequest req) throws Exception {
        int id = requireInt(req, "id");
        GiangVien existing = gvService.findByMaNguoidung(id);
        if (existing == null) {
            throw new Exception("Không tìm thấy giảng viên cần xóa.");
        }
        gvService.delete(id);
        deleteImageQuietly(
                req, existing.getHinhAnh(), "ảnh giảng viên đã xóa");
    }

    private GiangVien fromRequest(HttpServletRequest req) throws Exception {
        GiangVien gv = new GiangVien();
        gv.setHoTen(req.getParameter("hoTen"));
        gv.setEmail(req.getParameter("email"));
        gv.setMaChuyenNganh(requireInt(req, "maChuyenNganh"));
        gv.setHocVi(req.getParameter("hocVi"));
        gv.setGioiTinh(req.getParameter("gioiTinh"));
        gv.setNgaySinh(req.getParameter("ngaySinh"));
        gv.setDiaChi(req.getParameter("diaChi"));
        return gv;
    }

    private void deleteImageQuietly(HttpServletRequest req,
                                    String storedName,
                                    String description) {
        if (storedName == null || storedName.trim().isEmpty()) {
            return;
        }
        try {
            Utils.FileUploadUtils.deleteImage(
                    req.getServletContext(), "giangvien", storedName);
        } catch (Exception cleanupError) {
            LOGGER.log(Level.WARNING,
                    "Không thể dọn {0}: {1}",
                    new Object[]{description, cleanupError.getMessage()});
        }
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
        resp.sendRedirect(req.getContextPath() + "/admin/giangvien?" + type + "=" + encodedMsg);
    }
    
    private static Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private static int requireInt(HttpServletRequest req, String name) throws Exception {
        Integer val = parseInt(req.getParameter(name));
        if (val == null) throw new Exception("Thiếu tham số: " + name);
        return val;
    }
}
