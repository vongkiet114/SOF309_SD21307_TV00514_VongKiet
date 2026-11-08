package Servlet;

import Models.SinhVien;
import Models.TaiKhoan;
import Services.ChuyenNganhService;
import Services.LopHocService;
import Services.SinhVienService;

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

@WebServlet("/admin/sinhvien")
@MultipartConfig(
    fileSizeThreshold = 5 * 1024 * 1024,
    maxFileSize = 5L * 1024 * 1024,
    maxRequestSize = 6L * 1024 * 1024
)
public class SinhVienServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER =
            Logger.getLogger(SinhVienServlet.class.getName());

    private final SinhVienService svService;
    private final LopHocService lopService;
    private final ChuyenNganhService cnService;

    public SinhVienServlet() {
        this(new SinhVienService(), new LopHocService(), new ChuyenNganhService());
    }

    SinhVienServlet(SinhVienService svService) {
        this(svService, new LopHocService(), new ChuyenNganhService());
    }

    SinhVienServlet(SinhVienService svService,
                    LopHocService lopService,
                    ChuyenNganhService cnService) {
        this.svService = svService;
        this.lopService = lopService;
        this.cnService = cnService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("trang", "sinhvien");
        req.setAttribute("tieuDeTrang", "Quản lý Sinh viên");

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
                    redirect(req, resp, "msg", "Tạo sinh viên thành công.");
                    break;
                case "update":
                    update(req);
                    redirect(req, resp, "msg", "Cập nhật sinh viên thành công.");
                    break;
                case "delete":
                    delete(req);
                    redirect(req, resp, "msg", "Xóa sinh viên thành công.");
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
        Integer maLop = parseInt(req.getParameter("maLop"));
        int page = 1;
        int pageSize = Integer.MAX_VALUE;

        req.setAttribute("dsSinhVien", svService.search(keyword, maChuyenNganh, maLop, page, pageSize));
        req.setAttribute("keyword", keyword);
        req.setAttribute("maChuyenNganhDaChon", maChuyenNganh);
        req.setAttribute("maLopDaChon", maLop);
        
        req.setAttribute("totalPages", 1);
        req.setAttribute("currentPage", page);
        
        req.setAttribute("dsChuyenNganh", cnService.getAll());
        req.setAttribute("dsLop", lopService.getAll());
        forward(req, resp, "/WEB-INF/admin/sinhVienList.jsp");
    }

    private void prepareForm(HttpServletRequest req, HttpServletResponse resp, String action) throws ServletException, IOException {
        if ("edit".equals(action)) {
            Integer id = parseInt(req.getParameter("id"));
            if (id != null) {
                req.setAttribute("sv", svService.findByMaNguoidung(id));
            }
        }
        req.setAttribute("dsLop", lopService.getAll());
        forward(req, resp, "/WEB-INF/admin/sinhVienForm.jsp");
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = parseInt(req.getParameter("id"));
        if (id == null) {
            redirect(req, resp, "error", "Thiếu mã sinh viên.");
            return;
        }
        SinhVien sv = svService.findByMaNguoidung(id);
        if (sv == null) {
            redirect(req, resp, "error", "Không tìm thấy sinh viên.");
            return;
        }
        req.setAttribute("sv", sv);
        forward(req, resp, "/WEB-INF/admin/sinhVienDetail.jsp");
    }

    private void create(HttpServletRequest req) throws Exception {
        SinhVien sv = fromRequest(req);
        String uploaded = null;
        try {
            uploaded = Utils.FileUploadUtils.uploadImage(
                    req, "hinhAnhFile", "sinhvien");
            sv.setHinhAnh(uploaded);
            svService.create(sv);
        } catch (Exception e) {
            deleteImageQuietly(req, uploaded, "ảnh sinh viên vừa tải lên");
            throw e;
        }
    }

    private void update(HttpServletRequest req) throws Exception {
        int id = requireInt(req, "id");
        SinhVien existing = svService.findByMaNguoidung(id);
        if (existing == null) {
            throw new Exception("Không tìm thấy sinh viên cần cập nhật.");
        }
        SinhVien sv = fromRequest(req);
        sv.setMaNguoidung(id);
        // Ma so sinh vien khong duoc phep chinh sua sau khi tao -> giu nguyen gia tri cu.
        sv.setMssv(existing.getMssv());

        String uploaded = null;
        try {
            uploaded = Utils.FileUploadUtils.uploadImage(
                    req, "hinhAnhFile", "sinhvien");
            sv.setHinhAnh(uploaded == null
                    ? existing.getHinhAnh() : uploaded);
            svService.update(sv);
        } catch (Exception e) {
            deleteImageQuietly(req, uploaded, "ảnh sinh viên vừa tải lên");
            throw e;
        }
        if (uploaded != null
                && !Objects.equals(uploaded, existing.getHinhAnh())) {
            deleteImageQuietly(
                    req, existing.getHinhAnh(), "ảnh sinh viên cũ");
        }
    }

    private void delete(HttpServletRequest req) throws Exception {
        int id = requireInt(req, "id");
        SinhVien existing = svService.findByMaNguoidung(id);
        if (existing == null) {
            throw new Exception("Không tìm thấy sinh viên cần xóa.");
        }
        svService.delete(id);
        deleteImageQuietly(
                req, existing.getHinhAnh(), "ảnh sinh viên đã xóa");
    }

    private SinhVien fromRequest(HttpServletRequest req) throws Exception {
        SinhVien sv = new SinhVien();
        sv.setHoTen(req.getParameter("hoTen"));
        sv.setEmail(req.getParameter("email"));
        sv.setMssv(req.getParameter("mssv"));
        sv.setGioiTinh(req.getParameter("gioiTinh"));
        sv.setNgaySinh(req.getParameter("ngaySinh"));
        sv.setDiaChi(req.getParameter("diaChi"));
        sv.setCccd(req.getParameter("cccd"));
        sv.setDanToc(req.getParameter("danToc"));
        sv.setKhoaHoc(req.getParameter("khoaHoc"));
        sv.setNgayNhapHoc(req.getParameter("ngayNhapHoc"));
        sv.setMaLop(requireInt(req, "maLop"));
        sv.setTrangThai(req.getParameter("trangThai"));
        return sv;
    }

    private void deleteImageQuietly(HttpServletRequest req,
                                    String storedName,
                                    String description) {
        if (storedName == null || storedName.trim().isEmpty()) {
            return;
        }
        try {
            Utils.FileUploadUtils.deleteImage(
                    req.getServletContext(), "sinhvien", storedName);
        } catch (Exception cleanupError) {
            LOGGER.log(Level.WARNING,
                    "Không thể dọn {0}: {1}",
                    new Object[]{description, cleanupError.getMessage()});
        }
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

    private void forward(HttpServletRequest r, HttpServletResponse p, String path) throws ServletException, IOException {
        r.getRequestDispatcher(path).forward(r, p);
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String type, String message) throws IOException {
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/sinhvien?" + type + "=" + encodedMsg);
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
