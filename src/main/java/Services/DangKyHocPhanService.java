package Services;

import Models.DangKyHocPhan;
import Repository.DangKyHocPhanRepository;
import Repository.DangKyHocPhanRepository.LopHocPhanDaDangKy;
import Repository.DangKyHocPhanRepository.LopHocPhanKhaDung;
import java.util.List;

public class DangKyHocPhanService {

    private final DangKyHocPhanRepository repo = new DangKyHocPhanRepository();

    public boolean dangKy(int maSinhvien, int maLophocphan) {
        return repo.dangKy(maSinhvien, maLophocphan);
    }

    public boolean huyDangKy(int maDangky) {
        return repo.huyDangKy(maDangky);
    }

    public List<LopHocPhanKhaDung> dsLHPKhaDung(int maSinhvien) {
        return repo.dsLHPKhaDung(maSinhvien);
    }

    public List<DangKyHocPhan> dsDangKyCuaSV(int maSinhvien) {
        return repo.dsDangKyCuaSV(maSinhvien);
    }

    public List<LopHocPhanDaDangKy> dsDangKyChiTietCuaSV(int maSinhvien) {
        return repo.dsDangKyChiTietCuaSV(maSinhvien);
    }

    public int layMaSinhVienByNguoiDung(int maNguoidung) {
        return repo.findMaSinhVienByMaNguoiDung(maNguoidung);
    }
}
