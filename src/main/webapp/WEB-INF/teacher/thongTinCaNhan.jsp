<%@ page import="Models.GiangVien" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%!
    String hienThi(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return "Chưa cập nhật";
        }
        return String.valueOf(value);
    }
%>
<%
    GiangVien gv = (GiangVien) request.getAttribute("thongTin");
%>
<h1 class="trang-tieude">Thông tin cá nhân</h1>
<p class="trang-mota">Hồ sơ giảng viên của bạn.</p>
<div class="the">
<% if (gv == null) { %>
    <span class="trong-rong">Thông tin cá nhân chưa được cập nhật.</span>
<% } else { %>
    <div style="display:flex; gap:24px; align-items:flex-start; flex-wrap:wrap;">
        <div class="avatar-tron" style="width:72px;height:72px;font-size:30px;border-radius:12px;overflow:hidden;padding:0;">
            <% if (gv.getHinhAnh() != null && !gv.getHinhAnh().isEmpty()) { %>
                <img src="<%= request.getContextPath() %>/media/avatar/giangvien/<%= Html.escape(gv.getHinhAnh()) %>" style="width:100%; height:100%; object-fit:cover; display:block;">
            <% } else { %>
                <%= Html.escape(hienThi(gv.getHoTen()).substring(0, 1).toUpperCase()) %>
            <% } %>
        </div>
        <div style="flex:1; min-width:240px;">
            <h2 style="margin:0 0 4px;"><%= Html.escape(hienThi(gv.getHoTen())) %></h2>
            <table data-no-paginate>
                <tr><th style="width:200px;">Email</th><td><%= Html.escape(hienThi(gv.getEmail())) %></td></tr>
                <tr><th>Học vị</th><td><%= Html.escape(hienThi(gv.getHocVi())) %></td></tr>
                <tr><th>Khoa</th><td><%= Html.escape(hienThi(gv.getTenKhoa())) %></td></tr>
                <tr><th>Chuyên ngành</th><td><%= Html.escape(hienThi(gv.getTenChuyenNganh())) %></td></tr>
                <tr><th>Giới tính</th><td><%= Html.escape(hienThi(gv.getGioiTinh())) %></td></tr>
                <tr><th>Ngày sinh</th><td><%= Html.escape(hienThi(gv.getNgaySinh())) %></td></tr>
                <tr><th>Địa chỉ</th><td><%= Html.escape(hienThi(gv.getDiaChi())) %></td></tr>
            </table>
        </div>
    </div>
<% } %>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
