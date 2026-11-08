package Models;

// Thong ke giang day cua mot giang vien (tinh tren cac buoi da day den hom nay).
public class ThongKeGiangDay {
    private final int soCa;      // tong so ca (buoi lich hoc) da day
    private final int soBuoi;    // tong so buoi (ngay day rieng biet)
    private final int tongPhut;  // tong so phut giang -> quy doi ra gio

    public ThongKeGiangDay(int soCa, int soBuoi, int tongPhut) {
        this.soCa = soCa;
        this.soBuoi = soBuoi;
        this.tongPhut = tongPhut;
    }

    public int getSoCa() {
        return soCa;
    }

    public int getSoBuoi() {
        return soBuoi;
    }

    public int getTongPhut() {
        return tongPhut;
    }

    // Tong so gio giang (lam tron 1 chu so thap phan).
    public double getSoGio() {
        return Math.round(tongPhut / 60.0 * 10.0) / 10.0;
    }
}
