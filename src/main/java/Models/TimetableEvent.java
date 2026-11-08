package Models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class TimetableEvent {
    public enum Kind {
        CLASS, EXAM
    }

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final Kind kind;
    private final int id;
    private final LocalDate date;
    private final int shiftId;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String sectionCode;
    private final String courseName;
    private final String roomName;
    private final String shiftLabel;
    private final String teacherName;
    private final String category;
    private final String status;
    private final String categoryLabel;
    private final String statusLabel;
    private final String cssClass;
    private final String timeLabel;

    private TimetableEvent(Kind kind, int id, LocalDate date, int shiftId,
            LocalTime startTime, LocalTime endTime, String sectionCode,
            String courseName, String roomName, String shiftLabel,
            String teacherName, String category, String status) {
        this.kind = kind;
        this.id = id;
        this.date = date;
        this.shiftId = shiftId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sectionCode = text(sectionCode);
        this.courseName = text(courseName);
        this.roomName = text(roomName);
        this.shiftLabel = text(shiftLabel);
        this.teacherName = text(teacherName);
        this.category = text(category);
        this.status = text(status);
        this.categoryLabel = categoryLabel(kind, this.category);
        this.statusLabel = kind == Kind.CLASS ? statusLabel(this.status) : "";
        this.cssClass = cssClass(kind, this.category, this.status);
        this.timeLabel = timeLabel(startTime, endTime);
    }

    public static TimetableEvent fromClass(LichHoc value, CaHoc shift) {
        Objects.requireNonNull(value, "Lịch học không được để trống.");
        Objects.requireNonNull(shift, "Ca học không được để trống.");
        return new TimetableEvent(Kind.CLASS, value.getMaLichhoc(),
                value.getNgayHoc(), value.getMaCa(), shift.getGioBatdau(),
                shift.getGioKetthuc(), value.getMaLhp(), value.getTenMonHoc(),
                value.getTenPhong(), value.getTenCa(), value.getTenGiangVien(),
                value.getLoaiLich(), value.getTrangThai());
    }

    public static TimetableEvent fromExam(KyThi value, CaHoc shift) {
        Objects.requireNonNull(value, "Kỳ thi không được để trống.");
        Objects.requireNonNull(shift, "Ca học không được để trống.");
        return new TimetableEvent(Kind.EXAM, value.getMaKythi(),
                value.getNgayThi(), value.getMaCa(), shift.getGioBatdau(),
                shift.getGioKetthuc(), value.getMaLhp(), value.getTenMonHoc(),
                value.getTenPhong(), value.getTenCa(), value.getTenGiangVien(),
                value.getLoaiKythi(), "");
    }

    public Kind getKind() {
        return kind;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getShiftId() {
        return shiftId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getShiftLabel() {
        return shiftLabel;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    private static String categoryLabel(Kind kind, String value) {
        if (kind == Kind.CLASS) {
            if ("HocChinh".equals(value)) return "Học chính";
            if ("DayBu".equals(value)) return "Dạy bù";
        } else {
            if ("GiuaKy".equals(value)) return "Giữa kỳ";
            if ("BaoVe".equals(value)) return "Bảo vệ";
            if ("CuoiKy".equals(value)) return "Cuối kỳ";
        }
        return value == null ? "" : value;
    }

    private static String statusLabel(String value) {
        if ("BinhThuong".equals(value)) return "Bình thường";
        if ("XinNghi".equals(value)) return "Xin nghỉ";
        if ("DaHuy".equals(value)) return "Đã hủy";
        return "";
    }

    private static String cssClass(Kind kind, String category, String status) {
        if (kind == Kind.EXAM) return "event-exam";
        if ("DaHuy".equals(status)) return "event-cancelled";
        if ("XinNghi".equals(status)) return "event-leave";
        if ("DayBu".equals(category)) return "event-makeup";
        return "event-class";
    }

    private static String timeLabel(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) return "";
        return TIME_FORMAT.format(startTime) + " - " + TIME_FORMAT.format(endTime);
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
