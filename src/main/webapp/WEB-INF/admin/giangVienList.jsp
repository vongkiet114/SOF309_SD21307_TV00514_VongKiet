<%@ page import="java.util.List" %>
<%@ page import="Models.GiangVien" %>
<%@ page import="Models.ChuyenNganh" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<GiangVien> ds = (List<GiangVien>) request.getAttribute("dsGiangVien");
    @SuppressWarnings("unchecked")
    List<ChuyenNganh> dsChuyenNganh = (List<ChuyenNganh>) request.getAttribute("dsChuyenNganh");
    String keyword = (String) request.getAttribute("keyword");
    Integer maChuyenNganhDaChon = (Integer) request.getAttribute("maChuyenNganhDaChon");
    String hocViDaChon = (String) request.getAttribute("hocViDaChon");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div>
        <h1 class="trang-tieude">Quản lý giảng viên</h1>
        <p class="trang-mota">Danh sách đội ngũ giảng viên.</p>
    </div>
    <div style="display: flex; gap: 12px; align-items: center;">
        <div class="nhom-form" style="margin:0">
            <input type="text" class="o-nhap" placeholder="Tìm kiếm nhanh..." data-table-filter="gv-table" style="width: 250px;">
        </div>
        <a class="nut" href="<%= request.getContextPath() %>/admin/giangvien?action=add">+ Thêm giảng viên</a>
    </div>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>
<div class="the" style="margin-bottom:12px">
    <form method="get" action="<%= request.getContextPath() %>/admin/giangvien" style="display:flex;gap:12px;align-items:end;flex-wrap:wrap;">
        <div class="nhom-form" style="margin:0">
            <label>Tìm kiếm</label>
            <input class="o-nhap" type="text" name="keyword" value="<%= Html.escape(keyword) %>" placeholder="Tên, email, mã GV...">
        </div>
        <div class="nhom-form" style="margin:0">
            <label>Lọc theo chuyên ngành</label>
            <select class="o-nhap" name="maChuyenNganh">
                <option value="">Tất cả</option>
                <% if (dsChuyenNganh != null) for (ChuyenNganh cn : dsChuyenNganh) {
                    boolean selected = maChuyenNganhDaChon != null && maChuyenNganhDaChon == cn.getMaChuyenNganh(); %>
                    <option value="<%= cn.getMaChuyenNganh() %>" <%= selected ? "selected" : "" %>><%= Html.escape(cn.getTenChuyenNganh()) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form" style="margin:0">
            <label>Lọc theo học vị</label>
            <select class="o-nhap" name="hocVi">
                <option value="">Tất cả</option>
                <option value="Thạc sĩ" <%= "Thạc sĩ".equals(hocViDaChon) ? "selected" : "" %>>Thạc sĩ</option>
                <option value="Tiến sĩ" <%= "Tiến sĩ".equals(hocViDaChon) ? "selected" : "" %>>Tiến sĩ</option>
            </select>
        </div>
        <button class="nut" type="submit">Lọc</button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/giangvien">Xóa lọc</a>
    </form>
</div>
<div class="the">
    <div class="bang-cuon">
        <table id="gv-table" data-page-size="20">
            <thead>
            <tr><th>Mã GV</th><th>Tên giảng viên</th><th>Khoa</th><th>Học vị</th><th>Chuyên ngành</th><th>Hành động</th></tr>
            </thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="6"><span class="trong-rong">Chưa có giảng viên nào.</span></td></tr>
            <% } else { for (GiangVien giangVien : ds) { %>
                <tr>
                    <td><strong><%= Utils.MaGiangVien.format(giangVien.getMaNguoidung()) %></strong></td>
                    <td><%= Html.escape(giangVien.getHoTen()) %></td>
                    <td><%= Html.escape(giangVien.getTenKhoa()) %></td>
                    <td><%= Html.escape(giangVien.getHocVi()) %></td>
                    <td><%= Html.escape(giangVien.getTenChuyenNganh()) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien"
                           href="<%= request.getContextPath() %>/admin/giangvien?action=detail&id=<%= giangVien.getMaNguoidung() %>">Xem thêm</a>
                        <a class="nut nut-nho nut-vien"
                           href="<%= request.getContextPath() %>/admin/giangvien?action=edit&id=<%= giangVien.getMaNguoidung() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/giangvien"
                              style="display:inline" onsubmit="return confirm('Xóa giảng viên này?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= giangVien.getMaNguoidung() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
