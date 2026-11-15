<%@ page import="java.util.List" %>
<%@ page import="Models.LopHoc" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<LopHoc> ds = (List<LopHoc>) request.getAttribute("dsLop");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý lớp học</h1><p class="trang-mota">Danh sách lớp hành chính.</p></div>
    <div style="display: flex; gap: 12px; align-items: center;">

        <a class="nut" href="<%= request.getContextPath() %>/admin/lophoc?action=them">+ Thêm lớp</a>
    </div>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>
<div class="the" style="margin-bottom:12px">
    <div class="bo-loc-bang" data-filter-table="bangLopHoc">
        <div class="nhom-form"><label>Tìm kiếm</label><input class="o-nhap" data-filter-text type="text" placeholder="Mã lớp, tên lớp, chuyên ngành..."></div>
        <div class="nhom-form"><label>Lọc theo chuyên ngành</label><select class="o-nhap" data-filter-column="2" data-filter-auto><option value="">Tất cả</option></select></div>
        <div class="nhom-form"><label>Lọc theo khoa</label><select class="o-nhap" data-filter-column="3" data-filter-auto><option value="">Tất cả</option></select></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc</button>
    </div>
</div>
<div class="the">
    <div class="bang-cuon">
        <table id="bangLopHoc" data-page-size="20">
            <thead><tr><th>Mã lớp</th><th>Tên lớp</th><th>Chuyên ngành</th><th>Khoa</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="5"><span class="trong-rong">Chưa có lớp nào.</span></td></tr>
            <% } else { for (LopHoc l : ds) { %>
                <tr>
                    <td><strong><%= Html.escape(l.getMaLopHienThi()) %></strong></td>
                    <td><%= Html.escape(l.getTenLop()) %></td>
                    <td><%= Html.escape(l.getTenChuyenNganh()) %></td>
                    <td><%= Html.escape(l.getTenKhoa()) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/lophoc?action=sua&id=<%= l.getMaLop() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/lophoc" style="display:inline" onsubmit="return confirm('Xóa lớp này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= l.getMaLop() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có lớp học phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
