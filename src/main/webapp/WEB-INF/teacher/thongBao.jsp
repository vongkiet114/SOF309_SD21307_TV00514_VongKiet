<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.ThongBao" %>
<%@ page import="Utils.Html" %>
<%-- trang thong bao gui den giang vien --%>
<%
    request.setAttribute("trang", "thongbao_gv");
    request.setAttribute("tieuDeTrang", "Thông báo");
%>
<%@ include file="/WEB-INF/header.jsp" %>

<%
    @SuppressWarnings("unchecked")
    List<ThongBao> dsThongBao = (List<ThongBao>) request.getAttribute("dsThongBao");
    // dinh dang ngay gio kieu viet nam
    DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<h1 class="trang-tieude">Thông báo</h1>
<p class="trang-mota">Các thông báo gửi đến bạn từ nhà trường / khoa.</p>

<div class="the">
<%
    if (dsThongBao == null || dsThongBao.isEmpty()) {
%>
    <span class="trong-rong">Bạn chưa có thông báo nào.</span>
<%
    } else {
        for (ThongBao tb : dsThongBao) {
            String ngay = (tb.getNgayTao() == null) ? "" : tb.getNgayTao().format(dinhDangNgay);
%>
    <div style="border:1px solid #e2e8f0; border-radius:10px; padding:16px; margin-bottom:14px;">
        <div style="display:flex; justify-content:space-between; gap:12px; flex-wrap:wrap;">
            <strong style="color:#1d4ed8;">
                <%= Html.escape(tb.getTieuDe()) %>
                <% if (!tb.isDaDoc()) { %><span class="nhan-tt nhan-do" style="margin-left:6px;">Mới</span><% } %>
            </strong>
            <span style="color:#64748b; font-size:13px;"><%= Html.escape(ngay) %></span>
        </div>
        <p style="margin:8px 0 6px; color:#334155;"><%= Html.escape(tb.getNoiDung()) %></p>
        <small style="color:#64748b;">Người gửi: <%= Html.escape(tb.getNguoiGui()) %> · Phạm vi: <%= Html.escape(tb.getPhamVi()) %></small>
    </div>
<%
        }
    }
%>
</div>

<%@ include file="/WEB-INF/footer.jsp" %>
