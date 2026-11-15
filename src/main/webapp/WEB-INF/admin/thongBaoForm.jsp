<%@ page import="Models.ThongBao" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    ThongBao thongBao = (ThongBao) request.getAttribute("thongBao");
    String tieuDe = thongBao == null || thongBao.getTieuDe() == null ? "" : thongBao.getTieuDe();
    String noiDung = thongBao == null || thongBao.getNoiDung() == null ? "" : thongBao.getNoiDung();
    String error = (String) request.getAttribute("error");
%>
<h1 class="trang-tieude">Soạn thông báo</h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/thongbao">
        <input type="hidden" name="action" value="luu">
        <div class="nhom-form"><label>Tiêu đề</label><input class="o-nhap" type="text" name="tieuDe" value="<%= Html.escape(tieuDe) %>" required></div>
        <div class="nhom-form"><label>Phạm vi gửi</label>
            <select class="o-nhap" name="phamVi">
                <option value="ToanTruong">Toàn trường</option>
            </select>
            <small>Thông báo được gửi tới mọi tài khoản đang hoạt động.</small>
        </div>
        <div class="nhom-form"><label>Nội dung</label><textarea class="o-nhap" name="noiDung" rows="6" required><%= Html.escape(noiDung) %></textarea></div>
        <button class="nut" type="submit">Gửi thông báo</button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/thongbao">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
