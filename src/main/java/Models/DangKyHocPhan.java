package Models;

// Model cho bang danhsach_sv_lhp (dang ky hoc phan)
public class DangKyHocPhan {

    private int maDanhsach;
    private int maNguoidung;
    private int maLophocphan;
    private String ghiChu;

    // Transient fields
    private String mssv;
    private String hoTen;
    private String tenMonhoc;
    private String tenLop;

    public DangKyHocPhan() {}

    public int getMaDanhsach() { return maDanhsach; }
    public void setMaDanhsach(int maDanhsach) { this.maDanhsach = maDanhsach; }

    public int getMaNguoidung() { return maNguoidung; }
    public void setMaNguoidung(int maNguoidung) { this.maNguoidung = maNguoidung; }

    public int getMaLophocphan() { return maLophocphan; }
    public void setMaLophocphan(int maLophocphan) { this.maLophocphan = maLophocphan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getMssv() { return mssv; }
    public void setMssv(String mssv) { this.mssv = mssv; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getTenMonhoc() { return tenMonhoc; }
    public void setTenMonhoc(String tenMonhoc) { this.tenMonhoc = tenMonhoc; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }
}
