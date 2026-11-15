<%@ page import="Models.GiangVien" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    GiangVien giangVien = (GiangVien) request.getAttribute("giangVien");
    String chuCai = "G";
    if (giangVien != null && giangVien.getHoTen() != null && !giangVien.getHoTen().trim().isEmpty()) {
        chuCai = giangVien.getHoTen().trim().substring(0, 1).toUpperCase();
    }
%>

<div class="thanh-cong-cu">
    <div>
        <h1 class="trang-tieude">Hồ sơ giảng viên</h1>
        <p class="trang-mota">Thông tin chi tiết của giảng viên.</p>
    </div>
    <div>
        <% if (giangVien != null) { %>
            <a class="nut nut-vien" href="<%= request.getContextPath() %>/admin/giangvien?action=edit&id=<%= giangVien.getMaNguoidung() %>">Sửa</a>
        <% } %>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/giangvien">Quay lại</a>
    </div>
</div>

<% if (giangVien == null) { %>
    <div class="the"><span class="trong-rong">Không tìm thấy hồ sơ giảng viên.</span></div>
<% } else { %>
    <div class="hoso-sinhvien">
        <div class="hoso-noidung">
            <div class="the">
                <div class="the-tieude">Thông tin giảng viên</div>
                <div class="hoso-luoi">
                    <div class="hoso-dong"><span>Mã tài khoản</span><strong><%= giangVien.getMaNguoidung() %></strong></div>
                    <div class="hoso-dong"><span>Họ tên</span><strong><%= Html.escape(giangVien.getHoTen()) %></strong></div>
                    <div class="hoso-dong"><span>Email</span><strong><%= Html.escape(giangVien.getEmail()) %></strong></div>
                    <div class="hoso-dong"><span>Giới tính</span><strong><%= Html.escape(giangVien.getGioiTinh()) %></strong></div>
                    <div class="hoso-dong"><span>Ngày sinh</span><strong><%= Html.escape(giangVien.getNgaySinh()) %></strong></div>
                    <div class="hoso-dong"><span>Tỉnh/thành</span><strong><%= Html.escape(giangVien.getDiaChi()) %></strong></div>
                    <div class="hoso-dong"><span>Học vị</span><strong><%= Html.escape(giangVien.getHocVi()) %></strong></div>
                    <div class="hoso-dong"><span>Khoa</span><strong><%= Html.escape(giangVien.getTenKhoa()) %></strong></div>
                    <div class="hoso-dong hoso-rong"><span>Chuyên ngành</span><strong><%= Html.escape(giangVien.getTenChuyenNganh()) %></strong></div>
                </div>
            </div>
        </div>

        <aside class="the hoso-anh">
            <% if (giangVien.getHinhAnh() != null && !giangVien.getHinhAnh().trim().isEmpty()) { %>
                <img src="<%= request.getContextPath() %>/media/avatar/giangvien/<%= Html.escape(giangVien.getHinhAnh()) %>" alt="Hình giảng viên">
            <% } else { %>
                <div class="hoso-avatar"><%= Html.escape(chuCai) %></div>
            <% } %>
            <h2><%= Html.escape(giangVien.getHoTen()) %></h2>
            <p><%= Html.escape(giangVien.getEmail()) %></p>
            <span class="nhan-tt nhan-duong"><%= Utils.MaGiangVien.format(giangVien.getMaNguoidung()) %></span>
        </aside>
    </div>
<% } %>

<%@ include file="/WEB-INF/footer.jsp" %>
