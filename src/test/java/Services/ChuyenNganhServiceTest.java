package Services;

import Models.ChuyenNganh;
import Models.Khoa;
import Repository.ChuyenNganhRepository;
import Repository.KhoaRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChuyenNganhServiceTest {
    @Test void chuanHoaVaLuuChuyenNganhHopLe() throws Exception {
        FakeChuyenNganhRepository repo = new FakeChuyenNganhRepository();
        FakeKhoaRepository khoa = new FakeKhoaRepository();
        ChuyenNganh cn = new ChuyenNganh();
        cn.setMaCn("  udpm ");
        cn.setTenChuyenNganh("  Ứng dụng phần mềm ");
        cn.setMaKhoa(1);

        new ChuyenNganhService(repo, khoa).save(cn);

        assertEquals("UDPM", repo.saved.getMaCn());
        assertEquals("Ứng dụng phần mềm", repo.saved.getTenChuyenNganh());
    }

    @Test void chanMaTrungVaKhoaKhongTonTai() {
        FakeChuyenNganhRepository repo = new FakeChuyenNganhRepository();
        repo.duplicate = true;
        ChuyenNganh cn = valid();

        assertEquals("Mã chuyên ngành đã tồn tại.", assertThrows(CatalogException.class,
                () -> new ChuyenNganhService(repo, new FakeKhoaRepository()).save(cn)).getMessage());

        repo.duplicate = false;
        assertEquals("Khoa được chọn không tồn tại.", assertThrows(CatalogException.class,
                () -> new ChuyenNganhService(repo, new MissingKhoaRepository()).save(cn)).getMessage());
    }

    @Test void chanMaVaTenKhongHopLe() {
        FakeChuyenNganhRepository repo = new FakeChuyenNganhRepository();
        ChuyenNganhService service = new ChuyenNganhService(repo, new FakeKhoaRepository());

        ChuyenNganh thieuMa = valid();
        thieuMa.setMaCn("   ");
        assertEquals("Mã chuyên ngành không được để trống.",
                assertThrows(CatalogException.class, () -> service.save(thieuMa)).getMessage());

        ChuyenNganh maDai = valid();
        maDai.setMaCn("123456789012345678901");
        assertEquals("Mã chuyên ngành không được vượt quá 20 ký tự.",
                assertThrows(CatalogException.class, () -> service.save(maDai)).getMessage());

        ChuyenNganh thieuTen = valid();
        thieuTen.setTenChuyenNganh("   ");
        assertEquals("Tên chuyên ngành không được để trống.",
                assertThrows(CatalogException.class, () -> service.save(thieuTen)).getMessage());

        ChuyenNganh tenDai = valid();
        tenDai.setTenChuyenNganh(new String(new char[101]).replace('\0', 'a'));
        assertEquals("Tên chuyên ngành không được vượt quá 100 ký tự.",
                assertThrows(CatalogException.class, () -> service.save(tenDai)).getMessage());
    }

    @Test void capNhatLoaiTruDungIdKhoiKiemTraTrung() throws Exception {
        FakeChuyenNganhRepository repo = new FakeChuyenNganhRepository();
        ChuyenNganh cn = valid();
        cn.setMaChuyenNganh(7);

        new ChuyenNganhService(repo, new FakeKhoaRepository()).save(cn);

        assertEquals(7, repo.excludeId);
        assertEquals("UDPM", repo.checkedCode);
        assertEquals(7, repo.saved.getMaChuyenNganh());
        assertTrue(repo.updated);
    }

    @Test void chanXoaChuyenNganhDangDuocSuDung() {
        FakeChuyenNganhRepository repo = new FakeChuyenNganhRepository();
        repo.existing = valid();
        repo.existing.setMaChuyenNganh(3);
        repo.constraint = "đang có lớp sử dụng";

        CatalogException error = assertThrows(CatalogException.class,
                () -> new ChuyenNganhService(repo, new FakeKhoaRepository()).delete(3));

        assertTrue(error.getMessage().contains("lớp"));
    }

    @Test void chanXoaChuyenNganhKhongTonTai() {
        FakeChuyenNganhRepository repo = new FakeChuyenNganhRepository();

        assertEquals("Không tìm thấy chuyên ngành cần xóa.",
                assertThrows(CatalogException.class,
                        () -> new ChuyenNganhService(repo, new FakeKhoaRepository()).delete(9)).getMessage());
    }

    @Test void idSuaAmKhongDuocLuuThanhChuyenNganhMoi() {
        FakeChuyenNganhRepository repo = new FakeChuyenNganhRepository();
        ChuyenNganh cn = valid();
        cn.setMaChuyenNganh(-2);

        CatalogException error = assertThrows(CatalogException.class,
                () -> new ChuyenNganhService(repo, new FakeKhoaRepository()).save(cn));

        assertEquals("Mã chuyên ngành không hợp lệ.", error.getMessage());
        assertFalse(repo.added);
    }

    private static ChuyenNganh valid() {
        ChuyenNganh cn = new ChuyenNganh();
        cn.setMaCn("UDPM");
        cn.setTenChuyenNganh("Ứng dụng phần mềm");
        cn.setMaKhoa(1);
        return cn;
    }

    private static class FakeChuyenNganhRepository extends ChuyenNganhRepository {
        ChuyenNganh saved;
        boolean duplicate;
        boolean added;
        boolean updated;
        ChuyenNganh existing;
        String constraint;
        String checkedCode;
        int excludeId;

        @Override public boolean existsByMaCn(String value, int excludeId) {
            checkedCode = value;
            this.excludeId = excludeId;
            return duplicate;
        }
        @Override public boolean add(ChuyenNganh value) { added = true; saved = value; return true; }
        @Override public boolean update(ChuyenNganh value) { saved = value; updated = true; return true; }
        @Override public ChuyenNganh getById(int id) { return existing; }
        @Override public String getDeleteConstraint(int id) { return constraint; }
        @Override public boolean delete(int id) { return true; }
    }

    private static class FakeKhoaRepository extends KhoaRepository {
        @Override public Khoa getById(int id) {
            Khoa khoa = new Khoa();
            khoa.setMaKhoa(id);
            khoa.setTenKhoa("CNTT");
            return khoa;
        }
    }

    private static class MissingKhoaRepository extends KhoaRepository {
        @Override public Khoa getById(int id) { return null; }
    }
}
