package Services;

import Models.SinhVien;
import Models.VaiTroHeThong;
import Repository.SinhVienRepository;
import Repository.TaiKhoanRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SinhVienServiceTest {
    @Test
    void taoHoSoSinhVienBangTaiKhoanVaiTroStudent() throws Exception {
        CapturingAccountService accounts = new CapturingAccountService();
        CapturingSinhVienRepository profiles = new CapturingSinhVienRepository();
        SinhVienService service = new SinhVienService(
                profiles, new EmptyTaiKhoanRepository(), accounts);
        SinhVien sinhVien = validSinhVien();

        int createdId = service.create(sinhVien);

        assertEquals(88, createdId);
        assertEquals(VaiTroHeThong.STUDENT, accounts.role);
        assertEquals(88, sinhVien.getMaNguoidung());
        assertSame(sinhVien, profiles.saved);
    }

    private SinhVien validSinhVien() {
        SinhVien sinhVien = new SinhVien();
        sinhVien.setEmail("student@example.com");
        sinhVien.setHoTen("Sinh Vien");
        sinhVien.setMaLop(1);
        sinhVien.setNgaySinh("2000-01-01");
        sinhVien.setTrangThai("DangHoc");
        return sinhVien;
    }

    private static class CapturingAccountService extends TaiKhoanService {
        private VaiTroHeThong role;

        @Override
        public int themTaiKhoan(String email, String hoTen, VaiTroHeThong role) {
            this.role = role;
            return 88;
        }
    }

    private static class EmptyTaiKhoanRepository extends TaiKhoanRepository {
        @Override
        public Models.TaiKhoan findByEmail(String email) {
            return null;
        }
    }

    private static class CapturingSinhVienRepository extends SinhVienRepository {
        private SinhVien saved;

        @Override
        public String nextMssv() {
            return "SV001";
        }

        @Override
        public SinhVien findByMssv(String mssv) {
            return null;
        }

        @Override
        public boolean insert(SinhVien sinhVien) {
            saved = sinhVien;
            return true;
        }
    }
}
