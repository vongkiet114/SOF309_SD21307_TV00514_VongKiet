package Services;

import Models.LichHoc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class AttendanceSessionPolicy {
    private AttendanceSessionPolicy() {
    }

    public static List<LichHoc> filterSelectable(List<LichHoc> schedules,
                                                  int sectionId,
                                                  LocalDate today) {
        if (schedules == null || today == null) return Collections.emptyList();
        return schedules.stream()
                .filter(Objects::nonNull)
                .filter(value -> value.getMaLophocphan() == sectionId)
                .filter(value -> "BinhThuong".equals(value.getTrangThai()))
                .filter(value -> value.getNgayHoc() != null
                        && !value.getNgayHoc().isBefore(today))
                .sorted(Comparator.comparing(LichHoc::getNgayHoc)
                        .thenComparingInt(LichHoc::getMaCa))
                .collect(Collectors.toList());
    }
}
