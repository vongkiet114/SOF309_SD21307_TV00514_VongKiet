<%@ page import="java.util.List" %>
<%@ page import="Models.MonHoc" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<MonHoc> ds = (List<MonHoc>) request.getAttribute("dsMonHoc");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý môn học</h1><p class="trang-mota">Danh mục môn học.</p></div>
    <div style="display: flex; gap: 12px; align-items: center;">

        <a class="nut" href="<%= request.getContextPath() %>/admin/monhoc?action=them">+ Thêm môn học</a>
    </div>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>
<div class="the" style="margin-bottom:12px">
    <div class="bo-loc-bang" data-filter-table="bangMonHoc">
        <div class="nhom-form"><label>Tìm kiếm</label><input class="o-nhap" data-filter-text type="text" placeholder="Mã môn, tên môn, chuyên ngành..."></div>
        <div class="nhom-form"><label>Lọc theo số tín chỉ</label><select class="o-nhap" data-filter-column="2" data-filter-auto><option value="">Tất cả</option></select></div>
        <div class="nhom-form"><label>Lọc theo chuyên ngành</label><select class="o-nhap" data-filter-column="3" data-filter-auto><option value="">Tất cả</option></select></div>
        <div class="nhom-form"><label>Lọc theo khoa</label><select class="o-nhap" data-filter-column="4" data-filter-auto><option value="">Tất cả</option></select></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc</button>
    </div>
</div>
<div class="the">
    <div class="bang-cuon">
        <table id="bangMonHoc" data-page-size="20">
            <thead><tr><th>Mã môn</th><th>Tên môn học</th><th>Số TC</th><th>Chuyên ngành</th><th>Khoa</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="6"><span class="trong-rong">Chưa có môn học nào.</span></td></tr>
            <% } else { for (MonHoc m : ds) { %>
                <tr>
                    <td><strong><%= Html.escape(m.getMaMon()) %></strong></td>
                    <td><%= Html.escape(m.getTenMonhoc()) %></td>
                    <td><%= m.getSoTinchi() %></td>
                    <td><%= Html.escape(m.getTenChuyenNganh()) %></td>
                    <td><%= Html.escape(m.getTenKhoa()) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/monhoc?action=sua&id=<%= m.getMaMonhoc() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/monhoc" style="display:inline" onsubmit="return confirm('Xóa môn học này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= m.getMaMonhoc() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có môn học phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
