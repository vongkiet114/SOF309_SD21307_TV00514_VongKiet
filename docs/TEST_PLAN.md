# Kế Hoạch Kiểm Thử (Test Plan): Hệ Thống Quản Lý Đào Tạo & Học Vụ (SOF309)

- **Sinh viên thực hiện:** Vòng Kiệt (`TV00514` – Lớp `SD21307`)
- **Repository dự án:** [SOF309_SD21307_TV00514_VongKiet](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main)
- **Tài liệu đặc tả nguồn (RAG Context):** [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md)

## 1. Mục Tiêu Kiểm Thử (Test Objectives)

Kế hoạch kiểm thử này được sinh tự động bằng kỹ thuật **RAG (Retrieval-Augmented Generation)** dựa trên tài liệu đặc tả [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md) và mã nguồn tại [src/](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main/src), nhằm đảm bảo:

- Xác minh tính đúng đắn của các tầng xử lý logic nghiệp vụ (`Services`, `Controllers`, `Repository`, `Utils`) bằng kiểm thử đơn vị (**Unit Test với JUnit 5**).
- Đảm bảo các luồng tương tác giao diện người dùng (Đăng nhập, Quản lý tài khoản, Quản lý hồ sơ Sinh viên, Môn học và Nhập điểm CRUD) hoạt động chính xác trên trình duyệt thông qua **Selenium WebDriver (Page Object Model)**.
- Tự động hóa quá trình thực thi kiểm thử và xuất báo cáo qua pipeline **CI/CD GitHub Actions** ([.github/workflows/test.yml](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/.github/workflows/test.yml)).

## 2. Phạm Vi Kiểm Thử (Test Scope)

### 2.1. Các hạng mục trong phạm vi kiểm thử (In-Scope)

| STT | Phân hệ chức năng | Thành phần mã nguồn kiểm thử | Cấp độ kiểm thử áp dụng |
| :--- | :--- | :--- | :--- |
| **1** | **Xác thực Đăng nhập & Phân quyền** | [DangNhapController.java](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/src/main/java/Controllers/DangNhapController.java), `LoginGetServlet.java`, `LogoutServlet.java`, `AuthenticationFilter.java`, `AuthorizationFilter.java`, `CsrfFilter.java` | Unit Test (JUnit 5) & UI Automation (Selenium) |
| **2** | **Quản lý Tài khoản & Đổi mật khẩu** | `TaiKhoanServlet.java`, `DoiMatKhauServlet.java`, `TaiKhoan.java`, `VaiTroHeThong.java` | Unit Test (JUnit 5) & UI Automation (Selenium) |
| **3** | **Quản lý Sinh viên & Học vụ (CRUD)** | `SinhVienServlet.java`, `GiangVienServlet.java`, `MonHocServlet.java`, `LopHocPhanServlet.java`, `TieuChiTimKiem.java` | Unit Test (JUnit 5) & UI Automation (Selenium) |
| **4** | **Quản lý Điểm & Import Excel** | `NhapDiemServlet.java`, `NhapDiemExcelServlet.java`, `DiemSo.java`, `GradeImportPreview.java`, `CatalogIdParser.java` | Unit Test (JUnit 5 Boundary & Exception Test) |

### 2.2. Các hạng mục ngoài phạm vi kiểm thử (Out-of-Scope)

- Kiểm thử chịu tải và hiệu năng cao (Performance / Load Testing với JMeter).
- Kiểm thử xâm nhập bảo mật chuyên sâu (Penetration Testing ngoài các bộ lọc `CsrfFilter` và `AuthenticationFilter` của ứng dụng).

## 3. Chiến Lược & Kỹ Thuật Kiểm Thử (Test Strategy)

| Cấp độ kiểm thử | Công cụ & Thư viện | Kỹ thuật thiết kế Test Case | Tỷ lệ bao phủ mục tiêu |
| :--- | :--- | :--- | :--- |
| **Unit Testing (Kiểm thử đơn vị)** | JUnit Jupiter `5.10.2` (`@Test`, `@ParameterizedTest`, `Assertions`, `assertThrows`) cấu hình tại [pom.xml](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/pom.xml) | Phân vùng tương đương (Equivalence Partitioning), Phân tích giá trị biên (Boundary Value Analysis), Kiểm thử ngoại lệ (Exception Testing) | 100% các phương thức kiểm tra hợp lệ (validation), tính điểm và phân quyền cốt lõi |
| **UI Automation Testing (Kiểm thử giao diện)** | Selenium WebDriver `4.20.0`, WebDriverManager, Chrome Headless, mô hình **Page Object Model (POM)** | Kiểm thử theo kịch bản nghiệp vụ đầu-cuối (End-to-End BDD Flows: Positive & Negative UI Flows) | Bao phủ toàn bộ luồng Đăng nhập, Thêm/Sửa/Xóa/Tìm kiếm Sinh viên và Môn học |
| **Continuous Testing (CI/CD)** | GitHub Actions (`ubuntu-latest`, JDK 17, Maven Surefire Plugin) tại [.github/workflows/test.yml](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/.github/workflows/test.yml) | Tự động kích hoạt khi `push` hoặc tạo `pull_request` lên nhánh `main` | 100% bài kiểm thử chạy tự động đạt trạng thái `BUILD SUCCESS` (Tích xanh) |

## 4. Môi Trường & Công Cụ Thực Thi (Test Environment & Tools)

- **Hệ điều hành:** Windows 10/11 (môi trường phát triển cục bộ) và Ubuntu Latest (môi trường CI/CD GitHub Actions).
- **Nền tảng biên dịch:** Java Development Kit **JDK 17** (`maven.compiler.source = 17`), Apache Maven `3.9+`.
- **IDE & AI hỗ trợ:** IntelliJ IDEA / Google Antigravity, áp dụng kỹ thuật **RAG (Retrieval-Augmented Generation)** với tài liệu nguồn [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md).
- **Trình duyệt kiểm thử UI:** Google Chrome (chế độ chuẩn khi debug cục bộ và chế độ `--headless=new` khi chạy trên GitHub Actions).

## 5. Tiêu Chí Bắt Đầu, Tạm Dừng & Nghiệm Thu (Entry, Suspension & Exit Criteria)

- **Tiêu chí bắt đầu (Entry Criteria):**
  - Tài liệu đặc tả [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md) đã hoàn thiện đầy đủ các quy tắc nghiệp vụ (`BR-AUTH`, `BR-SV`, `BR-DIEM`).
  - Dự án biên dịch thành công bằng lệnh `mvn test-compile` không phát sinh lỗi cú pháp.
- **Tiêu chí tạm dừng (Suspension Criteria):**
  - Mã nguồn trong [src/main/java/](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main/src/main/java) bị lỗi biên dịch hoặc kết nối cơ sở dữ liệu mẫu gặp sự cố làm gián đoạn trên 50% ca kiểm thử.
- **Tiêu chí hoàn thành & nghiệm thu (Exit Criteria):**
  - Danh sách kịch bản kiểm thử tại [docs/TEST\_CASES.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/TEST_CASES.md) bao phủ đầy đủ các luồng Positive, Negative, Boundary và Exception.
  - 100% bài kiểm thử JUnit 5 và Selenium thực thi thành công (`Failures: 0, Errors: 0`).
  - Pipeline GitHub Actions hiển thị trạng thái **Pass (Tích xanh)** và đầy đủ 6 nhật ký AI trong thư mục [docs/ai-logs/](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/tree/main/docs/ai-logs).

## 6. Lịch Trình Thực Hiện 6 Tuần (Schedule & Deliverables)

| Tuần | Nội dung công việc trọng tâm | Sản phẩm bàn giao (Deliverables) |
| :--- | :--- | :--- |
| **Tuần 1–2** | Phân tích đặc tả hệ thống, áp dụng RAG sinh Kế hoạch kiểm thử và Danh sách kịch bản kiểm thử | [docs/SPECIFICATIONS.md](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/SPECIFICATIONS.md), [docs/TEST\_PLAN.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/TEST_PLAN.md), [docs/TEST\_CASES.md](https://www.google.com/search?q=https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/docs/TEST_CASES.md), `01_test_design_ai.md` |
| **Tuần 3** | Xây dựng bộ kiểm thử đơn vị (Unit Test) với JUnit 5 cho các lớp Service, Controller và Utils | Mã nguồn test trong `src/test/java/`, `02_junit_script_ai.md` |
| **Tuần 4** | Xây dựng bộ kiểm thử giao diện tự động với Selenium WebDriver theo mô hình Page Object Model | Các lớp Page Object và Test Script trong `src/test/java/`, `03_selenium_pom_ai.md` |
| **Tuần 5** | Tích hợp CI/CD tự động chạy kiểm thử trên GitHub Actions mỗi khi đẩy code | [.github/workflows/test.yml](https://github.com/vongkiet114/SOF309_SD21307_TV00514_VongKiet/blob/main/.github/workflows/test.yml), `05_github_actions_ai.md` |
| **Tuần 6** | Gỡ lỗi (Debug), đánh giá và tối ưu hóa mã nguồn kiểm thử với AI trước khi bảo vệ | `04_debug_logs_ai.md`, `06_code_review_ai.md` |
