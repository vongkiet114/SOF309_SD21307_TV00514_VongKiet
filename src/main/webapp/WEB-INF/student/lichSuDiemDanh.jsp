<%@ page import="java.util.List" %>
<%@ page import="Models.LichSuDiemDanhItem" %>
<%@ page import="Models.MonHoc" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%!
    // in gia tri an toan (null -> rong)
    String s(Object o) { return o == null ? "" : String.valueOf(o); }
    // doi ma trang thai diem danh sang chu tieng Viet + class mau
    String nhanTrangThai(Object tt) {
        String v = s(tt);
        if ("CoMat".equals(v))      return "<span class=\"nhan-tt nhan-xanh\">Có mặt</span>";
        if ("Vang".equals(v))       return "<span class=\"nhan-tt nhan-do\">Vắng</span>";
        return "";
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<LichSuDiemDanhItem> lichSu =
            (List<LichSuDiemDanhItem>) request.getAttribute("lichSu");
    @SuppressWarnings("unchecked")
    List<MonHoc> dsMonHoc = (List<MonHoc>) request.getAttribute("dsMonHoc");
    Integer maMonhocChon = (Integer) request.getAttribute("maMonhocChon");
    String thangChon = (String) request.getAttribute("thangChon");
    if (thangChon == null) thangChon = "";
    DateTimeFormatter ngayVietNam = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String urlGoc = request.getContextPath();
%>
<h1 class="trang-tieude">Lịch sử điểm danh</h1>
<p class="trang-mota">Lọc theo môn học và theo tháng để xem nhanh những buổi cần thiết.</p>

<div class="the">
    <form method="get" action="<%= urlGoc %>/sinhvien/diemdanh" class="diemdanh-bo-loc"
          style="display:flex; flex-wrap:wrap; align-items:end; gap:12px">
        <div class="nhom-form" style="margin:0">
            <label for="loc-mon">Môn học</label>
            <select id="loc-mon" name="maMonhoc" class="o-nhap">
                <option value="">-- Tất cả môn --</option>
                <% if (dsMonHoc != null) for (MonHoc mh : dsMonHoc) {
                       boolean chon = maMonhocChon != null && maMonhocChon == mh.getMaMonhoc(); %>
                    <option value="<%= mh.getMaMonhoc() %>" <%= chon ? "selected" : "" %>><%= Html.escape(mh.getTenMonhoc()) %></option>
                <% } %>
            </select>
        </div>
        <div class="nhom-form" style="margin:0">
            <label for="loc-thang">Tháng</label>
            <input id="loc-thang" type="month" name="thang" class="o-nhap" value="<%= Html.escape(thangChon) %>">
        </div>
        <button class="nut" type="submit">Lọc</button>
        <a class="nut nut-xam" href="<%= urlGoc %>/sinhvien/diemdanh?thang=tatca<%= maMonhocChon != null ? "&maMonhoc=" + maMonhocChon : "" %>">Tất cả các tháng</a>
    </form>
</div>

<div class="the">
    <div class="bang-cuon">
        <table>
            <thead><tr><th>Ngày học</th><th>Môn học</th><th>Trạng thái</th></tr></thead>
            <tbody>
            <% if (lichSu == null || lichSu.isEmpty()) { %>
                <tr><td colspan="3"><span class="trong-rong">Không có dữ liệu điểm danh phù hợp bộ lọc.</span></td></tr>
            <% } else { for (LichSuDiemDanhItem item : lichSu) { %>
                <tr>
                    <td><%= Html.escape(item.getNgayHoc() == null ? "" : ngayVietNam.format(item.getNgayHoc())) %></td>
                    <td><%= Html.escape(s(item.getTenMonHoc())) %></td>
                    <td><%= nhanTrangThai(item.getTrangThai()) %></td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
