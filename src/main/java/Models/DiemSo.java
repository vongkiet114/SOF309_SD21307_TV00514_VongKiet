package Models;

import java.math.BigDecimal;

public class DiemSo {

    private int maDiem;
    private int maDanhsach;
    private int maThanhphan;
    private BigDecimal giaTriDiem;

    // Transient fields for display
    private String tenThanhPhan;
    private BigDecimal trongSo;

    public DiemSo() {
    }

    public DiemSo(int maDiem, int maDanhsach, int maThanhphan, BigDecimal giaTriDiem) {
        this.maDiem = maDiem;
        this.maDanhsach = maDanhsach;
        this.maThanhphan = maThanhphan;
        this.giaTriDiem = giaTriDiem;
    }

    public int getMaDiem() {
        return maDiem;
    }

    public void setMaDiem(int maDiem) {
        this.maDiem = maDiem;
    }

    public int getMaDanhsach() {
        return maDanhsach;
    }

    public void setMaDanhsach(int maDanhsach) {
        this.maDanhsach = maDanhsach;
    }

    public int getMaThanhphan() {
        return maThanhphan;
    }

    public void setMaThanhphan(int maThanhphan) {
        this.maThanhphan = maThanhphan;
    }

    public BigDecimal getGiaTriDiem() {
        return giaTriDiem;
    }

    public void setGiaTriDiem(BigDecimal giaTriDiem) {
        this.giaTriDiem = giaTriDiem;
    }

    public String getTenThanhPhan() {
        return tenThanhPhan;
    }

    public void setTenThanhPhan(String tenThanhPhan) {
        this.tenThanhPhan = tenThanhPhan;
    }

    public BigDecimal getTrongSo() {
        return trongSo;
    }

    public void setTrongSo(BigDecimal trongSo) {
        this.trongSo = trongSo;
    }

    @Override
    public String toString() {
        return "DiemSo{" +
                "maDiem=" + maDiem +
                ", maDanhsach=" + maDanhsach +
                ", maThanhphan=" + maThanhphan +
                ", giaTriDiem=" + giaTriDiem +
                '}';
    }
}
