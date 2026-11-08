package Services;

import Models.ThanhPhanDiem;
import Repository.MonHocRepository;
import Repository.ThanhPhanDiemRepository;

import java.math.BigDecimal;
import java.util.List;

// Nghiep vu cau hinh cot diem (thanh phan diem) + trong so cho tung mon hoc.
public class ThanhPhanDiemService {

    private static final BigDecimal MOT = BigDecimal.ONE;

    private final ThanhPhanDiemRepository repo;
    private final MonHocRepository monHocRepo;

    public ThanhPhanDiemService() {
        this(new ThanhPhanDiemRepository(), new MonHocRepository());
    }

    ThanhPhanDiemService(ThanhPhanDiemRepository repo, MonHocRepository monHocRepo) {
        this.repo = repo;
        this.monHocRepo = monHocRepo;
    }

    public List<ThanhPhanDiem> danhSachTheoMon(int maMonHoc) {
        return repo.findByMaMonHoc(maMonHoc);
    }

    // Tong trong so hien tai cua mon (0..1).
    public BigDecimal tongTrongSo(int maMonHoc) {
        return repo.sumTrongSo(maMonHoc, 0);
    }

    public void luu(ThanhPhanDiem tp) throws CatalogException {
        if (tp.getMaMonhoc() <= 0 || monHocRepo.getById(tp.getMaMonhoc()) == null) {
            throw new CatalogException("Môn học được chọn không tồn tại.");
        }
        String ten = tp.getTenThanhphan() == null ? "" : tp.getTenThanhphan().trim();
        if (ten.isEmpty()) {
            throw new CatalogException("Tên cột điểm không được để trống.");
        }
        if (ten.length() > 50) {
            throw new CatalogException("Tên cột điểm không được vượt quá 50 ký tự.");
        }
        BigDecimal trongSo = tp.getTrongSo();
        if (trongSo == null || trongSo.compareTo(BigDecimal.ZERO) <= 0
                || trongSo.compareTo(MOT) > 0) {
            throw new CatalogException("Trọng số phải lớn hơn 0% và không quá 100%.");
        }

        int exceptId = tp.getMaThanhphan() > 0 ? tp.getMaThanhphan() : 0;
        BigDecimal tongConLai = repo.sumTrongSo(tp.getMaMonhoc(), exceptId);
        BigDecimal tongMoi = tongConLai.add(trongSo);
        if (tongMoi.compareTo(MOT) > 0) {
            throw new CatalogException("Tổng trọng số các cột điểm của môn vượt quá 100% (hiện tại "
                    + phanTram(tongConLai) + "%, thêm " + phanTram(trongSo)
                    + "% sẽ thành " + phanTram(tongMoi) + "%).");
        }

        tp.setTenThanhphan(ten);
        boolean ok = tp.getMaThanhphan() > 0 ? repo.update(tp) : repo.insert(tp);
        if (!ok) {
            throw new CatalogException("Không thể lưu cột điểm.");
        }
    }

    public void xoa(int maThanhphan) throws CatalogException {
        ThanhPhanDiem tp = repo.findById(maThanhphan);
        if (tp == null) {
            throw new CatalogException("Không tìm thấy cột điểm cần xóa.");
        }
        if (repo.coDiem(maThanhphan)) {
            throw new CatalogException("Không thể xóa cột điểm vì đã có điểm của sinh viên.");
        }
        if (!repo.delete(maThanhphan)) {
            throw new CatalogException("Không thể xóa cột điểm.");
        }
    }

    private static String phanTram(BigDecimal fraction) {
        return fraction.multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros().toPlainString();
    }
}
