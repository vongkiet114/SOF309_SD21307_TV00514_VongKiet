package Services;

import Models.LopHoc;
import Repository.ChuyenNganhRepository;
import Repository.DeleteConflictException;
import Repository.LopHocRepository;

import java.util.List;

public class LopHocService {
    private final LopHocRepository repo;
    private final ChuyenNganhRepository chuyenNganhRepo;

    public LopHocService() {
        this(new LopHocRepository(), new ChuyenNganhRepository());
    }

    LopHocService(LopHocRepository repo, ChuyenNganhRepository chuyenNganhRepo) {
        this.repo = repo;
        this.chuyenNganhRepo = chuyenNganhRepo;
    }

    public List<LopHoc> getAll() {
        return repo.getAll();
    }

    public LopHoc getById(int id) {
        return repo.getById(id);
    }

    public void save(LopHoc lop) throws CatalogException {
        if (lop.getMaLop() < 0) {
            throw new CatalogException("Mã lớp không hợp lệ.");
        }
        String ten = lop.getTenLop() == null ? "" : lop.getTenLop().trim();
        if (ten.isEmpty()) {
            throw new CatalogException("Tên lớp không được để trống.");
        }
        if (ten.length() > 50) {
            throw new CatalogException("Tên lớp không được vượt quá 50 ký tự.");
        }
        lop.setTenLop(ten);
        if (repo.existsByTenLop(ten, lop.getMaLop())) {
            throw new CatalogException("Tên lớp đã tồn tại.");
        }
        if (lop.getMaChuyenNganh() <= 0
                || chuyenNganhRepo.getById(lop.getMaChuyenNganh()) == null) {
            throw new CatalogException("Chuyên ngành được chọn không tồn tại.");
        }
        boolean ok = lop.getMaLop() > 0 ? repo.update(lop) : repo.add(lop) > 0;
        if (!ok) {
            throw new CatalogException("Không thể lưu lớp.");
        }
    }

    public void delete(int id) throws CatalogException {
        if (id <= 0 || repo.getById(id) == null) {
            throw new CatalogException("Không tìm thấy lớp cần xóa.");
        }
        String reason = repo.getDeleteConstraint(id);
        if (reason != null) {
            throw new CatalogException("Không thể xóa lớp vì " + reason + ".");
        }
        try {
            if (!repo.delete(id)) {
                throw new CatalogException("Không thể xóa lớp.");
            }
        } catch (DeleteConflictException e) {
            throw new CatalogException("Không thể xóa lớp vì bản ghi đang được sử dụng.");
        }
    }
}
