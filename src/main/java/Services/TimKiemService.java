package Services;

import Models.TieuChiTimKiem;
import Repository.TimKiemRepository;
import Repository.TimKiemRepository.KetQuaSinhVien;
import java.util.ArrayList;
import java.util.List;

import Utils.Pagination;

public class TimKiemService {

    private final TimKiemRepository repo = new TimKiemRepository();

    public List<KetQuaSinhVien> timSinhVien(TieuChiTimKiem tc) {
        return repo.timSinhVien(tc);
    }

    public long demSinhVien(TieuChiTimKiem tc) {
        return repo.demSinhVien(tc);
    }

    public Pagination layPhanTrang(TieuChiTimKiem tc) {
        return null;
    }
}
