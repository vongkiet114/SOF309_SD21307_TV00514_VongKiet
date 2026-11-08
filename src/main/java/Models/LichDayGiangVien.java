package Models;

import java.time.LocalDate;
import java.time.LocalTime;

// DTO lich day giang vien (tong hop tu lichhoc va thong tin giang vien)
public class LichDayGiangVien {

    private int maLichday;
    private int maGiangvien;
    private Integer maLichhoc;
    private Integer maPhong;
    private Integer maCa;
    private LocalDate ngay;
    private String tieuDe;
    private String loaiLich;
    private LocalTime gioBatdau;
    private LocalTime gioKetthuc;

    // Transient
    private String tenPhong;
    private String tenCa;
    private String tenMonhoc;
    private String hoTen;

    public LichDayGiangVien() {}

    public int getMaLichday() { return maLichday; }
    public void setMaLichday(int maLichday) { this.maLichday = maLichday; }

    public int getMaGiangvien() { return maGiangvien; }
    public void setMaGiangvien(int maGiangvien) { this.maGiangvien = maGiangvien; }

    public Integer getMaLichhoc() { return maLichhoc; }
    public void setMaLichhoc(Integer maLichhoc) { this.maLichhoc = maLichhoc; }

    public Integer getMaPhong() { return maPhong; }
    public void setMaPhong(Integer maPhong) { this.maPhong = maPhong; }

    public Integer getMaCa() { return maCa; }
    public void setMaCa(Integer maCa) { this.maCa = maCa; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getLoaiLich() { return loaiLich; }
    public void setLoaiLich(String loaiLich) { this.loaiLich = loaiLich; }

    public LocalTime getGioBatdau() { return gioBatdau; }
    public void setGioBatdau(LocalTime gioBatdau) { this.gioBatdau = gioBatdau; }

    public LocalTime getGioKetthuc() { return gioKetthuc; }
    public void setGioKetthuc(LocalTime gioKetthuc) { this.gioKetthuc = gioKetthuc; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getTenCa() { return tenCa; }
    public void setTenCa(String tenCa) { this.tenCa = tenCa; }

    public String getTenMonhoc() { return tenMonhoc; }
    public void setTenMonhoc(String tenMonhoc) { this.tenMonhoc = tenMonhoc; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
}
