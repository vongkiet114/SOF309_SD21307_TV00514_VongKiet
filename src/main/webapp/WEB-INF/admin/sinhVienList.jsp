<%@ page import="java.util.List" %>
<%@ page import="Models.SinhVien" %>
<%@ page import="Models.ChuyenNganh" %>
<%@ page import="Models.LopHoc" %>
<%@ page import="Utils.Html" %>
<%@ page import="Utils.TrangThaiTheoNgay" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<SinhVien> dsSinhVien = (List<SinhVien>) request.getAttribute("dsSinhVien");
    @SuppressWarnings("unchecked")
    List<ChuyenNganh> dsChuyenNganh = (List<ChuyenNganh>) request.getAttribute("dsChuyenNganh");
    @SuppressWarnings("unchecked")
    List<LopHoc> dsLop = (List<LopHoc>) request.getAttribute("dsLop");
    String keyword = (String) request.getAttribute("keyword");
    Integer maChuyenNganhDaChon = (Integer) request.getAttribute("maChuyenNganhDaChon");
    Integer maLopDaChon = (Integer) request.getAttribute("maLopDaChon");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div>
        <h1 class="trang-tieude">Quản lý sinh viên</h1>
        <p class="trang-mota">Danh sách sinh viên toàn trường.</p>
    </div>
    <a class="nut" href="<%= request.getContextPath() %>/admin/sinhvien?action=add">+ Thêm sinh viên</a>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<div class="the" style="margin-bottom:12px">
    <form method="get" action="<%= request.getContextPath() %>/admin/sinhvien" style="display:flex;gap:12px;align-items:end;">
        <div class="nhom-form" style="margin:0">
            <label>Tìm kiếm</label>
            <input class="o-nhap" type="text" name="keyword" value="<%= Html.escape(keyword) %>" placeholder="MSSV, họ tên, email...">
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
            <label>Lọc theo lớp</label>
            <select class="o-nhap" name="maLop">
                <option value="">Tất cả</option>
                <% if (dsLop != null) for (LopHoc l : dsLop) {
                    boolean selected = maLopDaChon != null && maLopDaChon == l.getMaLop(); %>
                    <option value="<%= l.getMaLop() %>" <%= selected ? "selected" : "" %>><%= Html.escape(l.getMaLopHienThi()) %> — <%= Html.escape(l.getTenLop()) %></option>
                <% } %>
            </select>
        </div>
        <button class="nut" type="submit">Lọc</button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/sinhvien">Xóa lọc</a>
    </form>
</div>

<div class="the">
    <div class="bang-cuon">
        <table id="sv-table" data-page-size="20">
            <thead>
            <tr><th>MSSV</th><th>Họ tên</th><th>Email</th><th>Lớp</th><th>Trạng thái</th><th>Hành động</th></tr>
            </thead>
            <tbody>
            <% if (dsSinhVien == null || dsSinhVien.isEmpty()) { %>
                <tr><td colspan="6"><span class="trong-rong">Không tìm thấy sinh viên nào.</span></td></tr>
            <% } else { for (SinhVien sv : dsSinhVien) { %>
                <tr>
                    <td><strong><%= Html.escape(sv.getMssv()) %></strong></td>
                    <td><%= Html.escape(sv.getHoTen()) %></td>
                    <td><%= Html.escape(sv.getEmail()) %></td>
                    <td><%= Html.escape(sv.getTenLop()) %></td>
                    <td><%= Html.escape(TrangThaiTheoNgay.nhan(sv.getTrangThai())) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/sinhvien?action=detail&id=<%= sv.getMaNguoidung() %>">Xem thêm</a>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/sinhvien?action=edit&id=<%= sv.getMaNguoidung() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/sinhvien" style="display:inline" onsubmit="return confirm('Xóa sinh viên này?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= sv.getMaNguoidung() %>">
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
