<%@ page import="java.util.List" %>
<%@ page import="Models.ChuyenNganh" %>
<%@ page import="Models.Khoa" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<ChuyenNganh> ds = (List<ChuyenNganh>) request.getAttribute("dsChuyenNganh");
    @SuppressWarnings("unchecked")
    List<Khoa> dsKhoa = (List<Khoa>) request.getAttribute("dsKhoa");
    String keyword = (String) request.getAttribute("keyword");
    Integer maKhoaDaChon = (Integer) request.getAttribute("maKhoaDaChon");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý chuyên ngành</h1><p class="trang-mota">Danh mục chuyên ngành thuộc các khoa.</p></div>
    <a class="nut" href="<%= request.getContextPath() %>/admin/chuyennganh?action=them">+ Thêm chuyên ngành</a>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>
<div class="the" style="margin-bottom:12px">
    <form method="get" action="<%= request.getContextPath() %>/admin/chuyennganh" style="display:flex;gap:12px;align-items:end;flex-wrap:wrap;">
        <div class="nhom-form" style="margin:0">
            <label>Tìm kiếm</label>
            <input class="o-nhap" type="text" name="keyword" value="<%= Html.escape(keyword) %>" placeholder="Mã, tên chuyên ngành, khoa...">
        </div>
        <div class="nhom-form" style="margin:0">
            <label>Lọc theo khoa</label>
            <select class="o-nhap" name="maKhoa">
                <option value="">Tất cả</option>
                <% if (dsKhoa != null) for (Khoa khoa : dsKhoa) {
                    boolean selected = maKhoaDaChon != null && maKhoaDaChon == khoa.getMaKhoa(); %>
                    <option value="<%= khoa.getMaKhoa() %>" <%= selected ? "selected" : "" %>><%= Html.escape(khoa.getTenKhoa()) %></option>
                <% } %>
            </select>
        </div>
        <button class="nut" type="submit">Lọc</button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/chuyennganh">Xóa lọc</a>
    </form>
</div>
<div class="the">
    <div class="bang-cuon">
        <table>
            <thead><tr><th>Mã CN</th><th>Tên chuyên ngành</th><th>Khoa</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="4"><span class="trong-rong">Chưa có chuyên ngành nào.</span></td></tr>
            <% } else { for (ChuyenNganh cn : ds) { %>
                <tr>
                    <td><strong><%= Html.escape(cn.getMaCn()) %></strong></td>
                    <td><%= Html.escape(cn.getTenChuyenNganh()) %></td>
                    <td><%= Html.escape(cn.getTenKhoa()) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/chuyennganh?action=sua&id=<%= cn.getMaChuyenNganh() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/chuyennganh" style="display:inline" onsubmit="return confirm('Xóa chuyên ngành này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= cn.getMaChuyenNganh() %>">
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
