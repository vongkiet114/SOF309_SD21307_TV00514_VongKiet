<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - Lỗi hệ thống</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/admin.css">
</head>
<body style="display:flex;align-items:center;justify-content:center;min-height:100vh;">
    <div class="the" style="text-align:center;max-width:420px;">
        <h1 style="font-size:64px;margin:0;color:#dc2626;">500</h1>
        <p class="trang-mota">Hệ thống gặp sự cố. Vui lòng thử lại sau.</p>
        <a class="nut" href="<%= request.getContextPath() %>/dashboard">Về trang chủ</a>
    </div>
</body>
</html>
