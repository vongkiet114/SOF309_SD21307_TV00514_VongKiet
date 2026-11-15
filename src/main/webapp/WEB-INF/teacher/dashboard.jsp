<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.GiangVienDashboardView" %>
<%@ page import="Models.KyThi" %>
<%@ page import="Models.LichHoc" %>
<%@ page import="Models.ThongBao" %>
<%@ page import="Utils.Html" %>
<%
    request.setAttribute("trang", "dashboard_gv");
    request.setAttribute("tieuDeTrang", "Bảng điều khiển");
%>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    GiangVienDashboardView dashboard =
            (GiangVienDashboardView) request.getAttribute("dashboard");
    DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>

<h1 class="trang-tieude">Xin chào, <%= Html.escape(currentUser.getHoTen()) %>!</h1>
<p class="trang-mota">Các công việc giảng dạy cần bạn quan tâm hôm nay.</p>

<div class="luoi-thongke">
    <div class="the-thongke">
        <p>Lớp đang dạy</p>
        <h2><%= dashboard.getSoLopDangDay() %></h2>
        <span class="nhan" style="color:#2563eb">lớp học phần</span>
    </div>
    <div class="the-thongke">
        <p>Sinh viên phụ trách</p>
        <h2><%= dashboard.getSoSinhVienPhuTrach() %></h2>
        <span class="nhan" style="color:#16a34a">sinh viên duy nhất</span>
    </div>
    <div class="the-thongke">
        <p>Buổi dạy hôm nay</p>
        <h2><%= dashboard.getSoBuoiHomNay() %></h2>
        <span class="nhan" style="color:#f59e0b">buổi bình thường</span>
    </div>
    <div class="the-thongke">
        <p>Buổi cần điểm danh</p>
        <h2><%= dashboard.getSoBuoiCanDiemDanh() %></h2>
        <span class="nhan" style="color:#dc2626">chưa có dữ liệu điểm danh</span>
    </div>
</div>

<div class="the">
    <div class="the-tieude">Lịch dạy hôm nay</div>
    <div class="bang-cuon">
        <table>
            <thead>
            <tr>
                <th>Ca học</th>
                <th>Lớp học phần</th>
                <th>Môn học</th>
                <th>Phòng</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <% if (dashboard.getLichDayHomNay().isEmpty()) { %>
                <tr>
                    <td colspan="5">
                        <span class="trong-rong">Hôm nay bạn không có buổi dạy bình thường nào.</span>
                    </td>
                </tr>
            <% } else { for (LichHoc b : dashboard.getLichDayHomNay()) { %>
                <tr>
                    <td><%= Html.escape(b.getTenCa() != null
                            ? b.getTenCa() : "Ca #" + b.getMaCa()) %></td>
                    <td><strong><%= Html.escape(b.getMaLhp()) %></strong></td>
                    <td><%= Html.escape(b.getTenMonHoc()) %></td>
                    <td><%= Html.escape(b.getTenPhong() != null
                            ? b.getTenPhong() : "Phòng #" + b.getMaPhong()) %></td>
                    <td>
                        <div style="display:flex; flex-wrap:wrap; gap:8px;">
                            <a class="nut nut-nho" href="<%= contextPath %>/giangvien/diemdanh?maLHP=<%= b.getMaLophocphan() %>&amp;maLichhoc=<%= b.getMaLichhoc() %>">Điểm danh</a>
                            <a class="nut nut-nho nut-vien" href="<%= contextPath %>/giangvien/lopgiangday">Xem lớp</a>
                        </div>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
</div>

<div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(300px,1fr)); gap:16px; margin-top:16px;">
    <div class="the">
        <div class="the-tieude">Kỳ thi sắp tới</div>
        <% if (dashboard.getKyThiSapToi().isEmpty()) { %>
            <p class="trong-rong">Chưa có kỳ thi sắp tới.</p>
        <% } else { %>
            <div class="bang-cuon">
                <table>
                    <thead><tr><th>Ngày</th><th>Môn học</th><th>Ca</th><th>Phòng</th></tr></thead>
                    <tbody>
                    <% for (KyThi kyThi : dashboard.getKyThiSapToi()) { %>
                        <tr>
                            <td><%= kyThi.getNgayThi().format(dinhDangNgay) %></td>
                            <td><%= Html.escape(kyThi.getTenMonHoc()) %></td>
                            <td><%= Html.escape(kyThi.getTenCa() != null
                                    ? kyThi.getTenCa() : "Ca #" + kyThi.getMaCa()) %></td>
                            <td><%= Html.escape(kyThi.getTenPhong() != null
                                    ? kyThi.getTenPhong() : "Phòng #" + kyThi.getMaPhong()) %></td>
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
                            <span style="color:#6b7280;">
                                · <%= thongBao.getNgayTao().toLocalDate().format(dinhDangNgay) %>
                            </span>
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
        <a class="nut nut-nho" href="<%= contextPath %>/giangvien/lopgiangday">Lớp giảng dạy</a>
        <a class="nut nut-nho nut-vien" href="<%= contextPath %>/giangvien/diemdanh">Điểm danh</a>
        <a class="nut nut-nho nut-vien" href="<%= contextPath %>/giangvien/nhapdiem">Nhập điểm</a>
        <a class="nut nut-nho nut-vien" href="<%= contextPath %>/giangvien/lichday">Lịch dạy</a>
    </div>
</div>

<%@ include file="/WEB-INF/footer.jsp" %>
