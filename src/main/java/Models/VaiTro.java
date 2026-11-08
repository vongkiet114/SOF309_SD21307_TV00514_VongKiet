package Models;

// Model cho bang vaitro
public class VaiTro {

    private int maVaitro;
    private String tenVaitro;

    public VaiTro() {}

    public VaiTro(int maVaitro, String tenVaitro) {
        this.maVaitro = maVaitro;
        this.tenVaitro = tenVaitro;
    }

    public int getMaVaitro() { return maVaitro; }
    public void setMaVaitro(int maVaitro) { this.maVaitro = maVaitro; }

    public String getTenVaitro() { return tenVaitro; }
    public void setTenVaitro(String tenVaitro) { this.tenVaitro = tenVaitro; }
}
