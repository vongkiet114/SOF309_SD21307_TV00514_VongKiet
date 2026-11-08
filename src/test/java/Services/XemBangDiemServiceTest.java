package Services;

import Models.BangDiemHocPhan;
import Models.DiemSo;
import Models.ThanhPhanDiem;
import Repository.DiemRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XemBangDiemServiceTest {
    private final XemBangDiemService service = new XemBangDiemService(new DiemRepository());

    @Test
    void tongKetNullChoToiKhiDuMoiThanhPhan() {
        assertNull(service.calculateTotal(twoComponents(),
                Collections.singletonList(score(1, "8"))));
    }

    @Test
    void tongKetDuThanhPhanLamTronHaiChuSo() {
        assertEquals(new BigDecimal("8.60"), service.calculateTotal(
                twoComponents(), Arrays.asList(score(1, "8"), score(2, "9"))));
    }

    @Test
    void bangDiemTheoLopDuocGanTongKetDung() {
        BangDiemHocPhan section = new BangDiemHocPhan();
        section.setMaLopHocPhan(20);
        section.setThanhPhanDiems(twoComponents());
        section.setDiemSoList(Arrays.asList(score(1, "8"), score(2, "9")));
        FakeRepo repo = new FakeRepo(Collections.singletonList(section));

        List<BangDiemHocPhan> result = new XemBangDiemService(repo).getBangDiem(99);

        assertEquals(99, repo.studentId);
        assertEquals(new BigDecimal("8.60"), result.get(0).getDiemTongKet());
    }

    private static List<ThanhPhanDiem> twoComponents() {
        return Arrays.asList(
                new ThanhPhanDiem(1, "Quá trình", new BigDecimal("0.4"), 3),
                new ThanhPhanDiem(2, "Cuối kỳ", new BigDecimal("0.6"), 3));
    }

    private static DiemSo score(int component, String value) {
        DiemSo score = new DiemSo();
        score.setMaThanhphan(component);
        score.setGiaTriDiem(new BigDecimal(value));
        return score;
    }

    private static class FakeRepo extends DiemRepository {
        private final List<BangDiemHocPhan> result;
        private int studentId;

        FakeRepo(List<BangDiemHocPhan> result) {
            this.result = result;
        }

        @Override
        public List<BangDiemHocPhan> findGradebookByStudent(int studentId) {
            this.studentId = studentId;
            return result;
        }
    }
}
