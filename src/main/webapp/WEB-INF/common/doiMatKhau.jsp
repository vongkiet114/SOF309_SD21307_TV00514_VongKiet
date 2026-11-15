<%@ page import="Utils.Html" %>
<%-- trang doi mat khau --%>
<%
    request.setAttribute("trang", "doimatkhau");
    request.setAttribute("tieuDeTrang", "Đổi mật khẩu");
%>
<%@ include file="/WEB-INF/header.jsp" %>

<%
    // doc thong bao tren url (sau khi submit form se quay lai day kem msg hoac error)
    String thongBaoOk  = request.getParameter("msg");
    String thongBaoLoi = request.getParameter("error");
    String urlGoc = request.getContextPath();
%>

<h1 class="trang-tieude">Đổi mật khẩu</h1>
<p class="trang-mota">Thiết lập lại mật khẩu truy cập hệ thống.</p>

<div class="the">
    <% if (thongBaoOk != null && !thongBaoOk.isEmpty()) { %>
        <div class="bao-thanhcong"><%= Html.escape(thongBaoOk) %></div>
    <% } %>
    <% if (thongBaoLoi != null && !thongBaoLoi.isEmpty()) { %>
        <div class="bao-loi"><%= Html.escape(thongBaoLoi) %></div>
    <% } %>

    <form method="post" action="<%= urlGoc %>/doimatkhau">
        <div class="nhom-form">
            <label for="matKhauCu">Mật khẩu hiện tại</label>
            <input type="password" id="matKhauCu" name="matKhauCu" class="o-nhap" required>
        </div>
        <div class="nhom-form">
            <label for="matKhauMoi">Mật khẩu mới (tối thiểu 6 ký tự)</label>
            <input type="password" id="matKhauMoi" name="matKhauMoi" class="o-nhap" required minlength="6">
        </div>
        <div class="nhom-form">
            <label for="nhapLaiMatKhau">Xác nhận mật khẩu mới</label>
            <input type="password" id="nhapLaiMatKhau" name="nhapLaiMatKhau" class="o-nhap" required minlength="6">
        </div>
        <div class="nhom-form">
            <label for="otp">Mã OTP (đã gửi qua email)</label>
            <div style="display: flex; gap: 8px;">
                <input type="text" id="otp" name="otp" class="o-nhap" required style="flex-grow: 1;" placeholder="Nhập mã 6 số...">
                <button type="button" class="nut nut-vien" id="btnSendOtp">Nhận mã</button>
            </div>
        </div>
        <button type="submit" class="nut">Cập nhật mật khẩu</button>
    </form>
</div>

<script>
document.getElementById('btnSendOtp').addEventListener('click', function() {
    this.disabled = true;
    this.innerText = 'Đang gửi...';
    fetch('<%= urlGoc %>/doimatkhau?action=sendOtp', { method: 'POST' })
    .then(r => r.text())
    .then(txt => {
        if (txt === 'OK') {
            alert('Mã OTP đã được gửi. Vui lòng kiểm tra hộp thư/log hệ thống.');
            setTimeout(() => {
                this.disabled = false;
                this.innerText = 'Nhận lại mã';
            }, 30000);
        } else {
            alert('Có lỗi xảy ra: ' + txt);
            this.disabled = false;
            this.innerText = 'Nhận mã';
        }
    })
    .catch(e => {
        alert('Lỗi kết nối');
        this.disabled = false;
        this.innerText = 'Nhận mã';
    });
});
</script>

<%@ include file="/WEB-INF/footer.jsp" %>
