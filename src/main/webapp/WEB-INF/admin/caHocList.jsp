<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="Models.CaHoc" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<CaHoc> ds = (List<CaHoc>) request.getAttribute("dsCaHoc");
    CaHoc caHoc = (CaHoc) request.getAttribute("caHoc");
    boolean laSua = caHoc != null;
    String msg = request.getParameter("msg");
    String err = request.getAttribute("error") instanceof String
            ? (String) request.getAttribute("error")
            : request.getParameter("error");

    String vMa = laSua ? String.valueOf(caHoc.getMaCa()) : "";
    LocalTime gioBd = laSua ? caHoc.getGioBatdau() : null;
    LocalTime gioKt = laSua ? caHoc.getGioKetthuc() : null;
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý ca học</h1><p class="trang-mota">Các ca học trong ngày kèm giờ bắt đầu, giờ kết thúc.</p></div>
    <a class="nut" href="<%= request.getContextPath() %>/admin/cahoc?action=them">Thêm ca học</a>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<div class="the">
    <h2 class="trang-tieude" style="font-size:18px"><%= laSua ? "Sửa ca học" : "Thêm ca học" %></h2>
    <form method="post" action="<%= request.getContextPath() %>/admin/cahoc">
        <input type="hidden" name="action" value="luu">
        <input type="hidden" name="id" value="<%= vMa %>">
        <div class="nhom-form"><label>Giờ bắt đầu</label><input class="o-nhap" type="time" name="gioBatdau" value="<%= gioBd != null ? gioBd : "" %>" required></div>
        <div class="nhom-form"><label>Giờ kết thúc</label><input class="o-nhap" type="time" name="gioKetthuc" value="<%= gioKt != null ? gioKt : "" %>" required></div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm" %></button>
        <% if (laSua) { %><a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/cahoc?action=them">Hủy</a><% } %>
    </form>
</div>

<div class="the">
    <div class="bo-loc-bang" data-filter-table="bangCaHoc" style="margin-bottom:16px">
        <div class="nhom-form"><label>Tìm kiếm</label><input class="o-nhap" data-filter-text type="text" placeholder="Ca học, khoảng giờ..."></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc</button>
    </div>
    <div class="bang-cuon">
        <table id="bangCaHoc" data-no-paginate>
            <thead><tr><th>Ca học</th><th>Khoảng giờ</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="3"><span class="trong-rong">Chưa có ca học nào.</span></td></tr>
            <% } else { for (CaHoc c : ds) { %>
                <tr>
                    <td><strong><%= c.getTenCa() %></strong></td>
                    <td><%= c.getGioBatdau() != null ? c.getGioBatdau() : "" %> - <%= c.getGioKetthuc() != null ? c.getGioKetthuc() : "" %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/cahoc?action=sua&id=<%= c.getMaCa() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/cahoc" style="display:inline" onsubmit="return confirm('Xóa ca học này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= c.getMaCa() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có ca học phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
