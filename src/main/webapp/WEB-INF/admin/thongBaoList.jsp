<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.ThongBao" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%!
    // doi ma pham vi sang chu tieng Viet
    String nhanPhamVi(String pv) {
        if ("ToanTruong".equals(pv)) return "Toàn trường";
        if ("Khoa".equals(pv))       return "Theo khoa";
        if ("Lop".equals(pv))        return "Theo lớp";
        if ("CaNhan".equals(pv))     return "Cá nhân";
        return pv == null || pv.trim().isEmpty() ? "Chưa xác định" : pv;
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<ThongBao> ds = (List<ThongBao>) request.getAttribute("dsThongBao");
    String msg = request.getParameter("msg");
    String err = request.getAttribute("error") != null
            ? String.valueOf(request.getAttribute("error"))
            : request.getParameter("error");
    DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý thông báo</h1><p class="trang-mota">Các thông báo đã gửi tới sinh viên / giảng viên.</p></div>
    <a class="nut" href="<%= request.getContextPath() %>/admin/thongbao?action=them">+ Soạn thông báo</a>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>
<div class="the">
    <div class="bang-cuon">
        <table>
            <thead><tr><th>Mã</th><th>Tiêu đề</th><th>Phạm vi</th><th>Ngày gửi</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="5"><span class="trong-rong">Chưa có thông báo nào.</span></td></tr>
            <% } else { for (ThongBao tb : ds) { %>
                <tr>
                    <td><strong><%= tb.getMaThongbao() %></strong></td>
                    <td><%= Html.escape(tb.getTieuDe()) %></td>
                    <td><span class="nhan-tt nhan-xanh"><%= nhanPhamVi(tb.getPhamVi()) %></span></td>
                    <td><%= tb.getNgayGui() != null ? tb.getNgayGui().format(dinhDangNgay) : "" %></td>
                    <td>
                        <form method="post" action="<%= request.getContextPath() %>/admin/thongbao" style="display:inline" onsubmit="return confirm('Xóa thông báo này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= tb.getMaThongbao() %>">
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
