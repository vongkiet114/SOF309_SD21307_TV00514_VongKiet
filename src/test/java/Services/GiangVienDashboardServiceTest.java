package Services;

import Models.GiangVienDashboardView;
import Models.KyThi;
import Models.LichHoc;
import Models.LopHocPhan;
import Models.ThongBao;
import Repository.DashboardRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GiangVienDashboardServiceTest {

    @Test
    void tongHopCongViecHomNayVaDanhSachSapToi() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        Fixture fixture = fixture()
                .withMetrics(73, 2)
                .withSections(section("DangHoc"), section("HoanThanh"))
                .withSchedules(
                        schedule(2, today, "BinhThuong"),
                        schedule(1, today, "DaHuy"),
                        schedule(3, today.plusDays(1), "BinhThuong"),
                        schedule(1, today, "BinhThuong"))
                .withExams(
                        exam(4, today.plusDays(2), 1),
                        exam(1, today.minusDays(1), 1),
                        exam(3, today.plusDays(1), 2),
                        exam(2, today.plusDays(1), 1))
                .withNotifications(6);

        GiangVienDashboardView view = fixture.buildService().build(7, today);

        assertEquals(1, view.getSoLopDangDay());
        assertEquals(73, view.getSoSinhVienPhuTrach());
        assertEquals(2, view.getSoBuoiHomNay());
        assertEquals(2, view.getSoBuoiCanDiemDanh());
        assertEquals(Arrays.asList(1, 2), Arrays.asList(
                view.getLichDayHomNay().get(0).getMaCa(),
                view.getLichDayHomNay().get(1).getMaCa()));
        assertEquals(Arrays.asList(2, 3, 4), Arrays.asList(
                view.getKyThiSapToi().get(0).getMaKythi(),
                view.getKyThiSapToi().get(1).getMaKythi(),
                view.getKyThiSapToi().get(2).getMaKythi()));
        assertEquals(5, view.getThongBaoMoi().size());
        assertEquals(5, fixture.notifications.requestedLimit);
        assertEquals(7, fixture.metrics.teacherId);
        assertEquals(today, fixture.metrics.today);
    }

    @Test
    void viewSaoChepVaKhoaCacDanhSach() {
        List<LichHoc> schedules = new ArrayList<>();
        schedules.add(schedule(1, LocalDate.of(2026, 7, 27), "BinhThuong"));
        GiangVienDashboardView view = new GiangVienDashboardView(
                1, 2, 3, 4, schedules,
                Collections.<KyThi>emptyList(),
                Collections.<ThongBao>emptyList());

        schedules.clear();

        assertEquals(1, view.getLichDayHomNay().size());
        assertThrows(UnsupportedOperationException.class,
                () -> view.getLichDayHomNay().clear());
    }

    private static Fixture fixture() {
        return new Fixture();
    }

    private static LopHocPhan section(String status) {
        LopHocPhan value = new LopHocPhan();
        value.setTrangThai(status);
        return value;
    }

    private static LichHoc schedule(int shift, LocalDate date, String status) {
        LichHoc value = new LichHoc();
        value.setMaCa(shift);
        value.setNgayHoc(date);
        value.setTrangThai(status);
        return value;
    }

    private static KyThi exam(int id, LocalDate date, int shift) {
        KyThi value = new KyThi();
        value.setMaKythi(id);
        value.setNgayThi(date);
        value.setMaCa(shift);
        return value;
    }

    private static final class Fixture {
        private final FakeSections sections = new FakeSections();
        private final FakeSchedules schedules = new FakeSchedules();
        private final FakeExams exams = new FakeExams();
        private final FakeNotifications notifications = new FakeNotifications();
        private final FakeDashboardRepository metrics = new FakeDashboardRepository();

        private Fixture withMetrics(int distinctStudents, int unattendedToday) {
            metrics.distinctStudents = distinctStudents;
            metrics.unattendedToday = unattendedToday;
            return this;
        }

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

        private GiangVienDashboardService buildService() {
            return new GiangVienDashboardService(
                    sections, schedules, exams, notifications, metrics);
        }
    }

    private static final class FakeSections extends LopHocPhanService {
        private List<LopHocPhan> values = Collections.emptyList();

        @Override
        public List<LopHocPhan> getAll(Integer termId, Integer teacherId) {
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

    private static final class FakeNotifications extends ThongBaoService {
        private List<ThongBao> values = Collections.emptyList();
        private int requestedLimit;

        @Override
        public List<ThongBao> findRecentByNguoiDung(int userId, int limit) {
            requestedLimit = limit;
            return new ArrayList<>(values.subList(0, Math.min(limit, values.size())));
        }
    }

    private static final class FakeDashboardRepository extends DashboardRepository {
        private int distinctStudents;
        private int unattendedToday;
        private int teacherId;
        private LocalDate today;

        @Override
        public int countDistinctActiveStudentsForTeacher(int value) {
            teacherId = value;
            return distinctStudents;
        }

        @Override
        public int countUnattendedSessionsForTeacher(int value, LocalDate date) {
            teacherId = value;
            today = date;
            return unattendedToday;
        }
    }
}
