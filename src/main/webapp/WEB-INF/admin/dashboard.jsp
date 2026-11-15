<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="Models.AdminDashboardView" %>
<%@ page import="Models.HoatDongDashboard" %>
<%@ page import="Models.LichHoc" %>
<%@ page import="Utils.Html" %>
<%
    request.setAttribute("trang", "dashboard");
    request.setAttribute("tieuDeTrang", "Bảng điều khiển");
%>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    AdminDashboardView dashboard =
            (AdminDashboardView) request.getAttribute("dashboard");
    DateTimeFormatter dinhDangThoiGian =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<h1 class="trang-tieude">Xin chào, <%= Html.escape(hoTen) %>!</h1>
<p class="trang-mota">Các số liệu và công việc vận hành cần quan tâm hôm nay.</p>

<div class="luoi-thongke">
    <div class="the-thongke"><p>Sinh viên đang hoạt động</p><h2><%= dashboard.getSoSinhVienHoatDong() %></h2><span class="nhan" style="color:#2563eb">tài khoản hoạt động, đang học</span></div>
    <div class="the-thongke"><p>Giảng viên đang hoạt động</p><h2><%= dashboard.getSoGiangVienHoatDong() %></h2><span class="nhan" style="color:#16a34a">tài khoản hoạt động</span></div>
    <div class="the-thongke"><p>Lớp học phần mở/hoạt động</p><h2><%= dashboard.getSoLopHocPhanMo() %></h2><span class="nhan" style="color:#f59e0b">mở lớp hoặc đang học</span></div>
    <div class="the-thongke"><p>Buổi học hôm nay</p><h2><%= dashboard.getSoBuoiHomNay() %></h2><span class="nhan" style="color:#64748b">buổi bình thường</span></div>
</div>

<div class="the">
    <div class="the-tieude">Thao tác nhanh</div>
    <div class="thanh-cong-cu" style="justify-content:flex-start;">
        <a class="nut" href="<%= contextPath %>/admin/sinhvien?action=add">Thêm sinh viên</a>
        <a class="nut nut-vien" href="<%= contextPath %>/admin/giangvien?action=add">Thêm giảng viên</a>
        <a class="nut nut-vien" href="<%= contextPath %>/admin/lophocphan?action=add">Thêm lớp học phần</a>
        <a class="nut nut-vien" href="<%= contextPath %>/admin/lichhoc?action=add">Thêm lịch học</a>
    </div>
</div>

<div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(300px,1fr)); gap:16px;">
    <div class="the">
        <div class="the-tieude">Hoạt động gần đây</div>
        <% if (dashboard.getHoatDongGanDay().isEmpty()) { %>
            <p class="trong-rong">Chưa có hoạt động nào được ghi nhận.</p>
        <% } else { %>
            <div class="bang-cuon">
                <table>
                    <thead><tr><th>Người thực hiện</th><th>Hành động</th><th>Thời gian</th></tr></thead>
                    <tbody>
                    <% for (HoatDongDashboard activity : dashboard.getHoatDongGanDay()) { %>
                        <tr>
                            <td><strong><%= Html.escape(activity.getHoTen()) %></strong></td>
                            <td><%= Html.escape(activity.getHanhDong()) %></td>
                            <td><%= activity.getThoiGian() == null
                                    ? "Chưa cập nhật"
                                    : activity.getThoiGian().format(dinhDangThoiGian) %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>

    <div class="the">
        <div class="the-tieude">Vận hành hôm nay</div>
        <% if (dashboard.getLichHomNay().isEmpty()) { %>
            <p class="trong-rong">Hôm nay không có buổi học bình thường nào.</p>
        <% } else { %>
            <div class="bang-cuon">
                <table>
                    <thead><tr><th>Ca</th><th>Lớp học phần</th><th>Phòng</th><th>Thao tác</th></tr></thead>
                    <tbody>
                    <% for (LichHoc schedule : dashboard.getLichHomNay()) { %>
                        <tr>
                            <td><%= Html.escape(schedule.getTenCa() != null
                                    ? schedule.getTenCa() : "Ca #" + schedule.getMaCa()) %></td>
                            <td><strong><%= Html.escape(schedule.getMaLhp()) %></strong></td>
                            <td><%= Html.escape(schedule.getTenPhong() != null
                                    ? schedule.getTenPhong() : "Phòng #" + schedule.getMaPhong()) %></td>
                            <td>
                                <div style="display:flex; flex-wrap:wrap; gap:8px;">
                                    <a class="nut nut-nho" href="<%= contextPath %>/admin/lichhoc">Quản lý lịch</a>
                                    <a class="nut nut-nho nut-vien" href="<%= contextPath %>/admin/diemdanhbu?maLHP=<%= schedule.getMaLophocphan() %>&amp;maLichhoc=<%= schedule.getMaLichhoc() %>">Điểm danh</a>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
