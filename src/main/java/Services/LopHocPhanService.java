package Services;

import Models.LopHocPhan;
import Repository.LopHocPhanRepository;

import java.util.Arrays;
import java.util.List;

public class LopHocPhanService {

    private final LopHocPhanRepository repo;

    public LopHocPhanService() {
        this(new LopHocPhanRepository());
    }

    LopHocPhanService(LopHocPhanRepository repo) {
        this.repo = repo;
    }

    public List<LopHocPhan> findByStudent(int maNguoiDung) {
        return repo.findByStudent(maNguoiDung);
    }
    
    // ... (rest of the existing methods)

    public List<LopHocPhan> getAll(Integer maHocKy, Integer maGiangVien) {
        return repo.getAll(maHocKy, maGiangVien);
    }

    public LopHocPhan findById(int id) {
        return repo.findById(id);
    }

    public int create(LopHocPhan lhp) throws AcademicException {
        validateBase(lhp);
        int id = repo.insert(lhp);
        if (id <= 0) {
            throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Tạo lớp học phần thất bại.");
        }
        return id;
    }

    public void update(LopHocPhan lhp) throws AcademicException {
        if (lhp.getMaLophocphan() <= 0) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                    "Thiếu mã lớp học phần để cập nhật.");
        }
        LopHocPhan existing = repo.findById(lhp.getMaLophocphan());
        if (existing == null) {
            throw new AcademicException(AcademicException.Code.NOT_FOUND,
                    "Không tìm thấy lớp học phần để cập nhật.");
        }
        validateBase(lhp);
        if (existing.getMaMonhoc() != lhp.getMaMonhoc()
                && (repo.hasStudents(lhp.getMaLophocphan()) || repo.hasScores(lhp.getMaLophocphan()))) {
            throw new AcademicException(AcademicException.Code.CONFLICT,
                    "Không thể đổi môn học khi lớp học phần đã có sinh viên hoặc điểm.");
        }
        if (existing.getMaHocky() != lhp.getMaHocky()
                && (repo.hasSchedules(lhp.getMaLophocphan()) || repo.hasExams(lhp.getMaLophocphan()))) {
            throw new AcademicException(AcademicException.Code.CONFLICT,
                    "Không thể đổi học kỳ khi lớp học phần đã có lịch học hoặc lịch thi.");
        }
        if (existing.getMaGiangvien() != lhp.getMaGiangvien()
                && repo.teacherChangeConflicts(lhp.getMaLophocphan(), lhp.getMaGiangvien())) {
            throw new AcademicException(AcademicException.Code.CONFLICT,
                    "Không thể đổi giảng viên vì lịch hiện tại của lớp bị trùng lịch với giảng viên mới.");
        }
        if (!repo.update(lhp)) {
            throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Cập nhật lớp học phần thất bại.");
        }
    }

    public void delete(int id) throws AcademicException {
        LopHocPhan lhp = repo.findById(id);
        if (lhp == null) {
            throw new AcademicException(AcademicException.Code.NOT_FOUND,
                    "Không tìm thấy lớp học phần để xóa.");
        }
        String constraint = repo.getDeleteConstraint(id);
        if (constraint != null) {
            throw new AcademicException(AcademicException.Code.CONFLICT,
                    "Không thể xóa lớp học phần vì " + constraint + ".");
        }
        if (!repo.delete(id)) {
            throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Xóa lớp học phần thất bại.");
        }
    }

    private void validateBase(LopHocPhan lhp) throws AcademicException {
        if (lhp.getMaMonhoc() <= 0 || lhp.getMaGiangvien() <= 0 || lhp.getMaHocky() <= 0) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                    "Vui lòng chọn môn học, giảng viên và học kỳ.");
        }
        if (!repo.termExists(lhp.getMaHocky())) {
            throw new AcademicException(AcademicException.Code.NOT_FOUND,
                    "Học kỳ không tồn tại.");
        }
        if (!Arrays.asList("MoLop", "DangHoc", "HoanThanh", "Huy").contains(lhp.getTrangThai())) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                    "Trạng thái lớp học phần không hợp lệ.");
        }
        if (!repo.isTeacherEligible(lhp.getMaGiangvien(), lhp.getMaMonhoc())) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                    "Giảng viên không hoạt động hoặc không đúng chuyên ngành môn học.");
        }
    }
}
