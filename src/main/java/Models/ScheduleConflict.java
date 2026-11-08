package Models;

public final class ScheduleConflict {
    public enum Kind {
        ROOM,
        TEACHER,
        STUDENT,
        DUPLICATE_EXAM,
        EXAM_ORDER
    }

    public enum ExistingType {
        CLASS,
        EXAM
    }

    private final Kind kind;
    private final ExistingType existingType;
    private final String existingSectionCode;

    public ScheduleConflict(Kind kind, ExistingType existingType, String existingSectionCode) {
        if (kind == null) {
            throw new IllegalArgumentException("Loại xung đột không được để trống.");
        }
        this.kind = kind;
        this.existingType = existingType;
        this.existingSectionCode = existingSectionCode;
    }

    public Kind getKind() {
        return kind;
    }

    public ExistingType getExistingType() {
        return existingType;
    }

    public String getExistingSectionCode() {
        return existingSectionCode;
    }
}
