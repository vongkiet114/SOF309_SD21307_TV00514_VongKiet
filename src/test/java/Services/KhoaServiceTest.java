package Services;

import Models.Khoa;
import Repository.KhoaRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KhoaServiceTest {
    @Test void luuKhoaChuanHoaTen() throws Exception {
        FakeKhoaRepository repo = new FakeKhoaRepository();
        KhoaService service = new KhoaService(repo);
        Khoa khoa = new Khoa();
        khoa.setTenKhoa("  Công nghệ thông tin  ");

        service.save(khoa);

        assertEquals("Công nghệ thông tin", repo.saved.getTenKhoa());
    }

    @Test void chanTenRongVaTenTrung() {
        FakeKhoaRepository repo = new FakeKhoaRepository();
        KhoaService service = new KhoaService(repo);
        Khoa rong = new Khoa();
        rong.setTenKhoa("   ");
        assertEquals("Tên khoa không được để trống.",
                assertThrows(CatalogException.class, () -> service.save(rong)).getMessage());

        repo.duplicate = true;
        Khoa trung = new Khoa();
        trung.setTenKhoa("CNTT");
        assertEquals("Tên khoa đã tồn tại.",
                assertThrows(CatalogException.class, () -> service.save(trung)).getMessage());
    }

    @Test void chanXoaKhoaDangCoChuyenNganh() {
        FakeKhoaRepository repo = new FakeKhoaRepository();
        repo.existing = new Khoa();
        repo.existing.setMaKhoa(3);
        repo.constraint = "đang có chuyên ngành sử dụng";
        CatalogException error = assertThrows(CatalogException.class,
                () -> new KhoaService(repo).delete(3));
        assertTrue(error.getMessage().contains("chuyên ngành"));
    }

    @Test void idSuaAmKhongDuocLuuThanhKhoaMoi() {
        FakeKhoaRepository repo = new FakeKhoaRepository();
        Khoa khoa = new Khoa();
        khoa.setMaKhoa(-2);
        khoa.setTenKhoa("Công nghệ thông tin");

        CatalogException error = assertThrows(CatalogException.class,
                () -> new KhoaService(repo).save(khoa));

        assertEquals("Mã khoa không hợp lệ.", error.getMessage());
        assertFalse(repo.added);
    }

    private static class FakeKhoaRepository extends KhoaRepository {
        Khoa saved;
        Khoa existing;
        boolean duplicate;
        boolean added;
        String constraint;
        @Override public boolean existsByTenKhoa(String value, int excludeId) { return duplicate; }
        @Override public boolean add(Khoa value) { added = true; saved = value; return true; }
        @Override public boolean update(Khoa value) { saved = value; return true; }
        @Override public Khoa getById(int id) { return existing; }
        @Override public String getDeleteConstraint(int id) { return constraint; }
        @Override public boolean delete(int id) { return true; }
    }
}
