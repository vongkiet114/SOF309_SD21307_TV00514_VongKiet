package Services;

import Models.GiangVien;
import Models.VaiTroHeThong;
import Repository.GiangVienRepository;
import Repository.TaiKhoanRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GiangVienServiceTest {
    @Test
    void taoHoSoGiangVienBangTaiKhoanVaiTroTeacher() throws Exception {
        CapturingAccountService accounts = new CapturingAccountService();
        CapturingGiangVienRepository profiles = new CapturingGiangVienRepository();
        GiangVienService service = new GiangVienService(
                profiles, new EmptyTaiKhoanRepository(), accounts);
        GiangVien giangVien = validGiangVien();

        int createdId = service.create(giangVien);

        assertEquals(89, createdId);
        assertEquals(VaiTroHeThong.TEACHER, accounts.role);
        assertEquals(89, giangVien.getMaNguoidung());
        assertSame(giangVien, profiles.saved);
    }

    private GiangVien validGiangVien() {
        GiangVien giangVien = new GiangVien();
        giangVien.setEmail("teacher@example.com");
        giangVien.setHoTen("Giang Vien");
        giangVien.setMaChuyenNganh(1);
        giangVien.setHocVi("Thạc sĩ");
        return giangVien;
    }

    private static class CapturingAccountService extends TaiKhoanService {
        private VaiTroHeThong role;

        @Override
        public int themTaiKhoan(String email, String hoTen, VaiTroHeThong role) {
            this.role = role;
            return 89;
        }
    }

    private static class EmptyTaiKhoanRepository extends TaiKhoanRepository {
        @Override
        public Models.TaiKhoan findByEmail(String email) {
            return null;
        }
    }

    private static class CapturingGiangVienRepository extends GiangVienRepository {
        private GiangVien saved;

        @Override
        public boolean insert(GiangVien giangVien) {
            saved = giangVien;
            return true;
        }
    }
}
