<%@ page import="Models.SinhVien" %>
<%@ page import="Utils.Html" %>
<%@ page import="Utils.TrangThaiTheoNgay" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    SinhVien sv = (SinhVien) request.getAttribute("sv");
    String chuCai = "S";
    if (sv != null && sv.getHoTen() != null && !sv.getHoTen().trim().isEmpty()) {
        chuCai = sv.getHoTen().trim().substring(0, 1).toUpperCase();
    }
%>

<div class="thanh-cong-cu">
    <div>
        <h1 class="trang-tieude">Hồ sơ sinh viên</h1>
        <p class="trang-mota">Thông tin chi tiết phục vụ quản lý sinh viên.</p>
    </div>
    <div>
        <% if (sv != null) { %>
            <a class="nut nut-vien" href="<%= request.getContextPath() %>/admin/sinhvien?action=edit&id=<%= sv.getMaNguoidung() %>">Sửa</a>
        <% } %>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/sinhvien">Quay lại</a>
    </div>
</div>

<% if (sv == null) { %>
    <div class="the"><span class="trong-rong">Không tìm thấy hồ sơ sinh viên.</span></div>
<% } else { %>
    <div class="hoso-sinhvien">
        <div class="hoso-noidung">
            <div class="the">
                <div class="the-tieude">Thông tin cá nhân</div>
                <div class="hoso-luoi">
                    <div class="hoso-dong"><span>Họ tên</span><strong><%= Html.escape(sv.getHoTen()) %></strong></div>
                    <div class="hoso-dong"><span>MSSV</span><strong><%= Html.escape(sv.getMssv()) %></strong></div>
                    <div class="hoso-dong"><span>Giới tính</span><strong><%= Html.escape(sv.getGioiTinh()) %></strong></div>
                    <div class="hoso-dong"><span>Ngày sinh</span><strong><%= Html.escape(sv.getNgaySinh()) %></strong></div>
                    <div class="hoso-dong hoso-rong"><span>Địa chỉ</span><strong><%= Html.escape(sv.getDiaChi()) %></strong></div>
                    <div class="hoso-dong"><span>CCCD</span><strong><%= Html.escape(sv.getCccd()) %></strong></div>
                    <div class="hoso-dong"><span>Dân tộc</span><strong><%= Html.escape(sv.getDanToc()) %></strong></div>
                </div>
            </div>

            <div class="the">
                <div class="the-tieude">Thông tin học tập</div>
                <div class="hoso-luoi">
                    <div class="hoso-dong"><span>Mã tài khoản</span><strong><%= sv.getMaNguoidung() %></strong></div>
                    <div class="hoso-dong"><span>Khóa</span><strong><%= Html.escape(sv.getKhoaHoc()) %></strong></div>
                    <div class="hoso-dong"><span>Chuyên ngành</span><strong><%= Html.escape(sv.getTenChuyenNganh()) %></strong></div>
                    <div class="hoso-dong"><span>Lớp</span><strong><%= Html.escape(sv.getTenLop()) %></strong></div>
                    <div class="hoso-dong"><span>Ngày nhập học</span><strong><%= Html.escape(sv.getNgayNhapHoc()) %></strong></div>
                    <div class="hoso-dong"><span>Trạng thái</span><strong><%= Html.escape(TrangThaiTheoNgay.nhan(sv.getTrangThai())) %></strong></div>
                </div>
            </div>
        </div>

        <aside class="the hoso-anh">
            <% if (sv.getHinhAnh() != null && !sv.getHinhAnh().trim().isEmpty()) { %>
                <img src="<%= request.getContextPath() %>/media/avatar/sinhvien/<%= Html.escape(sv.getHinhAnh()) %>" alt="Hình sinh viên">
            <% } else { %>
                <div class="hoso-avatar"><%= Html.escape(chuCai) %></div>
            <% } %>
            <h2><%= Html.escape(sv.getHoTen()) %></h2>
            <p><%= Html.escape(sv.getEmail()) %></p>
            <span class="nhan-tt nhan-duong"><%= Html.escape(sv.getMssv()) %></span>
        </aside>
    </div>
<% } %>

<%@ include file="/WEB-INF/footer.jsp" %>
