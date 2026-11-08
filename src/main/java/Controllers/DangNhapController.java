package Controllers;

import Models.TaiKhoan;
import Repository.TaiKhoanRepository;
import Utils.MatKhauUtil;

public class DangNhapController {

    private final TaiKhoanRepository repo;

    public DangNhapController() {
        this(new TaiKhoanRepository());
    }

    DangNhapController(TaiKhoanRepository repo) {
        this.repo = repo;
    }

    /**
     * Handles user login with email and password.
     * @param email The user's email.
     * @param matKhauTho The plain text password.
     * @return TaiKhoan object if login is successful, otherwise null.
     */
    public TaiKhoan dangNhap(String email, String matKhauTho) {
        if (email == null || email.trim().isEmpty() || matKhauTho == null || matKhauTho.isEmpty()) {
            return null;
        }

        TaiKhoan tk = repo.findByEmail(email.trim());

        if (tk == null) {
            return null; // User not found
        }

        if (!tk.isHoatDong()) {
            return null; // Account is inactive
        }

        String storedHash = tk.getMatKhau();
        if (!MatKhauUtil.khop(matKhauTho, storedHash)) {
            return null; // Incorrect password
        }

        if (MatKhauUtil.canNangCap(storedHash)) {
            repo.capNhatMatKhau(tk.getMaNguoidung(), MatKhauUtil.bam(matKhauTho));
        }
        tk.setMatKhau(null); // Do not keep the password hash in the session
        return tk;
    }

    /**
     * Changes the user's password.
     * @param maNguoidung The user's ID.
     * @param matKhauCu The current password.
     * @param matKhauMoi The new password.
     * @param nhapLaiMatKhauMoi The new password confirmation.
     * @throws Exception with a user-friendly message if an error occurs.
     */
    public void doiMatKhau(int maNguoidung, String matKhauCu, String matKhauMoi, String nhapLaiMatKhauMoi) throws Exception {
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new Exception("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (!matKhauMoi.equals(nhapLaiMatKhauMoi)) {
            throw new Exception("Mật khẩu mới nhập lại không khớp.");
        }

        TaiKhoan tk = repo.findByMaNguoidung(maNguoidung);
        if (tk == null) {
            throw new Exception("Không tìm thấy tài khoản.");
        }

        // If the account already has a password, the current password must be correct.
        if (tk.getMatKhau() != null && !tk.getMatKhau().isEmpty() && !MatKhauUtil.khop(matKhauCu, tk.getMatKhau())) {
            throw new Exception("Mật khẩu hiện tại không đúng.");
        }

        boolean ok = repo.capNhatMatKhau(maNguoidung, MatKhauUtil.bam(matKhauMoi));
        if (!ok) {
            throw new Exception("Cập nhật mật khẩu thất bại, vui lòng thử lại.");
        }
    }
}
