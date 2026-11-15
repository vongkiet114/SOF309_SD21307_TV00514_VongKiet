<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%
    // lay throwable that su (uu tien implicit object, neu null thi lay tu request attribute do container set)
    Throwable loi = exception;
    if (loi == null) {
        Object o = request.getAttribute("jakarta.servlet.error.exception");
        if (o instanceof Throwable) loi = (Throwable) o;
    }
    // dao toi nguyen nhan goc cua loi (root cause) de hien thi cho de chan doan
    Throwable goc = loi;
    while (goc != null && goc.getCause() != null && goc.getCause() != goc) {
        goc = goc.getCause();
    }
    String thongDiep = (goc != null) ? (goc.getClass().getName() + ": " + goc.getMessage()) : "(khong co thong tin)";
    String stack = "";
    if (loi != null) {
        StringWriter sw = new StringWriter();
        loi.printStackTrace(new PrintWriter(sw));
        stack = sw.toString();
    }
    Object uri = request.getAttribute("jakarta.servlet.error.request_uri");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đã xảy ra lỗi</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/admin.css">
</head>
<body style="padding:32px;">
    <div class="the" style="max-width:900px;margin:0 auto;">
        <h1 style="font-size:42px;margin:0;color:#dc2626;">Oops!</h1>
        <p class="trang-mota">Đã xảy ra lỗi không mong muốn trong quá trình xử lý.</p>
        <% if (uri != null) { %><p style="color:#64748b;margin:4px 0;">Trang: <code><%= uri %></code></p><% } %>

        <div class="bao-loi" style="text-align:left;white-space:pre-wrap;font-family:monospace;font-size:13px;">
<%= thongDiep %>
        </div>

        <% if (!stack.isEmpty()) { %>
        <details style="margin-top:12px;text-align:left;">
            <summary style="cursor:pointer;font-weight:600;">Chi tiết kỹ thuật (stack trace)</summary>
            <pre style="overflow:auto;max-height:380px;background:#0f172a;color:#e2e8f0;padding:14px;border-radius:8px;font-size:12px;"><%
                for (int i = 0; i < stack.length(); i++) {
                    char c = stack.charAt(i);
                    if (c == '<') out.print("&lt;");
                    else if (c == '>') out.print("&gt;");
                    else if (c == '&') out.print("&amp;");
                    else out.print(c);
                }
            %></pre>
        </details>
        <% } %>

        <a class="nut" href="<%= request.getContextPath() %>/dashboard" style="margin-top:14px;">Về trang chủ</a>
    </div>
</body>
</html>
