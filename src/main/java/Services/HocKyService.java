package Services;

import Models.HocKy;
import Repository.DeleteConflictException;
import Repository.HocKyRepository;

import java.util.List;

public class HocKyService {
    private final HocKyRepository repo;

    public HocKyService() {
        this(new HocKyRepository());
    }

    HocKyService(HocKyRepository repo) {
        this.repo = repo;
    }

    public List<HocKy> getAll() {
        return repo.getAll();
    }

    public HocKy getById(int id) {
        return repo.getById(id);
    }

    public void save(HocKy hocKy) throws CatalogException {
        if (hocKy.getMaHocky() < 0) {
            throw new CatalogException("Mã học kỳ không hợp lệ.");
        }
        String ten = hocKy.getTenHocky() == null ? "" : hocKy.getTenHocky().trim();
        String nam = hocKy.getNamHoc() == null ? "" : hocKy.getNamHoc().trim();
        if (ten.isEmpty()) {
            throw new CatalogException("Tên học kỳ không được để trống.");
        }
        if (ten.length() > 50) {
            throw new CatalogException("Tên học kỳ không được vượt quá 50 ký tự.");
        }
        if (nam.isEmpty()) {
            throw new CatalogException("Năm học không được để trống.");
        }
        if (nam.length() > 20) {
            throw new CatalogException("Năm học không được vượt quá 20 ký tự.");
        }
        if (hocKy.getBlock() != 1 && hocKy.getBlock() != 2) {
            throw new CatalogException("Block chỉ được là 1 hoặc 2.");
        }
        if (hocKy.getNgayBatdau() == null || hocKy.getNgayKetthuc() == null) {
            throw new CatalogException("Ngày bắt đầu và ngày kết thúc là bắt buộc.");
        }
        if (!hocKy.getNgayKetthuc().isAfter(hocKy.getNgayBatdau())) {
            throw new CatalogException("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        hocKy.setTenHocky(ten);
        hocKy.setNamHoc(nam);

        if (hocKy.getMaHocky() > 0) {
            HocKy existing = repo.getById(hocKy.getMaHocky());
            if (existing == null) {
                throw new CatalogException("Không tìm thấy học kỳ cần sửa.");
            }
            String trangThai = existing.getTrangThai();
            if ("DaKetThuc".equals(trangThai) || "DangDienRa".equals(trangThai)) {
                throw new CatalogException("Không thể sửa học kỳ đang diễn ra hoặc đã kết thúc.");
            }
        } else {
            if (hocKy.getNgayBatdau().isBefore(java.time.LocalDate.now())) {
                throw new CatalogException("Không thể tạo học kỳ với ngày bắt đầu trong quá khứ.");
            }
        }

        boolean ok = hocKy.getMaHocky() > 0 ? repo.update(hocKy) : repo.add(hocKy);
        if (!ok) {
            throw new CatalogException("Không thể lưu học kỳ.");
        }
    }

    public void delete(int id) throws CatalogException {
        HocKy existing = repo.getById(id);
        if (id <= 0 || existing == null) {
            throw new CatalogException("Không tìm thấy học kỳ cần xóa.");
        }
        String trangThai = existing.getTrangThai();
        if ("DaKetThuc".equals(trangThai) || "DangDienRa".equals(trangThai)) {
            throw new CatalogException("Không thể xóa học kỳ đang diễn ra hoặc đã kết thúc.");
        }
        String reason = repo.getDeleteConstraint(id);
        if (reason != null) {
            throw new CatalogException("Không thể xóa học kỳ vì " + reason + ".");
        }
        try {
            if (!repo.delete(id)) {
                throw new CatalogException("Không thể xóa học kỳ.");
            }
        } catch (DeleteConflictException e) {
            throw new CatalogException("Không thể xóa học kỳ vì bản ghi đang được sử dụng.");
        }
    }
}
