package Services;

import Models.AdminDashboardView;
import Models.HoatDongDashboard;
import Models.LichHoc;
import Repository.DashboardRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardServiceTest {
    @Test
    void dashboardAdminDungSoLieuVanHanhVaDanhSachNgan() {
        LocalDate today = LocalDate.of(2026, 7, 27);
        FakeDashboardRepository repository = new FakeDashboardRepository();
        repository.statistics =
                new DashboardRepository.AdminStatistics(90, 10, 12, 6);
        repository.activities = activities(8);
        FakeLichHocService schedules = new FakeLichHocService(
                schedule(today, 3, "BinhThuong"),
                schedule(today, 1, "BinhThuong"),
                schedule(today, 2, "DaHuy"),
                schedule(today.plusDays(1), 1, "BinhThuong"));

        AdminDashboardView view =
                new DashboardService(repository, schedules).build(today);

        assertEquals(90, view.getSoSinhVienHoatDong());
        assertEquals(10, view.getSoGiangVienHoatDong());
        assertEquals(12, view.getSoLopHocPhanMo());
        assertEquals(6, view.getSoBuoiHomNay());
        assertEquals(5, view.getHoatDongGanDay().size());
        assertEquals(2, view.getLichHomNay().size());
        assertEquals(1, view.getLichHomNay().get(0).getMaCa());
        assertEquals(3, view.getLichHomNay().get(1).getMaCa());
        assertEquals(today, repository.loadedDate);
        assertEquals(5, repository.activityLimit);
    }

    private static List<HoatDongDashboard> activities(int count) {
        List<HoatDongDashboard> values = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            values.add(new HoatDongDashboard(
                    "Người dùng " + index,
                    "Thao tác " + index,
                    LocalDateTime.of(2026, 7, 27, 8, index)));
        }
        return values;
    }

    private static LichHoc schedule(LocalDate date, int session, String status) {
        LichHoc value = new LichHoc();
        value.setNgayHoc(date);
        value.setMaCa(session);
        value.setTrangThai(status);
        return value;
    }

    private static final class FakeDashboardRepository extends DashboardRepository {
        private AdminStatistics statistics;
        private List<HoatDongDashboard> activities;
        private LocalDate loadedDate;
        private int activityLimit;

        @Override
        public AdminStatistics loadAdminStatistics(LocalDate today) {
            loadedDate = today;
            return statistics;
        }

        @Override
        public List<HoatDongDashboard> findRecentActivities(int limit) {
            activityLimit = limit;
            return activities;
        }
    }

    private static final class FakeLichHocService extends LichHocService {
        private final List<LichHoc> schedules;

        private FakeLichHocService(LichHoc... schedules) {
            this.schedules = Arrays.asList(schedules);
        }

        @Override
        public List<LichHoc> getAll() {
            return schedules;
        }
    }
}
