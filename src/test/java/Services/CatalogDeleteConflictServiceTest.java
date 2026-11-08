package Services;

import Models.CaHoc;
import Models.ChuyenNganh;
import Models.HocKy;
import Models.Khoa;
import Models.LopHoc;
import Models.MonHoc;
import Models.PhongHoc;
import Repository.CaHocRepository;
import Repository.ChuyenNganhRepository;
import Repository.DeleteConflictException;
import Repository.HocKyRepository;
import Repository.KhoaRepository;
import Repository.LopHocRepository;
import Repository.MonHocRepository;
import Repository.PhongHocRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogDeleteConflictServiceTest {
    @Test void khoaDoiXungDotXoaThanhThongBaoNghiepVu() {
        KhoaRepository repo = new KhoaRepository() {
            @Override public Khoa getById(int id) { return new Khoa(); }
            @Override public String getDeleteConstraint(int id) { return null; }
            @Override public boolean delete(int id) { throw deleteConflict(); }
        };

        assertDeleteConflictMessage("Không thể xóa khoa vì bản ghi đang được sử dụng.",
                () -> new KhoaService(repo).delete(1));
    }

    @Test void chuyenNganhDoiXungDotXoaThanhThongBaoNghiepVu() {
        ChuyenNganhRepository repo = new ChuyenNganhRepository() {
            @Override public ChuyenNganh getById(int id) { return new ChuyenNganh(); }
            @Override public String getDeleteConstraint(int id) { return null; }
            @Override public boolean delete(int id) { throw deleteConflict(); }
        };

        assertDeleteConflictMessage("Không thể xóa chuyên ngành vì bản ghi đang được sử dụng.",
                () -> new ChuyenNganhService(repo, new KhoaRepository()).delete(2));
    }

    @Test void lopDoiXungDotXoaThanhThongBaoNghiepVu() {
        LopHocRepository repo = new LopHocRepository() {
            @Override public LopHoc getById(int id) { return new LopHoc(); }
            @Override public String getDeleteConstraint(int id) { return null; }
            @Override public boolean delete(int id) { throw deleteConflict(); }
        };

        assertDeleteConflictMessage("Không thể xóa lớp vì bản ghi đang được sử dụng.",
                () -> new LopHocService(repo, new ChuyenNganhRepository()).delete(3));
    }

    @Test void monHocDoiXungDotXoaThanhThongBaoNghiepVu() {
        MonHocRepository repo = new MonHocRepository() {
            @Override public MonHoc getById(int id) { return new MonHoc(); }
            @Override public String getDeleteConstraint(int id) { return null; }
            @Override public boolean delete(int id) { throw deleteConflict(); }
        };

        assertDeleteConflictMessage("Không thể xóa môn học vì bản ghi đang được sử dụng.",
                () -> new MonHocService(repo, new ChuyenNganhRepository()).delete(4));
    }

    @Test void hocKyDoiXungDotXoaThanhThongBaoNghiepVu() {
        HocKyRepository repo = new HocKyRepository() {
            @Override public HocKy getById(int id) { return new HocKy(); }
            @Override public String getDeleteConstraint(int id) { return null; }
            @Override public boolean delete(int id) { throw deleteConflict(); }
        };

        assertDeleteConflictMessage("Không thể xóa học kỳ vì bản ghi đang được sử dụng.",
                () -> new HocKyService(repo).delete(5));
    }

    @Test void phongHocDoiXungDotXoaThanhThongBaoNghiepVu() {
        PhongHocRepository repo = new PhongHocRepository() {
            @Override public PhongHoc getById(int id) { return new PhongHoc(); }
            @Override public String getDeleteConstraint(int id) { return null; }
            @Override public boolean delete(int id) { throw deleteConflict(); }
        };

        assertDeleteConflictMessage("Không thể xóa phòng học vì bản ghi đang được sử dụng.",
                () -> new PhongHocService(repo).delete(6));
    }

    @Test void caHocDoiXungDotXoaThanhThongBaoNghiepVu() {
        CaHocRepository repo = new CaHocRepository() {
            @Override public CaHoc getById(int id) { return new CaHoc(); }
            @Override public String getDeleteConstraint(int id) { return null; }
            @Override public boolean delete(int id) { throw deleteConflict(); }
        };

        assertDeleteConflictMessage("Không thể xóa ca học vì bản ghi đang được sử dụng.",
                () -> new CaHocService(repo).delete(7));
    }

    private static void assertDeleteConflictMessage(String expected, Executable action) {
        CatalogException error = assertThrows(CatalogException.class, action);
        assertEquals(expected, error.getMessage());
    }

    private static DeleteConflictException deleteConflict() {
        return new DeleteConflictException("test", new SQLException("fk conflict", "23000", 547));
    }
}
