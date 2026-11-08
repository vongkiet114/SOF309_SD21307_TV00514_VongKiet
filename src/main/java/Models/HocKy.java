package Models;

import java.time.LocalDate;

public class HocKy {

    private int maHocky;
    private String tenHocky;
    private int block;
    private String namHoc;
    private LocalDate ngayBatdau;
    private LocalDate ngayKetthuc;

    public HocKy() {
    }

    public int getMaHocky() {
        return maHocky;
    }

    public void setMaHocky(int maHocky) {
        this.maHocky = maHocky;
    }

    public String getTenHocky() {
        return tenHocky;
    }

    public void setTenHocky(String tenHocky) {
        this.tenHocky = tenHocky;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public LocalDate getNgayBatdau() {
        return ngayBatdau;
    }

    public void setNgayBatdau(LocalDate ngayBatdau) {
        this.ngayBatdau = ngayBatdau;
    }

    public LocalDate getNgayKetthuc() {
        return ngayKetthuc;
    }

    public void setNgayKetthuc(LocalDate ngayKetthuc) {
        this.ngayKetthuc = ngayKetthuc;
    }

    // Transient field - computed by Repository/Service (not a DB column)
    private String trangThai;

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
