<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.KyThi" %>
<%@ page import="Models.LichHoc" %>
<%@ page import="Models.SinhVienDashboardView" %>
<%@ page import="Models.ThongBao" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    SinhVienDashboardView dashboard =
            (SinhVienDashboardView) request.getAttribute("dashboard");
    DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>
<h1 class="trang-tieude">Xin chào, <%= Html.escape(hoTen) %>!</h1>
<p class="trang-mota">Các công việc học tập cần bạn quan tâm.</p>

<div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:16px; margin-bottom:16px;">
    <div class="the">
        <p style="color:#6b7280; margin:0">Học phần đang học</p>
        <h2 style="margin:6px 0; font-size:32px;"><%= dashboard.getSoLopDangHoc() %></h2>
    </div>
    <div class="the">
        <p style="color:#6b7280; margin:0">Buổi học hôm nay</p>
        <h2 style="margin:6px 0; font-size:32px;"><%= dashboard.getSoBuoiHomNay() %></h2>
    </div>
    <div class="the">
        <p style="color:#6b7280; margin:0">Kỳ thi sắp tới</p>
        <h2 style="margin:6px 0; font-size:32px;"><%= dashboard.getSoKyThiSapToi() %></h2>
    </div>
    <div class="the">
        <p style="color:#6b7280; margin:0">Buổi vắng trong học kỳ</p>
        <h2 style="margin:6px 0; font-size:32px;"><%= dashboard.getSoBuoiVang() %></h2>
        <% if (!dashboard.isCoHocKyHienTai()) { %>
            <small style="color:#6b7280;">Hiện chưa có học kỳ đang diễn ra.</small>
        <% } %>
    </div>
</div>

<div class="the">
    <div class="the-tieude">
        <%= dashboard.isDangHienThiLichGanNhat() ? "Buổi học gần nhất" : "Lịch học hôm nay" %>
    </div>
    <div class="bang-cuon">
        <table>
            <thead>
            <tr><th>Ngày học</th><th>Lớp học phần</th><th>Môn học</th><th>Ca học</th><th>Phòng</th></tr>
            </thead>
            <tbody>
            <% if (dashboard.getLichNoiBat().isEmpty()) { %>
                <tr><td colspan="5"><span class="trong-rong">Chưa có buổi học sắp tới.</span></td></tr>
            <% } else { for (LichHoc lich : dashboard.getLichNoiBat()) { %>
                <tr>
                    <td><%= lich.getNgayHoc().format(dinhDangNgay) %></td>
                    <td><strong><%= Html.escape(lich.getMaLhp()) %></strong></td>
                    <td><%= Html.escape(lich.getTenMonHoc()) %></td>
                    <td><%= Html.escape(lich.getTenCa() != null ? lich.getTenCa() : "Ca #" + lich.getMaCa()) %></td>
                    <td><%= Html.escape(lich.getTenPhong() != null ? lich.getTenPhong() : "Phòng #" + lich.getMaPhong()) %></td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
</div>

<div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(300px,1fr)); gap:16px; margin-top:16px;">
    <div class="the">
        <div class="the-tieude">Kỳ thi gần nhất</div>
        <% if (dashboard.getKyThiSapToi().isEmpty()) { %>
            <p class="trong-rong">Chưa có kỳ thi sắp tới.</p>
        <% } else { %>
            <div class="bang-cuon">
                <table>
                    <thead><tr><th>Ngày thi</th><th>Môn học</th><th>Ca</th><th>Phòng</th></tr></thead>
                    <tbody>
                    <% for (KyThi kyThi : dashboard.getKyThiSapToi()) { %>
                        <tr>
                            <td><%= kyThi.getNgayThi().format(dinhDangNgay) %></td>
                            <td><%= Html.escape(kyThi.getTenMonHoc()) %></td>
                            <td><%= Html.escape(kyThi.getTenCa() != null ? kyThi.getTenCa() : "Ca #" + kyThi.getMaCa()) %></td>
                            <td><%= Html.escape(kyThi.getTenPhong() != null ? kyThi.getTenPhong() : "Phòng #" + kyThi.getMaPhong()) %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>

    <div class="the">
        <div class="the-tieude">Thông báo mới</div>
        <% if (dashboard.getThongBaoMoi().isEmpty()) { %>
            <p class="trong-rong">Chưa có thông báo mới.</p>
        <% } else { %>
            <ul style="margin:0; padding-left:20px;">
                <% for (ThongBao thongBao : dashboard.getThongBaoMoi()) { %>
                    <li style="margin-bottom:10px;">
                        <strong><%= Html.escape(thongBao.getTieuDe()) %></strong>
                        <% if (thongBao.getNgayTao() != null) { %>
                            <span style="color:#6b7280;"> · <%= thongBao.getNgayTao().toLocalDate().format(dinhDangNgay) %></span>
                        <% } %>
                    </li>
                <% } %>
            </ul>
        <% } %>
    </div>
</div>

<div class="the" style="margin-top:16px;">
    <div class="the-tieude">Lối tắt</div>
    <div style="display:flex; flex-wrap:wrap; gap:12px;">
        <a href="<%= contextPath %>/sinhvien/lichhoc">Xem thời khóa biểu</a>
        <a href="<%= contextPath %>/sinhvien/kythi">Xem lịch thi</a>
        <a href="<%= contextPath %>/sinhvien/diemdanh">Xem lịch sử điểm danh</a>
        <a href="<%= contextPath %>/sinhvien/thongbao">Xem thông báo</a>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
