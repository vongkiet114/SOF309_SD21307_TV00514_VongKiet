package Services;

import Models.PhongHoc;
import Repository.DeleteConflictException;
import Repository.PhongHocRepository;

import java.util.List;

public class PhongHocService {
    private final PhongHocRepository repo;

    public PhongHocService() {
        this(new PhongHocRepository());
    }

    PhongHocService(PhongHocRepository repo) {
        this.repo = repo;
    }

    public List<PhongHoc> getAll() {
        return repo.getAll();
    }

    public PhongHoc getById(int id) {
        return repo.getById(id);
    }

    public void save(PhongHoc phongHoc) throws CatalogException {
        if (phongHoc.getMaPhong() < 0) {
            throw new CatalogException("Mã phòng học không hợp lệ.");
        }
        String ten = phongHoc.getTenPhong() == null ? "" : phongHoc.getTenPhong().trim();
        if (ten.isEmpty()) {
            throw new CatalogException("Tên phòng không được để trống.");
        }
        if (ten.length() > 20) {
            throw new CatalogException("Tên phòng không được vượt quá 20 ký tự.");
        }
        phongHoc.setTenPhong(ten);
        if (repo.existsByTenPhong(ten, phongHoc.getMaPhong())) {
            throw new CatalogException("Tên phòng đã tồn tại.");
        }
        boolean ok = phongHoc.getMaPhong() > 0 ? repo.update(phongHoc) : repo.add(phongHoc);
        if (!ok) {
            throw new CatalogException("Không thể lưu phòng học.");
        }
    }

    public void delete(int id) throws CatalogException {
        if (id <= 0 || repo.getById(id) == null) {
            throw new CatalogException("Không tìm thấy phòng học cần xóa.");
        }
        String reason = repo.getDeleteConstraint(id);
        if (reason != null) {
            throw new CatalogException("Không thể xóa phòng học vì " + reason + ".");
        }
        try {
            if (!repo.delete(id)) {
                throw new CatalogException("Không thể xóa phòng học.");
            }
        } catch (DeleteConflictException e) {
            throw new CatalogException("Không thể xóa phòng học vì bản ghi đang được sử dụng.");
        }
    }
}
