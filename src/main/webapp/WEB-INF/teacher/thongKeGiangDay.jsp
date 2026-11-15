<%@ page import="Models.ThongKeGiangDay" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    ThongKeGiangDay tk = (ThongKeGiangDay) request.getAttribute("thongKe");
    if (tk == null) tk = new ThongKeGiangDay(0, 0, 0);
%>
<h1 class="trang-tieude">Thống kê giảng dạy</h1>
<p class="trang-mota">Tổng hợp khối lượng giảng dạy của bạn (tính đến hôm nay).</p>

<div class="luoi-thongke">
    <div class="the-thongke">
        <p>Tổng số ca đã dạy</p>
        <h2><%= tk.getSoCa() %></h2>
        <span class="nhan" style="color:#2563eb">ca học</span>
    </div>
    <div class="the-thongke">
        <p>Tổng số buổi đã dạy</p>
        <h2><%= tk.getSoBuoi() %></h2>
        <span class="nhan" style="color:#16a34a">ngày lên lớp</span>
    </div>
    <div class="the-thongke">
        <p>Tổng số giờ giảng</p>
        <h2><%= Html.escape(String.valueOf(tk.getSoGio())) %></h2>
        <span class="nhan" style="color:#f59e0b">giờ</span>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
