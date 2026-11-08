package Services;

import Models.GiangVien;
import Models.TaiKhoan;
import Models.VaiTroHeThong;
import Repository.GiangVienRepository;
import Repository.TaiKhoanRepository;
import Utils.Validator;

import java.util.List;

public class GiangVienService {

    private final GiangVienRepository gvRepo;
    private final TaiKhoanRepository tkRepo;
    private final TaiKhoanService tkService;

    public GiangVienService() {
        this(new GiangVienRepository(), new TaiKhoanRepository(), new TaiKhoanService());
    }

    GiangVienService(GiangVienRepository gvRepo,
                     TaiKhoanRepository tkRepo,
                     TaiKhoanService tkService) {
        this.gvRepo = gvRepo;
        this.tkRepo = tkRepo;
        this.tkService = tkService;
    }

    public List<GiangVien> dsGiangVien() {
        return gvRepo.getAll();
    }

    public List<GiangVien> search(String keyword, Integer maChuyenNganh, String hocVi) {
        return gvRepo.search(keyword, maChuyenNganh, hocVi);
    }

    public GiangVien findByMaNguoidung(int maNguoidung) {
        return gvRepo.findByMaNguoidung(maNguoidung);
    }

    public List<TaiKhoan> getPotentialUsersForTeacherRole() {
        return gvRepo.getPotentialUsersForTeacherRole();
    }

    /**
     * Creates a new teacher. This involves two steps:
     * 1. Create a user account (TaiKhoan).
     * 2. Create the teacher profile (GiangVien) linked to the user account.
     *
     * @param gv The GiangVien object containing all necessary info (including hoTen, email for the user account).
     * @return The ma_nguoidung of the new teacher.
     * @throws Exception if validation or creation fails.
     */
    public int create(GiangVien gv) throws Exception {
        validate(gv, true);

        // 1. Create the TaiKhoan first
        int maNguoidung = tkService.themTaiKhoan(
                gv.getEmail(), gv.getHoTen(), VaiTroHeThong.TEACHER);
        gv.setMaNguoidung(maNguoidung);

        // 2. Create the GiangVien
        if (!gvRepo.insert(gv)) {
            // If teacher creation fails, roll back the user creation.
            tkRepo.delete(maNguoidung);
            throw new Exception("Không thể tạo hồ sơ giảng viên.");
        }

        return maNguoidung;
    }

    /**
     * Updates a teacher's information.
     *
     * @param gv The GiangVien object with updated data.
     * @throws Exception if validation or update fails.
     */
    public void update(GiangVien gv) throws Exception {
        if (gv.getMaNguoidung() <= 0) {
            throw new Exception("Thiếu mã người dùng để cập nhật.");
        }
        validate(gv, false);

        // Update TaiKhoan info
        TaiKhoan tk = tkRepo.findByMaNguoidung(gv.getMaNguoidung());
        if (tk == null) {
            throw new Exception("Không tìm thấy tài khoản tương ứng.");
        }
        tk.setHoTen(gv.getHoTen());
        if (!tkRepo.update(tk)) {
            throw new Exception("Cập nhật thông tin tài khoản thất bại.");
        }

        // Update GiangVien info
        if (!gvRepo.update(gv)) {
            throw new Exception("Cập nhật thông tin giảng viên thất bại.");
        }
    }

    /**
     * Deletes a teacher.
     *
     * @param maNguoidung The ID of the user to delete.
     * @throws Exception if constraints are not met or deletion fails.
     */
    public void delete(int maNguoidung) throws Exception {
        GiangVien gv = gvRepo.findByMaNguoidung(maNguoidung);
        if (gv == null) {
            throw new Exception("Không tìm thấy giảng viên cần xóa.");
        }

        String constraint = gvRepo.getDeleteConstraint(maNguoidung);
        if (constraint != null) {
            throw new Exception("Không thể xóa giảng viên: " + constraint);
        }

        if (!tkRepo.delete(maNguoidung)) {
            throw new Exception("Xóa tài khoản giảng viên thất bại.");
        }
    }

    private void validate(GiangVien gv, boolean isNew) throws Exception {
        if (gv.getMaChuyenNganh() <= 0) {
            throw new Exception("Vui lòng chọn chuyên ngành.");
        }
        if (!Validator.khongRong(gv.getHoTen())) {
            throw new Exception("Họ tên không được để trống.");
        }

        if (isNew) {
            if (!Validator.khongRong(gv.getEmail()) || !Validator.laEmail(gv.getEmail())) {
                throw new Exception("Email không hợp lệ.");
            }
            if (tkRepo.findByEmail(gv.getEmail().trim()) != null) {
                throw new Exception("Email đã tồn tại: " + gv.getEmail());
            }
        }
    }
}
