package Utils;

// Dinh dang ma giang vien tu dong tang dan: GV001, GV002, GV003...
// Ma giang vien duoc dan xuat tu ma_nguoidung (khoa chinh IDENTITY tang dan),
// nen chi can dinh dang zero-pad de hien thi thong nhat o moi noi.
public final class MaGiangVien {

    private MaGiangVien() {
    }

    public static String format(int maNguoidung) {
        return String.format("GV%03d", maNguoidung);
    }
}
