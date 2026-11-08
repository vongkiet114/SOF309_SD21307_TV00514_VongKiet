package Services;

import Models.ChuyenNganh;
import Models.LopHoc;
import Repository.ChuyenNganhRepository;
import Repository.LopHocRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LopHocServiceTest {
    @Test void luuLopVoiTenVaChuyenNganhHopLe() throws Exception {
        FakeLopHocRepository repo = new FakeLopHocRepository();
        LopHoc lop = new LopHoc();
        lop.setTenLop("  SD20301 ");
        lop.setMaChuyenNganh(2);

        new LopHocService(repo, new ExistingChuyenNganhRepository()).save(lop);

        assertEquals("SD20301", repo.saved.getTenLop());
    }

    @Test void chanTenTrungVaChuyenNganhKhongTonTai() {
        FakeLopHocRepository repo = new FakeLopHocRepository();
        repo.duplicate = true;
        assertEquals("Tên lớp đã tồn tại.", assertThrows(CatalogException.class,
                () -> new LopHocService(repo, new ExistingChuyenNganhRepository()).save(valid())).getMessage());

        repo.duplicate = false;
        assertEquals("Chuyên ngành được chọn không tồn tại.", assertThrows(CatalogException.class,
                () -> new LopHocService(repo, new MissingChuyenNganhRepository()).save(valid())).getMessage());
    }

    @Test void chanXoaLopDangCoSinhVien() {
        FakeLopHocRepository repo = new FakeLopHocRepository();
        repo.existing = valid();
        repo.constraint = "đang có sinh viên sử dụng";

        CatalogException error = assertThrows(CatalogException.class,
                () -> new LopHocService(repo, new ExistingChuyenNganhRepository()).delete(4));

        assertTrue(error.getMessage().contains("sinh viên"));
    }

    @Test void chanTenLopKhongHopLe() {
        LopHocService service = new LopHocService(
                new FakeLopHocRepository(), new ExistingChuyenNganhRepository());
        LopHoc thieuTen = valid();
        thieuTen.setTenLop("   ");
        assertEquals("Tên lớp không được để trống.",
                assertThrows(CatalogException.class, () -> service.save(thieuTen)).getMessage());

        LopHoc tenDai = valid();
        tenDai.setTenLop(new String(new char[51]).replace('\0', 'a'));
        assertEquals("Tên lớp không được vượt quá 50 ký tự.",
                assertThrows(CatalogException.class, () -> service.save(tenDai)).getMessage());
    }

    @Test void capNhatLoaiTruDungIdKhoiKiemTraTrung() throws Exception {
        FakeLopHocRepository repo = new FakeLopHocRepository();
        LopHoc lop = valid();

        new LopHocService(repo, new ExistingChuyenNganhRepository()).save(lop);

        assertEquals(4, repo.excludeId);
        assertEquals("SD20301", repo.checkedName);
        assertEquals(4, repo.saved.getMaLop());
        assertTrue(repo.updated);
    }

    @Test void chanXoaLopKhongTonTai() {
        FakeLopHocRepository repo = new FakeLopHocRepository();

        assertEquals("Không tìm thấy lớp cần xóa.",
                assertThrows(CatalogException.class,
                        () -> new LopHocService(repo, new ExistingChuyenNganhRepository()).delete(9)).getMessage());
    }

    @Test void idSuaAmKhongDuocLuuThanhLopMoi() {
        FakeLopHocRepository repo = new FakeLopHocRepository();
        LopHoc lop = valid();
        lop.setMaLop(-2);

        CatalogException error = assertThrows(CatalogException.class,
                () -> new LopHocService(repo, new ExistingChuyenNganhRepository()).save(lop));

        assertEquals("Mã lớp không hợp lệ.", error.getMessage());
        assertFalse(repo.added);
    }

    private static LopHoc valid() {
        LopHoc lop = new LopHoc();
        lop.setMaLop(4);
        lop.setTenLop("SD20301");
        lop.setMaChuyenNganh(2);
        return lop;
    }

    private static class FakeLopHocRepository extends LopHocRepository {
        LopHoc saved;
        LopHoc existing;
        boolean duplicate;
        boolean added;
        boolean updated;
        String constraint;
        String checkedName;
        int excludeId;

        @Override public boolean existsByTenLop(String value, int excludeId) {
            checkedName = value;
            this.excludeId = excludeId;
            return duplicate;
        }
        @Override public int add(LopHoc value) { added = true; saved = value; return 4; }
        @Override public boolean update(LopHoc value) { saved = value; updated = true; return true; }
        @Override public LopHoc getById(int id) { return existing; }
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
