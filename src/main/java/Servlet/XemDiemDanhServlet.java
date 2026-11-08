package Servlet;

import Models.TaiKhoan;
import Services.XemDiemDanhService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@WebServlet("/sinhvien/diemdanh")
public class XemDiemDanhServlet extends HttpServlet {

    private final XemDiemDanhService xemDiemDanhService;

    public XemDiemDanhServlet() {
        this(new XemDiemDanhService());
    }

    XemDiemDanhServlet(XemDiemDanhService xemDiemDanhService) {
        this.xemDiemDanhService = xemDiemDanhService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan tk = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;

        if (tk == null || !"STUDENT".equalsIgnoreCase(tk.getTenVaiTro())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Loc theo mon hoc va theo thang. Mac dinh la thang hien tai.
        Integer maMonhoc = parseInt(req.getParameter("maMonhoc"));
        YearMonth thang = parseThang(req.getParameter("thang"));
        boolean locTheoThang = thang != null;

        try {
            req.setAttribute("dsMonHoc",
                    xemDiemDanhService.getMonHocDaDiemDanh(tk.getMaNguoidung()));
            req.setAttribute("lichSu", xemDiemDanhService.getLichSuDiemDanh(
                    tk.getMaNguoidung(),
                    maMonhoc,
                    locTheoThang ? thang.getYear() : null,
                    locTheoThang ? thang.getMonthValue() : null));
        } catch (RuntimeException e) {
            getServletContext().log("Không thể tải lịch sử điểm danh", e);
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Không thể tải lịch sử điểm danh lúc này.");
            return;
        }
        req.setAttribute("maMonhocChon", maMonhoc);
        req.setAttribute("thangChon", thang == null ? "" : thang.toString());
        req.setAttribute("trang", "diemdanh_sv");
        req.setAttribute("tieuDeTrang", "Lịch sử điểm danh");
        req.getRequestDispatcher("/WEB-INF/student/lichSuDiemDanh.jsp").forward(req, resp);
    }

    private static Integer parseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Rong -> thang hien tai (mac dinh); "tatca" -> khong loc theo thang; nguoc lai parse yyyy-MM.
    private static YearMonth parseThang(String s) {
        if (s == null || s.trim().isEmpty()) {
            return YearMonth.from(LocalDate.now());
        }
        if ("tatca".equalsIgnoreCase(s.trim())) {
            return null;
        }
        try {
            return YearMonth.parse(s.trim());
        } catch (DateTimeParseException e) {
            return YearMonth.from(LocalDate.now());
        }
    }
}
