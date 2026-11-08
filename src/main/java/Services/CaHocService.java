package Services;

import Models.CaHoc;
import Repository.CaHocRepository;
import Repository.DeleteConflictException;

import java.util.List;

public class CaHocService {
    private final CaHocRepository repo;

    public CaHocService() {
        this(new CaHocRepository());
    }

    CaHocService(CaHocRepository repo) {
        this.repo = repo;
    }

    public List<CaHoc> getAll() {
        return repo.getAll();
    }

    public CaHoc getById(int id) {
        return repo.getById(id);
    }

    public void save(CaHoc caHoc) throws CatalogException {
        if (caHoc.getMaCa() < 0) {
            throw new CatalogException("Mã ca học không hợp lệ.");
        }
        if (caHoc.getGioBatdau() == null || caHoc.getGioKetthuc() == null) {
            throw new CatalogException("Giờ bắt đầu và giờ kết thúc là bắt buộc.");
        }
        if (!caHoc.getGioKetthuc().isAfter(caHoc.getGioBatdau())) {
            throw new CatalogException("Giờ kết thúc phải sau giờ bắt đầu.");
        }
        boolean ok = caHoc.getMaCa() > 0 ? repo.update(caHoc) : repo.add(caHoc);
        if (!ok) {
            throw new CatalogException("Không thể lưu ca học.");
        }
    }

    public void delete(int id) throws CatalogException {
        if (id <= 0 || repo.getById(id) == null) {
            throw new CatalogException("Không tìm thấy ca học cần xóa.");
        }
        String reason = repo.getDeleteConstraint(id);
        if (reason != null) {
            throw new CatalogException("Không thể xóa ca học vì " + reason + ".");
        }
        try {
            if (!repo.delete(id)) {
                throw new CatalogException("Không thể xóa ca học.");
            }
        } catch (DeleteConflictException e) {
            throw new CatalogException("Không thể xóa ca học vì bản ghi đang được sử dụng.");
        }
    }
}
