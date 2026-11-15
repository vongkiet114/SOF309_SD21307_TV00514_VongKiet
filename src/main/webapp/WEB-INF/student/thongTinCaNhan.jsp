<%@ page import="Models.SinhVien" %>
<%@ page import="Utils.Html" %>
<%@ page import="Utils.TrangThaiTheoNgay" %>
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
    SinhVien sv = (SinhVien) request.getAttribute("thongTin");
%>
<h1 class="trang-tieude">Thông tin cá nhân</h1>
<p class="trang-mota">Hồ sơ sinh viên của bạn.</p>
<div class="the">
<% if (sv == null) { %>
    <span class="trong-rong">Thông tin cá nhân chưa được cập nhật.</span>
<% } else { %>
    <div style="display:flex; gap:24px; align-items:flex-start; flex-wrap:wrap;">
        <div class="avatar-tron" style="width:72px;height:72px;font-size:30px;border-radius:12px;overflow:hidden;padding:0;">
            <% if (sv.getHinhAnh() != null && !sv.getHinhAnh().isEmpty()) { %>
                <img src="<%= request.getContextPath() %>/media/avatar/sinhvien/<%= Html.escape(sv.getHinhAnh()) %>" style="width: 100%; height: 100%; object-fit: cover; display: block;">
            <% } else { %>
                <%= Html.escape(hienThi(sv.getHoTen()).substring(0, 1).toUpperCase()) %>
            <% } %>
        </div>
        <div style="flex:1; min-width:240px;">
            <h2 style="margin:0 0 4px;"><%= Html.escape(hienThi(sv.getHoTen())) %></h2>
            <table data-no-paginate>
                <tr><th style="width:200px;">Mã số sinh viên</th><td><%= Html.escape(hienThi(sv.getMssv())) %></td></tr>
                <tr><th>Email</th><td><%= Html.escape(hienThi(sv.getEmail())) %></td></tr>
                <tr><th>Lớp</th><td><%= Html.escape(hienThi(sv.getTenLop())) %></td></tr>
                <tr><th>Chuyên ngành</th><td><%= Html.escape(hienThi(sv.getTenChuyenNganh())) %></td></tr>
                <tr><th>Giới tính</th><td><%= Html.escape(hienThi(sv.getGioiTinh())) %></td></tr>
                <tr><th>Ngày sinh</th><td><%= Html.escape(hienThi(sv.getNgaySinh())) %></td></tr>
                <tr><th>Địa chỉ</th><td><%= Html.escape(hienThi(sv.getDiaChi())) %></td></tr>
                <tr><th>Căn cước công dân</th><td><%= Html.escape(hienThi(sv.getCccd())) %></td></tr>
                <tr><th>Dân tộc</th><td><%= Html.escape(hienThi(sv.getDanToc())) %></td></tr>
                <tr><th>Khóa học</th><td><%= Html.escape(hienThi(sv.getKhoaHoc())) %></td></tr>
                <tr><th>Ngày nhập học</th><td><%= Html.escape(hienThi(sv.getNgayNhapHoc())) %></td></tr>
                <tr><th>Trạng thái học tập</th><td><%= Html.escape(hienThi(TrangThaiTheoNgay.nhan(sv.getTrangThai()))) %></td></tr>
            </table>
        </div>
    </div>
<% } %>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
