package Services;

import Models.ThongBao;
import Repository.ThongBaoRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThongBaoServiceTest {

    @Test
    void layToiDaNamThongBaoMoiNhatCuaNguoiDung() {
        FakeThongBaoRepository repo = new FakeThongBaoRepository();
        ThongBaoService service = new ThongBaoService(repo);

        List<ThongBao> result = service.findRecentByNguoiDung(7, 5);

        assertEquals(7, repo.userId);
        assertEquals(5, repo.limit);
        assertEquals(5, result.size());
    }

    @Test
    void layTatCaThongBaoCuaNguoiDungKhongDatGioiHanNhoHon() {
        FakeThongBaoRepository repo = new FakeThongBaoRepository();
        ThongBaoService service = new ThongBaoService(repo);

        service.findByNguoiDung(9);

        assertEquals(9, repo.userId);
        assertEquals(Integer.MAX_VALUE, repo.limit);
    }

    private static class FakeThongBaoRepository extends ThongBaoRepository {
        private int userId;
        private int limit;

        @Override
        public List<ThongBao> findRecentByNguoiDung(int maNguoiDung, int soLuong) {
            userId = maNguoiDung;
            limit = soLuong;
            List<ThongBao> notifications = new ArrayList<>();
            for (int i = 0; i < Math.min(soLuong, 5); i++) {
                notifications.add(new ThongBao());
            }
            return notifications;
        }
    }
}
