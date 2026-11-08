package Models;

import java.time.LocalDateTime;

public class ThongBao {

    private int maThongbao;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime ngayTao;
    private boolean daDoc; // Transient
    private String nguoiGui; // Transient
    private String phamVi; // Transient

    public ThongBao() {
    }

    public int getMaThongbao() {
        return maThongbao;
    }

    public void setMaThongbao(int maThongbao) {
        this.maThongbao = maThongbao;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    // Fallback getter for JSP compatibility
    public LocalDateTime getNgayGui() { return ngayTao; }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public void setDaDoc(boolean daDoc) {
        this.daDoc = daDoc;
    }

    public String getNguoiGui() {
        return nguoiGui;
    }

    public void setNguoiGui(String nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public String getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(String phamVi) {
        this.phamVi = phamVi;
    }
}
