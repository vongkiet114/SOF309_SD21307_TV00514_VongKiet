<%@ page import="java.util.List" %>
<%@ page import="Models.Khoa" %>
<%@ page import="Models.ChuyenNganh" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    ChuyenNganh cn = (ChuyenNganh) request.getAttribute("chuyenNganh");
    String error = (String) request.getAttribute("error");
    boolean laSua = cn != null && cn.getMaChuyenNganh() > 0;
    @SuppressWarnings("unchecked")
    List<Khoa> dsKhoa = (List<Khoa>) request.getAttribute("dsKhoa");

    String vMa = laSua ? String.valueOf(cn.getMaChuyenNganh()) : "";
    String vMaCn = cn == null ? "" : Html.escape(cn.getMaCn());
    String vTen = cn == null ? "" : Html.escape(cn.getTenChuyenNganh());
    int vKhoa = cn == null ? 0 : cn.getMaKhoa();
%>
<h1 class="trang-tieude"><%= laSua ? "Sửa chuyên ngành" : "Thêm chuyên ngành" %></h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/chuyennganh">
        <input type="hidden" name="action" value="luu">
        <input type="hidden" name="id" value="<%= vMa %>">
        <div class="nhom-form"><label>Mã chuyên ngành</label>
            <input class="o-nhap" type="text" name="maCn" value="<%= vMaCn %>" maxlength="20" required></div>
        <div class="nhom-form"><label>Tên chuyên ngành</label>
            <input class="o-nhap" type="text" name="tenChuyenNganh" value="<%= vTen %>" maxlength="100" required></div>
        <div class="nhom-form"><label>Khoa</label>
            <select class="o-nhap" name="maKhoa" required>
                <option value="">-- Chọn khoa --</option>
                <% if (dsKhoa != null) for (Khoa k : dsKhoa) { %>
                    <option value="<%= k.getMaKhoa() %>" <%= (k.getMaKhoa()==vKhoa)?"selected":"" %>><%= Html.escape(k.getTenKhoa()) %></option>
                <% } %>
            </select></div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm" %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/chuyennganh">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
