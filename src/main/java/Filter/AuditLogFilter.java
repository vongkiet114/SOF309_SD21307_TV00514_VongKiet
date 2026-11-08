package Filter;

import Models.TaiKhoan;
import Services.NhatKyService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/*"})
public class AuditLogFilter implements Filter {

    private final NhatKyService nhatKyService = new NhatKyService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
        try {
            logActivity((HttpServletRequest) request);
        } catch (Exception ignored) {
            // Ignore all logging errors
        }
    }

    private void logActivity(HttpServletRequest req) {
        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            return;
        }

        HttpSession session = req.getSession(false);
        TaiKhoan tk = (session != null) ? (TaiKhoan) session.getAttribute("user") : null;
        if (tk == null) {
            return;
        }

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        String action = req.getParameter("action");
        String hanhDong;
        if (uri.contains("/login")) {
            hanhDong = "LOGIN";
        } else if (action != null && !action.isEmpty()) {
            hanhDong = action.toUpperCase() + " on " + uri;
        } else {
            hanhDong = "POST to " + uri;
        }

        nhatKyService.log(tk.getMaNguoidung(), hanhDong, layDiaChiIp(req));
    }

    // Lay dia chi IP that cua nguoi dung: uu tien X-Forwarded-For (khi qua proxy),
    // neu khong co thi dung dia chi ket noi truc tiep.
    private String layDiaChiIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            // X-Forwarded-For co the la danh sach "client, proxy1, proxy2" -> lay IP dau tien
            String ip = forwarded.split(",")[0].trim();
            if (!ip.isEmpty()) {
                return ip;
            }
        }
        return req.getRemoteAddr();
    }
}
