package Services;

import Models.LichHoc;
import Models.ScheduleConflict;
import Models.ScheduleWriteResult;
import Repository.LichHocRepository;
import Repository.ScheduleCommandRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LichHocService {
    private static final Set<String> VALID_TYPES = new HashSet<>(
            Arrays.asList("HocChinh", "DayBu"));
    private static final Set<String> VALID_STATUSES = new HashSet<>(
            Arrays.asList("BinhThuong", "XinNghi", "DaHuy"));

    private final LichHocRepository repo;
    private final ScheduleCommandRepository writer;

    public LichHocService() {
        this(new LichHocRepository(), new ScheduleCommandRepository());
    }

    public LichHocService(LichHocRepository repo, ScheduleCommandRepository writer) {
        this.repo = repo;
        this.writer = writer;
    }

    public List<LichHoc> findByNguoiDung(int maNguoiDung, String vaiTro) {
        return repo.findByNguoiDung(maNguoiDung, vaiTro);
    }

    public List<LichHoc> getAll() {
        return repo.getAll();
    }

    public LichHoc findById(int id) {
        return repo.findById(id);
    }

    public List<LichHoc> findByLopHocPhan(int maLopHocPhan) {
        return repo.findByLopHocPhan(maLopHocPhan);
    }

    public int create(LichHoc value) throws AcademicException {
        validate(value);
        return savedId(writer.saveClass(value));
    }

    public void update(LichHoc value) throws AcademicException {
        if (value == null || value.getMaLichhoc() <= 0) {
            throw invalid("Thiếu mã lịch học để cập nhật.");
        }
        validate(value);
        savedId(writer.saveClass(value));
    }

    public void delete(int id) throws AcademicException {
        if (id <= 0) {
            throw invalid("Mã lịch học không hợp lệ.");
        }
        if (repo.hasAttendance(id)) {
            throw new AcademicException(AcademicException.Code.DEPENDENCY,
                    "Buổi học đã có điểm danh; hãy chuyển trạng thái sang Đã hủy thay vì xóa.");
        }
        if (!repo.delete(id)) {
            throw new AcademicException(AcademicException.Code.NOT_FOUND,
                    "Không tìm thấy buổi học cần xóa.");
        }
    }

    private int savedId(ScheduleWriteResult result) throws AcademicException {
        if (result == null) {
            throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Không thể lưu lịch học.");
        }
        if (!result.isSaved()) {
            throw conflict(result.getConflict());
        }
        return result.getId();
    }

    private void validate(LichHoc value) throws AcademicException {
        if (value == null) {
            throw invalid("Thông tin lịch học không hợp lệ.");
        }
        if (value.getMaLophocphan() <= 0) {
            throw invalid("Vui lòng chọn lớp học phần.");
        }
        if (value.getMaPhong() <= 0 || value.getMaCa() <= 0) {
            throw invalid("Vui lòng chọn phòng và ca học.");
        }
        if (value.getNgayHoc() == null) {
            throw invalid("Vui lòng chọn ngày học.");
        }
        if (!VALID_TYPES.contains(value.getLoaiLich())) {
            throw invalid("Loại buổi học không hợp lệ.");
        }
        if (!VALID_STATUSES.contains(value.getTrangThai())) {
            throw invalid("Trạng thái buổi học không hợp lệ.");
        }
    }

    private AcademicException conflict(ScheduleConflict value) {
        if (value == null) {
            return new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Không thể lưu lịch học.");
        }
        String suffix = value.getExistingSectionCode() == null
                || value.getExistingSectionCode().trim().isEmpty()
                ? "." : " với lớp " + value.getExistingSectionCode() + ".";
        switch (value.getKind()) {
            case ROOM:
                return new AcademicException(AcademicException.Code.CONFLICT_ROOM,
                        "Phòng đã có lịch trong ngày và ca này" + suffix);
            case TEACHER:
                return new AcademicException(AcademicException.Code.CONFLICT_TEACHER,
                        "Giảng viên đã có lịch trong ngày và ca này" + suffix);
            case STUDENT:
                return new AcademicException(AcademicException.Code.CONFLICT_STUDENT,
                        "Có sinh viên đã có lịch trong ngày và ca này" + suffix);
            case DUPLICATE_EXAM:
                return new AcademicException(AcademicException.Code.DUPLICATE_EXAM,
                        "Lớp học phần đã có kỳ thi cùng loại.");
            default:
                return new AcademicException(AcademicException.Code.EXAM_ORDER,
                        "Giữa kỳ phải trước Cuối kỳ.");
        }
    }

    private AcademicException invalid(String message) {
        return new AcademicException(AcademicException.Code.INVALID_INPUT, message);
    }
}
