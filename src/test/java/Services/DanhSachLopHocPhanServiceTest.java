package Services;

import Models.LopHocPhan;
import Models.SinhVien;
import Repository.DanhSachLopHocPhanRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DanhSachLopHocPhanServiceTest {
    @Test
    void docThongTinLopThanhVienVaUngVienQuaRepository() {
        FakeRepo repo = new FakeRepo();
        LopHocPhan section = new LopHocPhan();
        section.setMaLophocphan(3);
        SinhVien member = student(8, "SV008");
        SinhVien candidate = student(9, "SV009");
        repo.section = section;
        repo.members = Collections.singletonList(member);
        repo.candidates = Collections.singletonList(candidate);

        DanhSachLopHocPhanService service = new DanhSachLopHocPhanService(repo);

        assertEquals(section, service.getSection(3));
        assertEquals(repo.members, service.getMembers(3));
        assertEquals(repo.candidates, service.findCandidates(3, "  An  "));
        assertEquals(3, repo.sectionId);
        assertEquals("An", repo.keyword);
    }

    @Test
    void neuDungChuyenNganhVaKhongTrungThiThemThanhCong() throws Exception {
        FakeRepo repo = new FakeRepo();
        repo.addResult = DanhSachLopHocPhanRepository.AddResult.ADDED;

        new DanhSachLopHocPhanService(repo).addStudent(3, 8);

        assertEquals(3, repo.sectionId);
        assertEquals(8, repo.studentId);
    }

    @Test
    void anhXaDayDuMoiLyDoKhongTheThemSinhVien() {
        assertAddFailure(DanhSachLopHocPhanRepository.AddResult.SECTION_NOT_FOUND,
                AcademicException.Code.NOT_FOUND, "Không tìm thấy lớp học phần");
        assertAddFailure(DanhSachLopHocPhanRepository.AddResult.SECTION_CLOSED,
                AcademicException.Code.INVALID_INPUT, "Mở lớp hoặc Đang học");
        assertAddFailure(DanhSachLopHocPhanRepository.AddResult.STUDENT_NOT_ACTIVE,
                AcademicException.Code.INVALID_INPUT, "Đang học");
        assertAddFailure(DanhSachLopHocPhanRepository.AddResult.WRONG_SPECIALIZATION,
                AcademicException.Code.INVALID_INPUT, "cùng chuyên ngành");
        assertAddFailure(DanhSachLopHocPhanRepository.AddResult.ALREADY_ENROLLED,
                AcademicException.Code.INVALID_INPUT, "đã có trong lớp học phần");
        assertAddFailure(DanhSachLopHocPhanRepository.AddResult.DUPLICATE_SUBJECT,
                AcademicException.Code.INVALID_INPUT, "cùng môn");
        assertAddFailure(DanhSachLopHocPhanRepository.AddResult.SCHEDULE_CONFLICT,
                AcademicException.Code.INVALID_INPUT, "trùng lịch");
    }

    @Test
    void goSinhVienKhiChuaCoDuLieuPhuThuoc() throws Exception {
        FakeRepo repo = new FakeRepo();
        repo.removeResult = DanhSachLopHocPhanRepository.RemoveResult.REMOVED;

        new DanhSachLopHocPhanService(repo).removeStudent(3, 12);

        assertEquals(3, repo.sectionId);
        assertEquals(12, repo.membershipId);
    }

    @Test
    void anhXaDayDuMoiLyDoKhongTheGoSinhVien() {
        assertRemoveFailure(DanhSachLopHocPhanRepository.RemoveResult.MEMBERSHIP_NOT_FOUND,
                AcademicException.Code.NOT_FOUND, "Không tìm thấy sinh viên");
        assertRemoveFailure(DanhSachLopHocPhanRepository.RemoveResult.HAS_GRADES,
                AcademicException.Code.CONFLICT, "điểm");
        assertRemoveFailure(DanhSachLopHocPhanRepository.RemoveResult.HAS_ATTENDANCE,
                AcademicException.Code.CONFLICT, "điểm danh");
    }

    @Test
    void tuChoiMaKhongHopLeTruocKhiGoiRepository() {
        FakeRepo repo = new FakeRepo();
        DanhSachLopHocPhanService service = new DanhSachLopHocPhanService(repo);

        for (int[] ids : Arrays.asList(new int[]{0, 8}, new int[]{3, 0})) {
            AcademicException addError = assertThrows(AcademicException.class,
                    () -> service.addStudent(ids[0], ids[1]));
            assertEquals(AcademicException.Code.INVALID_INPUT, addError.getCode());
        }
        for (int[] ids : Arrays.asList(new int[]{0, 12}, new int[]{3, 0})) {
            AcademicException removeError = assertThrows(AcademicException.class,
                    () -> service.removeStudent(ids[0], ids[1]));
            assertEquals(AcademicException.Code.INVALID_INPUT, removeError.getCode());
        }
        assertEquals(0, repo.writeCalls);
    }

    private static void assertAddFailure(DanhSachLopHocPhanRepository.AddResult result,
                                         AcademicException.Code code,
                                         String messagePart) {
        FakeRepo repo = new FakeRepo();
        repo.addResult = result;

        AcademicException error = assertThrows(AcademicException.class,
                () -> new DanhSachLopHocPhanService(repo).addStudent(3, 8));

        assertEquals(code, error.getCode());
        assertTrue(error.getMessage().contains(messagePart), error.getMessage());
    }

    private static void assertRemoveFailure(DanhSachLopHocPhanRepository.RemoveResult result,
                                            AcademicException.Code code,
                                            String messagePart) {
        FakeRepo repo = new FakeRepo();
        repo.removeResult = result;

        AcademicException error = assertThrows(AcademicException.class,
                () -> new DanhSachLopHocPhanService(repo).removeStudent(3, 12));

        assertEquals(code, error.getCode());
        assertTrue(error.getMessage().contains(messagePart), error.getMessage());
    }

    private static SinhVien student(int id, String mssv) {
        SinhVien student = new SinhVien();
        student.setMaNguoidung(id);
        student.setMssv(mssv);
        return student;
    }

    private static class FakeRepo extends DanhSachLopHocPhanRepository {
        AddResult addResult;
        RemoveResult removeResult;
        LopHocPhan section;
        List<SinhVien> members = Collections.emptyList();
        List<SinhVien> candidates = Collections.emptyList();
        int sectionId;
        int studentId;
        int membershipId;
        int writeCalls;
        String keyword;

        @Override
        public LopHocPhan getSection(int sectionId) {
            this.sectionId = sectionId;
            return section;
        }

        @Override
        public List<SinhVien> getMembers(int sectionId) {
            this.sectionId = sectionId;
            return members;
        }

        @Override
        public List<SinhVien> findCandidates(int sectionId, String keyword) {
            this.sectionId = sectionId;
            this.keyword = keyword;
            return candidates;
        }

        @Override
        public AddResult addAtomically(int section, int student) {
            writeCalls++;
            sectionId = section;
            studentId = student;
            return addResult;
        }

        @Override
        public RemoveResult removeAtomically(int section, int membership) {
            writeCalls++;
            sectionId = section;
            membershipId = membership;
            return removeResult;
        }
    }
}
