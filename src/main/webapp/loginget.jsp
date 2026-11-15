<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Utils.Html" %>
<%
    String error = request.getParameter("error");
    String errorMessage = null;
    if (error != null) {
        errorMessage = "Email hoặc mật khẩu không đúng.";
    }
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Hệ thống Quản lý Sinh viên</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/login.css?v=2">
</head>
<body>

<div class="khung-dangnhap">
    <div class="panel-trai">
        <div class="gioi-thieu">
            <h1>Hệ thống Quản lý<br>Đào tạo &amp; Sinh viên</h1>
            <p>Cổng đăng nhập dành cho Giảng viên, Sinh viên và Quản trị viên.</p>
        </div>
    </div>

    <div class="panel-phai">
        <h2>Xin chào!</h2>
        <p class="phu-de">Vui lòng đăng nhập tài khoản được cấp để vào hệ thống.</p>

        <% if (errorMessage != null) { %>
            <div class="bao-loi">
                <%= errorMessage %>
            </div>
        <% } %>

        <form method="post" action="<%= contextPath %>/login">
            <input type="hidden" name="_csrf" value="${requestScope.csrfToken}">
            <div class="nhom-nhap">
                <label for="email">Tên đăng nhập hoặc Email</label>
                <input type="text" id="email" name="email" required autofocus placeholder="vd: admin@fpt.edu.vn">
            </div>

            <div class="nhom-nhap">
                <label for="matKhau">Mật khẩu</label>
                <input type="password" id="matKhau" name="matKhau" required placeholder="Nhập mật khẩu">
            </div>

            <div class="hang-phu">
                <label>
                    <input type="checkbox" name="ghiNho" value="1"> Ghi nhớ đăng nhập
                </label>
                <a href="#">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="nut-dangnhap">Đăng nhập vào hệ thống</button>
        </form>
    </div>
</div>

</body>
</html>
