<%@ page import="java.util.List" %>
<%@ page import="Models.LopHocPhan" %>
<%@ page import="Models.SinhVien" %>
<%@ page import="Utils.Html" %>
<%@ page import="Utils.TrangThaiTheoNgay" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    LopHocPhan lhp = (LopHocPhan) request.getAttribute("lhp");
    @SuppressWarnings("unchecked")
    List<SinhVien> members = (List<SinhVien>) request.getAttribute("members");
    @SuppressWarnings("unchecked")
    List<SinhVien> candidates = (List<SinhVien>) request.getAttribute("candidates");
    String q = (String) request.getAttribute("q");
    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<div class="thanh-cong-cu">
    <div>
        <h1 class="trang-tieude">Danh sách sinh viên lớp học phần</h1>
        <p class="trang-mota">Admin trực tiếp thêm hoặc gỡ sinh viên theo quy định học vụ.</p>
    </div>
    <a class="nut nut-xam" href="<%= request.getContextPath() %>/admin/lophocphan">Quay lại danh sách lớp</a>
</div>

<% if (msg != null) { %><div class="bao-thanhcong"><%= Html.escape(msg) %></div><% } %>
<% if (error != null) { %><div class="bao-loi"><%= Html.escape(error) %></div><% } %>

<div class="the" style="margin-bottom:12px">
    <h2 style="margin-top:0"><%= Html.escape(lhp.getMaLhp()) %> — <%= Html.escape(lhp.getTenMonHoc()) %></h2>
    <div style="display:flex;gap:24px;flex-wrap:wrap">
        <span><strong>Giảng viên:</strong> <%= Html.escape(lhp.getTenGiangVien()) %></span>
        <span><strong>Học kỳ:</strong> <%= Html.escape(lhp.getTenHocKy()) %> — <%= Html.escape(lhp.getNamHoc()) %></span>
        <span><strong>Trạng thái:</strong> <%= Html.escape(TrangThaiTheoNgay.nhan(lhp.getTrangThai())) %></span>
        <span><strong>Sĩ số:</strong> <%= lhp.getSoLuongSV() %></span>
    </div>
</div>

<div class="the" style="margin-bottom:12px">
    <div style="display:flex; justify-content:space-between; align-items:end; margin-bottom:12px; flex-wrap:wrap; gap:12px">
        <h2 style="margin:0">Sinh viên trong lớp</h2>
        <div class="bo-loc-bang" data-filter-table="roster-table" style="margin:0">
            <div class="nhom-form" style="margin:0">
                <input type="text" class="o-nhap" placeholder="Tìm nhanh trong lớp..." data-filter-text>
            </div>
            <button class="nut nut-xam" type="button" data-filter-reset>Xóa lọc</button>
        </div>
    </div>
    <div class="bang-cuon">
        <table id="roster-table" data-page-size="20">
            <thead>
            <tr><th>MSSV</th><th>Họ tên</th><th>Lớp hành chính</th><th>Hành động</th></tr>
            </thead>
            <tbody>
            <% if (members == null || members.isEmpty()) { %>
                <tr><td colspan="4"><span class="trong-rong">Lớp học phần chưa có sinh viên.</span></td></tr>
            <% } else { for (SinhVien sv : members) { %>
                <tr>
                    <td><strong><%= Html.escape(sv.getMssv()) %></strong></td>
                    <td><%= Html.escape(sv.getHoTen()) %></td>
                    <td><%= Html.escape(sv.getTenLop()) %></td>
                    <td>
                        <form method="post" action="<%= request.getContextPath() %>/admin/lophocphan/sinhvien"
                              style="display:inline" onsubmit="return confirm('Gỡ sinh viên khỏi lớp học phần này?');">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="maLHP" value="<%= lhp.getMaLophocphan() %>">
                            <input type="hidden" name="maDanhsach" value="<%= sv.getMaDanhSach() %>">
                            <button class="nut nut-nho nut-do" type="submit">Gỡ</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
        <div class="trong-rong" data-filter-empty style="display:none">Không có sinh viên phù hợp bộ lọc.</div>
    </div>
</div>

<div class="the">
    <div class="thanh-cong-cu">
        <div>
            <h2 style="margin:0">Thêm sinh viên phù hợp</h2>
            <p class="trang-mota">Chỉ hiển thị sinh viên đang học và đúng chuyên ngành môn.</p>
        </div>
        <form method="get" action="<%= request.getContextPath() %>/admin/lophocphan/sinhvien"
              style="display:flex;gap:8px;align-items:end">
            <input type="hidden" name="maLHP" value="<%= lhp.getMaLophocphan() %>">
            <div class="nhom-form" style="margin:0">
                <label for="roster-search">Tìm theo MSSV hoặc họ tên</label>
                <input id="roster-search" class="o-nhap" type="search" name="q"
                       value="<%= Html.escape(q) %>" placeholder="Ví dụ: SV001 hoặc Nguyễn An">
            </div>
            <button class="nut" type="submit">Tìm kiếm</button>
        </form>
    </div>

    <div class="bang-cuon">
        <table id="candidate-table" data-page-size="20">
            <thead>
            <tr><th>MSSV</th><th>Họ tên</th><th>Lớp hành chính</th><th>Hành động</th></tr>
            </thead>
            <tbody>
            <% if (candidates == null || candidates.isEmpty()) { %>
                <tr><td colspan="4"><span class="trong-rong">Không có sinh viên phù hợp.<% if (q != null && !q.isEmpty()) { %> Thử thay đổi từ khóa.<% } %></span></td></tr>
            <% } else { for (SinhVien sv : candidates) { %>
                <tr>
                    <td><strong><%= Html.escape(sv.getMssv()) %></strong></td>
                    <td><%= Html.escape(sv.getHoTen()) %></td>
                    <td><%= Html.escape(sv.getTenLop()) %></td>
                    <td>
                        <form method="post" action="<%= request.getContextPath() %>/admin/lophocphan/sinhvien" style="display:inline">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="maLHP" value="<%= lhp.getMaLophocphan() %>">
                            <input type="hidden" name="maNguoidung" value="<%= sv.getMaNguoidung() %>">
                            <button class="nut nut-nho" type="submit">Thêm</button>
                        </form>
                    </td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
