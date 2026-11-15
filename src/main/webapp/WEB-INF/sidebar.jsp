<%--
    Menu sidebar, hiển thị các mục dựa trên vai trò của người dùng.
    Các biến 'userRole', 'currentPage', 'contextPath' được khai báo trong header.jsp.
--%>
<%!
    boolean active(String currentPage, String... pages) {
        if (currentPage == null) return false;
        for (String page : pages) {
            if (currentPage.equals(page)) return true;
        }
        return false;
    }
%>
<% if ("ADMIN".equals(userRole)) {
    boolean openNguoiDung = active(currentPage, "sinhvien", "giangvien", "taikhoan");
    boolean openDaoTao = active(currentPage, "khoa", "chuyennganh", "lophoc", "monhoc", "montienquyet", "lophocphan", "hocky", "thanhphandiem");
    boolean openLich = active(currentPage, "lichhoc", "kythi", "phonghoc", "cahoc", "diemdanhbu");
    boolean openHeThong = active(currentPage, "thongbao", "nhatky");
%>
    <a class="muc-menu <%= "dashboard".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/dashboard">Dashboard</a>

    <div class="menu-nhom <%= openNguoiDung ? "mo" : "" %>">
        <button class="menu-cha <%= openNguoiDung ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Người dùng</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "sinhvien".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/sinhvien">Sinh viên</a>
            <a class="muc-menu muc-menu-con <%= "giangvien".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/giangvien">Giảng viên</a>
            <a class="muc-menu muc-menu-con <%= "taikhoan".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/taikhoan">Tài khoản</a>
        </div>
    </div>

    <div class="menu-nhom <%= openDaoTao ? "mo" : "" %>">
        <button class="menu-cha <%= openDaoTao ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Đào tạo</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "khoa".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/khoa">Khoa</a>
            <a class="muc-menu muc-menu-con <%= "chuyennganh".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/chuyennganh">Chuyên ngành</a>
            <a class="muc-menu muc-menu-con <%= "lophoc".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/lophoc">Lớp học</a>
            <a class="muc-menu muc-menu-con <%= "monhoc".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/monhoc">Môn học</a>
            <a class="muc-menu muc-menu-con <%= "thanhphandiem".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/thanhphandiem">Cột điểm</a>
            <a class="muc-menu muc-menu-con <%= "montienquyet".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/montienquyet">Môn tiên quyết</a>
            <a class="muc-menu muc-menu-con <%= "lophocphan".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/lophocphan">Lớp học phần</a>
            <a class="muc-menu muc-menu-con <%= "hocky".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/hocky">Học kỳ</a>
        </div>
    </div>

    <div class="menu-nhom <%= openLich ? "mo" : "" %>">
        <button class="menu-cha <%= openLich ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Lịch học</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "lichhoc".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/lichhoc">Lịch học</a>
            <a class="muc-menu muc-menu-con <%= "kythi".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/kythi">Lịch thi</a>
            <a class="muc-menu muc-menu-con <%= "phonghoc".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/phonghoc">Phòng học</a>
            <a class="muc-menu muc-menu-con <%= "cahoc".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/cahoc">Ca học</a>
            <a class="muc-menu muc-menu-con <%= "diemdanhbu".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/diemdanhbu">Điểm danh bù</a>
        </div>
    </div>

    <div class="menu-nhom <%= openHeThong ? "mo" : "" %>">
        <button class="menu-cha <%= openHeThong ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Hệ thống</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "thongbao".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/thongbao">Thông báo</a>
            <a class="muc-menu muc-menu-con <%= "nhatky".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/admin/nhatky">Nhật ký</a>
        </div>
    </div>
<% } else if ("TEACHER".equals(userRole)) {
    boolean openGiangDay = active(currentPage, "dashboard_gv", "lopgiangday_gv", "lichday_gv", "kythi_gv", "nhapdiem", "diemdanh", "thongke_gv");
    boolean openCaNhan = active(currentPage, "thongbao_gv", "thongtin_gv");
%>
    <div class="menu-nhom <%= openGiangDay ? "mo" : "" %>">
        <button class="menu-cha <%= openGiangDay ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Giảng dạy</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "dashboard_gv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/dashboard">Dashboard</a>
            <a class="muc-menu muc-menu-con <%= "lopgiangday_gv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/lopgiangday">Lớp giảng dạy</a>
            <a class="muc-menu muc-menu-con <%= "lichday_gv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/lichday">Lịch dạy</a>
            <a class="muc-menu muc-menu-con <%= "kythi_gv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/kythi">Lịch thi</a>
            <a class="muc-menu muc-menu-con <%= "nhapdiem".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/nhapdiem">Nhập điểm</a>
            <a class="muc-menu muc-menu-con <%= "diemdanh".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/diemdanh">Điểm danh</a>
            <a class="muc-menu muc-menu-con <%= "thongke_gv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/thongke">Thống kê giảng dạy</a>
        </div>
    </div>
    <div class="menu-nhom <%= openCaNhan ? "mo" : "" %>">
        <button class="menu-cha <%= openCaNhan ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Cá nhân</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "thongbao_gv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/thongbao">Thông báo</a>
            <a class="muc-menu muc-menu-con <%= "thongtin_gv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/giangvien/thongtin">Thông tin cá nhân</a>
        </div>
    </div>
<% } else if ("STUDENT".equals(userRole)) {
    boolean openCaNhanSv = active(currentPage, "dashboard_sv", "thongtin_sv", "thongbao_sv");
    boolean openHocTap = active(currentPage, "lichhoc_sv", "kythi_sv", "bangdiem_sv", "diemdanh_sv");
%>
    <div class="menu-nhom <%= openCaNhanSv ? "mo" : "" %>">
        <button class="menu-cha <%= openCaNhanSv ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Cá nhân</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "dashboard_sv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/sinhvien/dashboard">Dashboard</a>
            <a class="muc-menu muc-menu-con <%= "thongtin_sv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/sinhvien/thongtin">Thông tin cá nhân</a>
            <a class="muc-menu muc-menu-con <%= "thongbao_sv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/sinhvien/thongbao">Thông báo</a>
        </div>
    </div>
    <div class="menu-nhom <%= openHocTap ? "mo" : "" %>">
        <button class="menu-cha <%= openHocTap ? "active" : "" %>" type="button" onclick="doiNhomMenu(this)">
            <span>Học tập</span><span class="menu-muiten">▾</span>
        </button>
        <div class="menu-con">
            <a class="muc-menu muc-menu-con <%= "lichhoc_sv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/sinhvien/lichhoc">Lịch học</a>
            <a class="muc-menu muc-menu-con <%= "kythi_sv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/sinhvien/kythi">Lịch thi</a>
            <a class="muc-menu muc-menu-con <%= "bangdiem_sv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/sinhvien/bangdiem">Bảng điểm</a>
            <a class="muc-menu muc-menu-con <%= "diemdanh_sv".equals(currentPage) ? "active" : "" %>" href="<%= contextPath %>/sinhvien/diemdanh">Lịch sử điểm danh</a>
        </div>
    </div>
<% } %>
<a class="muc-menu" href="<%= contextPath %>/doimatkhau">Đổi mật khẩu</a>
