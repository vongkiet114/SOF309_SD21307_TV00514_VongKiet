<%@ page import="java.util.List" %>
<%@ page import="Models.KyThi" %>
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
%>
<%
    KyThi kt = (KyThi) request.getAttribute("kyThi");
    Boolean coSua = (Boolean) request.getAttribute("isEdit");
    boolean laSua = coSua != null ? coSua : kt != null;
    boolean coDuLieu = kt != null;
    String error = (String) request.getAttribute("error");
    @SuppressWarnings("unchecked")
    List<LopHocPhan> dsLHP = (List<LopHocPhan>) request.getAttribute("dsLHP");
    @SuppressWarnings("unchecked")
    List<PhongHoc> dsPhong = (List<PhongHoc>) request.getAttribute("dsPhong");
    @SuppressWarnings("unchecked")
    List<CaHoc> dsCa = (List<CaHoc>) request.getAttribute("dsCa");

    int lhpChon = coDuLieu ? kt.getMaLophocphan() : -1;
    int phongChon = coDuLieu ? kt.getMaPhong() : -1;
    int caChon = coDuLieu ? kt.getMaCa() : -1;
    String loaiChon = coDuLieu ? kt.getLoaiKythi() : "GiuaKy";
%>
<h1 class="trang-tieude"><%= laSua ? "Sửa kỳ thi" : "Thêm kỳ thi" %></h1>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>
<div class="the">
    <form method="post" action="<%= request.getContextPath() %>/admin/kythi">
        <input type="hidden" name="action" value="<%= laSua ? "update" : "create" %>">
        <% if (laSua) { %><input type="hidden" name="id" value="<%= kt.getMaKythi() %>"><% } %>

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
        <div class="nhom-form"><label>Loại kỳ thi</label>
            <select class="o-nhap" name="loaiKythi" required>
                <option value="GiuaKy" <%= "GiuaKy".equals(loaiChon) ? "selected" : "" %>>Giữa kỳ</option>
                <option value="CuoiKy" <%= "CuoiKy".equals(loaiChon) ? "selected" : "" %>>Cuối kỳ</option>
                <option value="BaoVe" <%= "BaoVe".equals(loaiChon) ? "selected" : "" %>>Bảo vệ</option>
            </select>
        </div>
        <div class="nhom-form"><label>Ngày thi</label>
            <input class="o-nhap" type="date" name="ngayThi" min="<%= java.time.LocalDate.now() %>" value="<%= coDuLieu && kt.getNgayThi() != null ? kt.getNgayThi() : "" %>" required>
        </div>
        <div class="nhom-form"><label>Phòng thi</label>
            <select class="o-nhap" name="maPhong" required>
                <option value="">-- Chọn phòng thi --</option>
                <% if (dsPhong != null) for (PhongHoc phong : dsPhong) { %>
                    <%= opt(phong.getMaPhong(), phong.getTenPhong(), phongChon) %>
                <% } %>
            </select>
        </div>
        <div class="nhom-form"><label>Ca thi</label>
            <select class="o-nhap" name="maCa" required>
                <option value="">-- Chọn ca thi --</option>
                <% if (dsCa != null) for (CaHoc ca : dsCa) {
                    String nhanCa = ca.getTenCa() + " (" + ca.getGioBatdau() + " - " + ca.getGioKetthuc() + ")"; %>
                    <%= opt(ca.getMaCa(), nhanCa, caChon) %>
                <% } %>
            </select>
        </div>
        <button class="nut" type="submit"><%= laSua ? "Cập nhật" : "Thêm kỳ thi" %></button>
        <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/kythi">Hủy</a>
    </form>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
