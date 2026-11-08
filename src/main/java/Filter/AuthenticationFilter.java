package Filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// filter nay chi xac thuc: bat buoc da dang nhap moi vao duoc khu vuc duoc bao ve.
// quyet dinh vai tro nao duoc vao khu vuc nao nam tai AuthorizationFilter/RoleAccess.
// neu chua dang nhap -> chuyen ve trang /login.
@WebFilter(urlPatterns = {"/admin/*", "/giangvien/*", "/sinhvien/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession phien = req.getSession(false);
        boolean daDangNhap = (phien != null && phien.getAttribute("user") != null);

        if (!daDangNhap) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;     // chan request, khong cho di tiep
        }
        chain.doFilter(request, response);   // da dang nhap -> cho di tiep
    }
}
