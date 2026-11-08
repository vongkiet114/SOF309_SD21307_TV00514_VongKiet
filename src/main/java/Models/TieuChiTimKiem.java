package Models;

// DTO cho tim kiem sinh vien co phan trang
public class TieuChiTimKiem {

    private String tuKhoa;
    private Integer maKhoa;
    private Integer maChuyennganh;
    private Integer maLop;
    private String trangThai;
    private int trang;
    private int kichThuocTrang;

    public TieuChiTimKiem() {
        this.trang = 1;
        this.kichThuocTrang = 10;
    }

    public String getTuKhoa() { return tuKhoa; }
    public void setTuKhoa(String tuKhoa) { this.tuKhoa = tuKhoa; }

    public Integer getMaKhoa() { return maKhoa; }
    public void setMaKhoa(Integer maKhoa) { this.maKhoa = maKhoa; }

    public Integer getMaChuyennganh() { return maChuyennganh; }
    public void setMaChuyennganh(Integer maChuyennganh) { this.maChuyennganh = maChuyennganh; }

    public Integer getMaLop() { return maLop; }
    public void setMaLop(Integer maLop) { this.maLop = maLop; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getTrang() { return trang; }
    public void setTrang(int trang) { this.trang = trang; }

    public int getKichThuocTrang() { return kichThuocTrang; }
    public void setKichThuocTrang(int kichThuocTrang) { this.kichThuocTrang = kichThuocTrang; }
}
