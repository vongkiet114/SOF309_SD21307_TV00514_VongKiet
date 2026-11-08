package Models;

import java.time.LocalDate;

public class KyThi {

    private int maKythi;
    private int maLophocphan;
    private int maPhong;
    private int maCa;
    private LocalDate ngayThi;
    private String loaiKythi;

    // Transient fields
    private String maLhp;
    private String tenPhong;
    private String tenCa;
    private String tenMonHoc;
    private String tenGiangVien;

    public KyThi() {
    }

    public KyThi(int maKythi, int maLophocphan, int maPhong, int maCa, LocalDate ngayThi, String loaiKythi) {
        this.maKythi = maKythi;
        this.maLophocphan = maLophocphan;
        this.maPhong = maPhong;
        this.maCa = maCa;
        this.ngayThi = ngayThi;
        this.loaiKythi = loaiKythi;
    }

    public int getMaKythi() {
        return maKythi;
    }

    public void setMaKythi(int maKythi) {
        this.maKythi = maKythi;
    }

    public int getMaLophocphan() {
        return maLophocphan;
    }

    public void setMaLophocphan(int maLophocphan) {
        this.maLophocphan = maLophocphan;
    }

    public int getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(int maPhong) {
        this.maPhong = maPhong;
    }

    public int getMaCa() {
        return maCa;
    }

    public void setMaCa(int maCa) {
        this.maCa = maCa;
    }

    public LocalDate getNgayThi() {
        return ngayThi;
    }

    public void setNgayThi(LocalDate ngayThi) {
        this.ngayThi = ngayThi;
    }

    public String getLoaiKythi() {
        return loaiKythi;
    }

    public void setLoaiKythi(String loaiKythi) {
        this.loaiKythi = loaiKythi;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public String getMaLhp() {
        return maLhp;
    }

    public void setMaLhp(String maLhp) {
        this.maLhp = maLhp;
    }

    // Fallback getters for JSP compatibility
    public String getGhiChu() { return ""; }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public String getTenGiangVien() {
        return tenGiangVien;
    }

    public void setTenGiangVien(String tenGiangVien) {
        this.tenGiangVien = tenGiangVien;
    }
}
