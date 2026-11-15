# Nhật Ký Ứng Dụng AI Số 02: Áp Dụng JUnit 5 Nâng Cao Viết Unit Test Cho Controller

- **Sinh viên thực hiện:** Vòng Kiệt (`TV00514` – Lớp `SD21307`)
- **Repository cá nhân:** [SOF309_SD21307_TV00514_VongKiet](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main)
- **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI
- **Giảng viên hướng dẫn:** ThS. Trần Quang Bình (binhtq7 - Thầy Mười Giáo)

---

## 1. Ngữ Cảnh & Mục Tiêu (Context & Objective)

- **Giai đoạn thực hiện:** **Tuần 2 (09/11 – 15/11/2026)** – Trọng tâm kỹ thuật **Y3**: Viết Unit Test cho Controller và tầng xử lý nghiệp vụ bám sát tài liệu đặc tả [docs/SPECIFICATIONS.md](../SPECIFICATIONS.md) và kịch bản [docs/TEST_CASES.md](../TEST_CASES.md).
- **Mục tiêu kỹ thuật kế thừa từ Syllabus:**
  1. **Parameterized Test (Kế thừa Lab 2):** Sử dụng `@ParameterizedTest` kết hợp `@CsvSource`, `@ValueSource`, `@NullAndEmptySource` để quét qua toàn bộ dữ liệu biên của Form (email rỗng, mật khẩu rỗng, mật khẩu dưới 6 ký tự, ngày sinh tương lai, tuổi dưới 16...).
  2. **Exception Testing (Kế thừa Lab 3):** Sử dụng `assertThrows(Exception.class, () -> ...)` để kiểm chứng việc ném ngoại lệ chính xác khi vi phạm nghiệp vụ (nhập lại mật khẩu không khớp, trùng email, trùng MSSV, không tìm thấy tài khoản).
  3. **Timeout Testing (Kế thừa Lab 3):** Gắn `@Timeout(value = 2, unit = TimeUnit.SECONDS)` để phòng ngừa trường hợp vòng lặp vô tận gây treo hệ thống.
  4. **Kiểm thử độc lập (Isolation):** Không phụ thuộc vào cơ sở dữ liệu thật bằng cách triển khai Fake/In-Memory Repository, giúp lệnh `mvn test` thực thi tức thì và đạt 100% Pass trên mọi môi trường (kể cả GitHub Actions CI).

---

## 2. Câu Lệnh Prompt Đã Sử Dụng (Prompts Used)

### 2.1. Prompt 1: Yêu cầu AI viết Unit Test cho `DangNhapController`
```text
Đóng vai Senior Automation QA Engineer. Tôi đang làm Assignment môn SOF309 (Java 17, JUnit Jupiter 5.10.2).
Tôi có controller DangNhapController.java với 2 phương thức:
1. dangNhap(String email, String matKhauTho): trả về TaiKhoan nếu đúng, null nếu sai/bị khóa/trống. Có tính năng tự động nâng cấp hash mật khẩu sang PBKDF2 nếu hash cũ là SHA-256.
2. doiMatKhau(int maNguoidung, String matKhauCu, String matKhauMoi, String nhapLaiMatKhauMoi): ném Exception nếu mật khẩu mới < 6 ký tự, nhập lại không khớp, sai mật khẩu cũ, hoặc không tìm thấy tài khoản.

Hãy viết class AuthControllerTest.java nằm trong package com.poly.sof309.controllers đáp ứng các tiêu chuẩn sau:
- Sử dụng @ParameterizedTest + @CsvSource để test đăng nhập với các ca biên (email rỗng, space, pass rỗng, sai pass, tài khoản khóa).
- Sử dụng @ParameterizedTest + @ValueSource để test đổi mật khẩu với mật khẩu mới có độ dài từ 0 đến 5 ký tự (dưới 6 ký tự).
- Sử dụng assertThrows() bắt chính xác thông điệp lỗi tiếng Việt.
- Dùng @Timeout(2s) cho các test method.
- Viết một In-Memory Repository giả lập TaiKhoanRepository để không cần kết nối SQL Server thật.
```

### 2.2. Prompt 2: Yêu cầu AI viết Unit Test cho `SinhVienService`
```text
Tiếp tục với phân hệ Quản lý Sinh viên (SinhVienService.java):
Chức năng create(SinhVien sv) có các ràng buộc:
- Bắt buộc chọn lớp (maLop > 0)
- Họ tên không được để trống
- Ngày sinh không được để trống, không ở tương lai, và sinh viên phải từ đủ 16 tuổi trở lên
- Email hợp lệ và không được trùng với tài khoản đã có
- Tự động sinh MSSV và tạo tài khoản STUDENT

Hãy viết SinhVienControllerTest.java sử dụng JUnit 5:
- Áp dụng @ParameterizedTest + @ValueSource kiểm tra biên ngày sinh: tương lai ("2028-01-01"), dưới 16 tuổi ("2015-06-01"), sai định dạng ("invalid-date").
- Áp dụng @ParameterizedTest + @CsvSource kiểm tra các trường bắt buộc form (maLop <= 0, hoTen rỗng, email sai định dạng).
- Kiểm tra trùng email, xóa sinh viên không tồn tại bằng assertThrows().
```

---

## 3. Kết Quả Do AI Sinh Ra (Raw Output)

AI đã phản hồi mã nguồn khung cơ bản cho cả 2 class:

*Trích đoạn kết quả ban đầu do AI sinh ra:*
```java
@ParameterizedTest
@CsvSource({
    "'', '123456'",
    "'admin@fpt.edu.vn', ''"
})
void testLoginFail(String email, String pass) {
    assertNull(controller.dangNhap(email, pass));
}

@Test
void testDoiMatKhauNgan() {
    assertThrows(Exception.class, () -> {
        controller.doiMatKhau(1, "123456", "123", "123");
    });
}
```

Tuy nhiên, mã nguồn AI sinh ban đầu có một số lỗi kỹ thuật:
1. AI giả định `SinhVien` có trường `soDienthoai` và gọi `sv.setSoDienthoai(...)` (trong khi Model thực tế không có trường này).
2. AI gọi `fakeTkRepo.themTaiKhoan(tk)` thay vì hàm `insert(tk)` có sẵn của `TaiKhoanRepository`.
3. Hash mật khẩu cũ trong test nâng cấp mật khẩu bị sai định dạng, khiến `MatKhauUtil.khop()` trả về false dẫn đến test fail.
4. Constructor của `DangNhapController` và `SinhVienService` có phạm vi truy cập mặc định (`package-private`), khiến class test trong package `com.poly.sof309.controllers` không thể inject fake repository.

---

## 4. Chỉnh Sửa & Tối Ưu Hóa Của Sinh Viên (Manual Refinements)

Để hoàn thiện mã nguồn đạt chuẩn 100% Pass và tương thích tuyệt đối với dự án, mình đã thực hiện các bước tinh chỉnh:

| STT | Vấn đề phát hiện | Giải pháp chỉnh sửa thủ công | Kết quả đạt được |
| :---: | :--- | :--- | :--- |
| **1** | Constructor package-private chặn DI | Mở rộng phạm vi `public DangNhapController(TaiKhoanRepository repo)` và `public SinhVienService(...)` | Cho phép inject Repository giả lập từ bất kỳ package test nào mà không cần sửa kiến trúc code. |
| **2** | Hash legacy không khớp thuật toán | Sử dụng chuỗi hash SHA-256 chuẩn `3A4B766F...` kèm salt pepper `qlsv$` cho mật khẩu `"123"` | Test case `testLoginSuccess_UpgradesLegacyHash` pass, chứng minh AI-driven hash migration hoạt động hoàn hảo. |
| **3** | Sai lệch method Model & Repo | Xóa bỏ trường thừa `soDienthoai`, override chính xác method `insert(TaiKhoan)` | Code biên dịch sạch sẽ không còn cảnh báo `cannot find symbol`. |
| **4** | Bao phủ đầy đủ ca biên theo `TEST_CASES.md` | Bổ sung đầy đủ 23 ca test cho AuthControllerTest, 18 ca cho SinhVienControllerTest và 7 ca cho MonHocControllerTest | Bao phủ toàn bộ các mã test: `TC_AUTH_01`, `TC_AUTH_02`, `TC_AUTH_04`, `TC_AUTH_05`, `TC_SV_01`, `TC_SV_02`, `TC_SV_03`, `TC_SV_05`, `TC_SV_06`, `TC_MH_01`, `TC_MH_02`, `TC_MH_03`. |
| **5** | Đảm bảo tiêu chuẩn Lab 3 | Bổ sung annotation `@Timeout(value = 2, unit = TimeUnit.SECONDS)` cho toàn bộ test method | Bảo vệ bộ test suite không bị đơ hoặc timeout khi chạy trên server CI/CD. |

---

## 5. Kết Quả Nghiệm Thu Tuần 2 (Verification)

Chạy lệnh kiểm thử tại Terminal:
```bash
mvn test "-Dtest=*ControllerTest"
```
**Kết quả thực tế:**
- **Tổng số tests chạy:** 52 test cases (gồm cả các ca Parameterized mở rộng).
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Trạng thái:** **BUILD SUCCESS (100% Pass)**.
- **Thời gian thực thi:** Dưới 12 giây.
