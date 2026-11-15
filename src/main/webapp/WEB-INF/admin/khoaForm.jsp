<%@ page import="Models.Khoa" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    Khoa k = (Khoa) request.getAttribute("khoa");
    String error = (String) request.getAttribute("error");
    boolean laSua = k != null && k.getMaKhoa() > 0;
    String vMa = laSua ? String.valueOf(k.getMaKhoa()) : "";
    String vTen = k == null ? "" : Html.escape(k.getTenKhoa());
%>
<h1 class="trang-tieude"><%= laSua ? "Sửa khoa" : "Thêm khoa" %></h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/khoa">
        <input type="hidden" name="action" value="luu">
        <input type="hidden" name="id" value="<%= vMa %>">
        <div class="nhom-form"><label>Tên khoa</label><input class="o-nhap" type="text" name="tenKhoa" value="<%= vTen %>" maxlength="100" required></div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm" %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/khoa">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
