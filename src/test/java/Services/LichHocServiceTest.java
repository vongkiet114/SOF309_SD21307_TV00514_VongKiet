package Services;

import Models.LichHoc;
import Models.ScheduleConflict;
import Models.ScheduleWriteResult;
import Repository.LichHocRepository;
import Repository.ScheduleCommandRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LichHocServiceTest {
    @Test
    void capNhatKhongTuXungDot() throws Exception {
        FakeWriter writer = new FakeWriter();
        writer.result = ScheduleWriteResult.saved(10);
        LichHoc value = valid();
        value.setMaLichhoc(10);

        new LichHocService(new FakeReadRepo(), writer).update(value);

        assertNotNull(writer.savedClass);
        assertEquals(10, writer.savedClass.getMaLichhoc());
    }

    @Test
    void lichXinNghiKhongBiChanBoiXungDot() throws Exception {
        FakeWriter writer = new FakeWriter();
        writer.result = ScheduleWriteResult.saved(11);
        LichHoc value = valid();
        value.setTrangThai("XinNghi");

        assertEquals(11, new LichHocService(new FakeReadRepo(), writer).create(value));
        assertEquals("XinNghi", writer.savedClass.getTrangThai());
    }

    @Test
    void thongBaoPhanBietTrungPhongGiangVienSinhVien() {
        ScheduleConflict.Kind[] kinds = {
                ScheduleConflict.Kind.ROOM,
                ScheduleConflict.Kind.TEACHER,
                ScheduleConflict.Kind.STUDENT
        };
        AcademicException.Code[] codes = {
                AcademicException.Code.CONFLICT_ROOM,
                AcademicException.Code.CONFLICT_TEACHER,
                AcademicException.Code.CONFLICT_STUDENT
        };

        for (int index = 0; index < kinds.length; index++) {
            FakeWriter writer = new FakeWriter();
            writer.result = ScheduleWriteResult.rejected(new ScheduleConflict(
                    kinds[index], ScheduleConflict.ExistingType.EXAM, "WEB-LHP-009"));

            AcademicException error = assertThrows(AcademicException.class,
                    () -> new LichHocService(new FakeReadRepo(), writer).create(valid()));

            assertEquals(codes[index], error.getCode());
            assertTrue(error.getMessage().contains("WEB-LHP-009"));
        }
    }

    @Test
    void khongXoaBuoiDaCoDiemDanh() {
        FakeReadRepo repo = new FakeReadRepo();
        repo.hasAttendance = true;

        AcademicException error = assertThrows(AcademicException.class,
                () -> new LichHocService(repo, new FakeWriter()).delete(5));

        assertEquals(AcademicException.Code.DEPENDENCY, error.getCode());
        assertTrue(error.getMessage().contains("chuyển trạng thái"));
        assertEquals(0, repo.deleteCalls);
    }

    @Test
    void chiChapNhanLoaiLichVaTrangThaiTrongDanhMuc() {
        LichHoc invalidType = valid();
        invalidType.setLoaiLich("Khac");
        AcademicException loaiError = assertThrows(AcademicException.class,
                () -> new LichHocService(new FakeReadRepo(), new FakeWriter()).create(invalidType));
        assertEquals(AcademicException.Code.INVALID_INPUT, loaiError.getCode());

        LichHoc invalidStatus = valid();
        invalidStatus.setTrangThai("Khac");
        AcademicException statusError = assertThrows(AcademicException.class,
                () -> new LichHocService(new FakeReadRepo(), new FakeWriter()).create(invalidStatus));
        assertEquals(AcademicException.Code.INVALID_INPUT, statusError.getCode());
    }

    private static LichHoc valid() {
        return new LichHoc(0, 2, 3, 4, LocalDate.of(2026, 9, 10),
                "HocChinh", "BinhThuong");
    }

    private static class FakeReadRepo extends LichHocRepository {
        boolean hasAttendance;
        int deleteCalls;

        @Override
        public boolean hasAttendance(int id) {
            return hasAttendance;
        }

        @Override
        public boolean delete(int id) {
            deleteCalls++;
            return true;
        }
    }

    private static class FakeWriter extends ScheduleCommandRepository {
        ScheduleWriteResult result;
        LichHoc savedClass;

        @Override
        public ScheduleWriteResult saveClass(LichHoc value) {
            savedClass = value;
            return result;
        }
    }
}
