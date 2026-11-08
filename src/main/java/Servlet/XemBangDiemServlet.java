package Servlet;

import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.XemBangDiemService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/sinhvien/bangdiem")
public class XemBangDiemServlet extends HttpServlet {
    private final XemBangDiemService xemBangDiemService;

    public XemBangDiemServlet() {
        this(new XemBangDiemService());
    }

    XemBangDiemServlet(XemBangDiemService xemBangDiemService) {
        this.xemBangDiemService = xemBangDiemService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan account = session == null ? null : (TaiKhoan) session.getAttribute("user");
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!"STUDENT".equalsIgnoreCase(account.getTenVaiTro())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng này chỉ dành cho sinh viên.");
            return;
        }

        try {
            req.setAttribute("bangDiem",
                    xemBangDiemService.getBangDiem(account.getMaNguoidung()));
            req.setAttribute("trang", "bangdiem_sv");
            req.setAttribute("tieuDeTrang", "Bảng điểm cá nhân");
            req.getRequestDispatcher("/WEB-INF/student/bangDiem.jsp").forward(req, resp);
        } catch (RepositoryException e) {
            getServletContext().log("Lỗi dữ liệu bảng điểm sinh viên", e);
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Không thể xử lý yêu cầu lúc này.");
        }
    }
}
