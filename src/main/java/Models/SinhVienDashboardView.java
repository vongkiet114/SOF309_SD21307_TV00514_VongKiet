package Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SinhVienDashboardView {
    private final int soLopDangHoc;
    private final int soBuoiHomNay;
    private final int soKyThiSapToi;
    private final int soBuoiVang;
    private final boolean coHocKyHienTai;
    private final boolean dangHienThiLichGanNhat;
    private final List<LichHoc> lichNoiBat;
    private final List<KyThi> kyThiSapToi;
    private final List<ThongBao> thongBaoMoi;

    public SinhVienDashboardView(
            int soLopDangHoc,
            int soBuoiHomNay,
            int soKyThiSapToi,
            int soBuoiVang,
            boolean coHocKyHienTai,
            boolean dangHienThiLichGanNhat,
            List<LichHoc> lichNoiBat,
            List<KyThi> kyThiSapToi,
            List<ThongBao> thongBaoMoi) {
        this.soLopDangHoc = soLopDangHoc;
        this.soBuoiHomNay = soBuoiHomNay;
        this.soKyThiSapToi = soKyThiSapToi;
        this.soBuoiVang = soBuoiVang;
        this.coHocKyHienTai = coHocKyHienTai;
        this.dangHienThiLichGanNhat = dangHienThiLichGanNhat;
        this.lichNoiBat = immutableCopy(lichNoiBat);
        this.kyThiSapToi = immutableCopy(kyThiSapToi);
        this.thongBaoMoi = immutableCopy(thongBaoMoi);
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    public int getSoLopDangHoc() {
        return soLopDangHoc;
    }

    public int getSoBuoiHomNay() {
        return soBuoiHomNay;
    }

    public int getSoKyThiSapToi() {
        return soKyThiSapToi;
    }

    public int getSoBuoiVang() {
        return soBuoiVang;
    }

    public boolean isCoHocKyHienTai() {
        return coHocKyHienTai;
    }

    public boolean isDangHienThiLichGanNhat() {
        return dangHienThiLichGanNhat;
    }

    public List<LichHoc> getLichNoiBat() {
        return lichNoiBat;
    }

    public List<KyThi> getKyThiSapToi() {
        return kyThiSapToi;
    }

    public List<ThongBao> getThongBaoMoi() {
        return thongBaoMoi;
    }
}
