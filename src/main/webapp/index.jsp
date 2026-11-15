<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- trang goc: chuyen huong toi trang dang nhap (hoac dashboard neu da dang nhap) --%>
<%
    if (session.getAttribute("user") != null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>
