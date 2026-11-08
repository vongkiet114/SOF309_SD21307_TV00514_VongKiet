package Services;

import Models.KyThi;
import Models.ScheduleConflict;
import Models.ScheduleWriteResult;
import Repository.KyThiRepository;
import Repository.ScheduleCommandRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KyThiService {
    private static final Set<String> VALID_TYPES = new HashSet<>(
            Arrays.asList("GiuaKy", "CuoiKy", "BaoVe"));

    private final KyThiRepository repo;
    private final ScheduleCommandRepository writer;

    public KyThiService() {
        this(new KyThiRepository(), new ScheduleCommandRepository());
    }

    public KyThiService(KyThiRepository repo, ScheduleCommandRepository writer) {
        this.repo = repo;
        this.writer = writer;
    }

    public List<KyThi> findByNguoiDung(int maNguoiDung, String vaiTro) {
        return repo.findByNguoiDung(maNguoiDung, vaiTro);
    }

    public List<KyThi> getAll() {
        return repo.getAll();
    }

    public List<KyThi> findByLopHocPhan(int maLopHocPhan) {
        return repo.findByLopHocPhan(maLopHocPhan);
    }

    public KyThi findById(int id) {
        return repo.findById(id);
    }

    public int create(KyThi value) throws AcademicException {
        validate(value);
        return savedId(writer.saveExam(value));
    }

    public void update(KyThi value) throws AcademicException {
        if (value == null || value.getMaKythi() <= 0) {
            throw invalid("Thiếu mã kỳ thi để cập nhật.");
        }
        validate(value);
        savedId(writer.saveExam(value));
    }

    public void delete(int id) throws AcademicException {
        if (id <= 0) {
            throw invalid("Mã kỳ thi không hợp lệ.");
        }
        if (!repo.delete(id)) {
            throw new AcademicException(AcademicException.Code.NOT_FOUND,
                    "Không tìm thấy kỳ thi cần xóa.");
        }
    }

    private int savedId(ScheduleWriteResult result) throws AcademicException {
        if (result == null) {
            throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Không thể lưu lịch thi.");
        }
        if (!result.isSaved()) {
            throw conflict(result.getConflict());
        }
        return result.getId();
    }

    private void validate(KyThi value) throws AcademicException {
        if (value == null) {
            throw invalid("Thông tin lịch thi không hợp lệ.");
        }
        if (value.getMaLophocphan() <= 0) {
            throw invalid("Vui lòng chọn lớp học phần.");
        }
        if (value.getMaPhong() <= 0 || value.getMaCa() <= 0) {
            throw invalid("Vui lòng chọn phòng và ca thi.");
        }
        if (value.getNgayThi() == null) {
            throw invalid("Vui lòng chọn ngày thi.");
        }
        if (!VALID_TYPES.contains(value.getLoaiKythi())) {
            throw invalid("Loại kỳ thi không hợp lệ.");
        }
    }

    private AcademicException conflict(ScheduleConflict value) {
        if (value == null) {
            return new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Không thể lưu lịch thi.");
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
