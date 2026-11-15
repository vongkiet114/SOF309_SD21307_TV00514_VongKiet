<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.NhatKyHoatDong" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<NhatKyHoatDong> ds = (List<NhatKyHoatDong>) request.getAttribute("dsNhatKy");
    DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Nhật ký hoạt động</h1><p class="trang-mota">Các thao tác gần đây trong hệ thống (chỉ xem).</p></div>
</div>
<div class="the">
    <div class="bang-cuon">
        <table>
            <thead><tr><th>Thời gian</th><th>Người dùng</th><th>Hành động</th><th>Đối tượng</th><th>IP</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="5"><span class="trong-rong">Chưa có hoạt động nào được ghi nhận.</span></td></tr>
            <% } else { for (NhatKyHoatDong nk : ds) {
                   String ip = nk.getIpAddress(); %>
                <tr>
                    <td><%= nk.getThoiGian() != null ? nk.getThoiGian().format(dinhDangNgay) : "" %></td>
                    <td><strong><%= Html.escape(nk.getHoTenNguoiDung() != null ? nk.getHoTenNguoiDung() : "") %></strong> <span style="color:#64748b; font-size:12px;">(#<%= nk.getMaNguoidung() %>)</span></td>
                    <td><span class="nhan-tt nhan-xam"><%= Html.escape(nk.getHanhDong()) %></span></td>
                    <td><%= Html.escape(nk.getDoiTuongHienThi()) %></td>
                    <td><%= (ip == null || ip.isEmpty()) ? "<span style=\"color:#94a3b8; font-style:italic;\">—</span>" : Html.escape(ip) %></td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
