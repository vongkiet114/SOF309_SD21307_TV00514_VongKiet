<%@ page import="java.util.List" %>
<%@ page import="Models.LopHocPhan" %>
<%@ page import="Models.HocKy" %>
<%@ page import="Models.GiangVien" %>
<%@ page import="Utils.Html" %>
<%@ page import="Utils.TrangThaiTheoNgay" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<LopHocPhan> dsLHP = (List<LopHocPhan>) request.getAttribute("dsLHP");
    @SuppressWarnings("unchecked")
    List<HocKy> dsHocKy = (List<HocKy>) request.getAttribute("dsHocKy");
    @SuppressWarnings("unchecked")
    List<GiangVien> dsGiangVien = (List<GiangVien>) request.getAttribute("dsGiangVien");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý Lớp học phần</h1><p class="trang-mota">Mở lớp cho từng môn học theo học kỳ.</p></div>
    <a class="nut" href="<%= request.getContextPath() %>/admin/lophocphan?action=add">+ Thêm lớp học phần</a>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<div class="the" style="margin-bottom:12px">
    <form method="get" action="<%= request.getContextPath() %>/admin/lophocphan" style="display:flex;gap:12px;align-items:end;flex-wrap:wrap;">
        <div class="nhom-form" style="margin:0">
            <label>Lọc theo học kỳ</label>
            <select class="o-nhap" name="maHocKy">
                <option value="">Tất cả</option>
                <% if (dsHocKy != null) for (HocKy hk : dsHocKy) { %>
                    <option value="<%= hk.getMaHocky() %>"><%= Html.escape(hk.getTenHocky()) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form" style="margin:0">
            <label>Lọc theo giảng viên</label>
            <select class="o-nhap" name="maGiangVien">
                <option value="">Tất cả</option>
                <% if (dsGiangVien != null) for (GiangVien gv : dsGiangVien) { %>
                    <option value="<%= gv.getMaNguoidung() %>"><%= Html.escape(gv.getHoTen()) %></option>
                <% } %>
            </select>
        </div>
        <button class="nut" type="submit">Lọc</button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/lophocphan">Xóa lọc</a>
    </form>
</div>

<div class="the">
    <div class="bo-loc-bang" data-filter-table="bangLopHocPhan" style="margin-bottom:16px">
        <div class="nhom-form"><label>Tìm nhanh trong bảng</label><input class="o-nhap" data-filter-text type="text" placeholder="Mã LHP, môn, giảng viên, học kỳ..."></div>
        <div class="nhom-form"><label>Lọc trạng thái</label><select class="o-nhap" data-filter-column="5" data-filter-auto><option value="">Tất cả</option></select></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc nhanh</button>
    </div>
    <div class="bang-cuon">
        <table id="bangLopHocPhan" data-no-paginate>
            <thead><tr><th>Mã LHP</th><th>Môn học</th><th>Giảng viên</th><th>Học kỳ</th><th>Sĩ số</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (dsLHP == null || dsLHP.isEmpty()) { %>
                <tr><td colspan="7"><span class="trong-rong">Chưa có lớp học phần nào.</span></td></tr>
            <% } else { for (LopHocPhan lhp : dsLHP) { %>
                <tr>
                    <td><strong><%= Html.escape(lhp.getMaLhp()) %></strong></td>
                    <td><%= Html.escape(lhp.getTenMonHoc()) %> (<%= lhp.getSoTinChi() %> TC)</td>
                    <td><%= Html.escape(lhp.getTenGiangVien()) %></td>
                    <td><%= Html.escape(lhp.getTenHocKy()) %></td>
                    <td><%= lhp.getSoLuongSV() %></td>
                    <td><%= Html.escape(TrangThaiTheoNgay.nhan(lhp.getTrangThai())) %></td>
                    <td>
                        <a class="nut nut-nho" href="<%= request.getContextPath() %>/admin/lophocphan/sinhvien?maLHP=<%= lhp.getMaLophocphan() %>">Sinh viên</a>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/lophocphan?action=edit&id=<%= lhp.getMaLophocphan() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/lophocphan" style="display:inline" onsubmit="return confirm('Xóa lớp học phần này?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= lhp.getMaLophocphan() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có lớp học phần phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
