package Repository;

import Models.Quyen;
import Models.VaiTro;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuyenRepository {

    public List<VaiTro> dsVaiTro() {
        return new ArrayList<>();
    }

    public List<Quyen> dsVaiTroCuaNguoiDung(int maNguoidung) {
        return new ArrayList<>();
    }

    public Map<Integer, List<String>> tenVaiTroTheoNguoiDung() {
        return new HashMap<>();
    }

    public boolean ganVaiTro(int maNguoidung, int maVaitro) {
        return false;
    }

    public boolean goVaiTro(int maNguoidung, int maVaitro) {
        return false;
    }
}
