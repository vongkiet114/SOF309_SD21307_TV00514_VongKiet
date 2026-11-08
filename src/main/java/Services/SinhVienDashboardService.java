package Services;

import Models.HocKy;
import Models.KyThi;
import Models.LichHoc;
import Models.LichSuDiemDanhItem;
import Models.LopHocPhan;
import Models.SinhVienDashboardView;
import Models.ThongBao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SinhVienDashboardService {
    private static final int SO_KY_THI_HIEN_THI = 3;
    private static final int SO_THONG_BAO_HIEN_THI = 5;

    private final LopHocPhanService sections;
    private final LichHocService schedules;
    private final KyThiService exams;
    private final HocKyService terms;
    private final XemDiemDanhService attendance;
    private final ThongBaoService notifications;

    public SinhVienDashboardService() {
        this(new LopHocPhanService(), new LichHocService(),
                new KyThiService(), new HocKyService(),
                new XemDiemDanhService(), new ThongBaoService());
    }

    SinhVienDashboardService(
            LopHocPhanService sections,
            LichHocService schedules,
            KyThiService exams,
            HocKyService terms,
            XemDiemDanhService attendance,
            ThongBaoService notifications) {
        this.sections = sections;
        this.schedules = schedules;
        this.exams = exams;
        this.terms = terms;
        this.attendance = attendance;
        this.notifications = notifications;
    }

    public SinhVienDashboardView build(int userId, LocalDate today) {
        Objects.requireNonNull(today, "today");

        int activeSections = (int) values(sections.findByStudent(userId)).stream()
                .filter(Objects::nonNull)
                .filter(value -> "DangHoc".equals(value.getTrangThai()))
                .count();

        List<LichHoc> normalSchedules = values(
                schedules.findByNguoiDung(userId, "STUDENT")).stream()
                .filter(Objects::nonNull)
                .filter(value -> "BinhThuong".equals(value.getTrangThai()))
                .filter(value -> value.getNgayHoc() != null)
                .sorted(Comparator.comparing(LichHoc::getNgayHoc)
                        .thenComparingInt(LichHoc::getMaCa))
                .collect(Collectors.toList());
        List<LichHoc> todaySchedules = normalSchedules.stream()
                .filter(value -> today.equals(value.getNgayHoc()))
                .collect(Collectors.toList());
        List<LichHoc> featuredSchedules;
        boolean showingNearest;
        if (!todaySchedules.isEmpty()) {
            featuredSchedules = todaySchedules;
            showingNearest = false;
        } else {
            featuredSchedules = normalSchedules.stream()
                    .filter(value -> value.getNgayHoc().isAfter(today))
                    .limit(1)
                    .collect(Collectors.toList());
            showingNearest = !featuredSchedules.isEmpty();
        }

        List<KyThi> upcomingExams = values(
                exams.findByNguoiDung(userId, "STUDENT")).stream()
                .filter(Objects::nonNull)
                .filter(value -> value.getNgayThi() != null)
                .filter(value -> !value.getNgayThi().isBefore(today))
                .sorted(Comparator.comparing(KyThi::getNgayThi)
                        .thenComparingInt(KyThi::getMaCa))
                .collect(Collectors.toList());

        Optional<HocKy> currentTerm = values(terms.getAll()).stream()
                .filter(Objects::nonNull)
                .filter(value -> value.getNgayBatdau() != null
                        && value.getNgayKetthuc() != null)
                .filter(value -> !today.isBefore(value.getNgayBatdau())
                        && !today.isAfter(value.getNgayKetthuc()))
                .findFirst();
        int absences = currentTerm.map(term -> (int) values(
                        attendance.getLichSuDiemDanh(userId)).stream()
                        .filter(Objects::nonNull)
                        .filter(value -> "Vang".equals(value.getTrangThai()))
                        .map(LichSuDiemDanhItem::getNgayHoc)
                        .filter(Objects::nonNull)
                        .filter(date -> !date.isBefore(term.getNgayBatdau())
                                && !date.isAfter(term.getNgayKetthuc()))
                        .count())
                .orElse(0);

        List<ThongBao> recentNotifications = new ArrayList<>(values(
                notifications.findRecentByNguoiDung(userId, SO_THONG_BAO_HIEN_THI)));
        if (recentNotifications.size() > SO_THONG_BAO_HIEN_THI) {
            recentNotifications = new ArrayList<>(
                    recentNotifications.subList(0, SO_THONG_BAO_HIEN_THI));
        }

        return new SinhVienDashboardView(
                activeSections,
                todaySchedules.size(),
                upcomingExams.size(),
                absences,
                currentTerm.isPresent(),
                showingNearest,
                featuredSchedules,
                upcomingExams.subList(
                        0, Math.min(SO_KY_THI_HIEN_THI, upcomingExams.size())),
                recentNotifications);
    }

    private static <T> List<T> values(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
