# Đặc Tả Yêu Cầu Nghiệp Vụ: Hệ Thống Quản Lý Đào Tạo & Học Vụ (SOF309)

- **Sinh viên thực hiện:** Vòng Kiệt
- **Mã số sinh viên:** TV00514
- **Lớp:** SD21307
- **Repository dự án:** [SOF309_SD21307_TV00514_VongKiet](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main)

Tài liệu đặc tả yêu cầu phần mềm (Software Requirements Specification) này mô tả chi tiết các luồng nghiệp vụ, quy tắc kiểm tra dữ liệu đầu vào (Validation Rules) và kịch bản BDD cho dự án **Hệ Thống Quản Lý Đào Tạo & Học Vụ** trong kho mã nguồn [src/](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main/src), bám sát lộ trình tại [docs/HUONG\_DAN\_CHI\_TIET\_6\_TUAN.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/HUONG_DAN_CHI_TIET_6_TUAN.md).

## 1. Tổng Quan Kiến Trúc Hệ Thống Đích (System Under Test - SUT)

Hệ thống được xây dựng theo kiến trúc **Java Web (Servlet/JSP) mô hình MVC** (`Controllers` / `Servlet` – `Services` – `Repository` – `Models` – `Filter`), cấu hình biên dịch với **Java 17**, **JUnit 5 (`5.10.2`)** và **Selenium WebDriver (`4.20.0`)** tại tệp [pom.xml](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/pom.xml).

| Phân hệ kiểm thử trọng tâm | Lớp xử lý chính (Controllers / Servlets) | Thực thể dữ liệu (Models) | Bảo mật & Bộ lọc (Filters) |
| :--- | :--- | :--- | :--- |
| **1. Đăng nhập & Phân quyền** | [DangNhapController.java](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/src/main/java/Controllers/DangNhapController.java), `LoginGetServlet.java`, `LogoutServlet.java` | `TaiKhoan.java`, `VaiTro.java`, `VaiTroHeThong.java`, `Quyen.java` | `AuthenticationFilter`, `AuthorizationFilter`, `CsrfFilter` |
| **2. Quản lý Tài khoản & Đổi mật khẩu** | `TaiKhoanServlet.java`, `DoiMatKhauServlet.java`, `AvatarServlet.java` | `TaiKhoan.java`, `NhatKyHoatDong.java` | `AuditLogFilter`, `CharacterEncodingFilter` |
| **3. Quản lý Sinh viên & Đào tạo (CRUD)** | `SinhVienServlet.java`, `GiangVienServlet.java`, `MonHocServlet.java`, `LopHocPhanServlet.java` | `SinhVien.java`, `GiangVien.java`, `MonHoc.java`, `LopHocPhan.java`, `Khoa.java`, `ChuyenNganh.java` | Kiểm tra quyền `ADMIN` / `GIANG_VIEN` qua `AuthorizationFilter` |
| **4. Quản lý Điểm & Điểm danh** | `NhapDiemServlet.java`, `NhapDiemExcelServlet.java`, `DiemDanhServlet.java` | `DiemSo.java`, `ThanhPhanDiem.java`, `GradeImportPreview.java`, `DiemDanh.java` | Xác thực CSRF token và quyền giảng dạy lớp học phần |

## 2. Đặc Tả Phân Hệ 1: Xác Thực Đăng Nhập & Phân Quyền (`DangNhapController`, `LoginGetServlet`)

Phân hệ chịu trách nhiệm xác thực người dùng và điều hướng về đúng bảng điều khiển (`AdminDashboardView`, `GiangVienDashboardView`, `SinhVienDashboardView`) theo vai trò hệ thống (`VaiTroHeThong`).

### 2.1. Quy tắc kiểm tra dữ liệu đầu vào (Validation Rules)

| Tên trường (Field) | Định danh HTML / Param | Kiểu dữ liệu | Bắt buộc | Quy tắc hợp lệ (Business Rules) | Thông báo lỗi khi vi phạm (Error Message) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Tên đăng nhập / Email** | `username` (`#username`) | Chuỗi ký tự | Có | Không được rỗng, độ dài từ 3 đến 50 ký tự, tự động cắt khoảng trắng thừa ở 2 đầu (`trim`) | `"Vui lòng nhập tên đăng nhập"` |
| **Mật khẩu** | `password` (`#password`) | Password | Có | Không được rỗng, độ dài tối thiểu 6 ký tự | `"Vui lòng nhập mật khẩu"` |
| **CSRF Token** | `_csrf` | Hidden String | Có | Khớp với token trong Session do `CsrfFilter` cấp phát | `"Yêu cầu không hợp lệ (CSRF Token không khớp)"` |

### 2.2. Quy tắc nghiệp vụ và kịch bản BDD (Gherkin)

- **BR-AUTH-01 (Điều hướng theo vai trò):** Khi thông tin đăng nhập hợp lệ và trạng thái tài khoản đang hoạt động (`trangThai = true`), hệ thống lưu thông tin người dùng vào `HttpSession` và điều hướng theo `VaiTroHeThong`:
  - Vai trò `ADMIN` chuyển hướng tới `DashboardServlet` (`AdminDashboardView`).
  - Vai trò `GIANG_VIEN` chuyển hướng tới `GiangVienDashboardServlet` (`GiangVienDashboardView`).
  - Vai trò `SINH_VIEN` chuyển hướng tới `SinhVienDashboardServlet` (`SinhVienDashboardView`).
- **BR-AUTH-02 (Tài khoản sai hoặc bị khóa):** Nếu sai tên đăng nhập/mật khẩu thì hiển thị `"Tên đăng nhập hoặc mật khẩu không chính xác"`; nếu tài khoản bị khóa thì hiển thị `"Tài khoản của bạn đã bị khóa"`.
- **BR-AUTH-03 (Chặn truy cập trái phép qua Filter):** Mọi yêu cầu truy cập các đường dẫn quản trị khi chưa đăng nhập đều bị `AuthenticationFilter` chặn và chuyển hướng về `LoginGetServlet`.

```gherkin
Feature: Đăng nhập và phân quyền theo vai trò hệ thống (DangNhapController)

  Scenario: Đăng nhập thành công với tài khoản Quản trị viên (ADMIN)
    Given Người dùng truy cập trang đăng nhập qua "LoginGetServlet"
    When Người dùng nhập tên đăng nhập "admin" và mật khẩu hợp lệ
    And Người dùng nhấn nút Đăng nhập ("#btnLogin")
    Then "DangNhapController" xác thực thành công và khởi tạo phiên làm việc
    And Hệ thống chuyển hướng tới trang điều khiển "DashboardServlet" dành cho Admin

  Scenario: Đăng nhập thất bại khi để trống mật khẩu hoặc nhập sai mật khẩu
    Given Người dùng đang ở trang đăng nhập
    When Người dùng nhập tên đăng nhập "sinhvien01" và mật khẩu sai "SaiMatKhau123"
    And Người dùng nhấn nút Đăng nhập ("#btnLogin")
    Then Hệ thống giữ nguyên tại trang đăng nhập
    And Hiển thị thông báo lỗi "Tên đăng nhập hoặc mật khẩu không chính xác"
```

## 3. Đặc Tả Phân Hệ 2: Quản Lý Tài Khoản & Đổi Mật Khẩu (`TaiKhoanServlet`, `DoiMatKhauServlet`)

Phân hệ cho phép Quản trị viên cấp phát/đăng ký tài khoản mới (`TaiKhoanServlet`) và cho phép người dùng đổi mật khẩu cá nhân (`DoiMatKhauServlet`).

### 3.1. Quy tắc kiểm tra dữ liệu đầu vào (Validation Rules)

| Tên trường (Field) | Định danh HTML / Param | Kiểu dữ liệu | Bắt buộc | Quy tắc hợp lệ (Business Rules) | Thông báo lỗi khi vi phạm (Error Message) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Tên tài khoản** | `tenDangNhap` (`#tenDangNhap`) | Chuỗi ký tự | Có | Duy nhất (Unique), từ 4–30 ký tự chữ hoặc số, không chứa khoảng trắng | `"Tên tài khoản đã tồn tại hoặc không hợp lệ"` |
| **Email liên hệ** | `email` (`#email`) | Email | Có | Đúng định dạng email chuẩn (ví dụ `@fpt.edu.vn`), không trùng lặp | `"Định dạng email không hợp lệ"` |
| **Vai trò hệ thống** | `vaiTro` (`#vaiTro`) | Enum / Select | Có | Thuộc tập giá trị của `VaiTroHeThong` (`ADMIN`, `GIANG_VIEN`, `SINH_VIEN`) | `"Vui lòng chọn vai trò hợp lệ"` |
| **Mật khẩu mới** | `matKhauMoi` (`#matKhauMoi`) | Password | Có | Từ 6 đến 32 ký tự, phải khác mật khẩu cũ khi thực hiện đổi mật khẩu | `"Mật khẩu mới phải từ 6 ký tự trở lên"` |
| **Xác nhận mật khẩu** | `xacNhanMatKhau` (`#xacNhanMatKhau`) | Password | Có | Trùng khớp hoàn toàn với trường `matKhauMoi` | `"Mật khẩu xác nhận không khớp"` |

## 4. Đặc Tả Phân Hệ 3: Quản Lý Hồ Sơ Sinh Viên & Học Vụ CRUD (`SinhVienServlet`, `MonHocServlet`, `NhapDiemServlet`)

Phân hệ cung cấp đầy đủ các thao tác **Thêm mới (Create)**, **Xem & Tìm kiếm theo tiêu chí (Read/Search qua `TieuChiTimKiem`)**, **Cập nhật (Update)** và **Xóa (Delete)** đối với thực thể `SinhVien`, `MonHoc` và `DiemSo`.

### 4.1. Quy tắc kiểm tra dữ liệu thực thể Sinh Viên (`SinhVien.java`) và Môn Học (`MonHoc.java`)

| Thực thể | Tên trường (Field) | Định danh HTML / Param | Kiểu dữ liệu | Quy tắc hợp lệ & Giá trị biên (Boundary Rules) | Thông báo lỗi khi vi phạm (Error Message) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **SinhVien** | Mã sinh viên | `maSV` (`#maSV`) | Chuỗi ký tự | Bắt buộc, duy nhất (Unique), độ dài 5–15 ký tự (ví dụ `TV00514`), chỉ gồm chữ và số | `"Mã sinh viên không hợp lệ hoặc đã tồn tại"` |
| **SinhVien** | Họ và tên | `hoTen` (`#hoTen`) | Chuỗi ký tự | Bắt buộc, độ dài từ 2 đến 50 ký tự, không chứa chữ số hoặc ký tự đặc biệt | `"Họ tên sinh viên phải từ 2 đến 50 ký tự"` |
| **SinhVien** | Email sinh viên | `email` (`#emailSV`) | Email | Bắt buộc, đúng định dạng email và không trùng với sinh viên khác | `"Email sinh viên không hợp lệ hoặc đã được sử dụng"` |
| **SinhVien** | Chuyên ngành / Lớp | `maLop` (`#maLop`) | Foreign Key | Phải tham chiếu tới `LopHoc` và `ChuyenNganh` đang tồn tại trong hệ thống | `"Vui lòng chọn lớp học và chuyên ngành hợp lệ"` |
| **MonHoc** | Số tín chỉ | `soTinChi` (`#soTinChi`) | Số nguyên (`int`) | Giá trị nguyên nằm trong đoạn từ `1` đến `10` (`1 <= soTinChi <= 10`) | `"Số tín chỉ phải nằm trong khoảng từ 1 đến 10"` |
| **DiemSo** | Điểm thành phần / Thi | `diem` (`#diemSo`) | Số thực (`double`) | Giá trị thực nằm trong đoạn từ `0.0` đến `10.0` (`0.0 <= diem <= 10.0`) | `"Điểm số phải nằm trong khoảng từ 0.0 đến 10.0"` |

### 4.2. Các quy tắc nghiệp vụ CRUD & Kiểm tra xung đột (Business Rules)

- **BR-SV-01 (Thêm mới sinh viên - Create):** Khi nhập đầy đủ thông tin hợp lệ và `maSV` chưa tồn tại, `SinhVienServlet` gọi tầng `Services` và `Repository` lưu bản ghi mới đồng thời ghi nhận hành động qua `AuditLogFilter`.
- **BR-SV-02 (Cập nhật thông tin sinh viên - Update):** Cho phép chỉnh sửa họ tên, ngày sinh, email, số điện thoại, lớp học của sinh viên hiện có (không cho phép sửa khóa chính `maSV`).
- **BR-SV-03 (Xóa sinh viên - Delete):** Chỉ cho phép xóa hoặc chuyển trạng thái nghỉ học đối với sinh viên chưa phát sinh dữ liệu ràng buộc cứng trong bảng điểm (`BangDiemHocPhan`); nếu đã có điểm thi thì trả về thông báo cảnh báo ràng buộc dữ liệu.
- **BR-SV-04 (Tìm kiếm đa tiêu chí - Search):** Hỗ trợ lọc danh sách sinh viên qua đối tượng `TieuChiTimKiem` theo từ khóa (`maSV`, `hoTen`), theo `Khoa`, `ChuyenNganh` hoặc `LopHoc`.
- **BR-DIEM-01 (Nhập điểm thủ công & Import Excel):** Trong `NhapDiemServlet` và `NhapDiemExcelServlet` (`GradeImportPreview`), mọi đầu điểm ngoài biên `< 0.0` hoặc `> 10.0` đều phải bị từ chối và đánh dấu lỗi ngay trên bản xem trước.

```gherkin
Feature: Quản lý hồ sơ Sinh viên và Nhập điểm học phần (CRUD & Boundary Validation)

  Scenario: Thêm mới sinh viên thành công với dữ liệu hợp lệ
    Given Quản trị viên đã đăng nhập và đang ở trang quản lý "SinhVienServlet"
    When Quản trị viên nhập Mã sinh viên "TV00514", Họ tên "Vòng Kiệt", Email "kietvtv00514@fpt.edu.vn"
    And Chọn Lớp học "SD21307" và nhấn nút Lưu ("#btnSaveSV")
    Then Hệ thống lưu sinh viên mới vào cơ sở dữ liệu
    And Danh sách sinh viên hiển thị dòng dữ liệu có mã "TV00514"

  Scenario: Báo lỗi giá trị biên khi nhập điểm học phần vượt quá 10.0
    Given Giảng viên đang ở màn hình nhập điểm "NhapDiemServlet"
    When Giảng viên nhập điểm cuối kỳ là "10.5" cho sinh viên "TV00514"
    And Nhấn nút Lưu bảng điểm ("#btnSaveGrades")
    Then Hệ thống từ chối lưu dữ liệu
    And Hiển thị thông báo lỗi "Điểm số phải nằm trong khoảng từ 0.0 đến 10.0"
```
