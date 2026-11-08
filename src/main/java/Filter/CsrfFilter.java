package Filter;

import Utils.CsrfToken;
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
import java.util.Locale;

@WebFilter(filterName = "CsrfFilter", urlPatterns = {"/*"})
public class CsrfFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)
                || !(servletResponse instanceof HttpServletResponse)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (isStaticAsset(request.getRequestURI(), request.getContextPath())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        String token = session == null
                ? null
                : (String) session.getAttribute(CsrfToken.SESSION_ATTRIBUTE);
        if (token == null) {
            session = request.getSession(true);
            token = CsrfToken.generate();
            session.setAttribute(CsrfToken.SESSION_ATTRIBUTE, token);
        }
        request.setAttribute("csrfToken", token);

        if (requiresValidation(request.getMethod())
                && !CsrfToken.matches(token, request.getParameter("_csrf"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ");
            return;
        }

        chain.doFilter(request, response);
    }

    static boolean requiresValidation(String method) {
        if (method == null) {
            return false;
        }
        switch (method.toUpperCase(Locale.ROOT)) {
            case "POST":
            case "PUT":
            case "PATCH":
            case "DELETE":
                return true;
            default:
                return false;
        }
    }

    static boolean isStaticAsset(String requestUri, String contextPath) {
        if (requestUri == null) {
            return false;
        }
        String context = contextPath == null ? "" : contextPath;
        return requestUri.startsWith(context + "/assets/");
    }
}
