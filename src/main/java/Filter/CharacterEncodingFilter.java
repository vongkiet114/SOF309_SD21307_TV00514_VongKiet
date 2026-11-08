package Filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

// bo loc dat ma hoa UTF-8 cho moi request va response.
// muc dich: tranh loi hien thi tieng Viet bi vo (vd "Ä‘Äƒng nháº­p") khi nhan du lieu form POST
// va khi xuat noi dung ra trinh duyet.
// dat urlPatterns "/*" de ap dung cho TAT CA request, va chay truoc cac filter/servlet khac.
@WebFilter(urlPatterns = {"/*"})
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // ep ma hoa du lieu nhan vao (form POST) la UTF-8
        request.setCharacterEncoding("UTF-8");
        // ep ma hoa noi dung tra ve la UTF-8
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
