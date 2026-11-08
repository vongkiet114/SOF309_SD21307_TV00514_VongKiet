package Models;

public class LopHocPhan {

    private int maLophocphan;
    private int maMonhoc;
    private int maGiangvien;
    private int maHocky;
    private String trangThai;

    // Transient fields for display purposes (from JOINs)
    private String tenMonHoc;
    private String tenGiangVien;
    private String tenHocKy;
    private int soTinChi;
    private int soLuongSV;
    private String maLhp;
    private String namHoc;

    public LopHocPhan() {
    }

    public LopHocPhan(int maLophocphan, int maMonhoc, int maGiangvien, int maHocky, String trangThai) {
        this.maLophocphan = maLophocphan;
        this.maMonhoc = maMonhoc;
        this.maGiangvien = maGiangvien;
        this.maHocky = maHocky;
        this.trangThai = trangThai;
    }

    // Getters and Setters for persistent fields
    public int getMaLophocphan() {
        return maLophocphan;
    }

    public void setMaLophocphan(int maLophocphan) {
        this.maLophocphan = maLophocphan;
    }

    public int getMaMonhoc() {
        return maMonhoc;
    }

    public void setMaMonhoc(int maMonhoc) {
        this.maMonhoc = maMonhoc;
    }

    public int getMaGiangvien() {
        return maGiangvien;
    }

    public void setMaGiangvien(int maGiangvien) {
        this.maGiangvien = maGiangvien;
    }

    public int getMaHocky() {
        return maHocky;
    }

    public void setMaHocky(int maHocky) {
        this.maHocky = maHocky;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    // Getters and Setters for transient fields
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

    public String getTenHocKy() {
        return tenHocKy;
    }

    public void setTenHocKy(String tenHocKy) {
        this.tenHocKy = tenHocKy;
    }

    public int getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(int soTinChi) {
        this.soTinChi = soTinChi;
    }

    public int getSoLuongSV() {
        return soLuongSV;
    }

    public void setSoLuongSV(int soLuongSV) {
        this.soLuongSV = soLuongSV;
    }

    public String getMaLhp() {
        return maLhp;
    }

    public void setMaLhp(String maLhp) {
        this.maLhp = maLhp;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    @Override
    public String toString() {
        return "LopHocPhan{" +
                "maLophocphan=" + maLophocphan +
                ", maMonhoc=" + maMonhoc +
                ", maGiangvien=" + maGiangvien +
                ", maHocky=" + maHocky +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
