package com.poly.sof309.controllers;

import Models.MonHoc;
import Repository.ChuyenNganhRepository;
import Repository.DeleteConflictException;
import Repository.MonHocRepository;
import Services.CatalogException;
import Services.MonHocService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================================
 * [Y3] UNIT TEST QUẢN LÝ MÔN HỌC (MONHOCSERVICE / CONTROLLER)
 * Sinh viên: Vòng Kiệt - MSSV: TV00514 - Lớp: SD21307
 * Kế thừa kỹ thuật Tuần 2:
 *  - @ParameterizedTest + @CsvSource kiểm tra các giá trị biên (tín chỉ, mã môn)
 *  - assertThrows(CatalogException.class) kiểm tra ngoại lệ nghiệp vụ
 *  - @Timeout đảm bảo hiệu năng
 * Ánh xạ Test Cases: TC_MH_01, TC_MH_02, TC_MH_03
 * =========================================================================
 */
@DisplayName("[Tuần 2 - Y3] Kiểm thử Nghiệp vụ Quản lý Môn học")
public class MonHocControllerTest {

    private InMemoryMonHocRepository fakeRepo;
    private InMemoryChuyenNganhRepository fakeCnRepo;
    private MonHocService service;

    @BeforeEach
    void setUp() {
        fakeRepo = new InMemoryMonHocRepository();
        fakeCnRepo = new InMemoryChuyenNganhRepository();
        service = new MonHocService(fakeRepo, fakeCnRepo);
    }

    @Test
    @DisplayName("TC_MH_01: Lưu môn học thành công với dữ liệu hợp lệ")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testSaveSubjectSuccess() {
        MonHoc mh = new MonHoc();
        mh.setMaMonhoc(0);
        mh.setMaMon("SOF309");
        mh.setTenMonhoc("Kiểm thử nâng cao cùng AI");
        mh.setSoTinchi(3);
        mh.setMaChuyenNganh(1);

        assertDoesNotThrow(() -> service.save(mh));
        assertNotNull(fakeRepo.getById(mh.getMaMonhoc()));
    }

    @ParameterizedTest(name = "[{index}] maMon=''{0}'', tenMon=''{1}'', tinChi={2} -> Kỳ vọng lỗi: {3}")
    @DisplayName("TC_MH_02: Ném CatalogException khi vi phạm giá trị biên (số tín chỉ <= 0, mã môn rỗng hoặc > 20 ký tự)")
    @CsvSource({
            "'', 'Kiểm thử nâng cao', 3, 'Mã môn không được để trống.'",
            "'SOF309_MA_MON_DAI_HON_20_KY_TU_XXXXX', 'Kiểm thử nâng cao', 3, 'Mã môn không được vượt quá 20 ký tự.'",
            "'SOF309', '', 3, 'Tên môn học không được để trống.'",
            "'SOF309', 'Kiểm thử', 0, 'Số tín chỉ phải lớn hơn 0.'",
            "'SOF309', 'Kiểm thử', -1, 'Số tín chỉ phải lớn hơn 0.'"
    })
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testSaveSubject_BoundaryValidation_ThrowsCatalogException(String maMon, String tenMon, int tinChi, String expectedMsg) {
        MonHoc mh = new MonHoc();
        mh.setMaMonhoc(0);
        mh.setMaMon(maMon);
        mh.setTenMonhoc(tenMon);
        mh.setSoTinchi(tinChi);
        mh.setMaChuyenNganh(1);

        CatalogException exception = assertThrows(CatalogException.class, () -> {
            service.save(mh);
        });

        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisplayName("TC_MH_03: Chặn xóa môn học khi đang có dữ liệu ràng buộc (DeleteConflictException)")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testDeleteSubject_Conflict_ThrowsCatalogException() {
        MonHoc mh = new MonHoc();
        mh.setMaMonhoc(1);
        mh.setMaMon("SOF309");
        mh.setTenMonhoc("Kiểm thử");
        fakeRepo.data.put(1, mh);
        fakeRepo.conflictReason = "đang có lớp học phần sử dụng";

        CatalogException exception = assertThrows(CatalogException.class, () -> {
            service.delete(1);
        });

        assertTrue(exception.getMessage().contains("Không thể xóa môn học"));
    }

    private static class InMemoryMonHocRepository extends MonHocRepository {
        final Map<Integer, MonHoc> data = new HashMap<>();
        String conflictReason = null;
        int idSeq = 1;

        @Override
        public boolean add(MonHoc mon) {
            int id = idSeq++;
            mon.setMaMonhoc(id);
            data.put(id, mon);
            return true;
        }

        @Override
        public boolean update(MonHoc mon) {
            data.put(mon.getMaMonhoc(), mon);
            return true;
        }

        @Override
        public MonHoc getById(int id) {
            return data.get(id);
        }

        @Override
        public boolean existsByMaMon(String value, int excludeId) {
            return data.values().stream()
                    .anyMatch(m -> m.getMaMonhoc() != excludeId && value.equalsIgnoreCase(m.getMaMon()));
        }

        @Override
        public String getDeleteConstraint(int id) {
            return conflictReason;
        }

        @Override
        public boolean delete(int id) throws DeleteConflictException {
            if (conflictReason != null) {
                throw new DeleteConflictException(conflictReason, new SQLException("FK Conflict"));
            }
            data.remove(id);
            return true;
        }
    }

    private static class InMemoryChuyenNganhRepository extends ChuyenNganhRepository {
        @Override
        public Models.ChuyenNganh getById(int id) {
            Models.ChuyenNganh cn = new Models.ChuyenNganh();
            cn.setMaChuyenNganh(id);
            cn.setTenChuyenNganh("Công nghệ thông tin");
            return cn;
        }
    }
}
