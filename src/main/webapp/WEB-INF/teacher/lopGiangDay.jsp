<%@ page import="java.util.List" %>
<%@ page import="Models.LopHocPhan" %>
<%@ page import="Utils.Html" %>
<%-- trang danh sach lop hoc phan giang vien phu trach --%>
<%
    request.setAttribute("trang", "lopgiangday_gv");
    request.setAttribute("tieuDeTrang", "Lớp giảng dạy");
%>
<%@ include file="/WEB-INF/header.jsp" %>

<%
    @SuppressWarnings("unchecked")
    List<LopHocPhan> dsLop = (List<LopHocPhan>) request.getAttribute("dsLop");
    String gocUrl = request.getContextPath();
%>

<h1 class="trang-tieude">Danh sách lớp dạy phụ trách</h1>
<p class="trang-mota">Các lớp học phần bạn đang giảng dạy.</p>

<div class="the">
    <div class="bang-cuon">
        <table>
            <thead>
            <tr>
                <th>Mã LHP</th>
                <th>Môn học</th>
                <th>Số TC</th>
                <th>Học kỳ</th>
                <th>Sĩ số</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (dsLop == null || dsLop.isEmpty()) {
            %>
                <tr><td colspan="7"><span class="trong-rong">Bạn chưa được phân công lớp học phần nào.</span></td></tr>
            <%
                } else {
                    for (LopHocPhan lop : dsLop) {
                        // chon mau nhan theo trang thai lop hoc phan
                        String tt = lop.getTrangThai() == null ? "" : lop.getTrangThai();
                        String nhanCss; String nhanChu;
                        switch (tt) {
                            case "MoDangKy":   nhanCss = "nhan-xanh";  nhanChu = "Mở đăng ký";   break;
                            case "DongDangKy": nhanCss = "nhan-cam";   nhanChu = "Đóng đăng ký"; break;
                            case "DangHoc":    nhanCss = "nhan-duong"; nhanChu = "Đang học";     break;
                            case "HoanThanh":  nhanCss = "nhan-tim";   nhanChu = "Hoàn thành";   break;
                            case "Huy":        nhanCss = "nhan-do";    nhanChu = "Hủy";          break;
                            default:           nhanCss = "nhan-xam";   nhanChu = tt;
                        }
            %>
                <tr>
                    <td><strong><%= Html.escape(lop.getMaLhp()) %></strong></td>
                    <td><%= Html.escape(lop.getTenMonHoc()) %></td>
                    <td><%= lop.getSoTinChi() %></td>
                    <td><%= Html.escape(lop.getTenHocKy()) %> (<%= Html.escape(lop.getNamHoc()) %>)</td>
                    <td><%= lop.getSoLuongSV() %></td>
                    <td><span class="nhan-tt <%= nhanCss %>"><%= Html.escape(nhanChu) %></span></td>
                    <td>
                        <a class="nut nut-nho" href="<%= gocUrl %>/giangvien/nhapdiem?maLHP=<%= lop.getMaLophocphan() %>">Nhập điểm</a>
                        <a class="nut nut-nho nut-vien" href="<%= gocUrl %>/giangvien/diemdanh?maLHP=<%= lop.getMaLophocphan() %>">Điểm danh</a>
                    </td>
                </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/footer.jsp" %>
