package Services;

import Models.PhongHoc;
import Repository.PhongHocRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhongHocServiceTest {
    @Test void chuanHoaVaLuuTenPhong() throws Exception {
        FakePhongHocRepository repo = new FakePhongHocRepository();
        PhongHoc phong = new PhongHoc();
        phong.setTenPhong("  T101 ");

        new PhongHocService(repo).save(phong);

        assertEquals("T101", repo.saved.getTenPhong());
    }

    @Test void chanTenPhongRong() {
        PhongHoc phong = new PhongHoc();
        phong.setTenPhong("   ");

        assertEquals("Tên phòng không được để trống.", assertThrows(CatalogException.class,
                () -> new PhongHocService(new FakePhongHocRepository()).save(phong)).getMessage());
    }

    @Test void chanTenPhongTrung() {
        FakePhongHocRepository repo = new FakePhongHocRepository();
        repo.duplicate = true;
        PhongHoc phong = new PhongHoc();
        phong.setTenPhong("T101");

        assertEquals("Tên phòng đã tồn tại.", assertThrows(CatalogException.class,
                () -> new PhongHocService(repo).save(phong)).getMessage());
    }

    @Test void chanTenQuaDaiVaXoaPhongDangDuocDung() {
        PhongHoc dai = new PhongHoc();
        dai.setTenPhong("123456789012345678901");
        assertEquals("Tên phòng không được vượt quá 20 ký tự.", assertThrows(CatalogException.class,
                () -> new PhongHocService(new FakePhongHocRepository()).save(dai)).getMessage());

        FakePhongHocRepository repo = new FakePhongHocRepository();
        repo.existing = new PhongHoc();
        repo.existing.setMaPhong(1);
        repo.constraint = "đang được dùng trong lịch học";
        assertTrue(assertThrows(CatalogException.class, () -> new PhongHocService(repo).delete(1))
                .getMessage().contains("lịch học"));
    }

    @Test void idSuaAmKhongDuocLuuThanhPhongHocMoi() {
        FakePhongHocRepository repo = new FakePhongHocRepository();
        PhongHoc phong = new PhongHoc();
        phong.setMaPhong(-2);
        phong.setTenPhong("T101");

        CatalogException error = assertThrows(CatalogException.class,
                () -> new PhongHocService(repo).save(phong));

        assertEquals("Mã phòng học không hợp lệ.", error.getMessage());
        assertFalse(repo.added);
    }

    private static class FakePhongHocRepository extends PhongHocRepository {
        PhongHoc saved;
        PhongHoc existing;
        boolean duplicate;
        boolean added;
        String constraint;

        @Override public boolean existsByTenPhong(String value, int excludeId) { return duplicate; }
        @Override public boolean add(PhongHoc value) { added = true; saved = value; return true; }
        @Override public boolean update(PhongHoc value) { saved = value; return true; }
        @Override public PhongHoc getById(int id) { return existing; }
        @Override public String getDeleteConstraint(int id) { return constraint; }
        @Override public boolean delete(int id) { return true; }
    }
}
