package Services;

import Models.DiemDanh;
import Models.SinhVien;
import Repository.DiemDanhRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiemDanhService {
    private final DiemDanhRepository repo;

    public DiemDanhService() {
        this(new DiemDanhRepository());
    }

    public DiemDanhService(DiemDanhRepository repo) {
        this.repo = repo;
    }

    public List<SinhVien> getBangDiemDanh(int teacherId,
                                         int sectionId,
                                         int scheduleId)
            throws AcademicException {
        validateIds(teacherId, sectionId, scheduleId);
        return repo.findDiemDanhForBuoiHoc(teacherId, sectionId, scheduleId);
    }

    public void saveDiemDanh(int teacherId,
                             int sectionId,
                             int scheduleId,
                             List<DiemDanh> rows)
            throws AcademicException {
        saveDiemDanh(teacherId, sectionId, scheduleId, rows,
                LocalDate.now(Clock.systemDefaultZone()));
    }

    public void saveDiemDanh(int teacherId,
                             int sectionId,
                             int scheduleId,
                             List<DiemDanh> rows,
                             LocalDate today)
            throws AcademicException {
        validateIds(teacherId, sectionId, scheduleId);
        if (today == null) {
            throw invalid("Ngày điểm danh không hợp lệ.");
        }
        if (rows == null || rows.isEmpty()) {
            throw invalid("Danh sách điểm danh không được để trống.");
        }

        Set<Integer> memberships = new HashSet<>();
        List<DiemDanh> normalized = new ArrayList<>();
        for (DiemDanh row : rows) {
            if (row == null || row.getMaDanhsach() <= 0) {
                throw invalid("Dữ liệu điểm danh không hợp lệ.");
            }
            if (row.getMaLichhoc() != scheduleId) {
                throw invalid("Buổi học của dòng điểm danh không khớp.");
            }
            if (!memberships.add(row.getMaDanhsach())) {
                throw invalid("Sinh viên trong danh sách điểm danh bị lặp.");
            }
            String state = row.getTrangThai() == null ? "" : row.getTrangThai().trim();
            if (!"CoMat".equals(state) && !"Vang".equals(state)) {
                throw invalid("Trạng thái điểm danh chỉ được là Có mặt hoặc Vắng.");
            }
            DiemDanh value = new DiemDanh();
            value.setMaDanhsach(row.getMaDanhsach());
            value.setMaLichhoc(scheduleId);
            value.setTrangThai(state);
            normalized.add(value);
        }

        DiemDanhRepository.BatchResult result = repo.saveBatch(
                teacherId, sectionId, scheduleId, normalized, today);
        if (result == null) {
            throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                    "Không thể lưu điểm danh.");
        }
        switch (result) {
            case SAVED:
                return;
            case SESSION_NOT_OWNED:
                throw new AcademicException(AcademicException.Code.FORBIDDEN,
                        "Bạn không có quyền điểm danh buổi học này.");
            case SESSION_NOT_NORMAL:
                throw invalid("Chỉ có thể điểm danh buổi học ở trạng thái bình thường.");
            case SESSION_IN_PAST:
                throw invalid("Không thể điểm danh buổi học đã qua.");
            case UNKNOWN_MEMBERSHIP:
                throw invalid("Danh sách có sinh viên không tồn tại.");
            case RELATION_MISMATCH:
                throw invalid("Danh sách có sinh viên không thuộc lớp học phần này.");
            default:
                throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                        "Không thể lưu điểm danh.");
        }
    }

    private void validateIds(int teacherId, int sectionId, int scheduleId)
            throws AcademicException {
        if (teacherId <= 0 || sectionId <= 0 || scheduleId <= 0) {
            throw invalid("Thông tin lớp học phần hoặc buổi học không hợp lệ.");
        }
    }

    private static AcademicException invalid(String message) {
        return new AcademicException(AcademicException.Code.INVALID_INPUT, message);
    }
    public List<SinhVien> getBangDiemDanhAdmin(int sectionId, int scheduleId) throws AcademicException {
        if (sectionId <= 0 || scheduleId <= 0) {
            throw invalid("Thông tin lớp học phần hoặc buổi học không hợp lệ.");
        }
        return repo.findDiemDanhForBuoiHocAdmin(sectionId, scheduleId);
    }

    public void saveDiemDanhAdmin(int sectionId,
                                  int scheduleId,
                                  List<DiemDanh> rows)
            throws AcademicException {
        saveDiemDanhAdmin(sectionId, scheduleId, rows,
                LocalDate.now(Clock.systemDefaultZone()));
    }

    public void saveDiemDanhAdmin(int sectionId,
                                  int scheduleId,
                                  List<DiemDanh> rows,
                                  LocalDate today)
            throws AcademicException {
        if (sectionId <= 0 || scheduleId <= 0) {
            throw invalid("Thông tin lớp học phần hoặc buổi học không hợp lệ.");
        }
        if (today == null) {
            throw invalid("Ngày điểm danh không hợp lệ.");
        }
        if (rows == null || rows.isEmpty()) {
            throw invalid("Danh sách điểm danh không được để trống.");
        }

        Set<Integer> memberships = new HashSet<>();
        List<DiemDanh> normalized = new ArrayList<>();
        for (DiemDanh row : rows) {
            if (row == null || row.getMaDanhsach() <= 0) {
                throw invalid("Dữ liệu điểm danh không hợp lệ.");
            }
            if (row.getMaLichhoc() != scheduleId) {
                throw invalid("Buổi học của dòng điểm danh không khớp.");
            }
            if (!memberships.add(row.getMaDanhsach())) {
                throw invalid("Sinh viên trong danh sách điểm danh bị lặp.");
            }
            String state = row.getTrangThai() == null ? "" : row.getTrangThai().trim();
            if (!"CoMat".equals(state) && !"Vang".equals(state)) {
                throw invalid("Trạng thái điểm danh chỉ được là Có mặt hoặc Vắng.");
            }
            DiemDanh value = new DiemDanh();
            value.setMaDanhsach(row.getMaDanhsach());
            value.setMaLichhoc(scheduleId);
            value.setTrangThai(state);
            normalized.add(value);
        }

        DiemDanhRepository.BatchResult result =
                repo.saveBatchAdmin(sectionId, scheduleId, normalized, today);
        if (result == null) {
            throw new AcademicException(AcademicException.Code.OPERATION_FAILED, "Không thể lưu điểm danh.");
        }
        switch (result) {
            case SAVED:
                return;
            case SESSION_NOT_OWNED:
                throw new AcademicException(AcademicException.Code.FORBIDDEN, "Không tìm thấy buổi học.");
            case SESSION_NOT_NORMAL:
                throw invalid("Chỉ có thể điểm danh buổi học ở trạng thái bình thường.");
            case SESSION_IN_PAST:
                throw invalid("Không thể điểm danh buổi học đã qua.");
            case UNKNOWN_MEMBERSHIP:
                throw invalid("Danh sách có sinh viên không tồn tại.");
            case RELATION_MISMATCH:
                throw invalid("Danh sách có sinh viên không thuộc lớp học phần này.");
            default:
                throw new AcademicException(AcademicException.Code.OPERATION_FAILED, "Không thể lưu điểm danh.");
        }
    }
}
