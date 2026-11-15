<%@ page import="java.util.List" %>
<%@ page import="Models.LopHoc" %>
<%@ page import="Models.SinhVien" %>
<%@ page import="Utils.Html" %>
<%@ page import="Utils.DanToc" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    SinhVien sv = (SinhVien) request.getAttribute("sv");
    boolean isEdit = (sv != null);
    @SuppressWarnings("unchecked")
    List<LopHoc> dsLop = (List<LopHoc>) request.getAttribute("dsLop");

    String action = isEdit ? "update" : "create";
    String submitText = isEdit ? "Cập nhật" : "Thêm mới";
    String currentGioiTinh = isEdit && sv.getGioiTinh() != null ? sv.getGioiTinh() : "Nam";
%>
<h1 class="trang-tieude"><%= submitText %> Sinh viên</h1>

<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/sinhvien" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<%= action %>">
        <% if (isEdit) { %>
            <input type="hidden" name="id" value="<%= sv.getMaNguoidung() %>">
        <% } %>

        <div class="nhom-form">
            <label>Họ tên <span class="bat-buoc">*</span></label>
            <input class="o-nhap" type="text" name="hoTen" value="<%= isEdit ? Html.escape(sv.getHoTen()) : "" %>" required>
        </div>
        <div class="nhom-form">
            <label>Email <span class="bat-buoc">*</span> <%= isEdit ? "" : "(dùng để đăng nhập, mật khẩu mặc định '123456')" %></label>
            <input class="o-nhap" type="email" name="email" value="<%= isEdit ? Html.escape(sv.getEmail()) : "" %>" <%= isEdit ? "readonly" : "required" %>>
        </div>
        <div class="nhom-form">
            <label>Mã số sinh viên</label>
            <% if (isEdit) { %>
                <input class="o-nhap" type="text" value="<%= Html.escape(sv.getMssv()) %>" readonly title="Mã sinh viên không được phép chỉnh sửa sau khi tạo">
            <% } else { %>
                <input class="o-nhap" type="text" value="Hệ thống tự sinh mã (SV001, SV002, ...)" readonly>
            <% } %>
        </div>
        <div class="nhom-form">
            <label>Giới tính</label>
            <select class="o-nhap" name="gioiTinh">
                <option value="Nam" <%= "Nam".equals(currentGioiTinh) ? "selected" : "" %>>Nam</option>
                <option value="Nữ" <%= "Nữ".equals(currentGioiTinh) ? "selected" : "" %>>Nữ</option>
                <option value="Khác" <%= "Khác".equals(currentGioiTinh) ? "selected" : "" %>>Khác</option>
            </select>
        </div>
        <div class="nhom-form">
            <label>Ngày sinh <span class="bat-buoc">*</span></label>
            <input class="o-nhap" type="date" name="ngaySinh" value="<%= isEdit ? Html.escape(sv.getNgaySinh()) : "" %>" required>
            <small class="ghi-chu-form">Sinh viên phải từ đủ 16 tuổi trở lên.</small>
        </div>
        <div class="nhom-form">
            <label>Địa chỉ</label>
            <input class="o-nhap" type="text" name="diaChi" value="<%= isEdit ? Html.escape(sv.getDiaChi()) : "" %>">
        </div>
        <div class="nhom-form">
            <label>CCCD</label>
            <input class="o-nhap" type="text" name="cccd" value="<%= isEdit ? Html.escape(sv.getCccd()) : "" %>">
        </div>
        <div class="nhom-form">
            <label>Dân tộc</label>
            <% String currentDanToc = isEdit && sv.getDanToc() != null && !sv.getDanToc().isEmpty()
                       ? sv.getDanToc() : "Kinh"; %>
            <select class="o-nhap" name="danToc">
                <% if (!DanToc.DANH_SACH.contains(currentDanToc)) { %>
                    <option value="<%= Html.escape(currentDanToc) %>" selected><%= Html.escape(currentDanToc) %></option>
                <% } %>
                <% for (String dt : DanToc.DANH_SACH) { %>
                    <option value="<%= Html.escape(dt) %>" <%= dt.equals(currentDanToc) ? "selected" : "" %>><%= Html.escape(dt) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form">
            <label>Khóa</label>
            <input class="o-nhap" type="text" name="khoaHoc" value="<%= isEdit ? Html.escape(sv.getKhoaHoc()) : "K20" %>">
        </div>
        <div class="nhom-form">
            <label>Ngày nhập học</label>
            <input class="o-nhap" type="date" name="ngayNhapHoc" value="<%= isEdit ? Html.escape(sv.getNgayNhapHoc()) : "" %>">
        </div>
        <div class="nhom-form">
            <label>Hình ảnh đại diện</label>
            <% if (isEdit && sv.getHinhAnh() != null && !sv.getHinhAnh().isEmpty()) { %>
                <img src="<%= request.getContextPath() %>/media/avatar/sinhvien/<%= Html.escape(sv.getHinhAnh()) %>" style="width: 100px; height: 100px; object-fit: cover; border-radius: 8px; margin-bottom: 10px; display: block;">
            <% } %>
            <input class="o-nhap" type="file" name="hinhAnhFile" accept=".jpg,.jpeg,.png,.gif">
        </div>
        <div class="nhom-form">
            <label>Lớp <span class="bat-buoc">*</span></label>
            <select class="o-nhap" name="maLop" required>
                <option value="">-- Chọn lớp --</option>
                <% if (dsLop != null) for (LopHoc l : dsLop) {
                       boolean selected = isEdit && l.getMaLop() == sv.getMaLop(); %>
                    <option value="<%= l.getMaLop() %>" <%= selected ? "selected" : "" %>><%= Html.escape(l.getMaLopHienThi()) %> — <%= Html.escape(l.getTenLop()) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form">
            <label>Trạng thái sinh viên</label>
            <select class="o-nhap" name="trangThai">
                <% String currentStatus = isEdit ? sv.getTrangThai() : "DangHoc"; %>
                <option value="DangHoc" <%= "DangHoc".equals(currentStatus) ? "selected" : "" %>>Đang học</option>
                <option value="BaoLuu" <%= "BaoLuu".equals(currentStatus) ? "selected" : "" %>>Bảo lưu</option>
                <option value="NghiHoc" <%= "NghiHoc".equals(currentStatus) ? "selected" : "" %>>Nghỉ học</option>
                <option value="TotNghiep" <%= "TotNghiep".equals(currentStatus) ? "selected" : "" %>>Tốt nghiệp</option>
            </select>
        </div>

        <button class="nut" type="submit"><%= submitText %></button>
        <% if (isEdit) { %>
            <a class="nut nut-vien" href="<%= request.getContextPath() %>/admin/sinhvien?action=detail&id=<%= sv.getMaNguoidung() %>">Xem thêm</a>
        <% } %>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/sinhvien">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
