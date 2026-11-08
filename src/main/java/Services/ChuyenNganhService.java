package Services;

import Models.ChuyenNganh;
import Repository.ChuyenNganhRepository;
import Repository.DeleteConflictException;
import Repository.KhoaRepository;

import java.util.List;
import java.util.Locale;

public class ChuyenNganhService {
    private final ChuyenNganhRepository repo;
    private final KhoaRepository khoaRepo;

    public ChuyenNganhService() {
        this(new ChuyenNganhRepository(), new KhoaRepository());
    }

    ChuyenNganhService(ChuyenNganhRepository repo, KhoaRepository khoaRepo) {
        this.repo = repo;
        this.khoaRepo = khoaRepo;
    }

    public List<ChuyenNganh> getAll() {
        return repo.getAll();
    }

    public List<ChuyenNganh> search(String keyword, Integer maKhoa) {
        return repo.search(keyword, maKhoa);
    }

    public ChuyenNganh getById(int id) {
        return repo.getById(id);
    }

    public void save(ChuyenNganh cn) throws CatalogException {
        if (cn.getMaChuyenNganh() < 0) {
            throw new CatalogException("Mã chuyên ngành không hợp lệ.");
        }
        String maCn = cn.getMaCn() == null ? "" : cn.getMaCn().trim().toUpperCase(Locale.ROOT);
        String ten = cn.getTenChuyenNganh() == null ? "" : cn.getTenChuyenNganh().trim();
        if (maCn.isEmpty()) {
            throw new CatalogException("Mã chuyên ngành không được để trống.");
        }
        if (maCn.length() > 20) {
            throw new CatalogException("Mã chuyên ngành không được vượt quá 20 ký tự.");
        }
        if (ten.isEmpty()) {
            throw new CatalogException("Tên chuyên ngành không được để trống.");
        }
        if (ten.length() > 100) {
            throw new CatalogException("Tên chuyên ngành không được vượt quá 100 ký tự.");
        }
        cn.setMaCn(maCn);
        cn.setTenChuyenNganh(ten);
        if (repo.existsByMaCn(maCn, cn.getMaChuyenNganh())) {
            throw new CatalogException("Mã chuyên ngành đã tồn tại.");
        }
        if (cn.getMaKhoa() <= 0 || khoaRepo.getById(cn.getMaKhoa()) == null) {
            throw new CatalogException("Khoa được chọn không tồn tại.");
        }
        boolean ok = cn.getMaChuyenNganh() > 0 ? repo.update(cn) : repo.add(cn);
        if (!ok) {
            throw new CatalogException("Không thể lưu chuyên ngành.");
        }
    }

    public void delete(int id) throws CatalogException {
        if (id <= 0 || repo.getById(id) == null) {
            throw new CatalogException("Không tìm thấy chuyên ngành cần xóa.");
        }
        String reason = repo.getDeleteConstraint(id);
        if (reason != null) {
            throw new CatalogException("Không thể xóa chuyên ngành vì " + reason + ".");
        }
        try {
            if (!repo.delete(id)) {
                throw new CatalogException("Không thể xóa chuyên ngành.");
            }
        } catch (DeleteConflictException e) {
            throw new CatalogException(
                    "Không thể xóa chuyên ngành vì bản ghi đang được sử dụng.");
        }
    }
}
