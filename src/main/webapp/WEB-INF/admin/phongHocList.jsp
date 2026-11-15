<%@ page import="java.util.List" %>
<%@ page import="Models.PhongHoc" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<PhongHoc> ds = (List<PhongHoc>) request.getAttribute("dsPhongHoc");
    PhongHoc phongHoc = (PhongHoc) request.getAttribute("phongHoc");
    boolean laSua = phongHoc != null;
    String msg = request.getParameter("msg");
    String err = request.getAttribute("error") instanceof String
            ? (String) request.getAttribute("error")
            : request.getParameter("error");

    String vMa = laSua ? String.valueOf(phongHoc.getMaPhong()) : "";
    String vTen = laSua ? Html.escape(phongHoc.getTenPhong()) : "";
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý phòng học</h1><p class="trang-mota">Danh mục phòng học.</p></div>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<div class="the">
    <h2 class="trang-tieude" style="font-size:18px"><%= laSua ? "Sửa phòng học" : "Thêm phòng học" %></h2>
    <form method="post" action="<%= request.getContextPath() %>/admin/phonghoc">
        <input type="hidden" name="action" value="luu">
        <input type="hidden" name="id" value="<%= vMa %>">
        <div class="nhom-form"><label>Tên phòng</label><input class="o-nhap" type="text" name="tenPhong" value="<%= vTen %>" maxlength="20" placeholder="Ví dụ: A101" required></div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm" %></button>
        <% if (laSua) { %><a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/phonghoc?action=them">Hủy</a><% } %>
    </form>
</div>

<div class="the">
    <div class="bo-loc-bang" data-filter-table="bangPhongHoc" style="margin-bottom:16px">
        <div class="nhom-form"><label>Tìm kiếm</label><input class="o-nhap" data-filter-text type="text" placeholder="Mã phòng, tên phòng..."></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc</button>
    </div>
    <div class="bang-cuon">
        <table id="bangPhongHoc" data-no-paginate>
            <thead><tr><th>Mã phòng</th><th>Tên phòng</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="3"><span class="trong-rong">Chưa có phòng học nào.</span></td></tr>
            <% } else { for (PhongHoc p : ds) { %>
                <tr>
                    <td><strong><%= p.getMaPhong() %></strong></td>
                    <td><%= Html.escape(p.getTenPhong()) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/phonghoc?action=sua&id=<%= p.getMaPhong() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/phonghoc" style="display:inline" onsubmit="return confirm('Xóa phòng học này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= p.getMaPhong() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có phòng học phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
