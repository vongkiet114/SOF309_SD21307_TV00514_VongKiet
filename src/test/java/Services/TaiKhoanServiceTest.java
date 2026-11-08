package Services;

import Models.TaiKhoan;
import Models.VaiTroHeThong;
import Repository.TaiKhoanRepository;
import Repository.VaiTroRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaiKhoanServiceTest {
    @Test
    void taoSinhVienLayIdTuTenVaiTroThayViVietCung() throws Exception {
        CapturingTaiKhoanRepository accounts = new CapturingTaiKhoanRepository();
        TaiKhoanService service = new TaiKhoanService(accounts, new FixedVaiTroRepository(3));

        int createdId = service.themTaiKhoan(
                "sv@example.com", "Sinh Vien", VaiTroHeThong.STUDENT);

        assertEquals(101, createdId);
        assertEquals(3, accounts.saved.getMaVaitro());
        assertEquals("sv@example.com", accounts.saved.getEmail());
        assertEquals("Sinh Vien", accounts.saved.getHoTen());
    }

    @Test
    void baoLoiRoRangNeuDatabaseThieuVaiTro() {
        TaiKhoanService service = new TaiKhoanService(
                new CapturingTaiKhoanRepository(), new FixedVaiTroRepository(null));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.themTaiKhoan(
                        "sv@example.com", "Sinh Vien", VaiTroHeThong.STUDENT));

        assertEquals("Vai trò không tồn tại: STUDENT", error.getMessage());
    }

    private static class FixedVaiTroRepository extends VaiTroRepository {
        private final Integer id;

        FixedVaiTroRepository(Integer id) {
            this.id = id;
        }

        @Override
        public Integer findIdByName(String name) {
            return id;
        }
    }

    private static class CapturingTaiKhoanRepository extends TaiKhoanRepository {
        private TaiKhoan saved;

        @Override
        public TaiKhoan findByEmail(String email) {
            return null;
        }

        @Override
        public int insert(TaiKhoan account) {
            saved = account;
            return 101;
        }
    }
}
