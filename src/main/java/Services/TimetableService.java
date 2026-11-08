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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public class TimetableService {
    private final LichHocRepository classRepository;
    private final KyThiRepository examRepository;
    private final CaHocRepository shiftRepository;

    public TimetableService() {
        this(new LichHocRepository(), new KyThiRepository(), new CaHocRepository());
    }

    TimetableService(LichHocRepository classRepository,
            KyThiRepository examRepository, CaHocRepository shiftRepository) {
        this.classRepository = classRepository;
        this.examRepository = examRepository;
        this.shiftRepository = shiftRepository;
    }

    public TimetableWeek loadClasses(TimetableFilter filter, boolean autoAdvance) {
        requireFilter(filter);
        List<CaHoc> shifts = requireShifts();
        List<LichHoc> rows = requireRows(
                classRepository.findForTimetable(filter), "lịch học");
        TimetableFilter effective = filter;
        boolean moved = false;
        LocalDate nearest = null;

        if (rows.isEmpty()) {
            LocalDate next = classRepository.findNextDate(filter,
                    filter.getWeekStart());
            if (next != null && autoAdvance) {
                TimetableFilter candidate = filter.withWeekStart(next);
                List<LichHoc> candidateRows = requireRows(
                        classRepository.findForTimetable(candidate),
                        "lịch học");
                if (!candidateRows.isEmpty()) {
                    effective = candidate;
                    rows = candidateRows;
                    moved = !effective.getWeekStart().equals(filter.getWeekStart());
                } else {
                    nearest = classRepository.findNextDate(filter,
                            candidate.getWeekEnd().plusDays(1));
                }
            } else {
                nearest = next;
            }
        }

        EventMapping mapped = mapEvents(rows, shifts, LichHoc::getMaCa,
                TimetableEvent::fromClass);
        return TimetableWeek.of(effective.getWeekStart(), mapped.shifts,
                mapped.events,
                moved, nearest);
    }

    public TimetableWeek loadExams(TimetableFilter filter, boolean autoAdvance) {
        requireFilter(filter);
        List<CaHoc> shifts = requireShifts();
        List<KyThi> rows = requireRows(
                examRepository.findForTimetable(filter), "kỳ thi");
        TimetableFilter effective = filter;
        boolean moved = false;
        LocalDate nearest = null;

        if (rows.isEmpty()) {
            LocalDate next = examRepository.findNextDate(filter,
                    filter.getWeekStart());
            if (next != null && autoAdvance) {
                TimetableFilter candidate = filter.withWeekStart(next);
                List<KyThi> candidateRows = requireRows(
                        examRepository.findForTimetable(candidate),
                        "kỳ thi");
                if (!candidateRows.isEmpty()) {
                    effective = candidate;
                    rows = candidateRows;
                    moved = !effective.getWeekStart().equals(filter.getWeekStart());
                } else {
                    nearest = examRepository.findNextDate(filter,
                            candidate.getWeekEnd().plusDays(1));
                }
            } else {
                nearest = next;
            }
        }

        EventMapping mapped = mapEvents(rows, shifts, KyThi::getMaCa,
                TimetableEvent::fromExam);
        return TimetableWeek.of(effective.getWeekStart(), mapped.shifts,
                mapped.events,
                moved, nearest);
    }

    private void requireFilter(TimetableFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Bộ lọc thời khóa biểu không được để trống.");
        }
    }

    private List<CaHoc> requireShifts() {
        List<CaHoc> shifts = shiftRepository.getAll();
        if (shifts == null) {
            throw new IllegalStateException("Danh sách ca học không hợp lệ.");
        }
        return shifts;
    }

    private <T> List<T> requireRows(List<T> rows, String rowType) {
        if (rows == null) {
            throw new IllegalStateException("Danh sách " + rowType
                    + " không hợp lệ.");
        }
        for (T row : rows) {
            if (row == null) {
                throw new IllegalStateException("Dữ liệu " + rowType
                        + " không hợp lệ.");
            }
        }
        return rows;
    }

    private <T> EventMapping mapEvents(List<T> rows, List<CaHoc> shifts,
            ToIntFunction<T> shiftId, BiFunction<T, CaHoc, TimetableEvent> mapper) {
        List<CaHoc> effectiveShifts = shifts;
        if (!hasAllShifts(rows, effectiveShifts, shiftId)) {
            effectiveShifts = requireShifts();
        }

        List<TimetableEvent> events = new ArrayList<>();
        for (T row : rows) {
            events.add(mapper.apply(row,
                    requireShift(effectiveShifts, shiftId.applyAsInt(row))));
        }
        return new EventMapping(effectiveShifts, events);
    }

    private <T> boolean hasAllShifts(List<T> rows, List<CaHoc> shifts,
            ToIntFunction<T> shiftId) {
        for (T row : rows) {
            if (findShift(shifts, shiftId.applyAsInt(row)) == null) {
                return false;
            }
        }
        return true;
    }

    private CaHoc requireShift(List<CaHoc> shifts, int shiftId) {
        CaHoc shift = findShift(shifts, shiftId);
        if (shift != null) {
            return shift;
        }
        throw new IllegalStateException("Không tìm thấy ca học cho lịch có mã ca "
                + shiftId + ".");
    }

    private CaHoc findShift(List<CaHoc> shifts, int shiftId) {
        for (CaHoc shift : shifts) {
            if (shift != null && shift.getMaCa() == shiftId) {
                return shift;
            }
        }
        return null;
    }

    private static class EventMapping {
        private final List<CaHoc> shifts;
        private final List<TimetableEvent> events;

        private EventMapping(List<CaHoc> shifts, List<TimetableEvent> events) {
            this.shifts = shifts;
            this.events = events;
        }
    }
}
