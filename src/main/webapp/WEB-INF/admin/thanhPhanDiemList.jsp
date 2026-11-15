<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="Models.MonHoc" %>
<%@ page import="Models.ThanhPhanDiem" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<MonHoc> dsMonHoc = (List<MonHoc>) request.getAttribute("dsMonHoc");
    Integer maMonhocChon = (Integer) request.getAttribute("maMonhocChon");
    @SuppressWarnings("unchecked")
    List<ThanhPhanDiem> dsThanhPhan = (List<ThanhPhanDiem>) request.getAttribute("dsThanhPhan");
    ThanhPhanDiem sua = (ThanhPhanDiem) request.getAttribute("thanhPhanSua");
    String tongTrongSo = (String) request.getAttribute("tongTrongSoPhanTram");
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
    String urlGoc = request.getContextPath();

    boolean laSua = sua != null;
    String tenSua = laSua ? sua.getTenThanhphan() : "";
    String phanTramSua = laSua && sua.getTrongSo() != null
            ? sua.getTrongSo().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() : "";
%>
<h1 class="trang-tieude">Cấu hình cột điểm</h1>
<p class="trang-mota">Tùy biến các cột điểm (chuyên cần, giữa kỳ, thực hành, cuối kỳ...) và trọng số cho từng môn học.</p>

<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<div class="the">
    <form method="get" action="<%= urlGoc %>/admin/thanhphandiem" class="diemdanh-bo-loc"
          style="display:flex; align-items:end; gap:12px; flex-wrap:wrap">
        <div class="nhom-form" style="margin:0">
            <label for="chon-mon">Chọn môn học</label>
            <select id="chon-mon" name="maMonhoc" class="o-nhap" onchange="this.form.submit()">
                <option value="">-- Chọn môn học --</option>
                <% if (dsMonHoc != null) for (MonHoc mh : dsMonHoc) {
                       boolean chon = maMonhocChon != null && maMonhocChon == mh.getMaMonhoc(); %>
                    <option value="<%= mh.getMaMonhoc() %>" <%= chon ? "selected" : "" %>>
                        <%= Html.escape(mh.getMaMon()) %> — <%= Html.escape(mh.getTenMonhoc()) %>
                    </option>
                <% } %>
            </select>
        </div>
    </form>
</div>

<% if (maMonhocChon != null && maMonhocChon > 0) { %>
    <div class="the">
        <div class="the-tieude" style="display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap">
            <span>Danh sách cột điểm</span>
            <span class="nhan-tt <%= "100".equals(tongTrongSo) ? "nhan-xanh" : "nhan-do" %>">
                Tổng trọng số: <%= Html.escape(tongTrongSo == null ? "0" : tongTrongSo) %>%
            </span>
        </div>
        <div class="bang-cuon">
            <table>
                <thead><tr><th>Tên cột điểm</th><th>Trọng số</th><th>Hành động</th></tr></thead>
                <tbody>
                <% if (dsThanhPhan == null || dsThanhPhan.isEmpty()) { %>
                    <tr><td colspan="3"><span class="trong-rong">Môn học chưa có cột điểm nào.</span></td></tr>
                <% } else { for (ThanhPhanDiem tp : dsThanhPhan) {
                       String pt = tp.getTrongSo() == null ? "0"
                               : tp.getTrongSo().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString(); %>
                    <tr>
                        <td><strong><%= Html.escape(tp.getTenThanhphan()) %></strong></td>
                        <td><%= Html.escape(pt) %>%</td>
                        <td>
                            <a class="nut nut-nho nut-vien"
                               href="<%= urlGoc %>/admin/thanhphandiem?maMonhoc=<%= maMonhocChon %>&editId=<%= tp.getMaThanhphan() %>">Sửa</a>
                            <form method="post" action="<%= urlGoc %>/admin/thanhphandiem" style="display:inline"
                                  onsubmit="return confirm('Xóa cột điểm này?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="<%= tp.getMaThanhphan() %>">
                                <input type="hidden" name="maMonhoc" value="<%= maMonhocChon %>">
                                <button class="nut nut-nho nut-do" type="submit">Xóa</button>
                            </form>
                        </td>
                    </tr>
                <% } } %>
                </tbody>
            </table>
        </div>
    </div>

    <div class="the">
        <div class="the-tieude"><%= laSua ? "Sửa cột điểm" : "Thêm cột điểm" %></div>
        <form method="post" action="<%= urlGoc %>/admin/thanhphandiem">
            <input type="hidden" name="action" value="<%= laSua ? "update" : "create" %>">
            <input type="hidden" name="maMonhoc" value="<%= maMonhocChon %>">
            <% if (laSua) { %><input type="hidden" name="id" value="<%= sua.getMaThanhphan() %>"><% } %>
            <div class="nhom-form">
                <label>Tên cột điểm <span class="bat-buoc">*</span></label>
                <input class="o-nhap" type="text" name="tenThanhphan" maxlength="50"
                       value="<%= Html.escape(tenSua) %>" placeholder="VD: Chuyên cần, Giữa kỳ, Thực hành, Cuối kỳ" required>
            </div>
            <div class="nhom-form">
                <label>Trọng số (%) <span class="bat-buoc">*</span></label>
                <input class="o-nhap" type="number" name="trongSoPhanTram" min="1" max="100" step="1"
                       value="<%= Html.escape(phanTramSua) %>" required>
                <small class="ghi-chu-form">Tổng trọng số tất cả cột điểm của một môn phải bằng 100%.</small>
            </div>
            <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm mới" %></button>
            <% if (laSua) { %>
                <a class="nut nut-xam" href="<%= urlGoc %>/admin/thanhphandiem?maMonhoc=<%= maMonhocChon %>">Hủy</a>
            <% } %>
        </form>
    </div>
<% } %>
<%@ include file="/WEB-INF/footer.jsp" %>
