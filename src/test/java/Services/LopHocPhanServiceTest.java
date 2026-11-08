package Services;

import Models.LopHocPhan;
import Repository.LopHocPhanRepository;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LopHocPhanServiceTest {
    @Test void chanGiangVienLechChuyenNganh() {
        FakeRepo repo = new FakeRepo();
        repo.teacherEligible = false;
        AcademicException error = assertThrows(AcademicException.class,
                () -> new LopHocPhanService(repo).create(valid()));
        assertEquals("Giảng viên không hoạt động hoặc không đúng chuyên ngành môn học.", error.getMessage());
    }

    @Test void khongConCallSiteHoacUiSucChuaGiaLap() throws Exception {
        assertNoFakeCapacityReferences(Paths.get("src/main/java"), ".java");
        assertNoFakeCapacityReferences(Paths.get("src/main/webapp"), ".jsp");
    }

    @Test void chanDoiMonKhiDaCoSinhVienVaDoiHocKyKhiDaCoLich() {
        FakeRepo repo = new FakeRepo();
        repo.existing = valid();
        repo.existing.setMaLophocphan(9);
        repo.students = true;
        LopHocPhan changedSubject = valid();
        changedSubject.setMaLophocphan(9);
        changedSubject.setMaMonhoc(99);
        assertTrue(assertThrows(AcademicException.class,
                () -> new LopHocPhanService(repo).update(changedSubject)).getMessage().contains("đổi môn"));

        repo.students = false;
        repo.schedules = true;
        LopHocPhan changedTerm = valid();
        changedTerm.setMaLophocphan(9);
        changedTerm.setMaHocky(8);
        assertTrue(assertThrows(AcademicException.class,
                () -> new LopHocPhanService(repo).update(changedTerm)).getMessage().contains("đổi học kỳ"));
    }

    @Test void chanDoiGiangVienNeuLichCuBiTrungVaXoaNeuCoPhuThuoc() {
        FakeRepo repo = new FakeRepo();
        repo.existing = valid();
        repo.existing.setMaLophocphan(9);
        repo.teacherConflict = true;
        LopHocPhan changed = valid();
        changed.setMaLophocphan(9);
        changed.setMaGiangvien(7);
        assertTrue(assertThrows(AcademicException.class,
                () -> new LopHocPhanService(repo).update(changed)).getMessage().contains("trùng lịch"));

        repo.deleteConstraint = "đang có lịch thi";
        assertTrue(assertThrows(AcademicException.class,
                () -> new LopHocPhanService(repo).delete(9)).getMessage().contains("lịch thi"));
    }

    private static LopHocPhan valid() {
        return new LopHocPhan(0, 2, 3, 4, "MoLop");
    }

    private static void assertNoFakeCapacityReferences(Path root, String suffix) throws Exception {
        List<Path> sources;
        try (Stream<Path> paths = Files.walk(root)) {
            sources = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(suffix))
                    .collect(Collectors.toList());
        }

        for (Path source : sources) {
            String content = new String(Files.readAllBytes(source), StandardCharsets.UTF_8)
                    .toLowerCase(Locale.ROOT);
            assertFalse(content.contains("sisotoida") || content.contains("sĩ số tối đa"),
                    source + " vẫn còn call-site hoặc giao diện sức chứa giả.");
        }
    }

    private static class FakeRepo extends LopHocPhanRepository {
        LopHocPhan existing;
        boolean teacherEligible = true, students, scores, schedules, exams, attendance, teacherConflict;
        String deleteConstraint;

        @Override public LopHocPhan findById(int id) { return existing; }
        @Override public boolean termExists(int id) { return true; }
        @Override public boolean isTeacherEligible(int teacherId, int subjectId) { return teacherEligible; }
        @Override public boolean hasStudents(int id) { return students; }
        @Override public boolean hasScores(int id) { return scores; }
        @Override public boolean hasSchedules(int id) { return schedules; }
        @Override public boolean hasExams(int id) { return exams; }
        @Override public boolean hasAttendance(int id) { return attendance; }
        @Override public boolean teacherChangeConflicts(int id, int teacherId) { return teacherConflict; }
        @Override public String getDeleteConstraint(int id) { return deleteConstraint; }
        @Override public int insert(LopHocPhan value) { return 20; }
        @Override public boolean update(LopHocPhan value) { return true; }
        @Override public boolean delete(int id) { return true; }
    }
}
