package Controllers;

import Models.TaiKhoan;
import Repository.TaiKhoanRepository;
import Utils.MatKhauUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DangNhapControllerTest {
    private static final String LEGACY_HASH_123 =
            "3A4B766F18B7E187D65162B5D705C843B79E571DFDD888EFA8E782045AC6B283";

    @Test
    void dangNhapHashCuThanhCongVaNangCapLenPbkdf2() {
        CapturingTaiKhoanRepository repository = new CapturingTaiKhoanRepository(activeAccount());
        DangNhapController controller = new DangNhapController(repository);

        TaiKhoan loggedIn = controller.dangNhap("student@example.com", "123");

        assertNotNull(loggedIn);
        assertNull(loggedIn.getMatKhau());
        assertNotNull(repository.updatedHash);
        assertTrue(repository.updatedHash.startsWith("pbkdf2$"));
        assertTrue(MatKhauUtil.khop("123", repository.updatedHash));
    }

    @Test
    void saiMatKhauKhongNangCapHash() {
        CapturingTaiKhoanRepository repository = new CapturingTaiKhoanRepository(activeAccount());
        DangNhapController controller = new DangNhapController(repository);

        TaiKhoan loggedIn = controller.dangNhap("student@example.com", "sai");

        assertNull(loggedIn);
        assertNull(repository.updatedHash);
    }

    private TaiKhoan activeAccount() {
        TaiKhoan account = new TaiKhoan();
        account.setMaNguoidung(7);
        account.setEmail("student@example.com");
        account.setMatKhau(LEGACY_HASH_123);
        account.setHoatDong(true);
        account.setTenVaiTro("STUDENT");
        return account;
    }

    private static class CapturingTaiKhoanRepository extends TaiKhoanRepository {
        private final TaiKhoan account;
        private String updatedHash;

        CapturingTaiKhoanRepository(TaiKhoan account) {
            this.account = account;
        }

        @Override
        public TaiKhoan findByEmail(String email) {
            return account;
        }

        @Override
        public boolean capNhatMatKhau(int maNguoidung, String matKhauBam) {
            updatedHash = matKhauBam;
            return true;
        }
    }
}
