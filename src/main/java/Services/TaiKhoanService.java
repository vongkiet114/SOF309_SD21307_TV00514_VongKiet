package Services;

import Models.TaiKhoan;
import Models.VaiTroHeThong;
import Repository.TaiKhoanRepository;
import Repository.VaiTroRepository;
import Utils.MatKhauUtil;
import Utils.Validator;

import java.util.List;

public class TaiKhoanService {

    private final TaiKhoanRepository repo;
    private final VaiTroRepository vaiTroRepository;

    public static final String MAT_KHAU_MAC_DINH = "123456";

    public TaiKhoanService() {
        this(new TaiKhoanRepository(), new VaiTroRepository());
    }

    TaiKhoanService(TaiKhoanRepository repo, VaiTroRepository vaiTroRepository) {
        this.repo = repo;
        this.vaiTroRepository = vaiTroRepository;
    }

    /**
     * Tạo tài khoản theo mã vai trò ổn định của ứng dụng.
     */
    public int themTaiKhoan(String email, String hoTen, VaiTroHeThong vaiTro) throws Exception {
        Integer maVaitro = vaiTroRepository.findIdByName(vaiTro.name());
        if (maVaitro == null) {
            throw new IllegalStateException("Vai trò không tồn tại: " + vaiTro.name());
        }
        return createAccount(email, hoTen, maVaitro);
    }

    private int createAccount(String email, String hoTen, int maVaitro) throws Exception {
        if (!Validator.khongRong(email) || !Validator.laEmail(email.trim())) {
            throw new Exception("Email không hợp lệ.");
        }
        if (!Validator.khongRong(hoTen)) {
            throw new Exception("Vui lòng nhập họ tên.");
        }
        if (repo.findByEmail(email.trim()) != null) {
            throw new Exception("Email này đã tồn tại trong hệ thống.");
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setEmail(email.trim());
        taiKhoan.setHoTen(hoTen.trim());
        taiKhoan.setMatKhau(MatKhauUtil.bam(MAT_KHAU_MAC_DINH));
        taiKhoan.setMaVaitro(maVaitro);
        taiKhoan.setHoatDong(true);

        int id = repo.insert(taiKhoan);
        if (id <= 0) {
            throw new Exception("Tạo tài khoản thất bại.");
        }
        return id;
    }

    public List<TaiKhoan> dsTaiKhoan() {
        return repo.getAll();
    }

    public TaiKhoan timTheoEmail(String email) {
        return repo.findByEmail(email);
    }

    public TaiKhoan timTheoMa(int maNguoidung) {
        return repo.findByMaNguoidung(maNguoidung);
    }

    public boolean khoa(int maNguoidung) {
        return repo.capNhatTrangThai(maNguoidung, false);
    }

    public boolean moKhoa(int maNguoidung) {
        return repo.capNhatTrangThai(maNguoidung, true);
    }
}
