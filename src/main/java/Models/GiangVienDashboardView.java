package Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GiangVienDashboardView {
    private final int soLopDangDay;
    private final int soSinhVienPhuTrach;
    private final int soBuoiHomNay;
    private final int soBuoiCanDiemDanh;
    private final List<LichHoc> lichDayHomNay;
    private final List<KyThi> kyThiSapToi;
    private final List<ThongBao> thongBaoMoi;

    public GiangVienDashboardView(
            int soLopDangDay,
            int soSinhVienPhuTrach,
            int soBuoiHomNay,
            int soBuoiCanDiemDanh,
            List<LichHoc> lichDayHomNay,
            List<KyThi> kyThiSapToi,
            List<ThongBao> thongBaoMoi) {
        this.soLopDangDay = soLopDangDay;
        this.soSinhVienPhuTrach = soSinhVienPhuTrach;
        this.soBuoiHomNay = soBuoiHomNay;
        this.soBuoiCanDiemDanh = soBuoiCanDiemDanh;
        this.lichDayHomNay = immutableCopy(lichDayHomNay);
        this.kyThiSapToi = immutableCopy(kyThiSapToi);
        this.thongBaoMoi = immutableCopy(thongBaoMoi);
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    public int getSoLopDangDay() {
        return soLopDangDay;
    }

    public int getSoSinhVienPhuTrach() {
        return soSinhVienPhuTrach;
    }

    public int getSoBuoiHomNay() {
        return soBuoiHomNay;
    }

    public int getSoBuoiCanDiemDanh() {
        return soBuoiCanDiemDanh;
    }

    public List<LichHoc> getLichDayHomNay() {
        return lichDayHomNay;
    }

    public List<KyThi> getKyThiSapToi() {
        return kyThiSapToi;
    }

    public List<ThongBao> getThongBaoMoi() {
        return thongBaoMoi;
    }
}
