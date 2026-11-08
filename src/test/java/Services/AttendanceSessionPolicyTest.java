package Services;

import Models.LichHoc;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttendanceSessionPolicyTest {
    @Test
    void locFailClosedVaSapTheoNgayRoiCa() {
        LocalDate today = LocalDate.of(2026, 7, 27);

        List<LichHoc> result = AttendanceSessionPolicy.filterSelectable(
                Arrays.asList(
                        schedule(1, 8, today.minusDays(1), "BinhThuong", 3),
                        schedule(2, 8, today, "BinhThuong", 2),
                        schedule(3, 8, today.plusDays(1), "BinhThuong", 1),
                        schedule(4, 8, today.plusDays(1), "DaHuy", 2),
                        schedule(5, 9, today.plusDays(1), "BinhThuong", 3),
                        schedule(6, 8, null, "BinhThuong", 1),
                        schedule(7, 8, today.plusDays(2), "BinhThuong", 3),
                        schedule(8, 8, today.plusDays(2), "BinhThuong", 1),
                        schedule(9, 8, today.plusDays(2), "BinhThuong", 2)),
                8, today);

        assertEquals(Arrays.asList(2, 3, 8, 9, 7), result.stream()
                .map(LichHoc::getMaLichhoc)
                .collect(Collectors.toList()));
    }

    private static LichHoc schedule(int id,
                                    int section,
                                    LocalDate date,
                                    String status,
                                    int shift) {
        LichHoc value = new LichHoc();
        value.setMaLichhoc(id);
        value.setMaLophocphan(section);
        value.setNgayHoc(date);
        value.setTrangThai(status);
        value.setMaCa(shift);
        return value;
    }
}
