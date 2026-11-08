package Utils;

import java.time.LocalDate;

/**
 * Tinh trang thai theo ngay cho cac doi tuong co thoi gian (hoc ky, ...).
 * Quy uoc: SapDienRa (chua toi ngay bat dau), DangDienRa (trong khoang),
 *          DaKetThuc (qua ngay ket thuc).
 */
public final class TrangThaiTheoNgay {

    private TrangThaiTheoNgay() {}

    /** Trang thai hoc ky theo ngay hien tai. */
    public static String hocKy(LocalDate batDau, LocalDate ketThuc) {
        LocalDate homNay = LocalDate.now();
        if (batDau != null && homNay.isBefore(batDau)) return "SapDienRa";
        if (ketThuc != null && homNay.isAfter(ketThuc)) return "DaKetThuc";
        return "DangDienRa";
    }

    /** Nhan tieng Viet cho trang thai dang/sap/ket thuc. */
    public static String nhan(String trangThai) {
        if (trangThai == null) return "";
        if ("SapDienRa".equals(trangThai))  return "Sắp diễn ra";
        if ("DangDienRa".equals(trangThai)) return "Đang diễn ra";
        if ("DaKetThuc".equals(trangThai))  return "Đã kết thúc";
        if ("MoLop".equals(trangThai))      return "Mở lớp";
        if ("DangHoc".equals(trangThai))   return "Đang học";
        if ("HoanThanh".equals(trangThai)) return "Hoàn thành";
        if ("Huy".equals(trangThai))       return "Đã hủy";
        if ("BaoLuu".equals(trangThai))    return "Bảo lưu";
        if ("NghiHoc".equals(trangThai))   return "Nghỉ học";
        if ("TotNghiep".equals(trangThai)) return "Tốt nghiệp";
        if ("BinhThuong".equals(trangThai)) return "Bình thường";
        if ("XinNghi".equals(trangThai))    return "Xin nghỉ";
        if ("DaHuy".equals(trangThai))      return "Đã hủy";
        return trangThai;
    }
}
