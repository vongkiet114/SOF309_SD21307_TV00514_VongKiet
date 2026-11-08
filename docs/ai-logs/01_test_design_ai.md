# Nhật Ký Ứng Dụng AI Số 01: Áp Dụng RAG Phân Tích Đặc Tả, Sinh Test Plan & Test Cases

- **Sinh viên thực hiện:** Vòng Kiệt (`TV00514` – Lớp `SD21307`)
- **Repository cá nhân:** [SOF309_SD21307_TV00514_VongKiet](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main)

Tài liệu này ghi lại minh chứng quá trình ứng dụng kỹ thuật **RAG (Retrieval-Augmented Generation)** trong **Tuần 1** để phân tích mã nguồn dự án [src/](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main/src), xây dựng đặc tả [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md) và nạp đặc tả đó làm ngữ cảnh (Context) cho AI sinh ra **Kế hoạch kiểm thử** ([docs/TEST\_PLAN.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/TEST_PLAN.md)) cùng **Danh sách kịch bản kiểm thử** ([docs/TEST\_CASES.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/TEST_CASES.md)), tuân thủ đúng quy chuẩn 4 phần tại [docs/ai-logs/README.md](https://github.com/tqbinh1268/SOF309-Kiem-Thu-Nang-Cao-Cung-AI/blob/main/docs/ai-logs/README.md).

## 1. Ngữ Cảnh & Mục Tiêu (Context & Objective)

- **Giai đoạn thực hiện:** Tuần 1 – Khởi tạo dự án, viết đặc tả `SPECIFICATIONS.md` và áp dụng kỹ thuật **AI RAG** sinh `TEST_PLAN.md` và `TEST_CASES.md` theo lộ trình tại [docs/HUONG\_DAN\_CHI\_TIET\_6\_TUAN.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/HUONG_DAN_CHI_TIET_6_TUAN.md).
- **Kỹ thuật RAG (Retrieval-Augmented Generation) được áp dụng như thế nào:**
  - Thay vì yêu cầu AI sinh Test Plan và Test Cases chung chung dễ bị ảo giác (hallucination), mình nạp trực tiếp cấu trúc mã nguồn trong [src/main/java/](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main/src/main/java) và tệp đặc tả [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md) vào cửa sổ ngữ cảnh của AI.
  - AI truy xuất (Retrieve) đúng tên các Servlet/Controller (`DangNhapController`, `TaiKhoanServlet`, `SinhVienServlet`, `MonHocServlet`, `NhapDiemServlet`, `NhapDiemExcelServlet`), các bộ lọc (`AuthenticationFilter`, `CsrfFilter`) và các mã quy tắc nghiệp vụ (`BR-AUTH-01..03`, `BR-SV-01..04`, `BR-DIEM-01`) để sinh ra kế hoạch và kịch bản kiểm thử khớp 100% với dự án.

## 2. Câu Lệnh Prompt Đã Sử Dụng (Prompts Used)

### 2.1. Prompt 1: Phân tích mã nguồn và xây dựng tài liệu nguồn `docs/SPECIFICATIONS.md`

```text
Đóng vai chuyên gia QA/Automation Test Lead. Mình đã đẩy dự án Hệ Thống Quản Lý Đào Tạo & Học Vụ (Java Servlet/JSP MVC, JDK 17) lên repository https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main gồm:
- Controllers & Servlets: DangNhapController.java, LoginGetServlet.java, LogoutServlet.java, TaiKhoanServlet.java, DoiMatKhauServlet.java, SinhVienServlet.java, GiangVienServlet.java, MonHocServlet.java, LopHocPhanServlet.java, NhapDiemServlet.java, NhapDiemExcelServlet.java, DiemDanhServlet.java.
- Filters: AuthenticationFilter, AuthorizationFilter, CsrfFilter, AuditLogFilter.
- Models: TaiKhoan, VaiTroHeThong, SinhVien, GiangVien, MonHoc, LopHoc, LopHocPhan, DiemSo, BangDiemHocPhan, GradeImportPreview, TieuChiTimKiem.

Hãy soạn thảo hoàn chỉnh file docs/SPECIFICATIONS.md gồm bảng ánh xạ kiến trúc SUT, bảng Validation Rules (kèm ID HTML/Param, kiểu dữ liệu, giá trị biên, thông báo lỗi tiếng Việt) và kịch bản BDD Gherkin cho 3 phân hệ: (1) Đăng nhập & Phân quyền, (2) Quản lý Tài khoản & Đổi mật khẩu, (3) Quản lý Sinh viên, Môn học & Nhập điểm CRUD.
```

### 2.2. Prompt 2 (Kỹ thuật RAG): Nạp `SPECIFICATIONS.md` để sinh `docs/TEST_PLAN.md` và `docs/TEST_CASES.md`

```text
[Đính kèm ngữ cảnh RAG: Toàn bộ nội dung file docs/SPECIFICATIONS.md và cấu hình pom.xml (Java 17, JUnit 5.10.2, Selenium 4.20.0)]

Dựa CHỈ trên tài liệu đặc tả docs/SPECIFICATIONS.md được cung cấp ở trên (kỹ thuật RAG), hãy sinh cho mình 2 tệp tài liệu nghiệm thu:
1. File docs/TEST_PLAN.md: Gồm Mục tiêu kiểm thử, Phạm vi kiểm thử (In-Scope ánh xạ đúng tên các class Servlet/Controller/Filter trong repo và Out-of-Scope), Chiến lược kiểm thử (Unit Test JUnit 5, UI Test Selenium POM, CI/CD GitHub Actions), Môi trường kiểm thử, Tiêu chí Entry/Suspension/Exit và Lịch trình 6 tuần.
2. File docs/TEST_CASES.md: Lập bảng ma trận 20 kịch bản kiểm thử (TC01 đến TC20) bao phủ cả 3 phân hệ trong SPECIFICATIONS.md, gồm đầy đủ các ca kiểm thử Positive, Negative, Boundary (biên số tín chỉ 1..10, biên điểm số 0.0..10.0, độ dài mật khẩu >= 6) và Exception, có cột ánh xạ ngược về mã quy tắc nghiệp vụ (BR-AUTH, BR-SV, BR-DIEM).
```

## 3. Kết Quả AI Trả Về (AI Output Summary)

Sau khi nạp ngữ cảnh `SPECIFICATIONS.md`, AI đã sinh ra:
- **Bản thảo `docs/TEST_PLAN.md`:** Xác định rõ phạm vi kiểm thử cho 4 nhóm chức năng chính, chiến lược kết hợp JUnit 5 (`5.10.2`) + Selenium WebDriver (`4.20.0`) + GitHub Actions, tiêu chí Pass/Fail và lộ trình 6 tuần.
- **Bản thảo `docs/TEST_CASES.md`:** Danh sách các ca kiểm thử chia theo từng phân hệ với đầy đủ các bước thực hiện và kết quả kỳ vọng.

Trích đoạn kết quả ban đầu do AI sinh cho bảng Test Cases:

```markdown
| Mã TC | Tên Test Case | Loại | Bước thực hiện | Kết quả kỳ vọng |
| :--- | :--- | :--- | :--- | :--- |
| TC01 | Đăng nhập Admin thành công | Positive | Nhập admin / Admin@123 | Chuyển tới trang Dashboard |
| TC02 | Đăng nhập sai mật khẩu | Negative | Nhập admin / saipass | Báo lỗi sai mật khẩu |
| TC12 | Thêm sinh viên mới | Positive | Nhập mã SV, họ tên, lớp | Lưu sinh viên thành công |
```

## 4. Tối Ưu & Chỉnh Sửa Thủ Công (Manual Refinements & Optimization)

Để bộ 3 tài liệu [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md), [docs/TEST\_PLAN.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/TEST_PLAN.md) và [docs/TEST\_CASES.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/TEST_CASES.md) đạt độ chính xác cao nhất khi nghiệm thu, mình đã rà soát và tinh chỉnh thủ công 4 điểm sau:

| Hạng mục rà soát | Điểm hạn chế trong bản gốc của AI | Nội dung đã chỉnh sửa & tối ưu hóa thủ công |
| :--- | :--- | :--- |
| **1. Truy vết hai chiều (Requirements Traceability)** | Bảng `TEST_CASES.md` ban đầu của AI thiếu cột liên kết ngược về mã quy tắc nghiệp vụ trong `SPECIFICATIONS.md`. | Bổ sung cột **Quy tắc tham chiếu** (`BR-AUTH-01..03`, `BR-SV-01..04`, `BR-DIEM-01`) cho toàn bộ 20 Test Cases (`TC01` – `TC20`) để đảm bảo tính truy vết của kỹ thuật RAG. |
| **2. Kiểm thử phân quyền đa vai trò (`VaiTroHeThong`)** | AI chỉ viết 1 test case đăng nhập cho quyền Admin mà bỏ sót luồng điều hướng của Giảng viên và Sinh viên. | Tách rõ 3 kịch bản `TC01` (`AdminDashboardView`), `TC02` (`GiangVienDashboardView`), `TC03` (`SinhVienDashboardView`) và bổ sung `TC07` kiểm tra `AuthenticationFilter` khi truy cập trái phép. |
| **3. Bổ sung ca kiểm thử biên cho Điểm & Tín chỉ** | AI mới chỉ kiểm tra nhập điểm hợp lệ mà thiếu kiểm tra các giá trị sát biên âm và vượt biên 10. | Thêm `TC18` (biên số tín chỉ `0` và `11`), `TC19` (biên hợp lệ `0.0` và `10.0`) và `TC20` (biên không hợp lệ `-0.5` và `10.5` trên `NhapDiemServlet` và `NhapDiemExcelServlet`). |
| **4. Đồng bộ thông số phiên bản từ `pom.xml`** | Trong `TEST_PLAN.md`, AI ghi phiên bản thư viện chung chung. | Cập nhật chính xác phiên bản từ [pom.xml](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/pom.xml) của dự án: **JDK 17**, **JUnit Jupiter `5.10.2`**, **Selenium Java `4.20.0`** và **Maven Surefire Plugin `3.2.5`**. |
