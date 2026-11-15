<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.ThongBao" %>
<%@ page import="Utils.Html" %>
<%-- trang sinh vien xem thong bao --%>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<ThongBao> ds = (List<ThongBao>) request.getAttribute("dsThongBao");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<h1 class="trang-tieude">Thông báo</h1>
<p class="trang-mota">Các thông báo gửi đến bạn.</p>
<div class="the">
<% if (ds == null || ds.isEmpty()) { %>
    <span class="trong-rong">Bạn chưa có thông báo nào.</span>
<% } else { for (ThongBao tb : ds) {
       String ngay = (tb.getNgayTao() == null) ? "" : tb.getNgayTao().format(fmt); %>
    <div style="border:1px solid #e2e8f0; border-radius:10px; padding:16px; margin-bottom:14px;">
        <div style="display:flex; justify-content:space-between; gap:12px; flex-wrap:wrap;">
            <strong style="color:#1d4ed8;"><%= Html.escape(tb.getTieuDe()) %>
                <% if (!tb.isDaDoc()) { %><span class="nhan-tt nhan-do" style="margin-left:6px;">Mới</span><% } %>
            </strong>
            <span style="color:#64748b; font-size:13px;"><%= Html.escape(ngay) %></span>
        </div>
        <p style="margin:8px 0 6px; color:#334155;"><%= Html.escape(tb.getNoiDung()) %></p>
        <small style="color:#64748b;">Người gửi: <%= Html.escape(tb.getNguoiGui()) %> · Phạm vi: <%= Html.escape(tb.getPhamVi()) %></small>
    </div>
<% } } %>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
