package Services;

import Models.SinhVien;
import Models.TaiKhoan;
import Models.VaiTroHeThong;
import Repository.SinhVienRepository;
import Repository.TaiKhoanRepository;
import Utils.Validator;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SinhVienService {

    private final SinhVienRepository svRepo;
    private final TaiKhoanRepository tkRepo;
    private final TaiKhoanService tkService;

    public SinhVienService() {
        this(new SinhVienRepository(), new TaiKhoanRepository(), new TaiKhoanService());
    }

    SinhVienService(SinhVienRepository svRepo,
                    TaiKhoanRepository tkRepo,
                    TaiKhoanService tkService) {
        this.svRepo = svRepo;
        this.tkRepo = tkRepo;
        this.tkService = tkService;
    }

    public long count(String keyword, Integer maChuyenNganh, Integer maLop) {
        return svRepo.count(keyword, maChuyenNganh, maLop);
    }

    // ... (rest of the existing methods)
    
    public List<SinhVien> search(String keyword, Integer maChuyenNganh, Integer maLop, int page, int pageSize) {
        return svRepo.search(keyword, maChuyenNganh, maLop, page, pageSize);
    }
    
    public List<SinhVien> dsSinhVien(Integer maChuyenNganh, Integer maLop) {
        return svRepo.getAll(maChuyenNganh, maLop);
    }

    public SinhVien findByMaNguoidung(int maNguoidung) {
        return svRepo.findByMaNguoidung(maNguoidung);
    }

    public int create(SinhVien sv) throws Exception {
        // Ma so sinh vien duoc he thong sinh tu dong (SV001, SV002...), bo qua gia tri tu form.
        sv.setMssv(svRepo.nextMssv());
        validate(sv, true);

        int maNguoidung = tkService.themTaiKhoan(
                sv.getEmail(), sv.getHoTen(), VaiTroHeThong.STUDENT);
        sv.setMaNguoidung(maNguoidung);

        if (!svRepo.insert(sv)) {
            tkRepo.delete(maNguoidung); 
            throw new Exception("Không thể tạo hồ sơ sinh viên.");
        }

        return maNguoidung;
    }

    public void update(SinhVien sv) throws Exception {
        if (sv.getMaNguoidung() <= 0) {
            throw new Exception("Thiếu mã người dùng để cập nhật.");
        }
        validate(sv, false);

        TaiKhoan tk = tkRepo.findByMaNguoidung(sv.getMaNguoidung());
        if (tk == null) {
            throw new Exception("Không tìm thấy tài khoản tương ứng.");
        }
        tk.setHoTen(sv.getHoTen());
        if (!tkRepo.update(tk)) {
            throw new Exception("Cập nhật thông tin tài khoản thất bại.");
        }

        if (!svRepo.update(sv)) {
            throw new Exception("Cập nhật thông tin sinh viên thất bại.");
        }
    }

    public void delete(int maNguoidung) throws Exception {
        SinhVien sv = svRepo.findByMaNguoidung(maNguoidung);
        if (sv == null) {
            throw new Exception("Không tìm thấy sinh viên cần xóa.");
        }

        if (!tkRepo.delete(maNguoidung)) {
            throw new Exception("Xóa tài khoản sinh viên thất bại.");
        }
    }

    private void validate(SinhVien sv, boolean isNew) throws Exception {
        if (sv.getMaLop() <= 0) {
            throw new Exception("Phải chọn lớp cho sinh viên.");
        }
        if (!Validator.khongRong(sv.getHoTen())) {
            throw new Exception("Họ tên không được để trống.");
        }
        if (!Validator.khongRong(sv.getMssv())) {
            throw new Exception("Mã số sinh viên không được để trống.");
        }

        // Kiem tra do tuoi: chi cho phep luu khi sinh vien tu du 16 tuoi tro len.
        kiemTraTuoiToiThieu(sv.getNgaySinh(), isNew);

        if (isNew) {
            if (!Validator.khongRong(sv.getEmail()) || !Validator.laEmail(sv.getEmail())) {
                throw new Exception("Email không hợp lệ.");
            }
            if (tkRepo.findByEmail(sv.getEmail().trim()) != null) {
                throw new Exception("Email đã tồn tại: " + sv.getEmail());
            }
            if (svRepo.findByMssv(sv.getMssv().trim()) != null) {
                throw new Exception("MSSV đã tồn tại: " + sv.getMssv());
            }
        }
    }

    private static final int TUOI_TOI_THIEU = 16;

    // Sinh vien phai tu du 16 tuoi tro len (tinh den thoi diem hien tai).
    private void kiemTraTuoiToiThieu(String ngaySinh, boolean batBuoc) throws Exception {
        if (!Validator.khongRong(ngaySinh)) {
            if (batBuoc) {
                throw new Exception("Vui lòng nhập ngày sinh của sinh viên.");
            }
            return;
        }
        LocalDate ngay;
        try {
            ngay = LocalDate.parse(ngaySinh.trim());
        } catch (DateTimeParseException e) {
            throw new Exception("Ngày sinh không hợp lệ.");
        }
        LocalDate homNay = LocalDate.now();
        if (ngay.isAfter(homNay)) {
            throw new Exception("Ngày sinh không hợp lệ (không thể ở tương lai).");
        }
        int tuoi = Period.between(ngay, homNay).getYears();
        if (tuoi < TUOI_TOI_THIEU) {
            throw new Exception("Sinh viên phải từ đủ " + TUOI_TOI_THIEU
                    + " tuổi trở lên (hiện tại " + tuoi + " tuổi).");
        }
    }
}
