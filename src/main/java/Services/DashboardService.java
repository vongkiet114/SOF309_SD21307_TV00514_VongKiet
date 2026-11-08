package Services;

import Models.AdminDashboardView;
import Models.HoatDongDashboard;
import Models.LichHoc;
import Repository.DashboardRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardService {
    private final DashboardRepository repository;
    private final LichHocService lichHocService;

    public DashboardService() {
        this(new DashboardRepository(), new LichHocService());
    }

    DashboardService(
            DashboardRepository repository, LichHocService lichHocService) {
        this.repository = repository;
        this.lichHocService = lichHocService;
    }

    public AdminDashboardView build(LocalDate today) {
        DashboardRepository.AdminStatistics statistics =
                repository.loadAdminStatistics(today);
        List<HoatDongDashboard> activities =
                limit(repository.findRecentActivities(5), 5);
        List<LichHoc> allSchedules = lichHocService.getAll();
        if (allSchedules == null) {
            allSchedules = Collections.emptyList();
        }
        List<LichHoc> schedules = allSchedules.stream()
                .filter(value -> value != null && today.equals(value.getNgayHoc()))
                .filter(value -> "BinhThuong".equals(value.getTrangThai()))
                .sorted(Comparator.comparingInt(LichHoc::getMaCa))
                .limit(5)
                .collect(Collectors.toList());
        return new AdminDashboardView(statistics, activities, schedules);
    }

    private static <T> List<T> limit(List<T> values, int limit) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream().limit(limit).collect(Collectors.toList());
    }
}
