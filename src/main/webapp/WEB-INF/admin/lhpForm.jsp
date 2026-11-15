<%@ page import="java.util.List" %>
<%@ page import="Models.LopHocPhan" %>
<%@ page import="Models.MonHoc" %>
<%@ page import="Models.GiangVien" %>
<%@ page import="Models.HocKy" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    LopHocPhan lhp = (LopHocPhan) request.getAttribute("lhp");
    String error = (String) request.getAttribute("error");
    Object editAttribute = request.getAttribute("isEdit");
    boolean isEdit = editAttribute instanceof Boolean
            ? (Boolean) editAttribute
            : lhp != null && lhp.getMaLophocphan() > 0;
    @SuppressWarnings("unchecked")
    List<MonHoc> dsMon = (List<MonHoc>) request.getAttribute("dsMonHoc");
    @SuppressWarnings("unchecked")
    List<GiangVien> dsGv = (List<GiangVien>) request.getAttribute("dsGiangVien");
    @SuppressWarnings("unchecked")
    List<HocKy> dsHk = (List<HocKy>) request.getAttribute("dsHocKy");

    String action = isEdit ? "update" : "create";
    String submitText = isEdit ? "Cập nhật" : "Thêm mới";
%>
<h1 class="trang-tieude"><%= submitText %> Lớp học phần</h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>

<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/lophocphan">
        <input type="hidden" name="action" value="<%= action %>">
        <% if (isEdit) { %>
            <input type="hidden" name="id" value="<%= lhp.getMaLophocphan() %>">
        <% } %>

        <div class="nhom-form">
            <label>Mã lớp học phần</label>
            <input class="o-nhap" type="text"
                   value="<%= isEdit ? Html.escape(lhp.getMaLhp()) : "Hệ thống tự tạo khi lưu" %>" readonly>
        </div>

        <div class="nhom-form">
            <label>Môn học <%= !isEdit ? "(có thể chọn nhiều)" : "" %></label>
            <% if (isEdit) { %>
            <select class="o-nhap" name="maMonhoc" required>
                <option value="">-- Chọn môn học --</option>
                <% if (dsMon != null) for (MonHoc m : dsMon) {
                       boolean selected = lhp != null && m.getMaMonhoc() == lhp.getMaMonhoc(); %>
                    <option value="<%= m.getMaMonhoc() %>" <%= selected ? "selected" : "" %>><%= Html.escape(m.getMaMon()) %> — <%= Html.escape(m.getTenMonhoc()) %></option>
                <% } %>
            </select>
            <% } else { %>
            <div style="max-height: 200px; overflow-y: auto; border: 1px solid var(--border-color); padding: 10px; border-radius: var(--border-radius); background: var(--surface-color);">
                <% if (dsMon != null) for (MonHoc m : dsMon) { %>
                    <label style="display: block; margin-bottom: 6px; cursor: pointer;">
                        <input type="checkbox" name="maMonhoc" value="<%= m.getMaMonhoc() %>" style="margin-right: 8px;"> 
                        <strong><%= Html.escape(m.getMaMon()) %></strong> — <%= Html.escape(m.getTenMonhoc()) %>
                    </label>
                <% } %>
            </div>
            <script>
                document.querySelector('form').addEventListener('submit', function(e) {
                    if (!document.querySelector('input[name="maMonhoc"]:checked')) {
                        e.preventDefault();
                        alert('Vui lòng chọn ít nhất một môn học.');
                    }
                });
            </script>
            <% } %>
        </div>
        <div class="nhom-form">
            <label>Giảng viên</label>
            <select class="o-nhap" name="maGiangvien" required>
                <option value="">-- Chọn giảng viên --</option>
                <% if (dsGv != null) for (GiangVien gv : dsGv) {
                       boolean selected = lhp != null && gv.getMaNguoidung() == lhp.getMaGiangvien(); %>
                    <option value="<%= gv.getMaNguoidung() %>" <%= selected ? "selected" : "" %>><%= Html.escape(gv.getHoTen()) %> — <%= Html.escape(gv.getTenChuyenNganh()) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form">
            <label>Học kỳ</label>
            <select class="o-nhap" name="maHocky" required>
                <option value="">-- Chọn học kỳ --</option>
                <% if (dsHk != null) for (HocKy hk : dsHk) {
                       boolean selected = lhp != null && hk.getMaHocky() == lhp.getMaHocky(); %>
                    <option value="<%= hk.getMaHocky() %>" <%= selected ? "selected" : "" %>><%= Html.escape(hk.getTenHocky()) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form">
            <label>Trạng thái</label>
            <select class="o-nhap" name="trangThai">
                <% String currentStatus = lhp != null && lhp.getTrangThai() != null
                        ? lhp.getTrangThai() : "MoLop"; %>
                <option value="MoLop" <%= "MoLop".equals(currentStatus) ? "selected" : "" %>>Mở lớp</option>
                <option value="DangHoc" <%= "DangHoc".equals(currentStatus) ? "selected" : "" %>>Đang học</option>
                <option value="HoanThanh" <%= "HoanThanh".equals(currentStatus) ? "selected" : "" %>>Hoàn thành</option>
                <option value="Huy" <%= "Huy".equals(currentStatus) ? "selected" : "" %>>Đã hủy</option>
            </select>
        </div>

        <button class="nut" type="submit"><%= submitText %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/lophocphan">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
