package Models;

public class TaiKhoan {

    private int maNguoidung;
    private String email;
    private String matKhau;
    private String hoTen;
    private int maVaitro;
    private boolean hoatDong;

    // Transient fields for display purposes (from JOINs)
    private String tenVaiTro;
    private String maSo;

    public TaiKhoan() {
    }

    public TaiKhoan(int maNguoidung, String email, String matKhau, String hoTen, int maVaitro, boolean hoatDong) {
        this.maNguoidung = maNguoidung;
        this.email = email;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.maVaitro = maVaitro;
        this.hoatDong = hoatDong;
    }

    public int getMaNguoidung() {
        return maNguoidung;
    }

    public void setMaNguoidung(int maNguoidung) {
        this.maNguoidung = maNguoidung;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public int getMaVaitro() {
        return maVaitro;
    }

    public void setMaVaitro(int maVaitro) {
        this.maVaitro = maVaitro;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public String getTenVaiTro() {
        return tenVaiTro;
    }

    // Fallback getters for JSP compatibility
    public String getNccDangnhap() { return "Local"; }
    public String getVaiTro() { return tenVaiTro; }

    public void setTenVaiTro(String tenVaiTro) {
        this.tenVaiTro = tenVaiTro;
    }

    public String getMaSo() {
        return maSo;
    }

    public void setMaSo(String maSo) {
        this.maSo = maSo;
    }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "maNguoidung=" + maNguoidung +
                ", email='" + email + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", maVaitro=" + maVaitro +
                ", hoatDong=" + hoatDong +
                '}';
    }
}
