package Models;

import java.time.LocalDate;

public final class TimetableFilter {
    private final LocalDate weekStart;
    private final Integer termId;
    private final Integer sectionId;
    private final Integer teacherId;
    private final Integer roomId;
    private final Integer userId;
    private final String role;

    private TimetableFilter(LocalDate weekStart, Integer termId,
            Integer sectionId, Integer teacherId, Integer roomId,
            Integer userId, String role) {
        this.weekStart = weekStart;
        this.termId = termId;
        this.sectionId = sectionId;
        this.teacherId = teacherId;
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
    }

    public static TimetableFilter admin(LocalDate weekStart, Integer termId,
            Integer sectionId, Integer teacherId, Integer roomId) {
        return new TimetableFilter(TimetableWeek.mondayOf(weekStart),
                positive(termId), positive(sectionId), positive(teacherId),
                positive(roomId), null, "ADMIN");
    }

    public static TimetableFilter user(LocalDate weekStart, int userId, String role) {
        if (userId <= 0 || (!"STUDENT".equalsIgnoreCase(role)
                && !"TEACHER".equalsIgnoreCase(role))) {
            throw new IllegalArgumentException("Phạm vi thời khóa biểu không hợp lệ.");
        }
        return new TimetableFilter(TimetableWeek.mondayOf(weekStart),
                null, null, null, null, userId, role.toUpperCase());
    }

    public TimetableFilter withWeekStart(LocalDate value) {
        return new TimetableFilter(TimetableWeek.mondayOf(value), termId,
                sectionId, teacherId, roomId, userId, role);
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekStart.plusDays(6);
    }

    public Integer getTermId() {
        return termId;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    private static Integer positive(Integer value) {
        return value != null && value > 0 ? value : null;
    }
}
