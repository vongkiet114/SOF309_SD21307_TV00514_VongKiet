<%@ page import="java.util.List" %>
<%@ page import="Models.ChuyenNganh" %>
<%@ page import="Models.GiangVien" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    GiangVien giangVien = (GiangVien) request.getAttribute("giangVien");
    boolean isEdit = giangVien != null;
    @SuppressWarnings("unchecked")
    List<ChuyenNganh> dsChuyenNganh =
            (List<ChuyenNganh>) request.getAttribute("dsChuyenNganh");
    String action = isEdit ? "update" : "create";
    String currentGioiTinh = isEdit && giangVien.getGioiTinh() != null ? giangVien.getGioiTinh() : "Nam";
%>
<h1 class="trang-tieude"><%= isEdit ? "Cập nhật giảng viên" : "Thêm giảng viên" %></h1>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/giangvien" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<%= action %>">
        <% if (isEdit) { %>
            <input type="hidden" name="id" value="<%= giangVien.getMaNguoidung() %>">
        <% } %>

        <% if (isEdit) { %>
        <div class="nhom-form">
            <label>Mã giảng viên</label>
            <input class="o-nhap" type="text"
                   value="<%= Utils.MaGiangVien.format(giangVien.getMaNguoidung()) %>" readonly
                   title="Mã giảng viên không được phép chỉnh sửa sau khi tạo">
        </div>
        <% } else { %>
        <div class="nhom-form">
            <label>Mã giảng viên</label>
            <input class="o-nhap" type="text" value="Hệ thống tự sinh mã (GV001, GV002, ...)" readonly>
        </div>
        <% } %>
        <div class="nhom-form">
            <label>Họ tên <span class="bat-buoc">*</span></label>
            <input class="o-nhap" type="text" name="hoTen"
                   value="<%= isEdit ? Html.escape(giangVien.getHoTen()) : "" %>" required>
        </div>
        <div class="nhom-form">
            <label>Email <span class="bat-buoc">*</span> <%= isEdit ? "" : "(dùng để đăng nhập, mật khẩu mặc định '123456')" %></label>
            <input class="o-nhap" type="email" name="email"
                   value="<%= isEdit ? Html.escape(giangVien.getEmail()) : "" %>"
                   <%= isEdit ? "readonly" : "required" %>>
        </div>
        <div class="nhom-form">
            <label>Chuyên ngành</label>
            <select class="o-nhap" name="maChuyenNganh" required>
                <option value="">-- Chọn chuyên ngành --</option>
                <% if (dsChuyenNganh != null) for (ChuyenNganh chuyenNganh : dsChuyenNganh) {
                       boolean selected = isEdit
                               && chuyenNganh.getMaChuyenNganh() == giangVien.getMaChuyenNganh(); %>
                    <option value="<%= chuyenNganh.getMaChuyenNganh() %>"
                            <%= selected ? "selected" : "" %>>
                        <%= Html.escape(chuyenNganh.getTenChuyenNganh()) %>
                        (<%= Html.escape(chuyenNganh.getMaCn()) %>)
                    </option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form">
            <label>Học vị</label>
            <select class="o-nhap" name="hocVi">
                <option value="">-- Chọn học vị --</option>
                <%
                    String currentHocVi = isEdit && giangVien.getHocVi() != null ? giangVien.getHocVi() : "";
                    String[] dsHocVi = {"Cử nhân", "Thạc sĩ", "Tiến sĩ", "Khác"};
                    for (String hv : dsHocVi) {
                %>
                <option value="<%= hv %>" <%= currentHocVi.equals(hv) ? "selected" : "" %>><%= hv %></option>
                <% } %>
            </select>
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
            <label>Ngày sinh</label>
            <input class="o-nhap" type="date" name="ngaySinh"
                   value="<%= isEdit ? Html.escape(giangVien.getNgaySinh()) : "" %>">
        </div>
        <div class="nhom-form">
            <label>Tỉnh/thành</label>
            <input class="o-nhap" type="text" name="diaChi"
                   value="<%= isEdit ? Html.escape(giangVien.getDiaChi()) : "" %>">
        </div>
        <div class="nhom-form">
            <label>Hình ảnh đại diện</label>
            <% if (isEdit && giangVien.getHinhAnh() != null && !giangVien.getHinhAnh().isEmpty()) { %>
                <img src="<%= request.getContextPath() %>/media/avatar/giangvien/<%= Html.escape(giangVien.getHinhAnh()) %>" style="width: 100px; height: 100px; object-fit: cover; border-radius: 8px; margin-bottom: 10px; display: block;">
            <% } %>
            <input class="o-nhap" type="file" name="hinhAnhFile" accept=".jpg,.jpeg,.png,.gif">
        </div>

        <button class="nut" type="submit"><%= isEdit ? "Cập nhật" : "Thêm" %></button>
        <% if (isEdit) { %>
            <a class="nut nut-vien" href="<%= request.getContextPath() %>/admin/giangvien?action=detail&id=<%= giangVien.getMaNguoidung() %>">Xem thêm</a>
        <% } %>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/giangvien">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
