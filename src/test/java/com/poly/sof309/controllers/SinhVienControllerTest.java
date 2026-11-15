package com.poly.sof309.controllers;

import Models.SinhVien;
import Models.TaiKhoan;
import Models.VaiTroHeThong;
import Repository.SinhVienRepository;
import Repository.TaiKhoanRepository;
import Repository.VaiTroRepository;
import Services.SinhVienService;
import Services.TaiKhoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================================
 * [Y3] UNIT TEST QUẢN LÝ SINH VIÊN (SINHVIENSERVICE / CONTROLLER)
 * Sinh viên: Vòng Kiệt - MSSV: TV00514 - Lớp: SD21307
 * Kế thừa kỹ thuật Tuần 2:
 *  - @ParameterizedTest + @ValueSource kiểm tra giá trị biên tuổi & ngày sinh
 *  - @ParameterizedTest + @CsvSource kiểm tra các trường bắt buộc của form
 *  - assertThrows() bắt lỗi nghiệp vụ (trùng MSSV, trùng Email, chưa đủ tuổi)
 *  - @Timeout đảm bảo hiệu năng xử lý không bị treo
 * Ánh xạ Test Cases: TC_SV_01, TC_SV_02, TC_SV_03, TC_SV_06, TC_ACC_01, TC_ACC_02
 * =========================================================================
 */
@DisplayName("[Tuần 2 - Y3] Kiểm thử Nghiệp vụ Quản lý Sinh viên & Tài khoản")
public class SinhVienControllerTest {

    private InMemorySinhVienRepository fakeSvRepo;
    private InMemoryTaiKhoanRepository fakeTkRepo;
    private InMemoryVaiTroRepository fakeRoleRepo;
    private TaiKhoanService tkService;
    private SinhVienService svService;

    @BeforeEach
    void setUp() {
        fakeSvRepo = new InMemorySinhVienRepository();
        fakeTkRepo = new InMemoryTaiKhoanRepository();
        fakeRoleRepo = new InMemoryVaiTroRepository();

        // Cấp mã vai trò STUDENT
        fakeRoleRepo.roles.put("STUDENT", 3);

        tkService = new TaiKhoanService(fakeTkRepo, fakeRoleRepo);
        svService = new SinhVienService(fakeSvRepo, fakeTkRepo, tkService);
    }

    // =====================================================================
    // 1. KIỂM THỬ THÊM MỚI SINH VIÊN HỢP LỆ (TC_SV_01 & TC_ACC_01)
    // =====================================================================

    @Test
    @DisplayName("TC_SV_01: Thêm mới sinh viên thành công khi đủ tuổi (>= 16) và thông tin hợp lệ")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCreateStudentSuccess() throws Exception {
        SinhVien sv = new SinhVien();
        sv.setHoTen("Vòng Kiệt");
        sv.setEmail("tv00514@fpt.edu.vn");
        sv.setNgaySinh("2004-10-15"); // 20+ tuổi -> thỏa mãn >= 16
        sv.setMaLop(1);
        sv.setDiaChi("TP. Hồ Chí Minh");

        int maNguoidung = svService.create(sv);

        assertTrue(maNguoidung > 0, "Mã người dùng sinh viên phải lớn hơn 0");
        assertNotNull(fakeSvRepo.findByMaNguoidung(maNguoidung), "Phải lưu hồ sơ sinh viên vào repository");
        assertNotNull(fakeTkRepo.findByMaNguoidung(maNguoidung), "Phải tự động tạo tài khoản tương ứng");
        assertEquals("Vòng Kiệt", fakeSvRepo.findByMaNguoidung(maNguoidung).getHoTen());
    }

    // =====================================================================
    // 2. KIỂM THỬ GIÁ TRỊ BIÊN TUỔI & NGÀY SINH (TC_SV_02)
    // =====================================================================

    @ParameterizedTest(name = "[{index}] Ngày sinh=''{0}'' vi phạm quy tắc ngày sinh")
    @DisplayName("TC_SV_02: Ném ngoại lệ khi ngày sinh ở tương lai hoặc dưới 16 tuổi (Kiểm tra biên)")
    @ValueSource(strings = {
            "2028-01-01",  // Ngày sinh ở tương lai
            "2030-12-31",  // Ngày sinh ở tương lai xa
            "2015-06-01",  // Sinh năm 2015 -> khoảng 11 tuổi (< 16)
            "2012-01-01",  // Sinh năm 2012 -> khoảng 14 tuổi (< 16)
            "invalid-date",// Định dạng ngày không đúng
            "2024/02/30"   // Định dạng ngày sai
    })
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCreateStudent_InvalidBirthDate_ThrowsException(String invalidBirthDate) {
        SinhVien sv = createBaseStudent();
        sv.setNgaySinh(invalidBirthDate);

        Exception exception = assertThrows(Exception.class, () -> {
            svService.create(sv);
        }, "Phải ném ngoại lệ khi ngày sinh vi phạm ràng buộc nghiệp vụ");

        assertTrue(exception.getMessage().contains("Ngày sinh không hợp lệ")
                        || exception.getMessage().contains("từ đủ 16 tuổi trở lên"),
                "Thông báo lỗi phải chỉ rõ ngày sinh ở tương lai, không hợp lệ hoặc dưới 16 tuổi");
    }

    @Test
    @DisplayName("TC_SV_02_EMPTY: Ném ngoại lệ khi để trống ngày sinh của sinh viên mới")
    void testCreateStudent_EmptyBirthDate_ThrowsException() {
        SinhVien sv = createBaseStudent();
        sv.setNgaySinh("");

        Exception exception = assertThrows(Exception.class, () -> {
            svService.create(sv);
        });

        assertEquals("Vui lòng nhập ngày sinh của sinh viên.", exception.getMessage());
    }

    // =====================================================================
    // 3. KIỂM THỬ CÁC TRƯỜNG BẮT BUỘC & RÀNG BUỘC DUPLICATE (TC_SV_03)
    // =====================================================================

    @ParameterizedTest(name = "[{index}] maLop={0}, hoTen=''{1}'', email=''{2}'' -> Kỳ vọng lỗi: {3}")
    @DisplayName("TC_SV_03_FORM: Kiểm tra dữ liệu biên form thêm sinh viên (Lớp, Họ tên, Email)")
    @CsvSource({
            "0, 'Vòng Kiệt', 'tv00514@fpt.edu.vn', 'Phải chọn lớp cho sinh viên.'",
            "-1, 'Vòng Kiệt', 'tv00514@fpt.edu.vn', 'Phải chọn lớp cho sinh viên.'",
            "1, '', 'tv00514@fpt.edu.vn', 'Họ tên không được để trống.'",
            "1, '   ', 'tv00514@fpt.edu.vn', 'Họ tên không được để trống.'",
            "1, 'Vòng Kiệt', 'email-sai-dinh-dang', 'Email không hợp lệ.'",
            "1, 'Vòng Kiệt', 'user@', 'Email không hợp lệ.'",
            "1, 'Vòng Kiệt', '@domain.com', 'Email không hợp lệ.'"
    })
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCreateStudent_FormValidation_ThrowsException(int maLop, String hoTen, String email, String expectedMsg) {
        SinhVien sv = createBaseStudent();
        sv.setMaLop(maLop);
        sv.setHoTen(hoTen);
        sv.setEmail(email);

        Exception exception = assertThrows(Exception.class, () -> {
            svService.create(sv);
        });

        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisplayName("TC_SV_03_DUP_EMAIL: Ném ngoại lệ khi email sinh viên đã tồn tại trong hệ thống")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCreateStudent_DuplicateEmail_ThrowsException() {
        // Đã có tài khoản mang email này
        TaiKhoan existing = new TaiKhoan();
        existing.setMaNguoidung(10);
        existing.setEmail("existing@fpt.edu.vn");
        fakeTkRepo.users.put(10, existing);
        fakeTkRepo.emails.put("existing@fpt.edu.vn", existing);

        SinhVien sv = createBaseStudent();
        sv.setEmail("existing@fpt.edu.vn");

        Exception exception = assertThrows(Exception.class, () -> {
            svService.create(sv);
        });

        assertTrue(exception.getMessage().contains("Email đã tồn tại"),
                "Phải báo lỗi trùng email: " + exception.getMessage());
    }

    // =====================================================================
    // 4. KIỂM THỬ XÓA & TÌM KIẾM SINH VIÊN (TC_SV_06 & SEARCH)
    // =====================================================================

    @Test
    @DisplayName("TC_SV_06: Ném ngoại lệ khi xóa sinh viên có mã người dùng không tồn tại")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testDeleteStudent_NonExistentId_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            svService.delete(9999);
        });

        assertEquals("Không tìm thấy sinh viên cần xóa.", exception.getMessage());
    }

    @Test
    @DisplayName("TC_SV_07: Tìm kiếm sinh viên theo từ khóa và phân trang")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testSearchStudent() throws Exception {
        SinhVien sv1 = createBaseStudent();
        sv1.setHoTen("Nguyễn Văn An");
        sv1.setEmail("annv@fpt.edu.vn");
        svService.create(sv1);

        SinhVien sv2 = createBaseStudent();
        sv2.setHoTen("Trần Thị Bình");
        sv2.setEmail("binhtt@fpt.edu.vn");
        svService.create(sv2);

        List<SinhVien> results = svService.search("An", null, 1, 1, 10);
        assertEquals(1, results.size());
        assertEquals("Nguyễn Văn An", results.get(0).getHoTen());
    }

    // =====================================================================
    // HELPER & REPOSITORY GIẢ LẬP
    // =====================================================================

    private SinhVien createBaseStudent() {
        SinhVien sv = new SinhVien();
        sv.setMaLop(1);
        sv.setHoTen("Sinh Viên Mẫu");
        sv.setEmail("sv_mau@fpt.edu.vn");
        sv.setNgaySinh("2005-01-01");
        sv.setDiaChi("Hà Nội");
        return sv;
    }

    private static class InMemorySinhVienRepository extends SinhVienRepository {
        final Map<Integer, SinhVien> students = new HashMap<>();
        int idSeq = 100;

        @Override
        public String nextMssv() {
            return "SV" + (idSeq++);
        }

        @Override
        public SinhVien findByMssv(String mssv) {
            return students.values().stream()
                    .filter(s -> mssv.equalsIgnoreCase(s.getMssv()))
                    .findFirst().orElse(null);
        }

        @Override
        public SinhVien findByMaNguoidung(int maNguoidung) {
            return students.get(maNguoidung);
        }

        @Override
        public boolean insert(SinhVien sv) {
            students.put(sv.getMaNguoidung(), sv);
            return true;
        }

        @Override
        public List<SinhVien> search(String keyword, Integer maChuyenNganh, Integer maLop, int page, int pageSize) {
            List<SinhVien> list = new ArrayList<>();
            for (SinhVien sv : students.values()) {
                if (keyword == null || sv.getHoTen().contains(keyword) || sv.getMssv().contains(keyword)) {
                    list.add(sv);
                }
            }
            return list;
        }
    }

    private static class InMemoryTaiKhoanRepository extends TaiKhoanRepository {
        final Map<Integer, TaiKhoan> users = new HashMap<>();
        final Map<String, TaiKhoan> emails = new HashMap<>();
        int idSeq = 1;

        @Override
        public TaiKhoan findByEmail(String email) {
            if (email == null) return null;
            return emails.get(email.trim().toLowerCase());
        }

        @Override
        public TaiKhoan findByMaNguoidung(int maNguoidung) {
            return users.get(maNguoidung);
        }

        @Override
        public int insert(TaiKhoan tk) {
            int id = idSeq++;
            tk.setMaNguoidung(id);
            users.put(id, tk);
            emails.put(tk.getEmail().toLowerCase(), tk);
            return id;
        }

        @Override
        public boolean delete(int maNguoidung) {
            TaiKhoan removed = users.remove(maNguoidung);
            if (removed != null) {
                emails.remove(removed.getEmail().toLowerCase());
                return true;
            }
            return false;
        }
    }

    private static class InMemoryVaiTroRepository extends VaiTroRepository {
        final Map<String, Integer> roles = new HashMap<>();

        @Override
        public Integer findIdByName(String name) {
            return roles.get(name);
        }
    }
}
