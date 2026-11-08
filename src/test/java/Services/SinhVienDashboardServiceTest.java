package Services;

import Models.HocKy;
import Models.KyThi;
import Models.LichHoc;
import Models.LichSuDiemDanhItem;
import Models.LopHocPhan;
import Models.SinhVienDashboardView;
import Models.ThongBao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinhVienDashboardServiceTest {

    @Test
    void tongHopDungTheVaDanhSachRutGon() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        Fixture fixture = fixture()
                .withSections(section("DangHoc"), section("HoanThanh"))
                .withSchedules(
                        schedule(today, "BinhThuong", 2),
                        schedule(today.plusDays(2), "BinhThuong", 1))
                .withExams(
                        exam(5, today.plusDays(3), 1),
                        exam(3, today.plusDays(1), 2),
                        exam(1, today.minusDays(1), 1),
                        exam(2, today, 3),
                        exam(4, today.plusDays(1), 1))
                .withTerm(term(today.minusDays(10), today.plusDays(10)))
                .withAttendance(
                        attendance(today.minusDays(2), "Vang"),
                        attendance(today.minusDays(1), "CoMat"))
                .withNotifications(6);

        SinhVienDashboardView view = fixture.buildService().build(11, today);

        assertEquals(1, view.getSoLopDangHoc());
        assertEquals(1, view.getSoBuoiHomNay());
        assertEquals(4, view.getSoKyThiSapToi());
        assertEquals(1, view.getSoBuoiVang());
        assertTrue(view.isCoHocKyHienTai());
        assertFalse(view.isDangHienThiLichGanNhat());
        assertEquals(1, view.getLichNoiBat().size());
        assertEquals(3, view.getKyThiSapToi().size());
        assertEquals(Arrays.asList(2, 4, 3), Arrays.asList(
                view.getKyThiSapToi().get(0).getMaKythi(),
                view.getKyThiSapToi().get(1).getMaKythi(),
                view.getKyThiSapToi().get(2).getMaKythi()));
        assertEquals(5, view.getThongBaoMoi().size());
        assertEquals(5, fixture.notifications.requestedLimit);
    }

    @Test
    void khongCoLichHomNayChiHienThiBuoiTuongLaiGanNhat() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        LichHoc earlierShift = schedule(today.plusDays(1), "BinhThuong", 1);
        Fixture fixture = fixture().withSchedules(
                schedule(today.minusDays(1), "BinhThuong", 1),
                schedule(today.plusDays(2), "BinhThuong", 1),
                schedule(today.plusDays(1), "DaHuy", 1),
                earlierShift,
                schedule(today.plusDays(1), "BinhThuong", 3));

        SinhVienDashboardView view = fixture.buildService().build(11, today);

        assertEquals(0, view.getSoBuoiHomNay());
        assertTrue(view.isDangHienThiLichGanNhat());
        assertEquals(Collections.singletonList(earlierShift), view.getLichNoiBat());
    }

    @Test
    void khongCoHocKyHienTaiThiKhongDemVang() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        SinhVienDashboardView view = fixture()
                .withTerm(term(today.minusDays(20), today.minusDays(1)))
                .withAttendance(attendance(today.minusDays(2), "Vang"))
                .buildService()
                .build(11, today);

        assertEquals(0, view.getSoBuoiVang());
        assertFalse(view.isCoHocKyHienTai());
    }

    @Test
    void chiDemVangNamTrongHocKyHienTaiKeCaHaiNgayBien() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        LocalDate start = today.minusDays(10);
        LocalDate end = today.plusDays(10);
        SinhVienDashboardView view = fixture()
                .withTerm(term(start, end))
                .withAttendance(
                        attendance(start.minusDays(1), "Vang"),
                        attendance(start, "Vang"),
                        attendance(today, "CoMat"),
                        attendance(end, "Vang"),
                        attendance(end.plusDays(1), "Vang"))
                .buildService()
                .build(11, today);

        assertEquals(2, view.getSoBuoiVang());
    }

    @Test
    void viewSaoChepVaKhoaCacDanhSach() {
        List<LichHoc> schedules = new ArrayList<>();
        schedules.add(schedule(LocalDate.of(2026, 7, 27), "BinhThuong", 1));
        SinhVienDashboardView view = new SinhVienDashboardView(
                1, 1, 0, 0, false, false,
                schedules, Collections.<KyThi>emptyList(),
                Collections.<ThongBao>emptyList());

        schedules.clear();

        assertEquals(1, view.getLichNoiBat().size());
        assertThrows(UnsupportedOperationException.class,
                () -> view.getLichNoiBat().clear());
    }

    private static Fixture fixture() {
        return new Fixture();
    }

    private static LopHocPhan section(String status) {
        LopHocPhan value = new LopHocPhan();
        value.setTrangThai(status);
        return value;
    }

    private static LichHoc schedule(LocalDate date, String status, int shift) {
        LichHoc value = new LichHoc();
        value.setNgayHoc(date);
        value.setTrangThai(status);
        value.setMaCa(shift);
        return value;
    }

    private static KyThi exam(int id, LocalDate date, int shift) {
        KyThi value = new KyThi();
        value.setMaKythi(id);
        value.setNgayThi(date);
        value.setMaCa(shift);
        return value;
    }

    private static HocKy term(LocalDate start, LocalDate end) {
        HocKy value = new HocKy();
        value.setNgayBatdau(start);
        value.setNgayKetthuc(end);
        return value;
    }

    private static LichSuDiemDanhItem attendance(LocalDate date, String status) {
        return new LichSuDiemDanhItem(date, "Môn học", "Khoa", status);
    }

    private static final class Fixture {
        private final FakeSections sections = new FakeSections();
        private final FakeSchedules schedules = new FakeSchedules();
        private final FakeExams exams = new FakeExams();
        private final FakeTerms terms = new FakeTerms();
        private final FakeAttendance attendance = new FakeAttendance();
        private final FakeNotifications notifications = new FakeNotifications();

        private Fixture withSections(LopHocPhan... values) {
            sections.values = Arrays.asList(values);
            return this;
        }

        private Fixture withSchedules(LichHoc... values) {
            schedules.values = Arrays.asList(values);
            return this;
        }

        private Fixture withExams(KyThi... values) {
            exams.values = Arrays.asList(values);
            return this;
        }

        private Fixture withTerm(HocKy value) {
            terms.values = Collections.singletonList(value);
            return this;
        }

        private Fixture withAttendance(LichSuDiemDanhItem... values) {
            attendance.values = Arrays.asList(values);
            return this;
        }

        private Fixture withNotifications(int count) {
            List<ThongBao> values = new ArrayList<>();
            for (int index = 0; index < count; index++) {
                ThongBao value = new ThongBao();
                value.setMaThongbao(index + 1);
                values.add(value);
            }
            notifications.values = values;
            return this;
        }

        private SinhVienDashboardService buildService() {
            return new SinhVienDashboardService(
                    sections, schedules, exams, terms, attendance, notifications);
        }
    }

    private static final class FakeSections extends LopHocPhanService {
        private List<LopHocPhan> values = Collections.emptyList();

        @Override
        public List<LopHocPhan> findByStudent(int userId) {
            return values;
        }
    }

    private static final class FakeSchedules extends LichHocService {
        private List<LichHoc> values = Collections.emptyList();

        @Override
        public List<LichHoc> findByNguoiDung(int userId, String role) {
            return values;
        }
    }

    private static final class FakeExams extends KyThiService {
        private List<KyThi> values = Collections.emptyList();

        @Override
        public List<KyThi> findByNguoiDung(int userId, String role) {
            return values;
        }
    }

    private static final class FakeTerms extends HocKyService {
        private List<HocKy> values = Collections.emptyList();

        @Override
        public List<HocKy> getAll() {
            return values;
        }
    }

    private static final class FakeAttendance extends XemDiemDanhService {
        private List<LichSuDiemDanhItem> values = Collections.emptyList();

        @Override
        public List<LichSuDiemDanhItem> getLichSuDiemDanh(int userId) {
            return values;
        }
    }

    private static final class FakeNotifications extends ThongBaoService {
        private List<ThongBao> values = Collections.emptyList();
        private int requestedLimit;

        @Override
        public List<ThongBao> findRecentByNguoiDung(int userId, int limit) {
            requestedLimit = limit;
            return new ArrayList<>(values.subList(0, Math.min(limit, values.size())));
        }
    }
}
