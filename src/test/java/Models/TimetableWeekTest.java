package Models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimetableWeekTest {
    @Test
    void chuanHoaVeThuHaiVaKhongGopMatSuKien() {
        CaHoc ca = new CaHoc();
        ca.setMaCa(1);
        ca.setGioBatdau(LocalTime.of(7, 15));
        ca.setGioKetthuc(LocalTime.of(9, 15));

        LichHoc first = classRow(11, "UDPM-LHP-003", "T101", "BinhThuong");
        LichHoc second = classRow(12, "WEB-LHP-004", "T102", "DaHuy");
        TimetableWeek week = TimetableWeek.of(
                LocalDate.of(2026, 9, 9),
                Arrays.asList(ca),
                Arrays.asList(
                        TimetableEvent.fromClass(first, ca),
                        TimetableEvent.fromClass(second, ca)),
                false,
                LocalDate.of(2026, 9, 21));

        assertEquals(LocalDate.of(2026, 9, 7), week.getWeekStart());
        assertEquals(LocalDate.of(2026, 9, 13), week.getWeekEnd());
        assertEquals(7, week.getDays().size());
        assertEquals(2, week.getEvents(LocalDate.of(2026, 9, 7), 1).size());
        assertEquals(11, week.getEvents(LocalDate.of(2026, 9, 7), 1).get(0).getId());
        assertEquals(12, week.getEvents(LocalDate.of(2026, 9, 7), 1).get(1).getId());
        assertEquals("Đã hủy",
                week.getEvents(LocalDate.of(2026, 9, 7), 1).get(1).getStatusLabel());
        assertEquals("event-cancelled",
                week.getEvents(LocalDate.of(2026, 9, 7), 1).get(1).getCssClass());
        assertEquals(LocalDate.of(2026, 9, 21),
                week.getNearestEventWeekStart());
    }

    @Test
    void kyThiCoNhanTiengVietVaKhongCoQuyenQuanLyTrongModel() {
        KyThi exam = new KyThi(21, 5, 3, 2,
                LocalDate.of(2026, 10, 19), "BaoVe");
        exam.setMaLhp("TKDH-LHP-006");
        exam.setTenMonHoc("Thiết kế đồ họa");
        exam.setTenPhong("T103");
        exam.setTenCa("09:25 - 11:25");
        exam.setTenGiangVien("Nguyễn Giảng Viên");

        CaHoc ca = new CaHoc();
        ca.setMaCa(2);
        ca.setGioBatdau(LocalTime.of(9, 25));
        ca.setGioKetthuc(LocalTime.of(11, 25));
        TimetableEvent event = TimetableEvent.fromExam(exam, ca);

        assertEquals(TimetableEvent.Kind.EXAM, event.getKind());
        assertEquals("Bảo vệ", event.getCategoryLabel());
        assertEquals("event-exam", event.getCssClass());
        assertEquals("", event.getStatusLabel());
    }

    @Test
    void dayBuXinNghiGiuCaNhanVaMauTrangThai() {
        CaHoc ca = new CaHoc();
        ca.setMaCa(1);
        ca.setGioBatdau(LocalTime.of(7, 15));
        ca.setGioKetthuc(LocalTime.of(9, 15));
        LichHoc value = classRow(
                13, "MKT-LHP-005", "T103", "XinNghi");
        value.setLoaiLich("DayBu");

        TimetableEvent event = TimetableEvent.fromClass(value, ca);

        assertEquals("Dạy bù", event.getCategoryLabel());
        assertEquals("Xin nghỉ", event.getStatusLabel());
        assertEquals("event-leave", event.getCssClass());
        assertEquals("07:15 - 09:15", event.getTimeLabel());
    }

    @Test
    void doiTuanKhongLamMatBoLocDaChuanHoa() {
        TimetableFilter filter = TimetableFilter.admin(
                LocalDate.of(2026, 9, 9), 2, 3, 4, 5)
                .withWeekStart(LocalDate.of(2026, 9, 16));

        assertEquals(LocalDate.of(2026, 9, 14), filter.getWeekStart());
        assertEquals(2, filter.getTermId());
        assertEquals(3, filter.getSectionId());
        assertEquals(4, filter.getTeacherId());
        assertEquals(5, filter.getRoomId());
    }

    private static LichHoc classRow(int id, String code, String room, String status) {
        LichHoc value = new LichHoc(id, 5, 3, 1,
                LocalDate.of(2026, 9, 7), "HocChinh", status);
        value.setMaLhp(code);
        value.setTenMonHoc("Môn kiểm thử");
        value.setTenPhong(room);
        value.setTenCa("07:15 - 09:15");
        value.setTenGiangVien("Nguyễn Giảng Viên");
        return value;
    }
}
