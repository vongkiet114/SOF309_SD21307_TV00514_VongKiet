package Services;

import Models.LopHocPhan;
import Models.SinhVien;
import Repository.DanhSachLopHocPhanRepository;

import java.util.List;

public class DanhSachLopHocPhanService {
    private final DanhSachLopHocPhanRepository repo;

    public DanhSachLopHocPhanService() {
        this(new DanhSachLopHocPhanRepository());
    }

    DanhSachLopHocPhanService(DanhSachLopHocPhanRepository repo) {
        this.repo = repo;
    }

    public LopHocPhan getSection(int sectionId) {
        return repo.getSection(sectionId);
    }

    public List<SinhVien> getMembers(int sectionId) {
        return repo.getMembers(sectionId);
    }

    public List<SinhVien> findCandidates(int sectionId, String keyword) {
        return repo.findCandidates(sectionId, keyword == null ? "" : keyword.trim());
    }

    public void addStudent(int sectionId, int studentId) throws AcademicException {
        validateIds(sectionId, studentId);
        DanhSachLopHocPhanRepository.AddResult result = repo.addAtomically(sectionId, studentId);
        switch (result) {
            case ADDED:
                return;
            case SECTION_NOT_FOUND:
                throw new AcademicException(AcademicException.Code.NOT_FOUND,
                        "Không tìm thấy lớp học phần.");
            case SECTION_CLOSED:
                fail("Chỉ được thêm sinh viên khi lớp đang Mở lớp hoặc Đang học.");
                return;
            case STUDENT_NOT_ACTIVE:
                fail("Sinh viên không tồn tại hoặc không còn trạng thái Đang học.");
                return;
            case WRONG_SPECIALIZATION:
                fail("Sinh viên không cùng chuyên ngành với môn học.");
                return;
            case ALREADY_ENROLLED:
                fail("Sinh viên đã có trong lớp học phần này.");
                return;
            case DUPLICATE_SUBJECT:
                fail("Sinh viên đã tham gia một lớp học phần khác của cùng môn.");
                return;
            case SCHEDULE_CONFLICT:
                fail("Không thể thêm vì sinh viên bị trùng lịch học hoặc lịch thi.");
                return;
            default:
                throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                        "Không thể thêm sinh viên vào lớp học phần.");
        }
    }

    public void removeStudent(int sectionId, int membershipId) throws AcademicException {
        validateIds(sectionId, membershipId);
        DanhSachLopHocPhanRepository.RemoveResult result =
                repo.removeAtomically(sectionId, membershipId);
        switch (result) {
            case REMOVED:
                return;
            case MEMBERSHIP_NOT_FOUND:
                throw new AcademicException(AcademicException.Code.NOT_FOUND,
                        "Không tìm thấy sinh viên trong lớp học phần.");
            case HAS_GRADES:
                throw new AcademicException(AcademicException.Code.CONFLICT,
                        "Không thể gỡ sinh viên vì đã có điểm.");
            case HAS_ATTENDANCE:
                throw new AcademicException(AcademicException.Code.CONFLICT,
                        "Không thể gỡ sinh viên vì đã có dữ liệu điểm danh.");
            default:
                throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                        "Không thể gỡ sinh viên khỏi lớp học phần.");
        }
    }

    private static void validateIds(int sectionId, int relatedId) throws AcademicException {
        if (sectionId <= 0 || relatedId <= 0) {
            throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                    "Mã lớp học phần hoặc sinh viên không hợp lệ.");
        }
    }

    private static void fail(String message) throws AcademicException {
        throw new AcademicException(AcademicException.Code.INVALID_INPUT, message);
    }
}
