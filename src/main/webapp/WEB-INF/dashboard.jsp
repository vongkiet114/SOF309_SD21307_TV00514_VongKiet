<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.TaiKhoan, java.util.List" %>
<%
    TaiKhoan user = (TaiKhoan) session.getAttribute("user");
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) request.getAttribute("roles");

    String chao = "Xin chao";
    if (roles != null) {
        if (roles.contains("Admin"))            chao = "Xin chao Admin";
        else if (roles.contains("DaoTao"))      chao = "Xin chao Dao tao";
        else if (roles.contains("GiangVien"))   chao = "Xin chao Giang vien";
        else if (roles.contains("SinhVien"))    chao = "Xin chao Sinh vien";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Dashboard - QLSV</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .info { margin: 8px 0; }
        .roles { color: #2563eb; font-weight: 600; }
    </style>
</head>
<body>
    <h1><%= chao %><%= user != null ? ", " + user.getHoTen() : "" %>!</h1>

    <% if (user != null) { %>
        <div class="info">Email: <%= user.getEmail() %></div>
        <div class="info">Ma nguoi dung: <%= user.getMaNguoidung() %></div>
        <div class="info">Vai tro:
            <span class="roles">
                <%= (roles == null || roles.isEmpty()) ? "(chua gan vai tro)" : String.join(", ", roles) %>
            </span>
        </div>
    <% } %>

    <p><a href="<%= request.getContextPath() %>/login">Quay ve trang dang nhap</a></p>
</body>
</html>
