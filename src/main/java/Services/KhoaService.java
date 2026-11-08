package Services;

import Models.Khoa;
import Repository.DeleteConflictException;
import Repository.KhoaRepository;

import java.util.List;

public class KhoaService {
    private final KhoaRepository repo;

    public KhoaService() {
        this(new KhoaRepository());
    }

    KhoaService(KhoaRepository repo) {
        this.repo = repo;
    }

    public List<Khoa> getAll() {
        return repo.getAll();
    }

    public Khoa getById(int id) {
        return repo.getById(id);
    }

    public void save(Khoa khoa) throws CatalogException {
        if (khoa.getMaKhoa() < 0) {
            throw new CatalogException("Mã khoa không hợp lệ.");
        }
        String ten = khoa.getTenKhoa() == null ? "" : khoa.getTenKhoa().trim();
        if (ten.isEmpty()) {
            throw new CatalogException("Tên khoa không được để trống.");
        }
        if (ten.length() > 100) {
            throw new CatalogException("Tên khoa không được vượt quá 100 ký tự.");
        }
        khoa.setTenKhoa(ten);
        if (repo.existsByTenKhoa(ten, khoa.getMaKhoa())) {
            throw new CatalogException("Tên khoa đã tồn tại.");
        }
        boolean ok = khoa.getMaKhoa() > 0 ? repo.update(khoa) : repo.add(khoa);
        if (!ok) {
            throw new CatalogException("Không thể lưu khoa.");
        }
    }

    public void delete(int id) throws CatalogException {
        if (id <= 0 || repo.getById(id) == null) {
            throw new CatalogException("Không tìm thấy khoa cần xóa.");
        }
        String reason = repo.getDeleteConstraint(id);
        if (reason != null) {
            throw new CatalogException("Không thể xóa khoa vì " + reason + ".");
        }
        try {
            if (!repo.delete(id)) {
                throw new CatalogException("Không thể xóa khoa.");
            }
        } catch (DeleteConflictException e) {
            throw new CatalogException("Không thể xóa khoa vì bản ghi đang được sử dụng.");
        }
    }
}
