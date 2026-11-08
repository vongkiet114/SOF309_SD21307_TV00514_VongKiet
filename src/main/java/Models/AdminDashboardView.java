package Models;

import Repository.DashboardRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminDashboardView {
    private final int soSinhVienHoatDong;
    private final int soGiangVienHoatDong;
    private final int soLopHocPhanMo;
    private final int soBuoiHomNay;
    private final List<HoatDongDashboard> hoatDongGanDay;
    private final List<LichHoc> lichHomNay;

    public AdminDashboardView(
            DashboardRepository.AdminStatistics statistics,
            List<HoatDongDashboard> hoatDongGanDay,
            List<LichHoc> lichHomNay) {
        this(
                statistics.getSoSinhVienHoatDong(),
                statistics.getSoGiangVienHoatDong(),
                statistics.getSoLopHocPhanMo(),
                statistics.getSoBuoiHomNay(),
                hoatDongGanDay,
                lichHomNay);
    }

    public AdminDashboardView(
            int soSinhVienHoatDong,
            int soGiangVienHoatDong,
            int soLopHocPhanMo,
            int soBuoiHomNay,
            List<HoatDongDashboard> hoatDongGanDay,
            List<LichHoc> lichHomNay) {
        this.soSinhVienHoatDong = soSinhVienHoatDong;
        this.soGiangVienHoatDong = soGiangVienHoatDong;
        this.soLopHocPhanMo = soLopHocPhanMo;
        this.soBuoiHomNay = soBuoiHomNay;
        this.hoatDongGanDay = immutableCopy(hoatDongGanDay);
        this.lichHomNay = immutableCopy(lichHomNay);
    }

    public int getSoSinhVienHoatDong() {
        return soSinhVienHoatDong;
    }

    public int getSoGiangVienHoatDong() {
        return soGiangVienHoatDong;
    }

    public int getSoLopHocPhanMo() {
        return soLopHocPhanMo;
    }

    public int getSoBuoiHomNay() {
        return soBuoiHomNay;
    }

    public List<HoatDongDashboard> getHoatDongGanDay() {
        return hoatDongGanDay;
    }

    public List<LichHoc> getLichHomNay() {
        return lichHomNay;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
