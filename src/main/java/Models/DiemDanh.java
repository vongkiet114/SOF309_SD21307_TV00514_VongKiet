package Models;

public class DiemDanh {

    private int maDiemdanh;
    private int maDanhsach;
    private int maLichhoc;
    private String trangThai;

    public DiemDanh() {
    }

    public DiemDanh(int maDiemdanh, int maDanhsach, int maLichhoc, String trangThai) {
        this.maDiemdanh = maDiemdanh;
        this.maDanhsach = maDanhsach;
        this.maLichhoc = maLichhoc;
        this.trangThai = trangThai;
    }

    public int getMaDiemdanh() {
        return maDiemdanh;
    }

    public void setMaDiemdanh(int maDiemdanh) {
        this.maDiemdanh = maDiemdanh;
    }

    public int getMaDanhsach() {
        return maDanhsach;
    }

    public void setMaDanhsach(int maDanhsach) {
        this.maDanhsach = maDanhsach;
    }

    public int getMaLichhoc() {
        return maLichhoc;
    }

    public void setMaLichhoc(int maLichhoc) {
        this.maLichhoc = maLichhoc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "DiemDanh{" +
                "maDiemdanh=" + maDiemdanh +
                ", maDanhsach=" + maDanhsach +
                ", maLichhoc=" + maLichhoc +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
