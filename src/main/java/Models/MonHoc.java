package Models;

public class MonHoc {

    private int maMonhoc;
    private String maMon;
    private String tenMonhoc;
    private int soTinchi;
    private int maChuyenNganh;

    // Transient fields
    private String tenChuyenNganh;
    private String tenKhoa;
    private int maKhoa;

    public MonHoc() {
    }

    public int getMaMonhoc() {
        return maMonhoc;
    }

    public void setMaMonhoc(int maMonhoc) {
        this.maMonhoc = maMonhoc;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMonhoc() {
        return tenMonhoc;
    }

    public void setTenMonhoc(String tenMonhoc) {
        this.tenMonhoc = tenMonhoc;
    }

    public int getSoTinchi() {
        return soTinchi;
    }

    public void setSoTinchi(int soTinchi) {
        this.soTinchi = soTinchi;
    }

    public String getTenChuyenNganh() {
        return tenChuyenNganh;
    }

    public void setTenChuyenNganh(String tenChuyenNganh) {
        this.tenChuyenNganh = tenChuyenNganh;
    }

    public String getTenKhoa() {
        return tenKhoa;
    }

    public void setTenKhoa(String tenKhoa) {
        this.tenKhoa = tenKhoa;
    }

    public int getMaKhoa() {
        return maKhoa;
    }

    public void setMaKhoa(int maKhoa) {
        this.maKhoa = maKhoa;
    }

    public int getMaChuyenNganh() {
        return maChuyenNganh;
    }

    public void setMaChuyenNganh(int maChuyenNganh) {
        this.maChuyenNganh = maChuyenNganh;
    }
}
