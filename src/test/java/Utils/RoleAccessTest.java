package Utils;

import Models.VaiTroHeThong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleAccessTest {
    @Test
    void moiKhuVucCoMotVaiTroBatBuoc() {
        assertEquals(VaiTroHeThong.ADMIN, RoleAccess.requiredRole("/admin/sinhvien"));
        assertEquals(VaiTroHeThong.TEACHER, RoleAccess.requiredRole("/giangvien/nhapdiem"));
        assertEquals(VaiTroHeThong.STUDENT, RoleAccess.requiredRole("/sinhvien/bangdiem"));
    }

    @Test
    void duongDanCongKhaiKhongBiGanVaiTro() {
        assertNull(RoleAccess.requiredRole("/login"));
        assertNull(RoleAccess.requiredRole("/dashboard"));
        assertNull(RoleAccess.requiredRole(null));
    }

    @Test
    void gocKhuVucVanDuocBaoVe() {
        assertEquals(VaiTroHeThong.ADMIN, RoleAccess.requiredRole("/admin"));
        assertEquals(VaiTroHeThong.TEACHER, RoleAccess.requiredRole("/giangvien"));
        assertEquals(VaiTroHeThong.STUDENT, RoleAccess.requiredRole("/sinhvien"));
    }
}
