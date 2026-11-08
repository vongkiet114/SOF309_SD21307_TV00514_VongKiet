package Services;

import Models.LichSuDiemDanhItem;
import Repository.DiemDanhRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XemDiemDanhServiceTest {
    @Test
    void serviceTraDanhSachCoKieuThayChoMangObject() {
        LichSuDiemDanhItem item = new LichSuDiemDanhItem(
                LocalDate.of(2026, 7, 20), "Lập trình Java",
                "Công nghệ thông tin", "Vang");
        FakeDiemDanhRepository repo = new FakeDiemDanhRepository(item);

        List<LichSuDiemDanhItem> result =
                new XemDiemDanhService(repo).getLichSuDiemDanh(9);

        assertEquals(LocalDate.of(2026, 7, 20), result.get(0).getNgayHoc());
        assertEquals("Vang", result.get(0).getTrangThai());
    }

    private static final class FakeDiemDanhRepository extends DiemDanhRepository {
        private final List<LichSuDiemDanhItem> history;

        private FakeDiemDanhRepository(LichSuDiemDanhItem item) {
            this.history = Collections.singletonList(item);
        }

        @Override
        public List<LichSuDiemDanhItem> findLichSuByMaNguoiDung(int maNguoiDung) {
            return history;
        }
    }
}
