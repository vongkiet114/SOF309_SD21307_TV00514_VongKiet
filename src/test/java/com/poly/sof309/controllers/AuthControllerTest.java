package com.poly.sof309.controllers;

import Controllers.DangNhapController;
import Models.TaiKhoan;
import Repository.TaiKhoanRepository;
import Utils.MatKhauUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================================
 * [Y3] UNIT TEST CONTROLLER XÁC THỰC & ĐỔI MẬT KHẨU (DANGNHAPCONTROLLER)
 * Sinh viên: Vòng Kiệt - MSSV: TV00514 - Lớp: SD21307
 * Kế thừa kỹ thuật Tuần 2:
 *  - @ParameterizedTest + @CsvSource kiểm tra giá trị biên Form
 *  - assertThrows() kiểm tra ngoại lệ nghiệp vụ
 *  - @Timeout kiểm tra giới hạn thời gian thực thi (Lab 3)
 * Ánh xạ Test Cases: TC_AUTH_01, TC_AUTH_02, TC_AUTH_04, TC_AUTH_05
 * =========================================================================
 */
@DisplayName("[Tuần 2 - Y3] Kiểm thử Controller Xác thực & Đổi mật khẩu")
public class AuthControllerTest {

    private InMemoryTaiKhoanRepository fakeRepo;
    private DangNhapController controller;

    private static final String VALID_EMAIL = "admin@fpt.edu.vn";
    private static final String VALID_RAW_PASS = "123456";
    // SHA-256("qlsv$" + "123") chuẩn từ hệ thống cũ
    private static final String LEGACY_HASH_123 =
            "3A4B766F18B7E187D65162B5D705C843B79E571DFDD888EFA8E782045AC6B283";

    @BeforeEach
    void setUp() {
        fakeRepo = new InMemoryTaiKhoanRepository();
        controller = new DangNhapController(fakeRepo);

        // Khởi tạo tài khoản hoạt động bình thường
        TaiKhoan activeUser = new TaiKhoan();
        activeUser.setMaNguoidung(1);
        activeUser.setEmail(VALID_EMAIL);
        activeUser.setMatKhau(MatKhauUtil.bam(VALID_RAW_PASS)); // Băm chuẩn PBKDF2
        activeUser.setHoatDong(true);
        activeUser.setTenVaiTro("ADMIN");
        fakeRepo.save(activeUser);

        // Khởi tạo tài khoản dùng hash cũ (cần nâng cấp)
        TaiKhoan legacyUser = new TaiKhoan();
        legacyUser.setMaNguoidung(2);
        legacyUser.setEmail("legacy@fpt.edu.vn");
        legacyUser.setMatKhau(LEGACY_HASH_123);
        legacyUser.setHoatDong(true);
        legacyUser.setTenVaiTro("STUDENT");
        fakeRepo.save(legacyUser);

        // Khởi tạo tài khoản bị khóa
        TaiKhoan lockedUser = new TaiKhoan();
        lockedUser.setMaNguoidung(3);
        lockedUser.setEmail("locked@fpt.edu.vn");
        lockedUser.setMatKhau(MatKhauUtil.bam(VALID_RAW_PASS));
        lockedUser.setHoatDong(false);
        lockedUser.setTenVaiTro("STUDENT");
        fakeRepo.save(lockedUser);
    }

    // =====================================================================
    // 1. KIỂM THỬ ĐĂNG NHẬP (TC_AUTH_01 & TC_AUTH_02)
    // =====================================================================

    @Test
    @DisplayName("TC_AUTH_01: Đăng nhập thành công với tài khoản Admin hợp lệ")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testLoginSuccess_Admin() {
        TaiKhoan result = controller.dangNhap(VALID_EMAIL, VALID_RAW_PASS);

        assertNotNull(result, "Đăng nhập hợp lệ phải trả về đối tượng TaiKhoan");
        assertEquals(1, result.getMaNguoidung());
        assertEquals(VALID_EMAIL, result.getEmail());
        assertEquals("ADMIN", result.getTenVaiTro());
        assertNull(result.getMatKhau(), "Mật khẩu băm phải được xóa khỏi đối tượng session để bảo mật");
    }

    @Test
    @DisplayName("TC_AUTH_01_EXT: Tự động nâng cấp hash cũ lên PBKDF2 khi đăng nhập đúng mật khẩu")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testLoginSuccess_UpgradesLegacyHash() {
        TaiKhoan result = controller.dangNhap("legacy@fpt.edu.vn", "123");

        assertNotNull(result);
        assertNotNull(fakeRepo.lastUpdatedHash, "Hệ thống phải tự động nâng cấp hash mật khẩu");
        assertTrue(fakeRepo.lastUpdatedHash.startsWith("pbkdf2$"), "Hash mới phải theo định dạng pbkdf2$");
        assertTrue(MatKhauUtil.khop("123", fakeRepo.lastUpdatedHash));
    }

    @ParameterizedTest(name = "[{index}] Email=''{0}'', Mật khẩu=''{1}'' -> Kỳ vọng null")
    @DisplayName("TC_AUTH_02: Đăng nhập thất bại với các giá trị biên form và tài khoản không hợp lệ")
    @CsvSource({
            "'', '123456'",                 // Email rỗng
            "'   ', '123456'",              // Email toàn khoảng trắng
            "'admin@fpt.edu.vn', ''",       // Mật khẩu rỗng
            "'notfound@fpt.edu.vn', '123456'", // Email không tồn tại
            "'locked@fpt.edu.vn', '123456'",   // Tài khoản bị khóa (hoatDong = false)
            "'admin@fpt.edu.vn', 'SaiPass123'", // Sai mật khẩu
            "'admin@fpt.edu.vn', '123'",        // Mật khẩu thiếu ký tự
            "'admin@fpt.edu.vn', '123456 '"     // Mật khẩu thừa dấu cách
    })
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testLoginFailure_Parameterized(String email, String matKhau) {
        TaiKhoan result = controller.dangNhap(email, matKhau);
        assertNull(result, "Đăng nhập với dữ liệu sai phải trả về null");
    }

    @ParameterizedTest
    @DisplayName("TC_AUTH_02_EXT: Đăng nhập thất bại khi Email hoặc Password nhận giá trị null")
    @NullAndEmptySource
    void testLoginFailure_NullEmailOrPassword(String input) {
        assertNull(controller.dangNhap(input, VALID_RAW_PASS));
        assertNull(controller.dangNhap(VALID_EMAIL, input));
    }

    // =====================================================================
    // 2. KIỂM THỬ ĐỔI MẬT KHẨU (TC_AUTH_04 & TC_AUTH_05)
    // =====================================================================

    @ParameterizedTest(name = "[{index}] Mật khẩu mới=''{0}'' (độ dài < 6)")
    @DisplayName("TC_AUTH_04: Đổi mật khẩu thất bại khi mật khẩu mới dưới 6 ký tự (Kiểm tra biên)")
    @ValueSource(strings = {"", "1", "12", "123", "1234", "12345"})
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testDoiMatKhau_MatKhauMoiDuoi6KyTu_ThrowsException(String matKhauNgan) {
        Exception exception = assertThrows(Exception.class, () -> {
            controller.doiMatKhau(1, VALID_RAW_PASS, matKhauNgan, matKhauNgan);
        }, "Phải ném ngoại lệ khi mật khẩu mới có ít hơn 6 ký tự");

        assertEquals("Mật khẩu mới phải có ít nhất 6 ký tự.", exception.getMessage());
    }

    @Test
    @DisplayName("TC_AUTH_04_NULL: Đổi mật khẩu thất bại khi mật khẩu mới là null")
    void testDoiMatKhau_MatKhauMoiNull_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            controller.doiMatKhau(1, VALID_RAW_PASS, null, null);
        });
        assertEquals("Mật khẩu mới phải có ít nhất 6 ký tự.", exception.getMessage());
    }

    @Test
    @DisplayName("TC_AUTH_05: Đổi mật khẩu thất bại khi nhập lại mật khẩu không khớp")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testDoiMatKhau_NhapLaiKhongKhop_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            controller.doiMatKhau(1, VALID_RAW_PASS, "NewPass@123", "MismatchPass@123");
        }, "Phải ném ngoại lệ khi mật khẩu mới và nhập lại không khớp");

        assertEquals("Mật khẩu mới nhập lại không khớp.", exception.getMessage());
    }

    @Test
    @DisplayName("TC_AUTH_06: Đổi mật khẩu thất bại khi mã người dùng không tồn tại")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testDoiMatKhau_KhongTimThayTaiKhoan_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            controller.doiMatKhau(9999, VALID_RAW_PASS, "NewPass@123", "NewPass@123");
        });

        assertEquals("Không tìm thấy tài khoản.", exception.getMessage());
    }

    @Test
    @DisplayName("TC_AUTH_07: Đổi mật khẩu thất bại khi mật khẩu hiện tại không đúng")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testDoiMatKhau_SaiMatKhauCu_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            controller.doiMatKhau(1, "MatKhauCuSai123", "NewPass@123", "NewPass@123");
        });

        assertEquals("Mật khẩu hiện tại không đúng.", exception.getMessage());
    }

    @Test
    @DisplayName("TC_AUTH_08: Đổi mật khẩu thành công khi thông tin hoàn toàn hợp lệ (biên 6 ký tự)")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testDoiMatKhau_ThanhCong() {
        String newPassMinLength = "654321"; // Đúng 6 ký tự (giá trị biên hợp lệ)

        assertDoesNotThrow(() -> {
            controller.doiMatKhau(1, VALID_RAW_PASS, newPassMinLength, newPassMinLength);
        });

        assertNotNull(fakeRepo.lastUpdatedHash);
        assertTrue(MatKhauUtil.khop(newPassMinLength, fakeRepo.lastUpdatedHash),
                "Hash mới phải khớp với mật khẩu mới vừa đổi");
    }

    // =====================================================================
    // LỚP REPOSITORY GIẢ LẬP ĐỂ TEST ĐỘC LẬP (KHÔNG CẦN KẾT NỐI DATABASE)
    // =====================================================================
    private static class InMemoryTaiKhoanRepository extends TaiKhoanRepository {
        private final Map<Integer, TaiKhoan> byId = new HashMap<>();
        private final Map<String, TaiKhoan> byEmail = new HashMap<>();
        private String lastUpdatedHash;

        void save(TaiKhoan tk) {
            byId.put(tk.getMaNguoidung(), tk);
            byEmail.put(tk.getEmail().toLowerCase(), tk);
        }

        @Override
        public TaiKhoan findByEmail(String email) {
            if (email == null) return null;
            return byEmail.get(email.trim().toLowerCase());
        }

        @Override
        public TaiKhoan findByMaNguoidung(int maNguoidung) {
            return byId.get(maNguoidung);
        }

        @Override
        public boolean capNhatMatKhau(int maNguoidung, String matKhauBam) {
            TaiKhoan tk = byId.get(maNguoidung);
            if (tk != null) {
                tk.setMatKhau(matKhauBam);
                lastUpdatedHash = matKhauBam;
                return true;
            }
            return false;
        }
    }
}
