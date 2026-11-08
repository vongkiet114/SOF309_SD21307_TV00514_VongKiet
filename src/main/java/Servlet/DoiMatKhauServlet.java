package Servlet;

import Controllers.DangNhapController;
import Models.TaiKhoan;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/doimatkhau")
public class DoiMatKhauServlet extends HttpServlet {

    private final DangNhapController dangNhapController = new DangNhapController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/common/doiMatKhau.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan tk = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;

        if (tk == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("sendOtp".equals(action)) {
            String otp = Services.OtpService.generateOtp();
            session.setAttribute("otpCode", otp);
            session.setAttribute("otpTime", System.currentTimeMillis());
            String maskEmail = tk.getEmail() != null ? tk.getEmail() : "no-email@local";
            Services.OtpService.sendOtp(maskEmail, otp);
            resp.setContentType("text/plain");
            resp.getWriter().write("OK");
            return;
        }

        String otpInput = req.getParameter("otp");
        String storedOtp = (String) session.getAttribute("otpCode");
        Long otpTime = (Long) session.getAttribute("otpTime");

        if (storedOtp == null || otpTime == null || !storedOtp.equals(otpInput)) {
            redirect(req, resp, "error", "Mã OTP không hợp lệ.");
            return;
        }

        // Check expiration (5 minutes)
        if (System.currentTimeMillis() - otpTime > 5 * 60 * 1000) {
            session.removeAttribute("otpCode");
            session.removeAttribute("otpTime");
            redirect(req, resp, "error", "Mã OTP đã hết hạn.");
            return;
        }

        String matKhauCu = req.getParameter("matKhauCu");
        String matKhauMoi = req.getParameter("matKhauMoi");
        String nhapLai = req.getParameter("nhapLaiMatKhau");

        try {
            dangNhapController.doiMatKhau(tk.getMaNguoidung(), matKhauCu, matKhauMoi, nhapLai);
            session.removeAttribute("otpCode");
            session.removeAttribute("otpTime");
            redirect(req, resp, "msg", "Đổi mật khẩu thành công.");
        } catch (Exception e) {
            redirect(req, resp, "error", e.getMessage());
        }
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String type, String message) throws IOException {
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/doimatkhau?" + type + "=" + encodedMsg);
    }
}
