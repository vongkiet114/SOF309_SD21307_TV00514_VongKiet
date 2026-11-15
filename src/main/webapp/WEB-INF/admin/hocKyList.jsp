<%@ page import="java.util.List" %>
<%@ page import="Models.HocKy" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%!
    // doi ma trang thai hoc ky sang chu tieng Viet + mau rieng cho tung trang thai
    String nhanTrangThai(String tt) {
        if ("DangDienRa".equals(tt)) return "<span class=\"nhan-tt nhan-xanh\">Đang diễn ra</span>";
        if ("DaKetThuc".equals(tt))  return "<span class=\"nhan-tt nhan-xam\">Đã kết thúc</span>";
        return "<span class=\"nhan-tt nhan-cam\">Sắp diễn ra</span>";
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<HocKy> ds = (List<HocKy>) request.getAttribute("dsHocKy");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Quản lý học kỳ</h1><p class="trang-mota">Các học kỳ theo từng năm học.</p></div>
    <div style="display: flex; gap: 12px; align-items: center;">

        <a class="nut" href="<%= request.getContextPath() %>/admin/hocky?action=them">+ Thêm học kỳ</a>
    </div>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>
<div class="the">
    <div class="bo-loc-bang" data-filter-table="bangHocKy" style="margin-bottom:16px">
        <div class="nhom-form"><label>Tìm kiếm</label><input class="o-nhap" data-filter-text type="text" placeholder="Mã, tên học kỳ, năm học..."></div>
        <div class="nhom-form"><label>Lọc block</label><select class="o-nhap" data-filter-column="2" data-filter-auto><option value="">Tất cả</option></select></div>
        <div class="nhom-form"><label>Lọc năm học</label><select class="o-nhap" data-filter-column="3" data-filter-auto><option value="">Tất cả</option></select></div>
        <div class="nhom-form"><label>Lọc trạng thái</label><select class="o-nhap" data-filter-column="5" data-filter-auto><option value="">Tất cả</option></select></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc</button>
    </div>
    <div class="bang-cuon">
        <table id="bangHocKy" data-page-size="20">
            <thead><tr><th>Mã</th><th>Tên học kỳ</th><th>Block</th><th>Năm học</th><th>Khoảng ngày</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="7"><span class="trong-rong">Chưa có học kỳ nào.</span></td></tr>
            <% } else { for (HocKy hk : ds) { %>
                <tr>
                    <td><strong><%= hk.getMaHocky() %></strong></td>
                    <td><%= Html.escape(hk.getTenHocky()) %></td>
                    <td>Block <%= hk.getBlock() %></td>
                    <td><%= Html.escape(hk.getNamHoc()) %></td>
                    <td><%= hk.getNgayBatdau() != null ? hk.getNgayBatdau() : "" %> — <%= hk.getNgayKetthuc() != null ? hk.getNgayKetthuc() : "" %></td>
                    <td><%= nhanTrangThai(hk.getTrangThai()) %></td>
                    <td>
                        <a class="nut nut-nho nut-vien" href="<%= request.getContextPath() %>/admin/hocky?action=sua&id=<%= hk.getMaHocky() %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/admin/hocky" style="display:inline" onsubmit="return confirm('Xóa học kỳ này?');">
                            <input type="hidden" name="action" value="xoa"><input type="hidden" name="id" value="<%= hk.getMaHocky() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có học kỳ phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
