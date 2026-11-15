<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.LichHoc" %>
<%@ page import="Models.LopHocPhan" %>
<%@ page import="Models.SinhVien" %>
<%@ page import="Utils.Html" %>
<%
    request.setAttribute("trang", "diemdanhbu");
    request.setAttribute("tieuDeTrang", "Điểm danh bù");
%>
<%@ include file="/WEB-INF/header.jsp" %>

<%
    @SuppressWarnings("unchecked")
    List<LopHocPhan> dsLHP = (List<LopHocPhan>) request.getAttribute("dsLHP");
    Integer maLHPChon = (Integer) request.getAttribute("maLHP");
    Integer maLichhocChon = (Integer) request.getAttribute("maLichhoc");
    @SuppressWarnings("unchecked")
    List<LichHoc> dsBuoi = (List<LichHoc>) request.getAttribute("dsBuoi");
    @SuppressWarnings("unchecked")
    List<SinhVien> bang = (List<SinhVien>) request.getAttribute("bangDiemDanh");
    @SuppressWarnings("unchecked")
    Map<String, String> submitted =
            (Map<String, String>) request.getAttribute("submittedAttendance");

    String thongBaoOk = request.getParameter("msg");
    String thongBaoLoi = request.getParameter("error");
    Object loiThuoc = request.getAttribute("error");
    String urlGoc = request.getContextPath();
    DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>

<h1 class="trang-tieude">Điểm danh lớp học phần</h1>
<p class="trang-mota">Chọn lớp học phần và buổi học bình thường để điểm danh cả lớp.</p>

<% if (thongBaoOk != null && !thongBaoOk.isEmpty()) { %>
    <div class="bao-thanhcong"><%= Html.escape(thongBaoOk) %></div>
<% } %>
<% if (thongBaoLoi != null && !thongBaoLoi.isEmpty()) { %>
    <div class="bao-loi"><%= Html.escape(thongBaoLoi) %></div>
<% } %>
<% if (loiThuoc != null) { %>
    <div class="bao-loi"><%= Html.escape(loiThuoc) %></div>
<% } %>

<div class="the">
    <form method="get" action="<%= urlGoc %>/admin/diemdanhbu"
          class="diemdanh-bo-loc">
        <div>
            <label for="diemdanh-lop">Lớp học phần</label>
            <select id="diemdanh-lop" name="maLHP" class="o-nhap"
                    onchange="this.form.submit()">
                <option value="">-- Chọn lớp học phần --</option>
                <% if (dsLHP != null) {
                    for (LopHocPhan lop : dsLHP) {
                        boolean chon = maLHPChon != null
                                && maLHPChon == lop.getMaLophocphan(); %>
                    <option value="<%= lop.getMaLophocphan() %>" <%= chon ? "selected" : "" %>>
                        <%= Html.escape(lop.getMaLhp()) %> - <%= Html.escape(lop.getTenMonHoc()) %>
                    </option>
                <%  }
                } %>
            </select>
        </div>

        <% if (maLHPChon != null && dsBuoi != null) { %>
            <div>
                <label for="diemdanh-buoi">Buổi học</label>
                <select id="diemdanh-buoi" name="maLichhoc" class="o-nhap"
                        onchange="this.form.submit()">
                    <option value="">-- Chọn buổi học --</option>
                    <% for (LichHoc buoi : dsBuoi) {
                        boolean chon = maLichhocChon != null
                                && maLichhocChon == buoi.getMaLichhoc(); %>
                        <option value="<%= buoi.getMaLichhoc() %>" <%= chon ? "selected" : "" %>>
                            <%= buoi.getNgayHoc() == null ? ""
                                    : buoi.getNgayHoc().format(dinhDangNgay) %>
                            - <%= Html.escape(buoi.getTenCa()) %>
                        </option>
                    <% } %>
                </select>
            </div>
        <% } %>
    </form>
</div>

<% if (maLHPChon != null && maLichhocChon != null && bang != null) { %>
    <div class="the">
        <div class="diemdanh-tieu-de-hanh-dong" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px">
            <div style="display:flex; align-items:center; gap:12px">
                <div class="the-tieude" style="margin:0">Danh sách điểm danh</div>
                <div class="bo-loc-bang" data-filter-table="attendance-table"
                     style="margin:0; display:flex; align-items:end; gap:8px">
                    <div class="nhom-form" style="margin:0">
                        <input type="text" class="o-nhap" placeholder="Tìm MSSV, họ tên..." data-filter-text>
                    </div>
                    <button class="nut nut-xam" type="button" data-filter-reset>Xóa lọc</button>
                </div>
            </div>
            <% if (!bang.isEmpty()) { %>
                <div class="diemdanh-nut-hang-loat" style="display:flex; align-items:center; gap:8px">
                    <span style="font-size:13px; color:#666">Đã chọn: <span id="badge-comat" class="nhan-tt nhan-xanh">0</span> Có mặt, <span id="badge-vang" class="nhan-tt nhan-do">0</span> Vắng</span>
                    <button type="button" class="nut nut-vien"
                            onclick="danhDauTatCa('CoMat')">Đánh dấu tất cả Có mặt</button>
                    <button type="button" class="nut nut-xam"
                            onclick="danhDauTatCa('Vang')">Đánh dấu tất cả Vắng</button>
                </div>
            <% } %>
        </div>

        <form method="post" action="<%= urlGoc %>/admin/diemdanhbu">
            <input type="hidden" name="maLHP" value="<%= maLHPChon %>">
            <input type="hidden" name="maLichhoc" value="<%= maLichhocChon %>">
            <div class="bang-cuon bang-diem-lon">
                <table id="attendance-table">
                    <thead>
                    <tr>
                        <th class="cot-dinh">MSSV</th>
                        <th class="cot-dinh-2">Họ và tên</th>
                        <th>Trạng thái</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (bang.isEmpty()) { %>
                        <tr>
                            <td colspan="3">
                                <span class="trong-rong">Lớp học phần chưa có sinh viên.</span>
                            </td>
                        </tr>
                    <% } else {
                        for (SinhVien sinhVien : bang) {
                            String membership = String.valueOf(sinhVien.getMaDanhSach());
                            String currentState = submitted != null
                                    && submitted.containsKey(membership)
                                    ? submitted.get(membership)
                                    : sinhVien.getTrangThaiDiemDanh();
                            boolean daDiemDanh = "CoMat".equals(currentState)
                                    || "Vang".equals(currentState); %>
                        <tr>
                            <td class="cot-dinh"><%= Html.escape(sinhVien.getMssv()) %></td>
                            <td class="cot-dinh-2"><strong><%= Html.escape(sinhVien.getHoTen()) %></strong></td>
                            <td>
                                <input type="hidden" name="maDanhSach"
                                       value="<%= sinhVien.getMaDanhSach() %>">
                                <span class="o-diemdanh">
                                    <label class="ios-switch">
                                        <input type="checkbox" <%= "CoMat".equals(currentState) ? "checked" : "" %>>
                                        <span class="ios-slider"></span>
                                        <input type="hidden" name="trangThai" value="<%= daDiemDanh ? currentState : "" %>" data-attendance-state>
                                    </label>
                                    <span class="trangthai-text">
                                        <%= !daDiemDanh ? "<span class='nhan-tt'>Chưa điểm danh</span>"
                                                : "CoMat".equals(currentState)
                                                ? "<span class='nhan-tt nhan-xanh'>Có mặt</span>"
                                                : "<span class='nhan-tt nhan-do'>Vắng</span>" %>
                                    </span>
                                </span>
                            </td>
                        </tr>
                    <%  }
                    } %>
                    </tbody>
                </table>
            </div>
            <% if (!bang.isEmpty()) { %>
                <div class="diemdanh-luu" style="position: sticky; bottom: 0; background: #fff; padding: 12px 0; border-top: 1px solid #ddd; text-align: right; z-index: 10">
                    <button type="submit" class="nut">Lưu điểm danh</button>
                </div>
            <% } %>
        </form>
    </div>
<% } %>

<script>
    function danhDauTatCa(trangThai) {
        document.querySelectorAll('[data-attendance-state]').forEach(function (input) {
            input.value = trangThai;
            var checkbox = input.parentElement.querySelector('input[type="checkbox"]');
            if (checkbox) checkbox.checked = (trangThai === 'CoMat');
            var span = input.parentElement.nextElementSibling;
            if (span && span.classList.contains('trangthai-text')) {
                span.innerHTML = (trangThai === 'CoMat') ? "<span class='nhan-tt nhan-xanh'>Có mặt</span>" : "<span class='nhan-tt nhan-do'>Vắng</span>";
            }
        });
        capNhatBadge();
    }
    function capNhatBadge() {
        var coMat = 0, vang = 0;
        document.querySelectorAll('[data-attendance-state]').forEach(function (input) {
            if (input.value === 'CoMat') coMat++;
            if (input.value === 'Vang') vang++;
        });
        var bCoMat = document.getElementById('badge-comat');
        var bVang = document.getElementById('badge-vang');
        if (bCoMat) bCoMat.textContent = coMat;
        if (bVang) bVang.textContent = vang;
    }
    document.addEventListener('change', function(e) {
        if (e.target.matches('.ios-switch input[type="checkbox"]')) {
            var hidden = e.target.parentElement.querySelector('input[type="hidden"]');
            if (hidden) {
                hidden.value = e.target.checked ? 'CoMat' : 'Vang';
                var span = e.target.parentElement.nextElementSibling;
                if (span && span.classList.contains('trangthai-text')) {
                    span.innerHTML = e.target.checked ? "<span class='nhan-tt nhan-xanh'>Có mặt</span>" : "<span class='nhan-tt nhan-do'>Vắng</span>";
                }
            }
            capNhatBadge();
        }
    });
    document.addEventListener('DOMContentLoaded', capNhatBadge);
</script>

<%@ include file="/WEB-INF/footer.jsp" %>
