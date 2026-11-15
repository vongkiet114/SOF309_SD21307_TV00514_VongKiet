<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Models.TaiKhoan" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%!
    String nhom(String vaiTro) {
        String v = vaiTro == null ? "" : vaiTro;
        if (v.equalsIgnoreCase("ADMIN"))   return "Admin";
        if (v.equalsIgnoreCase("TEACHER")) return "GiangVien";
        if (v.equalsIgnoreCase("STUDENT")) return "SinhVien";
        return "Khac";
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<TaiKhoan> ds = (List<TaiKhoan>) request.getAttribute("dsTaiKhoan");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");

    List<TaiKhoan> nhomAdmin = new ArrayList<>();
    List<TaiKhoan> nhomGV = new ArrayList<>();
    List<TaiKhoan> nhomSV = new ArrayList<>();
    List<TaiKhoan> nhomKhac = new ArrayList<>();
    if (ds != null) for (TaiKhoan tk : ds) {
        switch (nhom(tk.getVaiTro())) {
            case "Admin":     nhomAdmin.add(tk); break;
            case "GiangVien": nhomGV.add(tk); break;
            case "SinhVien":  nhomSV.add(tk); break;
            default:          nhomKhac.add(tk);
        }
    }
    String[] tieuDeNhom = {"Quản trị viên", "Giảng viên", "Sinh viên", "Chưa gán vai trò"};
    List<List<TaiKhoan>> cacNhom = new ArrayList<>();
    cacNhom.add(nhomAdmin); cacNhom.add(nhomGV); cacNhom.add(nhomSV); cacNhom.add(nhomKhac);
%>
<h1 class="trang-tieude">Quản lý tài khoản</h1>
<p class="trang-mota">Danh sách thành viên theo vai trò, mã số và trạng thái hoạt động.</p>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<% for (int g = 0; g < cacNhom.size(); g++) {
       List<TaiKhoan> nh = cacNhom.get(g);
       if (nh.isEmpty()) continue;
%>
<div class="the">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
        <h2 class="trang-tieude" style="font-size:18px; margin:0;"><%= tieuDeNhom[g] %> (<%= nh.size() %>)</h2>
        <div class="nhom-form" style="margin:0">
            <input type="text" class="o-nhap" placeholder="Tìm kiếm..." data-table-filter="tk-table-<%= g %>">
        </div>
    </div>
    <div class="bang-cuon">
        <table id="tk-table-<%= g %>" data-page-size="20">
            <thead><tr><th>STT</th><th>Mã số</th><th>Họ và tên</th><th>Email</th><th>Đăng nhập</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
            <tbody>
            <% int stt = 0; for (TaiKhoan tk : nh) { stt++; %>
                <tr>
                    <td><%= stt %></td>
                    <td><strong><%= Html.escape(tk.getMaSo()) %></strong></td>
                    <td><%= Html.escape(tk.getHoTen()) %></td>
                    <td><%= Html.escape(tk.getEmail()) %></td>
                    <td><%= Html.escape(tk.getNccDangnhap()) %></td>
                    <td>
                        <% if (tk.isHoatDong()) { %><span class="nhan-tt nhan-xanh">Hoạt động</span>
                        <% } else { %><span class="nhan-tt nhan-vang">Tạm khóa</span><% } %>
                    </td>
                    <td>
                        <form method="post" action="<%= request.getContextPath() %>/admin/taikhoan" style="display:inline">
                            <input type="hidden" name="id" value="<%= tk.getMaNguoidung() %>">
                            <% if (tk.isHoatDong()) { %>
                                <input type="hidden" name="action" value="lock">
                                <button class="nut nut-nho nut-do" type="submit">Khóa</button>
                            <% } else { %>
                                <input type="hidden" name="action" value="unlock">
                                <button class="nut nut-nho" type="submit">Mở khóa</button>
                            <% } %>
                        </form>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
<% } %>
<%@ include file="/WEB-INF/footer.jsp" %>
