package Models;

import java.time.LocalDate;

public class LichSuDiemDanhItem {
    private final LocalDate ngayHoc;
    private final String tenMonHoc;
    private final String tenKhoa;
    private final String trangThai;
    private final int maMonHoc;

    public LichSuDiemDanhItem(LocalDate ngayHoc, String tenMonHoc,
                              String tenKhoa, String trangThai) {
        this(ngayHoc, tenMonHoc, tenKhoa, trangThai, 0);
    }

    public LichSuDiemDanhItem(LocalDate ngayHoc, String tenMonHoc,
                              String tenKhoa, String trangThai, int maMonHoc) {
        this.ngayHoc = ngayHoc;
        this.tenMonHoc = tenMonHoc;
        this.tenKhoa = tenKhoa;
        this.trangThai = trangThai;
        this.maMonHoc = maMonHoc;
    }

    public int getMaMonHoc() {
        return maMonHoc;
    }

    public LocalDate getNgayHoc() {
        return ngayHoc;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public String getTenKhoa() {
        return tenKhoa;
    }

    public String getTrangThai() {
        return trangThai;
    }
}
