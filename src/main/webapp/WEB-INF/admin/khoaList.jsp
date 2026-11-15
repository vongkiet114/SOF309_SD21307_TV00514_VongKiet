<%@ page import="java.util.List" %>
<%@ page import="Models.Khoa" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<Khoa> ds = (List<Khoa>) request.getAttribute("dsKhoa");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý khoa</h1><p class="trang-mota">Danh mục các khoa trong trường.</p></div>
    <a class="nut" href="<%= request.getContextPath() %>/admin/khoa?action=them">+ Thêm khoa</a>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>
<div class="the">
    <div class="bo-loc-bang" data-filter-table="bangKhoa" style="margin-bottom:16px">
        <div class="nhom-form"><label>Tìm kiếm</label><input class="o-nhap" data-filter-text type="text" placeholder="ID, tên khoa..."></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc</button>
    </div>
    <div class="bang-cuon">
        <table id="bangKhoa" data-no-paginate>
            <thead><tr><th>ID</th><th>Tên khoa</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="3"><span class="trong-rong">Chưa có khoa nào.</span></td></tr>
            <% } else { for (Khoa k : ds) { %>
                <tr>
                    <td><strong><%= k.getMaKhoa() %></strong></td>
                    <td><%= Html.escape(k.getTenKhoa()) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/khoa?action=sua&id=<%= k.getMaKhoa() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/khoa" style="display:inline" onsubmit="return confirm('Xóa khoa này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= k.getMaKhoa() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có khoa phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
