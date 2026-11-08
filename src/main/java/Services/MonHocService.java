package Services;

import Models.MonHoc;
import Repository.ChuyenNganhRepository;
import Repository.DeleteConflictException;
import Repository.MonHocRepository;

import java.util.List;
import java.util.Locale;

public class MonHocService {
    private final MonHocRepository repo;
    private final ChuyenNganhRepository chuyenNganhRepo;

    public MonHocService() {
        this(new MonHocRepository(), new ChuyenNganhRepository());
    }

    MonHocService(MonHocRepository repo, ChuyenNganhRepository chuyenNganhRepo) {
        this.repo = repo;
        this.chuyenNganhRepo = chuyenNganhRepo;
    }

    public List<MonHoc> getAll() {
        return repo.getAll();
    }

    public List<MonHoc> getAll(String keyword) {
        return repo.getAll(keyword);
    }

    public MonHoc getById(int id) {
        return repo.getById(id);
    }

    public void save(MonHoc mon) throws CatalogException {
        if (mon.getMaMonhoc() < 0) {
            throw new CatalogException("Mã môn học không hợp lệ.");
        }
        String maMon = mon.getMaMon() == null ? "" : mon.getMaMon().trim().toUpperCase(Locale.ROOT);
        String tenMon = mon.getTenMonhoc() == null ? "" : mon.getTenMonhoc().trim();
        if (maMon.isEmpty()) {
            throw new CatalogException("Mã môn không được để trống.");
        }
        if (maMon.length() > 20) {
            throw new CatalogException("Mã môn không được vượt quá 20 ký tự.");
        }
        if (tenMon.isEmpty()) {
            throw new CatalogException("Tên môn học không được để trống.");
        }
        if (tenMon.length() > 100) {
            throw new CatalogException("Tên môn học không được vượt quá 100 ký tự.");
        }
        if (mon.getSoTinchi() <= 0) {
            throw new CatalogException("Số tín chỉ phải lớn hơn 0.");
        }
        mon.setMaMon(maMon);
        mon.setTenMonhoc(tenMon);
        if (repo.existsByMaMon(maMon, mon.getMaMonhoc())) {
            throw new CatalogException("Mã môn đã tồn tại.");
        }
        if (mon.getMaChuyenNganh() <= 0
                || chuyenNganhRepo.getById(mon.getMaChuyenNganh()) == null) {
            throw new CatalogException("Chuyên ngành được chọn không tồn tại.");
        }
        boolean ok = mon.getMaMonhoc() > 0 ? repo.update(mon) : repo.add(mon);
        if (!ok) {
            throw new CatalogException("Không thể lưu môn học.");
        }
    }

    public void delete(int id) throws CatalogException {
        if (id <= 0 || repo.getById(id) == null) {
            throw new CatalogException("Không tìm thấy môn học cần xóa.");
        }
        String reason = repo.getDeleteConstraint(id);
        if (reason != null) {
            throw new CatalogException("Không thể xóa môn học vì " + reason + ".");
        }
        try {
            if (!repo.delete(id)) {
                throw new CatalogException("Không thể xóa môn học.");
            }
        } catch (DeleteConflictException e) {
            throw new CatalogException("Không thể xóa môn học vì bản ghi đang được sử dụng.");
        }
    }
}
