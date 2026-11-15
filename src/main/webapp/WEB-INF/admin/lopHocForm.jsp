<%@ page import="java.util.List" %>
<%@ page import="Models.ChuyenNganh" %>
<%@ page import="Models.LopHoc" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    LopHoc lop = (LopHoc) request.getAttribute("lop");
    String error = (String) request.getAttribute("error");
    boolean laSua = lop != null && lop.getMaLop() > 0;
    @SuppressWarnings("unchecked")
    List<ChuyenNganh> dsCN = (List<ChuyenNganh>) request.getAttribute("dsChuyenNganh");

    String vMa = laSua ? String.valueOf(lop.getMaLop()) : "";
    String vMaHienThi = laSua ? Html.escape(lop.getMaLopHienThi()) : "Hệ thống tự tạo khi lưu";
    String vTen = lop == null ? "" : Html.escape(lop.getTenLop());
    int vCN = lop == null ? 0 : lop.getMaChuyenNganh();
%>
<h1 class="trang-tieude"><%= laSua ? "Sửa lớp" : "Thêm lớp" %></h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/lophoc">
        <input type="hidden" name="action" value="luu">
        <input type="hidden" name="id" value="<%= vMa %>">
        <div class="nhom-form"><label>Mã lớp</label>
            <input class="o-nhap" type="text" value="<%= vMaHienThi %>" readonly></div>
        <div class="nhom-form"><label>Tên lớp</label>
            <input class="o-nhap" type="text" name="tenLop" value="<%= vTen %>" maxlength="50" required></div>
        <div class="nhom-form"><label>Chuyên ngành</label>
            <select class="o-nhap" name="maChuyenNganh" required>
                <option value="">-- Chọn chuyên ngành --</option>
                <% if (dsCN != null) for (ChuyenNganh cn : dsCN) { %>
                    <option value="<%= cn.getMaChuyenNganh() %>" <%= cn.getMaChuyenNganh() == vCN ? "selected" : "" %>><%= Html.escape(cn.getTenKhoa()) %> — <%= Html.escape(cn.getTenChuyenNganh()) %></option>
                <% } %>
            </select></div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm" %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/lophoc">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
