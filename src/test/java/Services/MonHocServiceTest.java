package Services;

import Models.ChuyenNganh;
import Models.MonHoc;
import Repository.ChuyenNganhRepository;
import Repository.MonHocRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonHocServiceTest {
    @Test void chuanHoaMaMonVaTenMon() throws Exception {
        FakeMonHocRepository repo = new FakeMonHocRepository();
        MonHoc mon = valid();
        mon.setMaMon("  pro1014 ");
        mon.setTenMonhoc("  Lập trình Java ");

        new MonHocService(repo, new ExistingChuyenNganhRepository()).save(mon);

        assertEquals("PRO1014", repo.saved.getMaMon());
        assertEquals("Lập trình Java", repo.saved.getTenMonhoc());
    }

    @Test void chanTinChiKhongDuong() {
        MonHoc mon = valid();
        mon.setSoTinchi(0);

        CatalogException error = assertThrows(CatalogException.class,
                () -> new MonHocService(new FakeMonHocRepository(),
                        new ExistingChuyenNganhRepository()).save(mon));

        assertEquals("Số tín chỉ phải lớn hơn 0.", error.getMessage());
    }

    @Test void chanMaTrungVaChuyenNganhKhongTonTai() {
        FakeMonHocRepository repo = new FakeMonHocRepository();
        repo.duplicate = true;
        assertEquals("Mã môn đã tồn tại.", assertThrows(CatalogException.class,
                () -> new MonHocService(repo, new ExistingChuyenNganhRepository()).save(valid())).getMessage());

        repo.duplicate = false;
        assertEquals("Chuyên ngành được chọn không tồn tại.", assertThrows(CatalogException.class,
                () -> new MonHocService(repo, new MissingChuyenNganhRepository()).save(valid())).getMessage());
    }

    @Test void chanXoaMonDangDuocSuDung() {
        FakeMonHocRepository repo = new FakeMonHocRepository();
        repo.existing = valid();
        repo.constraint = "đang có lớp học phần sử dụng";

        assertTrue(assertThrows(CatalogException.class,
                () -> new MonHocService(repo, new ExistingChuyenNganhRepository()).delete(7))
                .getMessage().contains("lớp học phần"));
    }

    @Test void idSuaAmKhongDuocLuuThanhMonHocMoi() {
        FakeMonHocRepository repo = new FakeMonHocRepository();
        MonHoc mon = valid();
        mon.setMaMonhoc(-2);

        CatalogException error = assertThrows(CatalogException.class,
                () -> new MonHocService(repo, new ExistingChuyenNganhRepository()).save(mon));

        assertEquals("Mã môn học không hợp lệ.", error.getMessage());
        assertFalse(repo.added);
    }

    private static MonHoc valid() {
        MonHoc mon = new MonHoc();
        mon.setMaMonhoc(7);
        mon.setMaMon("PRO1014");
        mon.setTenMonhoc("Lập trình Java");
        mon.setSoTinchi(3);
        mon.setMaChuyenNganh(2);
        return mon;
    }

    private static class FakeMonHocRepository extends MonHocRepository {
        MonHoc saved;
        MonHoc existing;
        boolean duplicate;
        boolean added;
        String constraint;

        @Override public boolean existsByMaMon(String value, int excludeId) { return duplicate; }
        @Override public boolean add(MonHoc value) { added = true; saved = value; return true; }
        @Override public boolean update(MonHoc value) { saved = value; return true; }
        @Override public MonHoc getById(int id) { return existing; }
        @Override public String getDeleteConstraint(int id) { return constraint; }
        @Override public boolean delete(int id) { return true; }
    }

    private static class ExistingChuyenNganhRepository extends ChuyenNganhRepository {
        @Override public ChuyenNganh getById(int id) {
            ChuyenNganh cn = new ChuyenNganh();
            cn.setMaChuyenNganh(id);
            return cn;
        }
    }

    private static class MissingChuyenNganhRepository extends ChuyenNganhRepository {
        @Override public ChuyenNganh getById(int id) { return null; }
    }
}
