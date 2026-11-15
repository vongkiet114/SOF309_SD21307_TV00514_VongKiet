<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="Models.LopHocPhan" %>
<%@ page import="Models.SinhVien" %>
<%@ page import="Models.ThanhPhanDiem" %>
<%@ page import="Models.DiemSo" %>
<%@ page import="Services.NhapDiemService.NhapDiemData" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<LopHocPhan> dsLHP = (List<LopHocPhan>) request.getAttribute("dsLHP");
    Integer maLHPChon = (Integer) request.getAttribute("maLHP");
    NhapDiemData data = (NhapDiemData) request.getAttribute("nhapDiemData");
    @SuppressWarnings("unchecked")
    Map<String, String> submittedScores =
            (Map<String, String>) request.getAttribute("submittedScores");
    String msg = request.getParameter("msg");
    String err = (String) request.getAttribute("error");
    if (err == null) err = request.getParameter("error");
%>
<h1 class="trang-tieude">Nhập điểm học phần</h1>
<p class="trang-mota">Nhập điểm thành phần cho sinh viên. Hệ thống sẽ tự tính điểm tổng kết.</p>

<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<div class="the">
    <form method="get" action="<%= request.getContextPath() %>/giangvien/nhapdiem">
        <label style="font-weight:600; font-size:13px;">Chọn lớp học phần:</label>
        <select name="maLHP" class="o-nhap" style="max-width:420px; display:inline-block;" onchange="this.form.submit()">
            <option value="">-- Chọn lớp --</option>
            <% if (dsLHP != null) for (LopHocPhan lhp : dsLHP) {
                   boolean selected = maLHPChon != null && lhp.getMaLophocphan() == maLHPChon; %>
                <option value="<%= lhp.getMaLophocphan() %>" <%= selected ? "selected" : "" %>><%= Html.escape(lhp.getMaLhp()) %> — <%= Html.escape(lhp.getTenMonHoc()) %> — <%= Html.escape(lhp.getTenHocKy()) %></option>
            <% } %>
        </select>
    </form>
</div>

<% if (data != null) { %>
<div class="the">
    <div style="display:flex; gap:12px; align-items:center; flex-wrap:wrap;">
        <a class="nut nut-phu" href="<%= request.getContextPath() %>/giangvien/nhapdiem/excel?action=template&amp;maLHP=<%= maLHPChon %>">
            Tải mẫu Excel
        </a>
        <form method="post"
              action="<%= request.getContextPath() %>/giangvien/nhapdiem/excel"
              enctype="multipart/form-data"
              style="display:flex; gap:8px; align-items:center; flex-wrap:wrap; margin:0;">
            <input type="hidden" name="action" value="preview">
            <input type="hidden" name="_csrf" value="<%= Html.escape(request.getAttribute("csrfToken")) %>">
            <input class="o-nhap" type="file" name="file" accept=".xlsx" required
                   style="max-width:360px;">
            <button type="submit" class="nut">Xem trước file Excel</button>
        </form>
    </div>
    <p class="trang-mota" style="margin:10px 0 0;">
        Chỉ dùng mẫu do hệ thống xuất, định dạng .xlsx và dung lượng tối đa 5 MB.
    </p>
</div>
<div class="the">
    <div style="display:flex; justify-content:space-between; align-items:end; margin-bottom:12px;">
        <h2 style="margin:0">Danh sách sinh viên</h2>
        <div class="nhom-form" style="margin:0">
            <input type="text" class="o-nhap" placeholder="Tìm theo MSSV, Họ tên..." data-table-filter="grade-table">
        </div>
    </div>
    <form method="post" action="<%= request.getContextPath() %>/giangvien/nhapdiem">
        <input type="hidden" name="maLHP" value="<%= maLHPChon %>">
        <div class="bang-cuon bang-diem-lon">
            <table id="grade-table" data-page-size="25">
                <thead>
                    <tr>
                        <th class="cot-dinh">MSSV</th>
                        <th class="cot-dinh-2">Họ và tên</th>
                        <% for (ThanhPhanDiem tpd : data.getThanhPhanDiems()) { %>
                            <th><%= Html.escape(tpd.getTenThanhphan()) %> (<%= tpd.getTrongSo().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() %>%)</th>
                        <% } %>
                    </tr>
                </thead>
                <tbody>
                    <% for (SinhVien sv : data.getSinhViens()) { %>
                    <tr>
                        <td class="cot-dinh"><%= Html.escape(sv.getMssv()) %></td>
                        <td class="cot-dinh-2"><strong><%= Html.escape(sv.getHoTen()) %></strong></td>
                        <% for (ThanhPhanDiem tpd : data.getThanhPhanDiems()) {
                               String diemValue = "";
                               String cellKey = sv.getMaDanhSach() + ":" + tpd.getMaThanhphan();
                               if (submittedScores != null && submittedScores.containsKey(cellKey)) {
                                   diemValue = submittedScores.get(cellKey);
                               } else if (data.getExistingScores() != null && data.getExistingScores().containsKey(sv.getMaDanhSach())) {
                                   for (DiemSo ds : data.getExistingScores().get(sv.getMaDanhSach())) {
                                       if (ds.getMaThanhphan() == tpd.getMaThanhphan()) {
                                           diemValue = ds.getGiaTriDiem() == null
                                                   ? "" : ds.getGiaTriDiem().toPlainString();
                                           break;
                                       }
                                   }
                               }
                        %>
                            <td>
                                <input type="hidden" name="maDanhSach" value="<%= sv.getMaDanhSach() %>">
                                <input type="hidden" name="maThanhPhan" value="<%= tpd.getMaThanhphan() %>">
                                <%
                                    boolean isError = false;
                                    if (submittedScores != null && submittedScores.containsKey(cellKey)) {
                                        try {
                                            if (!diemValue.isEmpty()) {
                                                double val = Double.parseDouble(diemValue);
                                                if (val < 0 || val > 10) isError = true;
                                            }
                                        } catch(Exception e) { isError = true; }
                                    }
                                %>
                                <input class="o-nhap o-diem <%= isError ? "o-diem-loi" : "" %>" type="number" step="0.01" min="0" max="10" name="diem" value="<%= Html.escape(diemValue) %>">
                            </td>
                        <% } %>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <div style="margin-top:14px; text-align:right;">
            <button type="submit" class="nut">Lưu tất cả thay đổi</button>
        </div>
    </form>
</div>
<% } %>
<%@ include file="/WEB-INF/footer.jsp" %>
