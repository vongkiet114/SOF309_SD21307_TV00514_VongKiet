package Services;

public class AcademicException extends Exception {
    public enum Code {
        INVALID_INPUT,
        NOT_FOUND,
        FORBIDDEN,
        DEPENDENCY,
        CONFLICT,
        OPERATION_FAILED,
        CONFLICT_ROOM,
        CONFLICT_TEACHER,
        CONFLICT_STUDENT,
        DUPLICATE_EXAM,
        EXAM_ORDER,
        EXPIRED_TEMPLATE,
        TAMPERED_TEMPLATE
    }

    private final Code code;

    public AcademicException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
