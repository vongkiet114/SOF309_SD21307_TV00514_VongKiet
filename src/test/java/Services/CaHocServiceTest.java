package Services;

import Models.CaHoc;
import Repository.CaHocRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaHocServiceTest {
    @Test void luuCaHocHopLe() throws Exception {
        FakeCaHocRepository repo = new FakeCaHocRepository();
        CaHoc ca = caHoc(LocalTime.of(7, 15), LocalTime.of(9, 15));

        new CaHocService(repo).save(ca);

        assertSame(ca, repo.saved);
    }

    @Test void tenCaDuocTaoTuMaDeHienThi() {
        CaHoc ca = new CaHoc();
        ca.setMaCa(7);

        assertEquals("Ca 7", ca.getTenCa());
    }

    @Test void chanGioKetThucKhongSauGioBatDau() {
        CaHoc bangNhau = caHoc(LocalTime.of(9, 15), LocalTime.of(9, 15));
        CaHoc somHon = caHoc(LocalTime.of(9, 15), LocalTime.of(8, 45));

        assertEquals("Giờ kết thúc phải sau giờ bắt đầu.", assertThrows(CatalogException.class,
                () -> new CaHocService(new FakeCaHocRepository()).save(bangNhau)).getMessage());
        assertEquals("Giờ kết thúc phải sau giờ bắt đầu.", assertThrows(CatalogException.class,
                () -> new CaHocService(new FakeCaHocRepository()).save(somHon)).getMessage());
    }

    @Test void chanThieuGioVaXoaCaDangDuocDung() {
        CaHoc thieu = new CaHoc();
        thieu.setGioBatdau(LocalTime.of(7, 15));
        assertEquals("Giờ bắt đầu và giờ kết thúc là bắt buộc.",
                assertThrows(CatalogException.class,
                        () -> new CaHocService(new FakeCaHocRepository()).save(thieu)).getMessage());

        FakeCaHocRepository repo = new FakeCaHocRepository();
        repo.existing = new CaHoc();
        repo.existing.setMaCa(1);
        repo.constraint = "đang được dùng trong kỳ thi";
        assertTrue(assertThrows(CatalogException.class, () -> new CaHocService(repo).delete(1))
                .getMessage().contains("kỳ thi"));
    }

    @Test void idSuaAmKhongBiLuuThanhThemMoi() {
        FakeCaHocRepository repo = new FakeCaHocRepository();
        CaHoc ca = caHoc(LocalTime.of(7, 15), LocalTime.of(9, 15));
        ca.setMaCa(-2);

        assertEquals("Mã ca học không hợp lệ.", assertThrows(CatalogException.class,
                () -> new CaHocService(repo).save(ca)).getMessage());
        assertNull(repo.saved);
    }

    private static CaHoc caHoc(LocalTime batDau, LocalTime ketThuc) {
        CaHoc ca = new CaHoc();
        ca.setGioBatdau(batDau);
        ca.setGioKetthuc(ketThuc);
        return ca;
    }

    private static class FakeCaHocRepository extends CaHocRepository {
        CaHoc saved;
        CaHoc existing;
        String constraint;

        @Override public boolean add(CaHoc value) { saved = value; return true; }
        @Override public boolean update(CaHoc value) { saved = value; return true; }
        @Override public CaHoc getById(int id) { return existing; }
        @Override public String getDeleteConstraint(int id) { return constraint; }
        @Override public boolean delete(int id) { return true; }
    }
}
