package Services;

import Models.LichSuDiemDanhItem;
import Repository.DiemDanhRepository;

import java.util.List;

public class XemDiemDanhService {

    private final DiemDanhRepository diemDanhRepo;

    public XemDiemDanhService() {
        this(new DiemDanhRepository());
    }

    XemDiemDanhService(DiemDanhRepository diemDanhRepo) {
        this.diemDanhRepo = diemDanhRepo;
    }

    public List<LichSuDiemDanhItem> getLichSuDiemDanh(int maNguoiDung) {
        return diemDanhRepo.findLichSuByMaNguoiDung(maNguoiDung);
    }

    // Lich su diem danh co loc theo mon hoc va theo thang.
    public List<LichSuDiemDanhItem> getLichSuDiemDanh(int maNguoiDung, Integer maMonhoc,
                                                      Integer nam, Integer thang) {
        return diemDanhRepo.findLichSu(maNguoiDung, maMonhoc, nam, thang);
    }

    // Danh sach mon hoc de do vao bo loc.
    public List<Models.MonHoc> getMonHocDaDiemDanh(int maNguoiDung) {
        return diemDanhRepo.findMonHocDaDiemDanh(maNguoiDung);
    }
}
