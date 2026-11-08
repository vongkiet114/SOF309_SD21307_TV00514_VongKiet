package Services;

import Models.CaHoc;
import Models.KyThi;
import Models.LichHoc;
import Models.TimetableEvent;
import Models.TimetableFilter;
import Models.TimetableWeek;
import Repository.CaHocRepository;
import Repository.KyThiRepository;
import Repository.LichHocRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimetableServiceTest {
    @Test
    void lanMoDauTuChuyenDenTuanCoLichGanNhat() {
        LocalDate eventDate = LocalDate.of(2026, 9, 9);
        TimetableService service = new TimetableService(
                new FakeClassRepository(eventDate), new FakeExamRepository(null),
                new FakeShiftRepository());
        TimetableFilter filter = TimetableFilter.admin(
                LocalDate.of(2026, 7, 20), null, null, null, null);

        TimetableWeek week = service.loadClasses(filter, true);

        assertEquals(LocalDate.of(2026, 9, 7), week.getWeekStart());
        assertTrue(week.isAutoAdvanced());
        assertEquals(1, week.getEventCount());
    }

    @Test
    void tuanNguoiDungDaChonKhongTuYChuyenVaGiuLichGanNhat() {
        TimetableService service = new TimetableService(
                new FakeClassRepository(LocalDate.of(2026, 9, 9)),
                new FakeExamRepository(null), new FakeShiftRepository());
        TimetableFilter filter = TimetableFilter.admin(
                LocalDate.of(2026, 7, 20), null, null, null, null);

        TimetableWeek week = service.loadClasses(filter, false);

        assertEquals(LocalDate.of(2026, 7, 20), week.getWeekStart());
        assertFalse(week.isAutoAdvanced());
        assertEquals(0, week.getEventCount());
        assertEquals(LocalDate.of(2026, 9, 7),
                week.getNearestEventWeekStart());
    }

    @Test
    void tuanDaCoLichKhongTuChuyenDuocPhep() {
        LocalDate eventDate = LocalDate.of(2026, 7, 22);
        TimetableService service = new TimetableService(
                new FakeClassRepository(eventDate), new FakeExamRepository(null),
                new FakeShiftRepository());
        TimetableFilter filter = TimetableFilter.admin(
                LocalDate.of(2026, 7, 20), null, null, null, null);

        TimetableWeek week = service.loadClasses(filter, true);

        assertEquals(LocalDate.of(2026, 7, 20), week.getWeekStart());
        assertFalse(week.isAutoAdvanced());
        assertEquals(1, week.getEventCount());
        assertEquals(null, week.getNearestEventWeekStart());
    }

    @Test
    void lichHocDuocAnhXaThanhEventClass() {
        LocalDate classDate = LocalDate.of(2026, 10, 19);
        TimetableService service = new TimetableService(
                new FakeClassRepository(classDate), new FakeExamRepository(null),
                new FakeShiftRepository());

        TimetableWeek week = service.loadClasses(
                TimetableFilter.admin(classDate, null, null, null, null), false);

        TimetableEvent event = week.getEvents(classDate, 1).get(0);
        assertEquals(TimetableEvent.Kind.CLASS, event.getKind());
        assertEquals("Học chính", event.getCategoryLabel());
    }

    @Test
    void lichThiDuocAnhXaThanhEventExam() {
        LocalDate examDate = LocalDate.of(2026, 10, 19);
        TimetableService service = new TimetableService(
                new FakeClassRepository(null), new FakeExamRepository(examDate),
                new FakeShiftRepository());

        TimetableWeek week = service.loadExams(
                TimetableFilter.admin(examDate, null, null, null, null), false);

        TimetableEvent event = week.getEvents(examDate, 1).get(0);
        assertEquals(TimetableEvent.Kind.EXAM, event.getKind());
        assertEquals("Bảo vệ", event.getCategoryLabel());
    }

    @Test
    void lichThamChieuCaKhongTonTaiBaoLoiRoRang() {
        EmptyShiftRepository shifts = new EmptyShiftRepository();
        TimetableService service = new TimetableService(
                new FakeClassRepository(LocalDate.of(2026, 7, 22)),
                new FakeExamRepository(null), shifts);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.loadClasses(TimetableFilter.admin(
                        LocalDate.of(2026, 7, 20), null, null, null, null), false));

        assertTrue(error.getMessage().toLowerCase().contains("ca học"));
        assertEquals(2, shifts.getCallCount());
    }

    @Test
    void danhSachLichHocNullBaoLoiRoRang() {
        TimetableService service = new TimetableService(
                new NullClassRowsRepository(), new FakeExamRepository(null),
                new FakeShiftRepository());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.loadClasses(filter(), false));

        assertTrue(error.getMessage().toLowerCase().contains("lịch học"));
    }

    @Test
    void phanTuLichHocNullBaoLoiRoRang() {
        TimetableService service = new TimetableService(
                new NullClassRowRepository(), new FakeExamRepository(null),
                new FakeShiftRepository());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.loadClasses(filter(), false));

        assertTrue(error.getMessage().toLowerCase().contains("lịch học"));
    }

    @Test
    void danhSachKyThiNullBaoLoiRoRang() {
        TimetableService service = new TimetableService(
                new FakeClassRepository(null), new NullExamRowsRepository(),
                new FakeShiftRepository());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.loadExams(filter(), false));

        assertTrue(error.getMessage().toLowerCase().contains("kỳ thi"));
    }

    @Test
    void phanTuKyThiNullBaoLoiRoRang() {
        TimetableService service = new TimetableService(
                new FakeClassRepository(null), new NullExamRowRepository(),
                new FakeShiftRepository());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.loadExams(filter(), false));

        assertTrue(error.getMessage().toLowerCase().contains("kỳ thi"));
    }

    @Test
    void reloadLichHocTuanUngVienRongKhongBaoDaTuChuyen() {
        TimetableService service = new TimetableService(
                new StaleClassRepository(), new FakeExamRepository(null),
                new FakeShiftRepository());

        TimetableWeek week = service.loadClasses(filter(), true);

        assertEquals(LocalDate.of(2026, 7, 20), week.getWeekStart());
        assertFalse(week.isAutoAdvanced());
        assertEquals(0, week.getEventCount());
        assertEquals(LocalDate.of(2026, 10, 19),
                week.getNearestEventWeekStart());
    }

    @Test
    void reloadKyThiTuanUngVienRongKhongBaoDaTuChuyen() {
        TimetableService service = new TimetableService(
                new FakeClassRepository(null), new StaleExamRepository(),
                new FakeShiftRepository());

        TimetableWeek week = service.loadExams(filter(), true);

        assertEquals(LocalDate.of(2026, 7, 20), week.getWeekStart());
        assertFalse(week.isAutoAdvanced());
        assertEquals(0, week.getEventCount());
        assertEquals(LocalDate.of(2026, 10, 19),
                week.getNearestEventWeekStart());
    }

    @Test
    void ungVienCungTuanCoLichMoiKhongBaoDaTuChuyen() {
        TimetableService service = new TimetableService(
                new RaceInsertedClassRepository(), new FakeExamRepository(null),
                new FakeShiftRepository());

        TimetableWeek week = service.loadClasses(filter(), true);

        assertEquals(LocalDate.of(2026, 7, 20), week.getWeekStart());
        assertFalse(week.isAutoAdvanced());
        assertEquals(1, week.getEventCount());
    }

    @Test
    void lichHocMoiSauSnapshotCaDuocTaiLaiMotLan() {
        LateShiftRepository shifts = new LateShiftRepository();
        TimetableService service = new TimetableService(
                new FakeClassRepository(LocalDate.of(2026, 7, 22)),
                new FakeExamRepository(null), shifts);

        TimetableWeek week = service.loadClasses(filter(), false);

        assertEquals(1, week.getEventCount());
        assertEquals(2, shifts.getCallCount());
    }

    @Test
    void kyThiMoiSauSnapshotCaDuocTaiLaiMotLan() {
        LateShiftRepository shifts = new LateShiftRepository();
        TimetableService service = new TimetableService(
                new FakeClassRepository(null),
                new FakeExamRepository(LocalDate.of(2026, 7, 22)), shifts);

        TimetableWeek week = service.loadExams(filter(), false);

        assertEquals(1, week.getEventCount());
        assertEquals(2, shifts.getCallCount());
    }

    private static TimetableFilter filter() {
        return TimetableFilter.admin(LocalDate.of(2026, 7, 20),
                null, null, null, null);
    }

    private static boolean includes(TimetableFilter filter, LocalDate eventDate) {
        return eventDate != null && !eventDate.isBefore(filter.getWeekStart())
                && !eventDate.isAfter(filter.getWeekEnd());
    }

    private static final class FakeClassRepository extends LichHocRepository {
        private final LocalDate eventDate;

        private FakeClassRepository(LocalDate eventDate) {
            this.eventDate = eventDate;
        }

        @Override
        public List<LichHoc> findForTimetable(TimetableFilter filter) {
            return includes(filter, eventDate)
                    ? Collections.singletonList(classRow(eventDate))
                    : Collections.emptyList();
        }

        @Override
        public LocalDate findNextDate(TimetableFilter filter, LocalDate onOrAfter) {
            return eventDate != null && !eventDate.isBefore(onOrAfter)
                    ? eventDate : null;
        }
    }

    private static final class FakeExamRepository extends KyThiRepository {
        private final LocalDate eventDate;

        private FakeExamRepository(LocalDate eventDate) {
            this.eventDate = eventDate;
        }

        @Override
        public List<KyThi> findForTimetable(TimetableFilter filter) {
            return includes(filter, eventDate)
                    ? Collections.singletonList(examRow(eventDate))
                    : Collections.emptyList();
        }

        @Override
        public LocalDate findNextDate(TimetableFilter filter, LocalDate onOrAfter) {
            return eventDate != null && !eventDate.isBefore(onOrAfter)
                    ? eventDate : null;
        }
    }

    private static final class NullClassRowsRepository extends LichHocRepository {
        @Override
        public List<LichHoc> findForTimetable(TimetableFilter filter) {
            return null;
        }
    }

    private static final class NullClassRowRepository extends LichHocRepository {
        @Override
        public List<LichHoc> findForTimetable(TimetableFilter filter) {
            return Collections.singletonList(null);
        }
    }

    private static final class NullExamRowsRepository extends KyThiRepository {
        @Override
        public List<KyThi> findForTimetable(TimetableFilter filter) {
            return null;
        }
    }

    private static final class NullExamRowRepository extends KyThiRepository {
        @Override
        public List<KyThi> findForTimetable(TimetableFilter filter) {
            return Collections.singletonList(null);
        }
    }

    private static final class StaleClassRepository extends LichHocRepository {
        @Override
        public List<LichHoc> findForTimetable(TimetableFilter filter) {
            return Collections.emptyList();
        }

        @Override
        public LocalDate findNextDate(TimetableFilter filter, LocalDate onOrAfter) {
            return onOrAfter.isBefore(LocalDate.of(2026, 9, 14))
                    ? LocalDate.of(2026, 9, 9) : LocalDate.of(2026, 10, 21);
        }
    }

    private static final class StaleExamRepository extends KyThiRepository {
        @Override
        public List<KyThi> findForTimetable(TimetableFilter filter) {
            return Collections.emptyList();
        }

        @Override
        public LocalDate findNextDate(TimetableFilter filter, LocalDate onOrAfter) {
            return onOrAfter.isBefore(LocalDate.of(2026, 9, 14))
                    ? LocalDate.of(2026, 9, 9) : LocalDate.of(2026, 10, 21);
        }
    }

    private static final class RaceInsertedClassRepository
            extends LichHocRepository {
        private int queryCount;

        @Override
        public List<LichHoc> findForTimetable(TimetableFilter filter) {
            queryCount++;
            return queryCount == 1 ? Collections.emptyList()
                    : Collections.singletonList(classRow(LocalDate.of(2026, 7, 22)));
        }

        @Override
        public LocalDate findNextDate(TimetableFilter filter, LocalDate onOrAfter) {
            return LocalDate.of(2026, 7, 22);
        }
    }

    private static class FakeShiftRepository extends CaHocRepository {
        @Override
        public List<CaHoc> getAll() {
            CaHoc shift = new CaHoc();
            shift.setMaCa(1);
            shift.setGioBatdau(LocalTime.of(7, 15));
            shift.setGioKetthuc(LocalTime.of(9, 15));
            return Collections.singletonList(shift);
        }
    }

    private static final class EmptyShiftRepository extends FakeShiftRepository {
        private int callCount;

        @Override
        public List<CaHoc> getAll() {
            callCount++;
            return Collections.emptyList();
        }

        private int getCallCount() {
            return callCount;
        }
    }

    private static final class LateShiftRepository extends CaHocRepository {
        private int callCount;

        @Override
        public List<CaHoc> getAll() {
            callCount++;
            return callCount == 1 ? Collections.emptyList()
                    : new FakeShiftRepository().getAll();
        }

        private int getCallCount() {
            return callCount;
        }
    }

    private static LichHoc classRow(LocalDate date) {
        LichHoc value = new LichHoc(11, 5, 3, 1, date,
                "HocChinh", "BinhThuong");
        value.setMaLhp("UDPM-LHP-003");
        value.setTenMonHoc("Môn kiểm thử");
        value.setTenPhong("T101");
        value.setTenCa("07:15 - 09:15");
        value.setTenGiangVien("Nguyễn Giảng Viên");
        return value;
    }

    private static KyThi examRow(LocalDate date) {
        KyThi value = new KyThi(21, 5, 3, 1, date, "BaoVe");
        value.setMaLhp("TKDH-LHP-006");
        value.setTenMonHoc("Thiết kế đồ họa");
        value.setTenPhong("T103");
        value.setTenCa("07:15 - 09:15");
        value.setTenGiangVien("Nguyễn Giảng Viên");
        return value;
    }
}
