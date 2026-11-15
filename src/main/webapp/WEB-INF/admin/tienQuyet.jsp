<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="Models.MonHoc" %>
<%@ page import="Models.MonHocTienQuyet" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<MonHocTienQuyet> ds = (List<MonHocTienQuyet>) request.getAttribute("dsTienQuyet");
    @SuppressWarnings("unchecked")
    List<MonHoc> dsMon = (List<MonHoc>) request.getAttribute("dsMonHoc");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");

    // bang tra ten mon hoc tu ma mon, dung de hien thi cho de hieu thay vi chi so
    Map<Integer, String> tenMon = new LinkedHashMap<>();
    if (dsMon != null) for (MonHoc m : dsMon) tenMon.put(m.getMaMonhoc(), m.getTenMonhoc());
%>
<div class="thanh-cong-cu">
    <div><h1 class="trang-tieude">Môn tiên quyết</h1><p class="trang-mota">Khai báo môn phải học trước (tiên quyết) cho từng môn.</p></div>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<div class="the">
    <h2 class="trang-tieude" style="font-size:18px">Thêm ràng buộc tiên quyết</h2>
    <form method="post" action="<%= request.getContextPath() %>/admin/montienquyet">
        <input type="hidden" name="action" value="luu">
        <div class="nhom-form"><label>Môn học</label>
            <select class="o-nhap" name="maMonhoc" required>
                <option value="">-- Chọn môn học --</option>
                <% if (dsMon != null) for (MonHoc m : dsMon) { %>
                    <option value="<%= m.getMaMonhoc() %>"><%= Html.escape(m.getTenMonhoc()) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form"><label>Phải học trước môn (tiên quyết)</label>
            <select class="o-nhap" name="maMonhocTienquyet" required>
                <option value="">-- Chọn môn tiên quyết --</option>
                <% if (dsMon != null) for (MonHoc m : dsMon) { %>
                    <option value="<%= m.getMaMonhoc() %>"><%= Html.escape(m.getTenMonhoc()) %></option>
                <% } %>
            </select>
        </div>
        <button class="nut" type="submit">Thêm</button>
    </form>
</div>

<div class="the">
    <div class="bo-loc-bang" data-filter-table="bangTienQuyet" style="margin-bottom:16px">
        <div class="nhom-form"><label>Tìm kiếm</label><input class="o-nhap" data-filter-text type="text" placeholder="Tên môn học hoặc môn tiên quyết..."></div>
        <div class="nhom-form"><label>Lọc theo môn học</label><select class="o-nhap" data-filter-column="0" data-filter-auto><option value="">Tất cả</option></select></div>
        <div class="nhom-form"><label>Lọc theo môn tiên quyết</label><select class="o-nhap" data-filter-column="1" data-filter-auto><option value="">Tất cả</option></select></div>
        <button class="nut" type="button" data-filter-reset>Xóa lọc</button>
    </div>
    <div class="bang-cuon">
        <table id="bangTienQuyet" data-no-paginate>
            <thead><tr><th>Môn học</th><th>Yêu cầu học trước</th><th>Hành động</th></tr></thead>
            <tbody>
            <% if (ds == null || ds.isEmpty()) { %>
                <tr><td colspan="3"><span class="trong-rong">Chưa khai báo ràng buộc tiên quyết nào.</span></td></tr>
            <% } else { for (MonHocTienQuyet tq : ds) {
                   String tenA = tenMon.getOrDefault(tq.getMaMonhoc(), "Môn #" + tq.getMaMonhoc());
                   String tenB = tenMon.getOrDefault(tq.getMaMonhocTienquyet(), "Môn #" + tq.getMaMonhocTienquyet());
            %>
                <tr>
                    <td><%= Html.escape(tenA) %></td>
                    <td><%= Html.escape(tenB) %></td>
                    <td>
                        <form method="post" action="<%= request.getContextPath() %>/admin/montienquyet" style="display:inline" onsubmit="return confirm('Xóa ràng buộc này?');">
                            <input type="hidden" name="action" value="xoa">
                            <input type="hidden" name="maMonhoc" value="<%= tq.getMaMonhoc() %>">
                            <input type="hidden" name="maMonhocTienquyet" value="<%= tq.getMaMonhocTienquyet() %>">
                            <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có ràng buộc phù hợp bộ lọc.</div>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
