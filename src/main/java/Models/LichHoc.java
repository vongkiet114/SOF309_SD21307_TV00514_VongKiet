package Models;

import java.time.LocalDate;

public class LichHoc {

    private int maLichhoc;
    private int maLophocphan;
    private int maPhong;
    private int maCa;
    private LocalDate ngayHoc;
    private String loaiLich;
    private String trangThai;

    // Transient fields
    private String maLhp;
    private String tenPhong;
    private String tenCa;
    private String tenMonHoc;
    private String tenGiangVien;

    public LichHoc() {
    }

    public LichHoc(int maLichhoc, int maLophocphan, int maPhong, int maCa, LocalDate ngayHoc, String loaiLich, String trangThai) {
        this.maLichhoc = maLichhoc;
        this.maLophocphan = maLophocphan;
        this.maPhong = maPhong;
        this.maCa = maCa;
        this.ngayHoc = ngayHoc;
        this.loaiLich = loaiLich;
        this.trangThai = trangThai;
    }

    // Getters and Setters...

    public int getMaLichhoc() {
        return maLichhoc;
    }

    public void setMaLichhoc(int maLichhoc) {
        this.maLichhoc = maLichhoc;
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

    public LocalDate getNgayHoc() {
        return ngayHoc;
    }

    public void setNgayHoc(LocalDate ngayHoc) {
        this.ngayHoc = ngayHoc;
    }

    public String getLoaiLich() {
        return loaiLich;
    }

    public void setLoaiLich(String loaiLich) {
        this.loaiLich = loaiLich;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
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
    public int getMaKhoa() { return 0; }
    public int getMaMonhoc() { return 0; }
    public int getMaGiangvien() { return 0; }
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
