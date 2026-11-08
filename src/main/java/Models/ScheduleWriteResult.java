package Models;

public final class ScheduleWriteResult {
    private final boolean saved;
    private final int id;
    private final ScheduleConflict conflict;

    private ScheduleWriteResult(boolean saved, int id, ScheduleConflict conflict) {
        this.saved = saved;
        this.id = id;
        this.conflict = conflict;
    }

    public static ScheduleWriteResult saved(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Mã lịch đã lưu phải lớn hơn 0.");
        }
        return new ScheduleWriteResult(true, id, null);
    }

    public static ScheduleWriteResult rejected(ScheduleConflict conflict) {
        if (conflict == null) {
            throw new IllegalArgumentException("Kết quả từ chối phải có xung đột.");
        }
        return new ScheduleWriteResult(false, 0, conflict);
    }

    public boolean isSaved() {
        return saved;
    }

    public int getId() {
        return id;
    }

    public ScheduleConflict getConflict() {
        return conflict;
    }
}
