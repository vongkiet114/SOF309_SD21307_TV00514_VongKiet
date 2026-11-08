package Services;

import Models.GiangVienDashboardView;
import Models.KyThi;
import Models.LichHoc;
import Models.LopHocPhan;
import Models.ThongBao;
import Repository.DashboardRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GiangVienDashboardService {
    private static final int SO_KY_THI_HIEN_THI = 3;
    private static final int SO_THONG_BAO_HIEN_THI = 5;

    private final LopHocPhanService sections;
    private final LichHocService schedules;
    private final KyThiService exams;
    private final ThongBaoService notifications;
    private final DashboardRepository metrics;

    public GiangVienDashboardService() {
        this(new LopHocPhanService(), new LichHocService(),
                new KyThiService(), new ThongBaoService(),
                new DashboardRepository());
    }

    GiangVienDashboardService(
            LopHocPhanService sections,
            LichHocService schedules,
            KyThiService exams,
            ThongBaoService notifications,
            DashboardRepository metrics) {
        this.sections = sections;
        this.schedules = schedules;
        this.exams = exams;
        this.notifications = notifications;
        this.metrics = metrics;
    }

    public GiangVienDashboardView build(int teacherId, LocalDate today) {
        Objects.requireNonNull(today, "today");

        int activeSections = (int) values(sections.getAll(null, teacherId)).stream()
                .filter(Objects::nonNull)
                .filter(value -> "DangHoc".equals(value.getTrangThai()))
                .count();

        List<LichHoc> todaySchedules = values(
                schedules.findByNguoiDung(teacherId, "TEACHER")).stream()
                .filter(Objects::nonNull)
                .filter(value -> today.equals(value.getNgayHoc()))
                .filter(value -> "BinhThuong".equals(value.getTrangThai()))
                .sorted(Comparator.comparingInt(LichHoc::getMaCa))
                .collect(Collectors.toList());

        List<KyThi> upcomingExams = values(
                exams.findByNguoiDung(teacherId, "TEACHER")).stream()
                .filter(Objects::nonNull)
                .filter(value -> value.getNgayThi() != null)
                .filter(value -> !value.getNgayThi().isBefore(today))
                .sorted(Comparator.comparing(KyThi::getNgayThi)
                        .thenComparingInt(KyThi::getMaCa))
                .limit(SO_KY_THI_HIEN_THI)
                .collect(Collectors.toList());

        List<ThongBao> recentNotifications = new ArrayList<>(values(
                notifications.findRecentByNguoiDung(
                        teacherId, SO_THONG_BAO_HIEN_THI)));
        if (recentNotifications.size() > SO_THONG_BAO_HIEN_THI) {
            recentNotifications = new ArrayList<>(
                    recentNotifications.subList(0, SO_THONG_BAO_HIEN_THI));
        }

        return new GiangVienDashboardView(
                activeSections,
                metrics.countDistinctActiveStudentsForTeacher(teacherId),
                todaySchedules.size(),
                metrics.countUnattendedSessionsForTeacher(teacherId, today),
                todaySchedules,
                upcomingExams,
                recentNotifications);
    }

    private static <T> List<T> values(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
