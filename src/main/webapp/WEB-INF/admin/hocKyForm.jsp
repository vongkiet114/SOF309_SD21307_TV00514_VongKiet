<%@ page import="Models.HocKy" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    HocKy hk = (HocKy) request.getAttribute("hocKy");
    String error = (String) request.getAttribute("error");
    boolean laSua = hk != null && hk.getMaHocky() > 0;

    String vMa = laSua ? String.valueOf(hk.getMaHocky()) : "";
    String vTen = hk == null ? "" : Html.escape(hk.getTenHocky());
    int vBlock = hk == null || hk.getBlock() == 0 ? 1 : hk.getBlock();
    String vNam = hk == null ? "" : Html.escape(hk.getNamHoc());
    String vBd = (hk != null && hk.getNgayBatdau() != null) ? hk.getNgayBatdau().toString() : "";
    String vKt = (hk != null && hk.getNgayKetthuc() != null) ? hk.getNgayKetthuc().toString() : "";
%>
<h1 class="trang-tieude"><%= laSua ? "Sửa học kỳ" : "Thêm học kỳ" %></h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/hocky">
        <input type="hidden" name="action" value="luu">
        <input type="hidden" name="maHocky" value="<%= vMa %>">
        <div class="nhom-form"><label>Tên học kỳ</label><input class="o-nhap" type="text" name="tenHocky" value="<%= vTen %>" maxlength="50" placeholder="Ví dụ: Học kỳ 1" required></div>
        <div class="nhom-form"><label>Block</label>
            <select class="o-nhap" name="block" required>
                <option value="1" <%= vBlock == 1 ? "selected" : "" %>>Block 1</option>
                <option value="2" <%= vBlock == 2 ? "selected" : "" %>>Block 2</option>
            </select></div>
        <div class="nhom-form"><label>Năm học</label><input class="o-nhap" type="text" name="namHoc" value="<%= vNam %>" maxlength="20" placeholder="Ví dụ: 2025-2026" required></div>
        <div class="nhom-form"><label>Ngày bắt đầu</label><input class="o-nhap flatpickr" type="text" name="ngayBatdau" value="<%= vBd %>" required placeholder="Chọn ngày bắt đầu..."></div>
        <div class="nhom-form"><label>Ngày kết thúc</label><input class="o-nhap flatpickr" type="text" name="ngayKetthuc" value="<%= vKt %>" required placeholder="Chọn ngày kết thúc..."></div>
        <small style="color:#6b7280; display:block; margin-bottom:15px;">Trạng thái được tự động xác định theo khoảng ngày.</small>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm" %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/hocky">Hủy</a>
    </form>
</div>

<!-- Flatpickr for better date picking UI -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
<script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        flatpickr(".flatpickr", {
            dateFormat: "Y-m-d",
            minDate: "today",
            allowInput: true
        });
    });
</script>
<%@ include file="/WEB-INF/footer.jsp" %>
