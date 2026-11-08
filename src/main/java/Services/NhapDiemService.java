package Services;

import Models.DiemSo;
import Models.LopHocPhan;
import Models.SinhVien;
import Models.ThanhPhanDiem;
import Repository.DiemRepository;
import Repository.LopHocPhanRepository;
import Repository.SinhVienRepository;
import Repository.ThanhPhanDiemRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NhapDiemService {
    private final LopHocPhanRepository lhpRepo;
    private final SinhVienRepository svRepo;
    private final ThanhPhanDiemRepository tpdRepo;
    private final DiemRepository diemRepo;

    public NhapDiemService() {
        this(new LopHocPhanRepository(), new SinhVienRepository(),
                new ThanhPhanDiemRepository(), new DiemRepository());
    }

    NhapDiemService(DiemRepository diemRepo) {
        this(new LopHocPhanRepository(), new SinhVienRepository(),
                new ThanhPhanDiemRepository(), diemRepo);
    }

    NhapDiemService(LopHocPhanRepository lhpRepo,
                    SinhVienRepository svRepo,
                    ThanhPhanDiemRepository tpdRepo,
                    DiemRepository diemRepo) {
        this.lhpRepo = lhpRepo;
        this.svRepo = svRepo;
        this.tpdRepo = tpdRepo;
        this.diemRepo = diemRepo;
    }

    public NhapDiemData getNhapDiemData(int teacherId, int sectionId)
            throws AcademicException {
        if (teacherId <= 0 || sectionId <= 0
                || !diemRepo.isSectionOwned(teacherId, sectionId)) {
            throw new AcademicException(AcademicException.Code.FORBIDDEN,
                    "Bạn không được xem bảng điểm của lớp học phần này.");
        }

        LopHocPhan lhp = lhpRepo.findById(sectionId);
        if (lhp == null) {
            throw new AcademicException(AcademicException.Code.NOT_FOUND,
                    "Không tìm thấy lớp học phần.");
        }

        List<SinhVien> sinhViens = svRepo.findByLopHocPhan(sectionId);
        List<ThanhPhanDiem> thanhPhanDiems = tpdRepo.findByMaMonHoc(lhp.getMaMonhoc());
        Map<Integer, List<DiemSo>> existingScores = diemRepo.findByMaLopHocPhan(sectionId);
        return new NhapDiemData(sinhViens, thanhPhanDiems, existingScores);
    }

    public void saveDiem(int teacherId, int sectionId, List<DiemSo> diemSoList)
            throws AcademicException {
        validateBatch(teacherId, sectionId, diemSoList);
        DiemRepository.BatchResult result = diemRepo.saveBatch(
                teacherId, sectionId, diemSoList);
        switch (result) {
            case SAVED:
                return;
            case SECTION_NOT_OWNED:
                throw new AcademicException(AcademicException.Code.FORBIDDEN,
                        "Bạn không được nhập điểm cho lớp học phần này.");
            case UNKNOWN_MEMBERSHIP:
                throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                        "Danh sách sinh viên có dữ liệu không tồn tại.");
            case UNKNOWN_COMPONENT:
                throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                        "Thành phần điểm không tồn tại.");
            case RELATION_MISMATCH:
                throw new AcademicException(AcademicException.Code.INVALID_INPUT,
                        "Sinh viên hoặc thành phần điểm không thuộc lớp học phần này.");
            default:
                throw new AcademicException(AcademicException.Code.OPERATION_FAILED,
                        "Không thể lưu bảng điểm.");
        }
    }

    private void validateBatch(int teacherId, int sectionId, List<DiemSo> values)
            throws AcademicException {
        if (teacherId <= 0 || sectionId <= 0 || values == null || values.isEmpty()) {
            throw invalid("Dữ liệu nhập điểm không hợp lệ.");
        }
        Set<Long> pairs = new HashSet<>();
        for (DiemSo value : values) {
            if (value == null || value.getMaDanhsach() <= 0 || value.getMaThanhphan() <= 0) {
                throw invalid("Dữ liệu nhập điểm không hợp lệ.");
            }
            BigDecimal score = value.getGiaTriDiem();
            if (score != null && (score.compareTo(BigDecimal.ZERO) < 0
                    || score.compareTo(BigDecimal.TEN) > 0
                    || Math.max(0, score.stripTrailingZeros().scale()) > 2)) {
                throw invalid("Điểm phải từ 0 đến 10 và có tối đa hai chữ số thập phân.");
            }
            long pair = (((long) value.getMaDanhsach()) << 32)
                    ^ (value.getMaThanhphan() & 0xffffffffL);
            if (!pairs.add(pair)) {
                throw invalid("Cặp sinh viên và thành phần điểm bị lặp.");
            }
        }
    }

    private AcademicException invalid(String message) {
        return new AcademicException(AcademicException.Code.INVALID_INPUT, message);
    }

    public static class NhapDiemData {
        private final List<SinhVien> sinhViens;
        private final List<ThanhPhanDiem> thanhPhanDiems;
        private final Map<Integer, List<DiemSo>> existingScores;

        public NhapDiemData(List<SinhVien> sinhViens,
                            List<ThanhPhanDiem> thanhPhanDiems,
                            Map<Integer, List<DiemSo>> existingScores) {
            this.sinhViens = sinhViens;
            this.thanhPhanDiems = thanhPhanDiems;
            this.existingScores = existingScores;
        }

        public List<SinhVien> getSinhViens() {
            return sinhViens;
        }

        public List<ThanhPhanDiem> getThanhPhanDiems() {
            return thanhPhanDiems;
        }

        public Map<Integer, List<DiemSo>> getExistingScores() {
            return existingScores;
        }
    }
}
