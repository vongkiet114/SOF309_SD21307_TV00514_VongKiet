package Utils;

// lop tien ich xu ly chuoi de in ra html mot cach an toan.
// dung trong cac trang jsp (scriptlet) de chong loi xss (chen ma doc qua du lieu nguoi dung).
public final class Html {

    // chan tao doi tuong vi day la lop tien ich
    private Html() {
    }

    // chuyen cac ky tu dac biet cua html thanh dang an toan de hien thi.
    // vi du dau "<" se thanh "&lt;" nen trinh duyet khong hieu nham la the html.
    public static String escape(Object giaTri) {
        if (giaTri == null) {
            return "";
        }
        String chuoi = String.valueOf(giaTri);
        StringBuilder ketQua = new StringBuilder(chuoi.length());
        for (int i = 0; i < chuoi.length(); i++) {
            char c = chuoi.charAt(i);
            switch (c) {
                case '&':  ketQua.append("&amp;");  break;
                case '<':  ketQua.append("&lt;");   break;
                case '>':  ketQua.append("&gt;");   break;
                case '"':  ketQua.append("&quot;"); break;
                case '\'': ketQua.append("&#39;");  break;
                default:   ketQua.append(c);
            }
        }
        return ketQua.toString();
    }

    // ten ngan goi cho tien khi dung trong jsp
    public static String esc(Object giaTri) {
        return escape(giaTri);
    }
}
