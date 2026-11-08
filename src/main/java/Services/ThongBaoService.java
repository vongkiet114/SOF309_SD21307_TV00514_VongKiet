package Services;

import Models.ThongBao;
import Repository.ThongBaoRepository;

import java.util.List;

// Service nghiep vu cho thong bao.
public class ThongBaoService {
    private final ThongBaoRepository repo;

    public ThongBaoService() {
        this(new ThongBaoRepository());
    }

    ThongBaoService(ThongBaoRepository repo) {
        this.repo = repo;
    }

    public List<ThongBao> getAll() {
        return repo.getAllWithRecipientScope();
    }

    public List<ThongBao> findByNguoiDung(int id) {
        return repo.findRecentByNguoiDung(id, Integer.MAX_VALUE);
    }

    public List<ThongBao> findRecentByNguoiDung(int id, int limit) {
        return repo.findRecentByNguoiDung(id, limit);
    }

    public void create(ThongBao value) {
        validate(value);
        repo.insertForAllActiveUsers(value);
    }

    public void delete(int id) {
        repo.delete(id);
    }

    private void validate(ThongBao value) {
        if (value == null || value.getTieuDe() == null || value.getTieuDe().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống.");
        }
        if (value.getNoiDung() == null || value.getNoiDung().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung không được để trống.");
        }
        if (!"ToanTruong".equals(value.getPhamVi())) {
            throw new IllegalArgumentException("Phạm vi gửi không hợp lệ.");
        }
        value.setTieuDe(value.getTieuDe().trim());
        value.setNoiDung(value.getNoiDung().trim());
    }
}
