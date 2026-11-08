package Models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BangDiemHocPhan {
    private int maLopHocPhan;
    private String maLhp;
    private String tenMonHoc;
    private List<ThanhPhanDiem> thanhPhanDiems = new ArrayList<>();
    private List<DiemSo> diemSoList = new ArrayList<>();
    private BigDecimal diemTongKet;

    public int getMaLopHocPhan() {
        return maLopHocPhan;
    }

    public void setMaLopHocPhan(int maLopHocPhan) {
        this.maLopHocPhan = maLopHocPhan;
    }

    public String getMaLhp() {
        return maLhp;
    }

    public void setMaLhp(String maLhp) {
        this.maLhp = maLhp;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public List<ThanhPhanDiem> getThanhPhanDiems() {
        return thanhPhanDiems;
    }

    public void setThanhPhanDiems(List<ThanhPhanDiem> thanhPhanDiems) {
        this.thanhPhanDiems = thanhPhanDiems;
    }

    public List<DiemSo> getDiemSoList() {
        return diemSoList;
    }

    public void setDiemSoList(List<DiemSo> diemSoList) {
        this.diemSoList = diemSoList;
    }

    public BigDecimal getDiemTongKet() {
        return diemTongKet;
    }

    public void setDiemTongKet(BigDecimal diemTongKet) {
        this.diemTongKet = diemTongKet;
    }
}
