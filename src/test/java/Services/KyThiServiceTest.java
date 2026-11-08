package Services;

import Models.KyThi;
import Models.ScheduleConflict;
import Models.ScheduleWriteResult;
import Repository.KyThiRepository;
import Repository.ScheduleCommandRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KyThiServiceTest {
    @Test
    void chanTrungLoaiKyThi() {
        FakeWriter writer = new FakeWriter();
        writer.result = ScheduleWriteResult.rejected(new ScheduleConflict(
                ScheduleConflict.Kind.DUPLICATE_EXAM,
                ScheduleConflict.ExistingType.EXAM, "UDPM-LHP-003"));

        AcademicException error = assertThrows(AcademicException.class,
                () -> new KyThiService(new FakeKyThiRepository(), writer).create(validExam()));

        assertEquals(AcademicException.Code.DUPLICATE_EXAM, error.getCode());
        assertTrue(error.getMessage().contains("đã có kỳ thi"));
    }

    @Test
    void chanGiuaKySauCuoiKy() {
        FakeWriter writer = new FakeWriter();
        writer.result = ScheduleWriteResult.rejected(new ScheduleConflict(
                ScheduleConflict.Kind.EXAM_ORDER,
                ScheduleConflict.ExistingType.EXAM, "UDPM-LHP-003"));

        AcademicException error = assertThrows(AcademicException.class,
                () -> new KyThiService(new FakeKyThiRepository(), writer).create(validExam()));

        assertEquals(AcademicException.Code.EXAM_ORDER, error.getCode());
        assertTrue(error.getMessage().contains("Giữa kỳ phải trước Cuối kỳ"));
    }

    @Test
    void xungDotCheoNoiRoLoaiTaiNguyenVaLop() {
        for (ScheduleConflict.Kind kind : new ScheduleConflict.Kind[]{
                ScheduleConflict.Kind.ROOM,
                ScheduleConflict.Kind.TEACHER,
                ScheduleConflict.Kind.STUDENT}) {
            FakeWriter writer = new FakeWriter();
            writer.result = ScheduleWriteResult.rejected(new ScheduleConflict(
                    kind, ScheduleConflict.ExistingType.CLASS, "WEB-LHP-004"));

            AcademicException error = assertThrows(AcademicException.class,
                    () -> new KyThiService(new FakeKyThiRepository(), writer).create(validExam()));

            assertTrue(error.getMessage().contains("WEB-LHP-004"));
        }
    }

    @Test
    void capNhatGiuMaBanGhiDeRepositoryLoaiChinhNo() throws Exception {
        FakeWriter writer = new FakeWriter();
        writer.result = ScheduleWriteResult.saved(21);
        KyThi exam = validExam();
        exam.setMaKythi(21);

        new KyThiService(new FakeKyThiRepository(), writer).update(exam);

        assertEquals(21, writer.savedExam.getMaKythi());
    }

    @Test
    void chiChapNhanBaLoaiKyThi() {
        FakeWriter writer = new FakeWriter();
        KyThi exam = validExam();
        exam.setLoaiKythi("Khac");

        AcademicException error = assertThrows(AcademicException.class,
                () -> new KyThiService(new FakeKyThiRepository(), writer).create(exam));

        assertEquals(AcademicException.Code.INVALID_INPUT, error.getCode());
    }

    private static KyThi validExam() {
        return new KyThi(0, 2, 3, 4, LocalDate.of(2026, 9, 20), "GiuaKy");
    }

    private static class FakeKyThiRepository extends KyThiRepository {
    }

    private static class FakeWriter extends ScheduleCommandRepository {
        ScheduleWriteResult result;
        KyThi savedExam;

        @Override
        public ScheduleWriteResult saveExam(KyThi value) {
            savedExam = value;
            return result;
        }
    }
}
