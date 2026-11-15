<%@ page import="java.util.List" %>
<%@ page import="Models.BangDiemHocPhan" %>
<%@ page import="Models.DiemSo" %>
<%@ page import="Models.ThanhPhanDiem" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<BangDiemHocPhan> bangDiem =
            (List<BangDiemHocPhan>) request.getAttribute("bangDiem");
%>
<h1 class="trang-tieude">Bảng điểm cá nhân</h1>
<p class="trang-mota">Điểm chi tiết theo từng lớp học phần.</p>

<div class="the">
    <% if (bangDiem == null || bangDiem.isEmpty()) { %>
        <p class="trong-rong">Chưa có lớp học phần hoặc thành phần điểm nào.</p>
    <% } else {
        for (BangDiemHocPhan hocPhan : bangDiem) {
    %>
        <h3 style="margin-top:20px;">
            <%= Html.escape(hocPhan.getMaLhp()) %> —
            <%= Html.escape(hocPhan.getTenMonHoc()) %>
        </h3>
        <div class="bang-cuon">
            <table>
                <thead>
                    <tr>
                        <th>Thành phần điểm</th>
                        <th>Trọng số</th>
                        <th>Điểm</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (ThanhPhanDiem thanhPhan : hocPhan.getThanhPhanDiems()) {
                        DiemSo diemThanhPhan = null;
                        for (DiemSo diem : hocPhan.getDiemSoList()) {
                            if (diem.getMaThanhphan() == thanhPhan.getMaThanhphan()) {
                                diemThanhPhan = diem;
                                break;
                            }
                        }
                    %>
                    <tr>
                        <td><%= Html.escape(thanhPhan.getTenThanhphan()) %></td>
                        <td><%= thanhPhan.getTrongSo().multiply(
                                java.math.BigDecimal.valueOf(100))
                                .stripTrailingZeros().toPlainString() %>%</td>
                        <td>
                            <%= diemThanhPhan == null || diemThanhPhan.getGiaTriDiem() == null
                                    ? "Chưa có điểm"
                                    : Html.escape(diemThanhPhan.getGiaTriDiem().toPlainString()) %>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
                <tfoot>
                    <tr>
                        <th colspan="2" style="text-align:right;">Điểm tổng kết</th>
                        <th><%= hocPhan.getDiemTongKet() == null
                                ? "Chưa đủ điểm"
                                : Html.escape(hocPhan.getDiemTongKet().toPlainString()) %></th>
                    </tr>
                </tfoot>
            </table>
        </div>
    <%  }
       } %>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
