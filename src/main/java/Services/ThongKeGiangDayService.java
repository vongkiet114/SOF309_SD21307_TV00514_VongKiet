package Services;

import Models.ThongKeGiangDay;
import Repository.GiangVienPortalRepository;

public class ThongKeGiangDayService {

    private final GiangVienPortalRepository repo;

    public ThongKeGiangDayService() {
        this(new GiangVienPortalRepository());
    }

    ThongKeGiangDayService(GiangVienPortalRepository repo) {
        this.repo = repo;
    }

    public ThongKeGiangDay thongKe(int maGiangVien) {
        return repo.thongKeGiangDay(maGiangVien);
    }
}
