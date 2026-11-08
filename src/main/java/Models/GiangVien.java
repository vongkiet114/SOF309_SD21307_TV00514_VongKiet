package Models;

public class GiangVien {

    private int maNguoidung;
    private int maChuyenNganh;
    private String hocVi;
    private String gioiTinh;
    private String ngaySinh;
    private String diaChi;
    private String hinhAnh;

    // Transient fields for display purposes (from JOINs)
    private String hoTen;
    private String email;
    private String tenChuyenNganh;
    private String tenKhoa;


    public GiangVien() {
    }

    public GiangVien(int maNguoidung, int maChuyenNganh, String hocVi) {
        this.maNguoidung = maNguoidung;
        this.maChuyenNganh = maChuyenNganh;
        this.hocVi = hocVi;
    }

    public int getMaNguoidung() {
        return maNguoidung;
    }

    public void setMaNguoidung(int maNguoidung) {
        this.maNguoidung = maNguoidung;
    }

    public int getMaChuyenNganh() {
        return maChuyenNganh;
    }

    public void setMaChuyenNganh(int maChuyenNganh) {
        this.maChuyenNganh = maChuyenNganh;
    }

    public String getHocVi() {
        return hocVi;
    }

    public void setHocVi(String hocVi) {
        this.hocVi = hocVi;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenChuyenNganh() {
        return tenChuyenNganh;
    }

    // Fallback getters for JSP compatibility
    public int getMaKhoa() { return 0; }
    public int getMaGiangvien() { return maNguoidung; }
    public String getTrangThai() { return "DangLam"; }

    public void setTenChuyenNganh(String tenChuyenNganh) {
        this.tenChuyenNganh = tenChuyenNganh;
    }

    public String getTenKhoa() {
        return tenKhoa;
    }

    public void setTenKhoa(String tenKhoa) {
        this.tenKhoa = tenKhoa;
    }

    @Override
    public String toString() {
        return "GiangVien{" +
                "maNguoidung=" + maNguoidung +
                ", maChuyenNganh=" + maChuyenNganh +
                ", hocVi='" + hocVi + '\'' +
                '}';
    }
}
