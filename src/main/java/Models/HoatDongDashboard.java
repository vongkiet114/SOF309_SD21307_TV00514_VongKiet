package Models;

import java.time.LocalDateTime;

public class HoatDongDashboard {
    private final String hoTen;
    private final String hanhDong;
    private final LocalDateTime thoiGian;

    public HoatDongDashboard(
            String hoTen, String hanhDong, LocalDateTime thoiGian) {
        this.hoTen = hoTen;
        this.hanhDong = hanhDong;
        this.thoiGian = thoiGian;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }
}
