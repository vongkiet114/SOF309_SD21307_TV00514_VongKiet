package Services;

import Models.DiemSo;
import Repository.DiemRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NhapDiemServiceTest {
    @Test
    void chanDiemNgoaiMienVaQuaHaiSoThapPhan() {
        for (String value : new String[]{"-0.01", "10.01", "7.123"}) {
            DiemSo cell = cell(1, 2, new BigDecimal(value));

            AcademicException error = assertThrows(AcademicException.class,
                    () -> new NhapDiemService(new FakeRepo()).saveDiem(
                            7, 8, Arrays.asList(cell)));

            assertEquals(AcademicException.Code.INVALID_INPUT, error.getCode());
        }
    }

    @Test
    void chanCapSinhVienThanhPhanLap() {
        DiemSo a = cell(1, 2, new BigDecimal("8"));
        DiemSo b = cell(1, 2, null);

        AcademicException error = assertThrows(AcademicException.class,
                () -> new NhapDiemService(new FakeRepo()).saveDiem(
                        7, 8, Arrays.asList(a, b)));

        assertTrue(error.getMessage().contains("bị lặp"));
    }

    @Test
    void oTrongDuocChuyenThanhLenhXoaTrongCungBatch() throws Exception {
        FakeRepo repo = new FakeRepo();
        DiemSo blank = cell(1, 2, null);

        new NhapDiemService(repo).saveDiem(7, 8, Arrays.asList(blank));

        assertNull(repo.saved.get(0).getGiaTriDiem());
    }

    @Test
    void chanGiangVienGhiVaDocLopNguoiKhac() {
        FakeRepo repo = new FakeRepo();
        repo.result = DiemRepository.BatchResult.SECTION_NOT_OWNED;
        AcademicException writeError = assertThrows(AcademicException.class,
                () -> new NhapDiemService(repo).saveDiem(7, 8,
                        Arrays.asList(cell(1, 2, BigDecimal.ONE))));
        assertEquals(AcademicException.Code.FORBIDDEN, writeError.getCode());

        repo.owned = false;
        AcademicException readError = assertThrows(AcademicException.class,
                () -> new NhapDiemService(repo).getNhapDiemData(7, 8));
        assertEquals(AcademicException.Code.FORBIDDEN, readError.getCode());
    }

    @Test
    void anhXaTatCaKetQuaBatchThanhLoiNghiepVuAnToan() {
        assertBatchError(DiemRepository.BatchResult.UNKNOWN_MEMBERSHIP,
                AcademicException.Code.INVALID_INPUT, "sinh viên");
        assertBatchError(DiemRepository.BatchResult.UNKNOWN_COMPONENT,
                AcademicException.Code.INVALID_INPUT, "thành phần");
        assertBatchError(DiemRepository.BatchResult.RELATION_MISMATCH,
                AcademicException.Code.INVALID_INPUT, "không thuộc");
    }

    private static void assertBatchError(DiemRepository.BatchResult result,
                                         AcademicException.Code expectedCode,
                                         String expectedMessage) {
        FakeRepo repo = new FakeRepo();
        repo.result = result;
        AcademicException error = assertThrows(AcademicException.class,
                () -> new NhapDiemService(repo).saveDiem(7, 8,
                        Arrays.asList(cell(1, 2, BigDecimal.ONE))));
        assertEquals(expectedCode, error.getCode());
        assertTrue(error.getMessage().toLowerCase().contains(expectedMessage));
    }

    private static DiemSo cell(int membership, int component, BigDecimal score) {
        DiemSo value = new DiemSo();
        value.setMaDanhsach(membership);
        value.setMaThanhphan(component);
        value.setGiaTriDiem(score);
        return value;
    }

    private static class FakeRepo extends DiemRepository {
        BatchResult result = BatchResult.SAVED;
        List<DiemSo> saved;
        boolean owned = true;

        @Override
        public BatchResult saveBatch(int teacher, int section, List<DiemSo> values) {
            saved = values;
            return result;
        }

        @Override
        public boolean isSectionOwned(int teacher, int section) {
            return owned;
        }
    }
}
