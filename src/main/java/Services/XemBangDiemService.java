package Services;

import Models.BangDiemHocPhan;
import Models.DiemSo;
import Models.ThanhPhanDiem;
import Repository.DiemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XemBangDiemService {
    private final DiemRepository diemRepo;

    public XemBangDiemService() {
        this(new DiemRepository());
    }

    XemBangDiemService(DiemRepository diemRepo) {
        this.diemRepo = diemRepo;
    }

    public List<BangDiemHocPhan> getBangDiem(int studentId) {
        List<BangDiemHocPhan> result = diemRepo.findGradebookByStudent(studentId);
        for (BangDiemHocPhan gradebook : result) {
            gradebook.setDiemTongKet(calculateTotal(
                    gradebook.getThanhPhanDiems(), gradebook.getDiemSoList()));
        }
        return result;
    }

    public BigDecimal calculateTotal(List<ThanhPhanDiem> components,
                                     List<DiemSo> scores) {
        if (components == null || components.isEmpty() || scores == null) {
            return null;
        }
        Map<Integer, BigDecimal> scoreByComponent = new HashMap<>();
        for (DiemSo score : scores) {
            if (score != null && score.getGiaTriDiem() != null) {
                scoreByComponent.put(score.getMaThanhphan(), score.getGiaTriDiem());
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ThanhPhanDiem component : components) {
            BigDecimal score = scoreByComponent.get(component.getMaThanhphan());
            if (score == null || component.getTrongSo() == null) {
                return null;
            }
            total = total.add(score.multiply(component.getTrongSo()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
