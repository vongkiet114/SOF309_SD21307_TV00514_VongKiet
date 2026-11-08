package Servlet;

import Models.TaiKhoan;
import Services.ThongBaoService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(urlPatterns = {"/sinhvien/thongbao", "/giangvien/thongbao"})
public class XemThongBaoServlet extends HttpServlet {

    private final ThongBaoService thongBaoService = new ThongBaoService();

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

        req.setAttribute("dsThongBao", thongBaoService.findByNguoiDung(tk.getMaNguoidung()));

        String viewPath;
        if ("STUDENT".equalsIgnoreCase(role)) {
            req.setAttribute("trang", "thongbao_sv");
            viewPath = "/WEB-INF/student/thongBao.jsp";
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            req.setAttribute("trang", "thongbao_gv");
            viewPath = "/WEB-INF/teacher/thongBao.jsp";
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        req.setAttribute("tieuDeTrang", "Thông báo");
        req.getRequestDispatcher(viewPath).forward(req, resp);
    }
}
