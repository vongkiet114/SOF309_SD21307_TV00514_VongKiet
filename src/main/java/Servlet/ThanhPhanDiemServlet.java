package Servlet;

import Models.TaiKhoan;
import Models.ThanhPhanDiem;
import Services.MonHocService;
import Services.ThanhPhanDiemService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/thanhphandiem")
public class ThanhPhanDiemServlet extends HttpServlet {

    private final ThanhPhanDiemService service;
    private final MonHocService monHocService;

    public ThanhPhanDiemServlet() {
        this(new ThanhPhanDiemService(), new MonHocService());
    }

    ThanhPhanDiemServlet(ThanhPhanDiemService service, MonHocService monHocService) {
        this.service = service;
        this.monHocService = monHocService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        req.setAttribute("trang", "thanhphandiem");
        req.setAttribute("tieuDeTrang", "Cấu hình cột điểm");

        Integer maMonhoc = parseInt(req.getParameter("maMonhoc"));
        req.setAttribute("dsMonHoc", monHocService.getAll());
        req.setAttribute("maMonhocChon", maMonhoc);

        if (maMonhoc != null && maMonhoc > 0) {
            req.setAttribute("dsThanhPhan", service.danhSachTheoMon(maMonhoc));
            BigDecimal tong = service.tongTrongSo(maMonhoc);
            req.setAttribute("tongTrongSoPhanTram",
                    tong.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString());

            Integer editId = parseInt(req.getParameter("editId"));
            if (editId != null) {
                req.setAttribute("thanhPhanSua", findInList(req, editId));
            }
        }
        req.getRequestDispatcher("/WEB-INF/admin/thanhPhanDiemList.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) return;

        String action = req.getParameter("action");
        Integer maMonhoc = parseInt(req.getParameter("maMonhoc"));
        try {
            switch (action == null ? "" : action) {
                case "create":
                case "update":
                    luu(req);
                    redirect(req, resp, maMonhoc, "msg", "Lưu cột điểm thành công.");
                    break;
                case "delete":
                    service.xoa(requireInt(req, "id"));
                    redirect(req, resp, maMonhoc, "msg", "Xóa cột điểm thành công.");
                    break;
                default:
                    redirect(req, resp, maMonhoc, "error", "Hành động không hợp lệ.");
                    break;
            }
        } catch (Exception e) {
            redirect(req, resp, maMonhoc, "error", e.getMessage());
        }
    }

    private void luu(HttpServletRequest req) throws Exception {
        ThanhPhanDiem tp = new ThanhPhanDiem();
        Integer id = parseInt(req.getParameter("id"));
        if (id != null) tp.setMaThanhphan(id);
        tp.setMaMonhoc(requireInt(req, "maMonhoc"));
        tp.setTenThanhphan(req.getParameter("tenThanhphan"));
        tp.setTrongSo(parsePhanTram(req.getParameter("trongSoPhanTram")));
        service.luu(tp);
    }

    // Chuyen phan tram (0..100) nguoi dung nhap thanh phan so (0..1) de luu.
    private BigDecimal parsePhanTram(String value) throws Exception {
        if (value == null || value.trim().isEmpty()) {
            throw new Exception("Trọng số không được để trống.");
        }
        try {
            BigDecimal phanTram = new BigDecimal(value.trim());
            return phanTram.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new Exception("Trọng số phải là một số (ví dụ 40 nghĩa là 40%).");
        }
    }

    private ThanhPhanDiem findInList(HttpServletRequest req, int editId) {
        Object ds = req.getAttribute("dsThanhPhan");
        if (ds instanceof java.util.List) {
            for (Object o : (java.util.List<?>) ds) {
                if (o instanceof ThanhPhanDiem && ((ThanhPhanDiem) o).getMaThanhphan() == editId) {
                    return (ThanhPhanDiem) o;
                }
            }
        }
        return null;
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

    private void redirect(HttpServletRequest req, HttpServletResponse resp,
                          Integer maMonhoc, String type, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        String monParam = maMonhoc != null ? "&maMonhoc=" + maMonhoc : "";
        resp.sendRedirect(req.getContextPath() + "/admin/thanhphandiem?" + type + "=" + encoded + monParam);
    }

    private static Integer parseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int requireInt(HttpServletRequest req, String name) throws Exception {
        Integer val = parseInt(req.getParameter(name));
        if (val == null) throw new Exception("Thiếu tham số: " + name);
        return val;
    }
}
