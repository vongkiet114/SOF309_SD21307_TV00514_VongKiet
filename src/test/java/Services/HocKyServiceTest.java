package Services;

import Models.HocKy;
import Repository.HocKyRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HocKyServiceTest {
    @Test void luuHocKyHopLe() throws Exception {
        FakeHocKyRepository repo = new FakeHocKyRepository();
        repo.existing = valid();
        HocKy hk = valid();
        hk.setTenHocky("  Fall 2026 ");
        hk.setNamHoc(" 2026 ");

        new HocKyService(repo).save(hk);

        assertEquals("Fall 2026", repo.saved.getTenHocky());
        assertEquals("2026", repo.saved.getNamHoc());
    }

    @Test void chanBlockVaKhoangNgaySai() {
        HocKy blockSai = valid();
        blockSai.setBlock(3);
        assertEquals("Block chỉ được là 1 hoặc 2.", assertThrows(CatalogException.class,
                () -> new HocKyService(new FakeHocKyRepository()).save(blockSai)).getMessage());

        HocKy khoangNgaySai = valid();
        khoangNgaySai.setNgayKetthuc(khoangNgaySai.getNgayBatdau());
        assertEquals("Ngày kết thúc phải sau ngày bắt đầu.", assertThrows(CatalogException.class,
                () -> new HocKyService(new FakeHocKyRepository()).save(khoangNgaySai)).getMessage());
    }

    @Test void chanNgayThieuVaXoaHocKyDangDuocDung() {
        HocKy thieuNgay = valid();
        thieuNgay.setNgayBatdau(null);
        assertEquals("Ngày bắt đầu và ngày kết thúc là bắt buộc.", assertThrows(CatalogException.class,
                () -> new HocKyService(new FakeHocKyRepository()).save(thieuNgay)).getMessage());

        FakeHocKyRepository repo = new FakeHocKyRepository();
        repo.existing = valid();
        repo.constraint = "đang có lớp học phần sử dụng";
        assertTrue(assertThrows(CatalogException.class, () -> new HocKyService(repo).delete(2))
                .getMessage().contains("lớp học phần"));
    }

    @Test void chanTenVaNamHocKhongHopLe() {
        HocKy tenSai = valid();
        tenSai.setTenHocky(" ");
        assertEquals("Tên học kỳ không được để trống.", assertThrows(CatalogException.class,
                () -> new HocKyService(new FakeHocKyRepository()).save(tenSai)).getMessage());

        HocKy namSai = valid();
        namSai.setNamHoc("123456789012345678901");
        assertEquals("Năm học không được vượt quá 20 ký tự.", assertThrows(CatalogException.class,
                () -> new HocKyService(new FakeHocKyRepository()).save(namSai)).getMessage());
    }

    @Test void chanMaHocKyAmDeKhongChenMoi() {
        FakeHocKyRepository repo = new FakeHocKyRepository();
        HocKy hocKy = valid();
        hocKy.setMaHocky(-2);

        CatalogException error = assertThrows(CatalogException.class,
                () -> new HocKyService(repo).save(hocKy));

        assertEquals("Mã học kỳ không hợp lệ.", error.getMessage());
        assertNull(repo.saved);
    }

    private static HocKy valid() {
        HocKy hk = new HocKy();
        hk.setMaHocky(2);
        hk.setTenHocky("Fall 2026");
        hk.setBlock(1);
        hk.setNamHoc("2026");
        hk.setNgayBatdau(LocalDate.of(2026, 9, 1));
        hk.setNgayKetthuc(LocalDate.of(2026, 10, 30));
        return hk;
    }

    private static class FakeHocKyRepository extends HocKyRepository {
        HocKy saved;
        HocKy existing;
        String constraint;

        @Override public boolean add(HocKy value) { saved = value; return true; }
        @Override public boolean update(HocKy value) { saved = value; return true; }
        @Override public HocKy getById(int id) { return existing; }
        @Override public String getDeleteConstraint(int id) { return constraint; }
        @Override public boolean delete(int id) { return true; }
    }
}
