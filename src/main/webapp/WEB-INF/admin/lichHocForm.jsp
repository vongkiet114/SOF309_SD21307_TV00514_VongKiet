<%@ page import="java.util.List" %>
<%@ page import="Models.LichHoc" %>
<%@ page import="Models.LopHocPhan" %>
<%@ page import="Models.PhongHoc" %>
<%@ page import="Models.CaHoc" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%!
    String opt(int value, String nhan, int dangChon) {
        String selected = value == dangChon ? " selected" : "";
        return "<option value=\"" + value + "\"" + selected + ">" + Html.escape(nhan) + "</option>";
    }
    int soNguyen(String value, int macDinh) {
        try { return Integer.parseInt(value); }
        catch (Exception ignored) { return macDinh; }
    }
%>
<%
    LichHoc lichHoc = (LichHoc) request.getAttribute("lichHoc");
    Boolean coSua = (Boolean) request.getAttribute("isEdit");
    boolean laSua = coSua != null ? coSua : lichHoc != null;
    boolean coDuLieu = lichHoc != null;
    String error = (String) request.getAttribute("error");
    @SuppressWarnings("unchecked")
    List<LopHocPhan> dsLHP = (List<LopHocPhan>) request.getAttribute("dsLHP");
    @SuppressWarnings("unchecked")
    List<PhongHoc> dsPhong = (List<PhongHoc>) request.getAttribute("dsPhong");
    @SuppressWarnings("unchecked")
    List<CaHoc> dsCa = (List<CaHoc>) request.getAttribute("dsCa");

    int lhpChon = coDuLieu ? lichHoc.getMaLophocphan()
            : soNguyen(request.getParameter("maLophocphan"), -1);
    int phongChon = coDuLieu ? lichHoc.getMaPhong()
            : soNguyen(request.getParameter("maPhong"), -1);
    int caChon = coDuLieu ? lichHoc.getMaCa() : -1;
    String loaiChon = coDuLieu ? lichHoc.getLoaiLich()
            : request.getParameter("loaiLich") == null ? "HocChinh" : request.getParameter("loaiLich");
    String trangThaiChon = coDuLieu ? lichHoc.getTrangThai()
            : request.getParameter("trangThai") == null ? "BinhThuong" : request.getParameter("trangThai");
    String tuNgayChon = request.getParameter("tuNgay") == null ? "" : request.getParameter("tuNgay");
    String denNgayChon = request.getParameter("denNgay") == null ? "" : request.getParameter("denNgay");
    String[] caThuGuiLen = request.getParameterValues("maCa_Thu");
    java.util.Set<String> caThuDaChon = new java.util.HashSet<>();
    if (caThuGuiLen != null) java.util.Collections.addAll(caThuDaChon, caThuGuiLen);
%>
<h1 class="trang-tieude"><%= laSua ? "Sửa buổi học" : "Thêm buổi học" %></h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/lichhoc">
        <input type="hidden" name="action" value="<%= laSua ? "update" : "create" %>">
        <% if (laSua) { %><input type="hidden" name="id" value="<%= lichHoc.getMaLichhoc() %>"><% } %>

        <div class="nhom-form"><label>Lớp học phần</label>
            <select class="o-nhap" name="maLophocphan" required>
                <option value="">-- Chọn lớp học phần --</option>
                <% if (dsLHP != null) for (LopHocPhan lhp : dsLHP) {
                    String nhanLhp = lhp.getMaLhp() + " — " + lhp.getTenMonHoc()
                            + " — " + lhp.getTenGiangVien() + " — " + lhp.getTenHocKy(); %>
                    <%= opt(lhp.getMaLophocphan(), nhanLhp, lhpChon) %>
                <% } %>
            </select>
        </div>
        <div class="nhom-form"><label>Phòng học</label>
            <select class="o-nhap" name="maPhong" required>
                <option value="">-- Chọn phòng học --</option>
                <% if (dsPhong != null) for (PhongHoc phong : dsPhong) { %>
                    <%= opt(phong.getMaPhong(), phong.getTenPhong(), phongChon) %>
                <% } %>
            </select>
        </div>
        <% if (laSua) { %>
        <div class="nhom-form"><label>Ca học</label>
            <select class="o-nhap" name="maCa" required>
                <% if (dsCa != null) for (CaHoc ca : dsCa) { %>
                    <%= opt(ca.getMaCa(), ca.getTenCa() + " (" + ca.getGioBatdau() + " - " + ca.getGioKetthuc() + ")", caChon) %>
                <% } %>
            </select>
        </div>
        <div class="nhom-form"><label>Ngày học</label>
            <input class="o-nhap" type="date" name="ngayHoc" min="<%= java.time.LocalDate.now() %>" value="<%= coDuLieu && lichHoc.getNgayHoc() != null ? lichHoc.getNgayHoc() : "" %>" required>
        </div>
        <% } else { %>
        <div class="nhom-form"><label>Từ ngày</label>
            <input class="o-nhap" type="date" name="tuNgay" min="<%= java.time.LocalDate.now() %>" value="<%= Html.escape(tuNgayChon) %>" required>
        </div>
        <div class="nhom-form"><label>Đến ngày</label>
            <input class="o-nhap" type="date" name="denNgay" min="<%= java.time.LocalDate.now() %>" value="<%= Html.escape(denNgayChon) %>" required>
        </div>
        <div class="nhom-form" style="max-width: 100%;"><label>Ca học (Bảng đặt chỗ: Chọn nhiều Ca/Thứ)</label>
            <div class="matrix-container" style="overflow-x: auto; border: 1px solid var(--mau-vien); border-radius: 8px;">
                <table class="bang-ma-tran" style="width: 100%; text-align: center; border-collapse: collapse;">
                    <thead>
                        <tr style="background: var(--mau-chinh-nhat);">
                            <th style="padding: 10px; border: 1px solid var(--mau-vien); border-bottom: 2px solid var(--mau-chinh);">Ca \ Thứ</th>
                            <% for (int thu = 2; thu <= 7; thu++) { %><th style="padding: 10px; border: 1px solid var(--mau-vien); border-bottom: 2px solid var(--mau-chinh);">Thứ <%= thu %></th><% } %>
                            <th style="padding: 10px; border: 1px solid var(--mau-vien); border-bottom: 2px solid var(--mau-chinh);">CN</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (dsCa != null) for (CaHoc ca : dsCa) { %>
                        <tr>
                            <td style="padding: 10px; border: 1px solid var(--mau-vien); font-weight: 600; background: #fafafa;">
                                <%= Html.escape(ca.getTenCa()) %><br><span style="font-size: 12px; color: var(--mau-chu-mo);"><%= ca.getGioBatdau() %> - <%= ca.getGioKetthuc() %></span>
                            </td>
                            <% for (int thu = 2; thu <= 8; thu++) { %>
                            <td style="padding: 10px; border: 1px solid var(--mau-vien); transition: background 0.2s;" onmouseover="this.style.background='#f0f9ff'" onmouseout="this.style.background='transparent'">
                                <label style="display: block; width: 100%; height: 100%; cursor: pointer;">
                                    <input type="checkbox" name="maCa_Thu" value="<%= ca.getMaCa() %>_<%= thu %>" <%= caThuDaChon.contains(ca.getMaCa() + "_" + thu) ? "checked" : "" %> style="width: 18px; height: 18px; accent-color: var(--mau-chinh);">
                                </label>
                            </td>
                            <% } %>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
            <p style="font-size: 13px; color: var(--mau-chu-mo); margin-top: 8px;">Lưu ý: Hệ thống sẽ tự động dò tìm các ngày khớp với "Thứ" trong khoảng "Từ ngày" đến "Đến ngày" để lên lịch.</p>
        </div>
        <% } %>
        <div class="nhom-form"><label>Loại buổi học</label>
            <select class="o-nhap" name="loaiLich" required>
                <option value="HocChinh" <%= "HocChinh".equals(loaiChon) ? "selected" : "" %>>Học chính</option>
                <option value="DayBu" <%= "DayBu".equals(loaiChon) ? "selected" : "" %>>Dạy bù</option>
            </select>
        </div>
        <div class="nhom-form"><label>Trạng thái</label>
            <select class="o-nhap" name="trangThai" required>
                <option value="BinhThuong" <%= "BinhThuong".equals(trangThaiChon) ? "selected" : "" %>>Bình thường</option>
                <option value="XinNghi" <%= "XinNghi".equals(trangThaiChon) ? "selected" : "" %>>Xin nghỉ</option>
                <option value="DaHuy" <%= "DaHuy".equals(trangThaiChon) ? "selected" : "" %>>Đã hủy</option>
            </select>
        </div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm buổi học" %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/lichhoc">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
