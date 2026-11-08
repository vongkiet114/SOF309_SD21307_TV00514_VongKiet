package Models;

import java.time.LocalTime;

public class CaHoc {
    private int maCa;
    private LocalTime gioBatdau;
    private LocalTime gioKetthuc;

    public CaHoc() {
    }

    public int getMaCa() {
        return maCa;
    }

    public void setMaCa(int maCa) {
        this.maCa = maCa;
    }

    public String getTenCa() {
        return "Ca " + maCa;
    }

    public LocalTime getGioBatdau() {
        return gioBatdau;
    }

    public void setGioBatdau(LocalTime gioBatdau) {
        this.gioBatdau = gioBatdau;
    }

    public LocalTime getGioKetthuc() {
        return gioKetthuc;
    }

    public void setGioKetthuc(LocalTime gioKetthuc) {
        this.gioKetthuc = gioKetthuc;
    }
}
