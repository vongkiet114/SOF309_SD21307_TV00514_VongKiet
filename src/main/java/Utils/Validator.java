package Utils;

import java.util.regex.Pattern;

// lop tien ich kiem tra du lieu dau vao (dung o tang Service/Servlet truoc khi luu xuong CSDL).
// tat ca phuong thuc deu tinh (static) de goi truc tiep: Validator.khongRong(...).
public final class Validator {

    // mau kiem tra email don gian: co ky tu @ va ten mien
    private static final Pattern MAU_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    private Validator() {
    }

    // chuoi khong rong (khac null va khong chi gom khoang trang)
    public static boolean khongRong(String chuoi) {
        return chuoi != null && !chuoi.trim().isEmpty();
    }

    // do dai chuoi nam trong khoang [toiThieu, toiDa]
    public static boolean doDaiTrongKhoang(String chuoi, int toiThieu, int toiDa) {
        if (chuoi == null) return false;
        int doDai = chuoi.trim().length();
        return doDai >= toiThieu && doDai <= toiDa;
    }

    // dung dinh dang email
    public static boolean laEmail(String chuoi) {
        return chuoi != null && MAU_EMAIL.matcher(chuoi.trim()).matches();
    }

    // chuoi la so nguyen
    public static boolean laSoNguyen(String chuoi) {
        if (!khongRong(chuoi)) return false;
        try {
            Integer.parseInt(chuoi.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // gia tri so thuc nam trong khoang [min, max]
    public static boolean trongKhoang(double giaTri, double min, double max) {
        return giaTri >= min && giaTri <= max;
    }

    // so dien thoai viet nam: 10-11 chu so, co the bat dau bang 0 hoac +84
    public static boolean laSoDienThoai(String chuoi) {
        if (!khongRong(chuoi)) return false;
        String s = chuoi.trim().replace(" ", "");
        return s.matches("^(0|\\+84)\\d{9,10}$");
    }
}
