package Servlet;

import Models.GiangVien;
import Models.SinhVien;
import Models.TaiKhoan;
import Services.GiangVienService;
import Services.SinhVienService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(urlPatterns = {"/sinhvien/thongtin", "/giangvien/thongtin"})
public class XemThongTinServlet extends HttpServlet {

    private final SinhVienService svService;
    private final GiangVienService gvService;

    public XemThongTinServlet() {
        this(new SinhVienService(), new GiangVienService());
    }

    XemThongTinServlet(SinhVienService svService, GiangVienService gvService) {
        this.svService = svService;
        this.gvService = gvService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan tk = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;

        if (tk == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = tk.getTenVaiTro();
        if (role == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không xác định được vai trò.");
            return;
        }

        String viewPath;
        try {
            if ("STUDENT".equalsIgnoreCase(role)) {
                SinhVien sv = svService.findByMaNguoidung(tk.getMaNguoidung());
                req.setAttribute("thongTin", sv);
                req.setAttribute("trang", "thongtin_sv");
                viewPath = "/WEB-INF/student/thongTinCaNhan.jsp";
            } else if ("TEACHER".equalsIgnoreCase(role)) {
                GiangVien gv = gvService.findByMaNguoidung(tk.getMaNguoidung());
                req.setAttribute("thongTin", gv);
                req.setAttribute("trang", "thongtin_gv");
                viewPath = "/WEB-INF/teacher/thongTinCaNhan.jsp";
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (RuntimeException e) {
            getServletContext().log("Không thể tải thông tin cá nhân", e);
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Không thể tải thông tin cá nhân lúc này.");
            return;
        }

        req.setAttribute("tieuDeTrang", "Thông tin cá nhân");
        req.getRequestDispatcher(viewPath).forward(req, resp);
    }
}
