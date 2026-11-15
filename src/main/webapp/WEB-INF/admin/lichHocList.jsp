<%@ page import="Models.LopHocPhan" %>
<%@ page import="Models.PhongHoc" %>
<%@ page import="Models.TimetableFilter" %>
<%@ page import="Models.TimetableWeek" %>
<%@ page import="Utils.Html" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<LopHocPhan> dsLHP = (List<LopHocPhan>) request.getAttribute("dsLHP");
    @SuppressWarnings("unchecked")
    List<PhongHoc> dsPhong = (List<PhongHoc>) request.getAttribute("dsPhong");
    TimetableWeek adminTimetable =
            (TimetableWeek) request.getAttribute("timetable");
    TimetableFilter selected =
            (TimetableFilter) request.getAttribute("timetableFilter");
    String timetableBasePath = String.valueOf(
            request.getAttribute("timetableBasePath"));
    String msg = request.getParameter("msg");
    String err = request.getParameter("error");
    if (err == null && request.getAttribute("error") != null) {
        err = String.valueOf(request.getAttribute("error"));
    }
    Map<Integer, String> terms = new LinkedHashMap<>();
    Map<Integer, String> sections = new LinkedHashMap<>();
    Map<Integer, String> teachers = new LinkedHashMap<>();
    if (dsLHP != null) {
        for (LopHocPhan row : dsLHP) {
            terms.put(row.getMaHocky(), row.getTenHocKy());
            sections.put(row.getMaLophocphan(),
                    row.getMaLhp() + " - " + row.getTenMonHoc());
            teachers.put(row.getMaGiangvien(), row.getTenGiangVien());
        }
    }
%>
<div class="thanh-cong-cu">
    <div>
        <h1 class="trang-tieude">Thời khóa biểu</h1>
        <p class="trang-mota">Xem lịch học toàn trường theo tuần và bộ lọc.</p>
    </div>
    <a class="nut" href="<%= Html.escape(request.getContextPath()) %>/admin/lichhoc?action=add">+ Thêm buổi học</a>
</div>
<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (err != null) { %><div class="bao-loi"><%= Html.escape(err) %></div><% } %>

<form class="the" method="get" action="<%= Html.escape(request.getContextPath() + timetableBasePath) %>">
    <input type="hidden" name="week" value="<%= Html.escape(adminTimetable.getWeekStart()) %>">
    <div class="nhom-form">
        <label for="lich-hoc-hoc-ky">Học kỳ</label>
        <select id="lich-hoc-hoc-ky" class="o-nhap" name="maHocKy">
            <option value="">Tất cả học kỳ</option>
            <% for (Map.Entry<Integer, String> option : terms.entrySet()) { %>
                <option value="<%= option.getKey() %>" <%= option.getKey().equals(selected.getTermId()) ? "selected" : "" %>><%= Html.escape(option.getValue()) %></option>
            <% } %>
        </select>
    </div>
    <div class="nhom-form">
        <label for="lich-hoc-lhp">Lớp học phần</label>
        <select id="lich-hoc-lhp" class="o-nhap" name="maLhp">
            <option value="">Tất cả lớp học phần</option>
            <% for (Map.Entry<Integer, String> option : sections.entrySet()) { %>
                <option value="<%= option.getKey() %>" <%= option.getKey().equals(selected.getSectionId()) ? "selected" : "" %>><%= Html.escape(option.getValue()) %></option>
            <% } %>
        </select>
    </div>
    <div class="nhom-form">
        <label for="lich-hoc-giang-vien">Giảng viên</label>
        <select id="lich-hoc-giang-vien" class="o-nhap" name="maGiangVien">
            <option value="">Tất cả giảng viên</option>
            <% for (Map.Entry<Integer, String> option : teachers.entrySet()) { %>
                <option value="<%= option.getKey() %>" <%= option.getKey().equals(selected.getTeacherId()) ? "selected" : "" %>><%= Html.escape(option.getValue()) %></option>
            <% } %>
        </select>
    </div>
    <div class="nhom-form">
        <label for="lich-hoc-phong">Phòng</label>
        <select id="lich-hoc-phong" class="o-nhap" name="maPhong">
            <option value="">Tất cả phòng</option>
            <% if (dsPhong != null) for (PhongHoc option : dsPhong) { %>
                <option value="<%= option.getMaPhong() %>" <%= Integer.valueOf(option.getMaPhong()).equals(selected.getRoomId()) ? "selected" : "" %>><%= Html.escape(option.getTenPhong()) %></option>
            <% } %>
        </select>
    </div>
    <div class="nhom-form">
        <label>&nbsp;</label>
        <button class="nut" type="submit">Áp dụng</button>
        <a class="nut nut-xam" href="<%= Html.escape(request.getContextPath() + timetableBasePath) %>?week=<%= Html.escape(adminTimetable.getWeekStart()) %>">Xóa lọc</a>
    </div>
</form>

<%@ include file="/WEB-INF/common/timetableWeek.jspf" %>
<%@ include file="/WEB-INF/footer.jsp" %>
