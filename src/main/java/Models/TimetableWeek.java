package Models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TimetableWeek {
    private static final Comparator<String> TEXT_ORDER =
            Comparator.nullsFirst(String::compareTo);
    private static final Comparator<LocalTime> TIME_ORDER =
            Comparator.nullsLast(Comparator.naturalOrder());

    private final LocalDate weekStart;
    private final List<LocalDate> days;
    private final List<CaHoc> shifts;
    private final Map<LocalDate, Map<Integer, List<TimetableEvent>>> events;
    private final List<TimetableEvent> allEvents;
    private final boolean autoAdvanced;
    private final LocalDate nearestEventWeekStart;

    private TimetableWeek(LocalDate weekStart, List<LocalDate> days,
            List<CaHoc> shifts,
            Map<LocalDate, Map<Integer, List<TimetableEvent>>> events,
            List<TimetableEvent> allEvents, boolean autoAdvanced,
            LocalDate nearestEventWeekStart) {
        this.weekStart = weekStart;
        this.days = days;
        this.shifts = shifts;
        this.events = events;
        this.allEvents = allEvents;
        this.autoAdvanced = autoAdvanced;
        this.nearestEventWeekStart = nearestEventWeekStart;
    }

    public static TimetableWeek of(LocalDate requestedWeekStart,
            List<CaHoc> sourceShifts, List<TimetableEvent> sourceEvents,
            boolean autoAdvanced, LocalDate nearestEventDate) {
        LocalDate weekStart = mondayOf(requestedWeekStart);
        List<LocalDate> days = new ArrayList<>();
        for (int offset = 0; offset < 7; offset++) {
            days.add(weekStart.plusDays(offset));
        }

        List<CaHoc> shifts = copyShifts(sourceShifts);
        Map<LocalDate, Map<Integer, List<TimetableEvent>>> grouped =
                groupEvents(weekStart, sourceEvents);
        List<TimetableEvent> allEvents = allEvents(grouped);

        return new TimetableWeek(weekStart,
                Collections.unmodifiableList(days),
                Collections.unmodifiableList(shifts), grouped, allEvents,
                autoAdvanced,
                nearestEventDate == null ? null : mondayOf(nearestEventDate));
    }

    public static LocalDate mondayOf(LocalDate date) {
        return Objects.requireNonNull(date, "Ngày bắt đầu tuần không được để trống.")
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekStart.plusDays(6);
    }

    public LocalDate getPreviousWeekStart() {
        return weekStart.minusWeeks(1);
    }

    public LocalDate getNextWeekStart() {
        return weekStart.plusWeeks(1);
    }

    public List<LocalDate> getDays() {
        return days;
    }

    public List<CaHoc> getShifts() {
        List<CaHoc> copies = new ArrayList<>();
        for (CaHoc shift : shifts) {
            copies.add(copyShift(shift));
        }
        return Collections.unmodifiableList(copies);
    }

    public List<TimetableEvent> getEvents(LocalDate day, int shiftId) {
        Map<Integer, List<TimetableEvent>> byShift = events.get(day);
        if (byShift == null) return Collections.emptyList();
        List<TimetableEvent> values = byShift.get(shiftId);
        return values == null ? Collections.emptyList() : values;
    }

    public List<TimetableEvent> getAllEvents() {
        return allEvents;
    }

    public int getEventCount() {
        return allEvents.size();
    }

    public boolean isAutoAdvanced() {
        return autoAdvanced;
    }

    public LocalDate getNearestEventWeekStart() {
        return nearestEventWeekStart;
    }

    private static List<CaHoc> copyShifts(List<CaHoc> source) {
        List<CaHoc> copies = new ArrayList<>();
        if (source != null) {
            for (CaHoc shift : source) {
                if (shift != null) copies.add(copyShift(shift));
            }
        }
        copies.sort(Comparator.comparing(CaHoc::getGioBatdau, TIME_ORDER)
                .thenComparingInt(CaHoc::getMaCa));
        return copies;
    }

    private static CaHoc copyShift(CaHoc source) {
        CaHoc copy = new CaHoc();
        copy.setMaCa(source.getMaCa());
        copy.setGioBatdau(source.getGioBatdau());
        copy.setGioKetthuc(source.getGioKetthuc());
        return copy;
    }

    private static Map<LocalDate, Map<Integer, List<TimetableEvent>>> groupEvents(
            LocalDate weekStart, List<TimetableEvent> source) {
        Map<LocalDate, Map<Integer, List<TimetableEvent>>> grouped =
                new HashMap<>();
        LocalDate weekEnd = weekStart.plusDays(6);
        if (source != null) {
            for (TimetableEvent event : source) {
                if (event == null || event.getDate() == null
                        || event.getDate().isBefore(weekStart)
                        || event.getDate().isAfter(weekEnd)) {
                    continue;
                }
                grouped.computeIfAbsent(event.getDate(), ignored -> new HashMap<>())
                        .computeIfAbsent(event.getShiftId(), ignored -> new ArrayList<>())
                        .add(event);
            }
        }

        Comparator<TimetableEvent> cellOrder = Comparator
                .comparing(TimetableEvent::getSectionCode, TEXT_ORDER)
                .thenComparingInt(TimetableEvent::getId);
        Map<LocalDate, Map<Integer, List<TimetableEvent>>> immutable =
                new HashMap<>();
        for (Map.Entry<LocalDate, Map<Integer, List<TimetableEvent>>> day
                : grouped.entrySet()) {
            Map<Integer, List<TimetableEvent>> byShift = new HashMap<>();
            for (Map.Entry<Integer, List<TimetableEvent>> shift
                    : day.getValue().entrySet()) {
                shift.getValue().sort(cellOrder);
                byShift.put(shift.getKey(),
                        Collections.unmodifiableList(new ArrayList<>(shift.getValue())));
            }
            immutable.put(day.getKey(), Collections.unmodifiableMap(byShift));
        }
        return Collections.unmodifiableMap(immutable);
    }

    private static List<TimetableEvent> allEvents(
            Map<LocalDate, Map<Integer, List<TimetableEvent>>> grouped) {
        List<TimetableEvent> result = new ArrayList<>();
        for (Map<Integer, List<TimetableEvent>> byShift : grouped.values()) {
            for (List<TimetableEvent> events : byShift.values()) {
                result.addAll(events);
            }
        }
        result.sort(Comparator.comparing(TimetableEvent::getDate)
                .thenComparing(TimetableEvent::getStartTime, TIME_ORDER)
                .thenComparing(TimetableEvent::getSectionCode, TEXT_ORDER)
                .thenComparingInt(TimetableEvent::getId));
        return Collections.unmodifiableList(result);
    }
}
