package Models;

import java.math.BigDecimal;

public class ThanhPhanDiem {

    private int maThanhphan;
    private String tenThanhphan;
    private BigDecimal trongSo;
    private int maMonhoc;

    public ThanhPhanDiem() {
    }

    public ThanhPhanDiem(int maThanhphan, String tenThanhphan, BigDecimal trongSo, int maMonhoc) {
        this.maThanhphan = maThanhphan;
        this.tenThanhphan = tenThanhphan;
        this.trongSo = trongSo;
        this.maMonhoc = maMonhoc;
    }

    public int getMaThanhphan() {
        return maThanhphan;
    }

    public void setMaThanhphan(int maThanhphan) {
        this.maThanhphan = maThanhphan;
    }

    public String getTenThanhphan() {
        return tenThanhphan;
    }

    public void setTenThanhphan(String tenThanhphan) {
        this.tenThanhphan = tenThanhphan;
    }

    public BigDecimal getTrongSo() {
        return trongSo;
    }

    public void setTrongSo(BigDecimal trongSo) {
        this.trongSo = trongSo;
    }

    public int getMaMonhoc() {
        return maMonhoc;
    }

    public void setMaMonhoc(int maMonhoc) {
        this.maMonhoc = maMonhoc;
    }

    @Override
    public String toString() {
        return "ThanhPhanDiem{" +
                "maThanhphan=" + maThanhphan +
                ", tenThanhphan='" + tenThanhphan + '\'' +
                ", trongSo=" + trongSo +
                ", maMonhoc=" + maMonhoc +
                '}';
    }
}
