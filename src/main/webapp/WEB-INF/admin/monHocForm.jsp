<%@ page import="java.util.List" %>
<%@ page import="Models.ChuyenNganh" %>
<%@ page import="Models.MonHoc" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    MonHoc mon = (MonHoc) request.getAttribute("monHoc");
    String error = (String) request.getAttribute("error");
    boolean laSua = mon != null && mon.getMaMonhoc() > 0;
    @SuppressWarnings("unchecked")
    List<ChuyenNganh> dsCN = (List<ChuyenNganh>) request.getAttribute("dsChuyenNganh");

    String vMa = laSua ? String.valueOf(mon.getMaMonhoc()) : "";
    String vMaMon = mon == null ? "" : Html.escape(mon.getMaMon());
    String vTen = mon == null ? "" : Html.escape(mon.getTenMonhoc());
    String vTinChi = mon == null || mon.getSoTinchi() <= 0 ? "3" : String.valueOf(mon.getSoTinchi());
    int vCN = mon == null ? 0 : mon.getMaChuyenNganh();
%>
<h1 class="trang-tieude"><%= laSua ? "Sửa môn học" : "Thêm môn học" %></h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/monhoc">
        <input type="hidden" name="action" value="luu">
        <input type="hidden" name="id" value="<%= vMa %>">
        <div class="nhom-form"><label>Mã môn</label>
            <input class="o-nhap" type="text" name="maMon" value="<%= vMaMon %>" maxlength="20" required></div>
        <div class="nhom-form"><label>Tên môn học</label>
            <input class="o-nhap" type="text" name="tenMonhoc" value="<%= vTen %>" maxlength="100" required></div>
        <div class="nhom-form"><label>Số tín chỉ</label>
            <input class="o-nhap" type="number" name="soTinchi" value="<%= vTinChi %>" min="1" required></div>
        <div class="nhom-form"><label>Chuyên ngành</label>
            <select class="o-nhap" name="maChuyenNganh" required>
                <option value="">-- Chọn chuyên ngành --</option>
                <% if (dsCN != null) for (ChuyenNganh cn : dsCN) { %>
                    <option value="<%= cn.getMaChuyenNganh() %>" <%= cn.getMaChuyenNganh() == vCN ? "selected" : "" %>><%= Html.escape(cn.getTenKhoa()) %> — <%= Html.escape(cn.getTenChuyenNganh()) %></option>
                <% } %>
            </select></div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm" %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/monhoc">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
