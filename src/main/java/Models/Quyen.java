package Models;

// Stub: phan quyen nguoi dung (1 user - 1 role qua cot ma_vaitro trong nguoidung)
public class Quyen {

    private int maNguoidung;
    private int maVaitro;
    private String tenVaitro;

    public Quyen() {}

    public Quyen(int maNguoidung, int maVaitro, String tenVaitro) {
        this.maNguoidung = maNguoidung;
        this.maVaitro = maVaitro;
        this.tenVaitro = tenVaitro;
    }

    public int getMaNguoidung() { return maNguoidung; }
    public void setMaNguoidung(int maNguoidung) { this.maNguoidung = maNguoidung; }

    public int getMaVaitro() { return maVaitro; }
    public void setMaVaitro(int maVaitro) { this.maVaitro = maVaitro; }

    public String getTenVaitro() { return tenVaitro; }
    public void setTenVaitro(String tenVaitro) { this.tenVaitro = tenVaitro; }
}
