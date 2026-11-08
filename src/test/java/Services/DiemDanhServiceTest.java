package Services;

import Models.DiemDanh;
import Models.SinhVien;
import Repository.DiemDanhRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiemDanhServiceTest {
    @Test
    void chiChapNhanCoMatHoacVangVaChuanHoaKhoangTrang() throws Exception {
        for (String state : new String[]{"Tre", "VangCoPhep", "Chưa điểm danh", ""}) {
            assertThrows(AcademicException.class, () ->
                    new DiemDanhService(new FakeRepo()).saveDiemDanh(
                            7, 8, 9, Collections.singletonList(row(1, 9, state))));
        }

        FakeRepo repo = new FakeRepo();
        new DiemDanhService(repo).saveDiemDanh(
                7, 8, 9, Arrays.asList(row(1, 9, " CoMat "), row(2, 9, "Vang")));

        assertEquals("CoMat", repo.saved.get(0).getTrangThai());
        assertEquals("Vang", repo.saved.get(1).getTrangThai());
    }

    @Test
    void chanDongLapDanhSachRongVaQuanHeBuoiSai() {
        DiemDanh a = row(1, 9, "CoMat");
        DiemDanh b = row(1, 9, "Vang");
        AcademicException duplicate = assertThrows(AcademicException.class, () ->
                new DiemDanhService(new FakeRepo()).saveDiemDanh(
                        7, 8, 9, Arrays.asList(a, b)));
        assertTrue(duplicate.getMessage().contains("bị lặp"));

        assertThrows(AcademicException.class, () ->
                new DiemDanhService(new FakeRepo()).saveDiemDanh(
                        7, 8, 9, Collections.<DiemDanh>emptyList()));
        assertThrows(AcademicException.class, () ->
                new DiemDanhService(new FakeRepo()).saveDiemDanh(
                        7, 8, 9, Collections.singletonList(row(1, 10, "CoMat"))));
    }

    @Test
    void chanGiangVienLopKhacVaBuoiKhongBinhThuong() {
        FakeRepo repo = new FakeRepo();
        repo.result = DiemDanhRepository.BatchResult.SESSION_NOT_OWNED;
        assertEquals(AcademicException.Code.FORBIDDEN,
                assertThrows(AcademicException.class, () ->
                        new DiemDanhService(repo).saveDiemDanh(
                                7, 8, 9, Collections.singletonList(row(1, 9, "CoMat"))))
                        .getCode());

        repo.result = DiemDanhRepository.BatchResult.SESSION_NOT_NORMAL;
        AcademicException cancelled = assertThrows(AcademicException.class, () ->
                new DiemDanhService(repo).saveDiemDanh(
                        7, 8, 9, Collections.singletonList(row(1, 9, "CoMat"))));
        assertTrue(cancelled.getMessage().contains("bình thường"));
    }

    @Test
    void chanBuoiQuaKhuVaTruyenTodayXuongRepository() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        FakeRepo repo = new FakeRepo();
        repo.result = DiemDanhRepository.BatchResult.SESSION_IN_PAST;

        AcademicException past = assertThrows(AcademicException.class, () ->
                new DiemDanhService(repo).saveDiemDanh(
                        7, 8, 9, Collections.singletonList(row(1, 9, "CoMat")), today));

        assertEquals(AcademicException.Code.INVALID_INPUT, past.getCode());
        assertTrue(past.getMessage().contains("đã qua"));
        assertEquals(today, repo.savedToday);
    }

    @Test
    void adminCungChanBuoiQuaKhuVaTruyenTodayXuongRepository() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        FakeRepo repo = new FakeRepo();
        repo.result = DiemDanhRepository.BatchResult.SESSION_IN_PAST;

        AcademicException past = assertThrows(AcademicException.class, () ->
                new DiemDanhService(repo).saveDiemDanhAdmin(
                        8, 9, Collections.singletonList(row(1, 9, "CoMat")), today));

        assertEquals(AcademicException.Code.INVALID_INPUT, past.getCode());
        assertTrue(past.getMessage().contains("đã qua"));
        assertEquals(today, repo.adminSavedToday);
    }

    @Test
    void phanBietThanhVienKhongTonTaiVaKhongThuocLop() {
        FakeRepo repo = new FakeRepo();
        repo.result = DiemDanhRepository.BatchResult.UNKNOWN_MEMBERSHIP;
        assertTrue(assertThrows(AcademicException.class, () ->
                new DiemDanhService(repo).saveDiemDanh(
                        7, 8, 9, Collections.singletonList(row(1, 9, "CoMat"))))
                .getMessage().contains("không tồn tại"));

        repo.result = DiemDanhRepository.BatchResult.RELATION_MISMATCH;
        assertTrue(assertThrows(AcademicException.class, () ->
                new DiemDanhService(repo).saveDiemDanh(
                        7, 8, 9, Collections.singletonList(row(1, 9, "CoMat"))))
                .getMessage().contains("không thuộc"));
    }

    @Test
    void docBangDiemDanhLuonKemGiangVienLopVaBuoi() throws Exception {
        FakeRepo repo = new FakeRepo();
        repo.sheet = Collections.singletonList(new SinhVien());

        List<SinhVien> result = new DiemDanhService(repo).getBangDiemDanh(7, 8, 9);

        assertEquals(1, result.size());
        assertEquals(7, repo.readTeacher);
        assertEquals(8, repo.readSection);
        assertEquals(9, repo.readSchedule);
    }

    @Test
    void tuChoiIdKhongHopLeTruocKhiGoiRepository() {
        FakeRepo repo = new FakeRepo();

        assertThrows(AcademicException.class,
                () -> new DiemDanhService(repo).getBangDiemDanh(0, 8, 9));
        assertThrows(AcademicException.class, () ->
                new DiemDanhService(repo).saveDiemDanh(
                        7, -1, 9, Collections.singletonList(row(1, 9, "CoMat"))));
        assertEquals(0, repo.saveCalls);
    }

    @Test
    void adminDungCungQuyTacHaiTrangThaiVaRepositoryRieng() throws Exception {
        FakeRepo repo = new FakeRepo();
        DiemDanhService service = new DiemDanhService(repo);

        service.saveDiemDanhAdmin(
                8, 9, Arrays.asList(row(1, 9, " CoMat "), row(2, 9, "Vang")));

        assertEquals(1, repo.adminSaveCalls);
        assertEquals(8, repo.adminSection);
        assertEquals(9, repo.adminSchedule);
        assertEquals("CoMat", repo.adminSaved.get(0).getTrangThai());
        assertThrows(AcademicException.class, () -> service.saveDiemDanhAdmin(
                8, 9, Collections.singletonList(row(1, 9, "Tre"))));
    }

    @Test
    void adminDocBangDiemDanhTheoDungLopVaBuoi() throws Exception {
        FakeRepo repo = new FakeRepo();
        repo.adminSheet = Collections.singletonList(new SinhVien());

        List<SinhVien> result = new DiemDanhService(repo).getBangDiemDanhAdmin(8, 9);

        assertEquals(1, result.size());
        assertEquals(8, repo.adminReadSection);
        assertEquals(9, repo.adminReadSchedule);
    }

    private static DiemDanh row(int membership, int schedule, String state) {
        DiemDanh value = new DiemDanh();
        value.setMaDanhsach(membership);
        value.setMaLichhoc(schedule);
        value.setTrangThai(state);
        return value;
    }

    private static class FakeRepo extends DiemDanhRepository {
        private BatchResult result = BatchResult.SAVED;
        private List<DiemDanh> saved;
        private List<SinhVien> sheet = Collections.emptyList();
        private int readTeacher;
        private int readSection;
        private int readSchedule;
        private int saveCalls;
        private int adminSaveCalls;
        private int adminSection;
        private int adminSchedule;
        private int adminReadSection;
        private int adminReadSchedule;
        private List<DiemDanh> adminSaved;
        private List<SinhVien> adminSheet = Collections.emptyList();
        private LocalDate savedToday;
        private LocalDate adminSavedToday;

        @Override
        public BatchResult saveBatch(int teacher, int section, int schedule,
                                     List<DiemDanh> rows) {
            saveCalls++;
            saved = rows;
            return result;
        }

        @Override
        public BatchResult saveBatch(int teacher, int section, int schedule,
                                     List<DiemDanh> rows, LocalDate today) {
            saveCalls++;
            saved = rows;
            savedToday = today;
            return result;
        }

        @Override
        public List<SinhVien> findDiemDanhForBuoiHoc(int teacher, int section, int schedule) {
            readTeacher = teacher;
            readSection = section;
            readSchedule = schedule;
            return sheet;
        }

        @Override
        public BatchResult saveBatchAdmin(int section, int schedule, List<DiemDanh> rows) {
            adminSaveCalls++;
            adminSection = section;
            adminSchedule = schedule;
            adminSaved = rows;
            return result;
        }

        @Override
        public BatchResult saveBatchAdmin(int section, int schedule,
                                          List<DiemDanh> rows, LocalDate today) {
            adminSaveCalls++;
            adminSection = section;
            adminSchedule = schedule;
            adminSaved = rows;
            adminSavedToday = today;
            return result;
        }

        @Override
        public List<SinhVien> findDiemDanhForBuoiHocAdmin(int section, int schedule) {
            adminReadSection = section;
            adminReadSchedule = schedule;
            return adminSheet;
        }
    }
}
