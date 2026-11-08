package Repository;

import Models.DangKyHocPhan;
import java.util.ArrayList;
import java.util.List;

public class DangKyHocPhanRepository {

    public static class LopHocPhanKhaDung {
        // Stub
    }

    public static class LopHocPhanDaDangKy {
        // Stub
    }

    public boolean dangKy(int maSinhvien, int maLophocphan) {
        return false;
    }

    public boolean huyDangKy(int maDangky) {
        return false;
    }

    public List<LopHocPhanKhaDung> dsLHPKhaDung(int maSinhvien) {
        return new ArrayList<>();
    }

    public List<DangKyHocPhan> dsDangKyCuaSV(int maSinhvien) {
        return new ArrayList<>();
    }

    public List<LopHocPhanDaDangKy> dsDangKyChiTietCuaSV(int maSinhvien) {
        return new ArrayList<>();
    }

    public int findMaSinhVienByMaNguoiDung(int maNguoidung) {
        return 0;
    }
}
