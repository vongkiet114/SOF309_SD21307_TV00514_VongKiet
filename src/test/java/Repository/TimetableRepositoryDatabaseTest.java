package Repository;

import Models.KyThi;
import Models.LichHoc;
import Models.TimetableFilter;
import Models.TimetableWeek;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class TimetableRepositoryDatabaseTest {
    @Test
    void adminChiNhanDuLieuTrongBayNgayVaDungBoLocPhong() throws Exception {
        LichHocRepository repo = new LichHocRepository();
        TimetableFilter seed = TimetableFilter.admin(
                LocalDate.of(2026, 1, 5), null, null, null, null);
        LocalDate first = repo.findNextDate(seed, LocalDate.of(2026, 1, 1));
        assertNotNull(first);
        LocalDate classBound = first.plusDays(1);
        LocalDate nextClassDate = repo.findNextDate(seed, classBound);
        assertTrue(nextClassDate == null || !nextClassDate.isBefore(classBound));
        if (nextClassDate != null) {
            assertTrue(repo.findForTimetable(seed.withWeekStart(nextClassDate))
                    .stream().anyMatch(row -> row.getNgayHoc().equals(nextClassDate)));
        }

        int roomId = scalar("SELECT TOP 1 ma_phong FROM dbo.lichhoc "
                + "WHERE ngay_hoc = ? ORDER BY ma_lichhoc", first);
        TimetableFilter filter = TimetableFilter.admin(
                first, null, null, null, roomId);
        List<LichHoc> rows = repo.findForTimetable(filter);

        assertFalse(rows.isEmpty());
        assertTrue(rows.stream().allMatch(row ->
                !row.getNgayHoc().isBefore(filter.getWeekStart())
                && !row.getNgayHoc().isAfter(filter.getWeekEnd())
                && row.getMaPhong() == roomId));

        LichHoc sample = rows.get(0);
        int termId = scalar("SELECT ma_hocky FROM dbo.lophocphan "
                + "WHERE ma_lophocphan = ?", sample.getMaLophocphan());
        int teacherId = scalar("SELECT ma_giangvien FROM dbo.lophocphan "
                + "WHERE ma_lophocphan = ?", sample.getMaLophocphan());
        List<LichHoc> fullyFiltered = repo.findForTimetable(
                TimetableFilter.admin(first, termId,
                        sample.getMaLophocphan(), teacherId, roomId));
        assertFalse(fullyFiltered.isEmpty());
        for (LichHoc row : fullyFiltered) {
            assertEquals(sample.getMaLophocphan(), row.getMaLophocphan());
            assertEquals(roomId, row.getMaPhong());
            assertEquals(termId, scalar("SELECT ma_hocky FROM dbo.lophocphan "
                    + "WHERE ma_lophocphan = ?", row.getMaLophocphan()));
            assertEquals(teacherId, scalar("SELECT ma_giangvien FROM dbo.lophocphan "
                    + "WHERE ma_lophocphan = ?", row.getMaLophocphan()));
        }
        assertTrue(repo.findForTimetable(TimetableFilter.admin(first,
                different(termId), sample.getMaLophocphan(), teacherId, roomId)).isEmpty());
        assertTrue(repo.findForTimetable(TimetableFilter.admin(first,
                termId, sample.getMaLophocphan(), different(teacherId), roomId)).isEmpty());
    }

    @Test
    void sinhVienVaGiangVienKhongNhanLichNgoaiPhamVi() throws Exception {
        int studentId = scalar("SELECT ma_nguoidung FROM dbo.nguoidung "
                + "WHERE email = ?", "sv1@fpt.edu.vn");
        int teacherId = scalar("SELECT ma_nguoidung FROM dbo.nguoidung "
                + "WHERE email = ?", "gv1@fpt.edu.vn");
        LichHocRepository repo = new LichHocRepository();

        TimetableFilter studentSeed = TimetableFilter.user(
                LocalDate.of(2026, 1, 5), studentId, "STUDENT");
        LocalDate firstStudentDate = repo.findNextDate(
                studentSeed, LocalDate.of(2026, 1, 1));
        assertNotNull(firstStudentDate);
        List<LichHoc> studentRows = repo.findForTimetable(
                studentSeed.withWeekStart(firstStudentDate));
        assertFalse(studentRows.isEmpty());
        for (LichHoc row : studentRows) {
            assertEquals(1, scalar("SELECT COUNT(*) FROM dbo.danhsach_sv_lhp "
                    + "WHERE ma_nguoidung = ? AND ma_lophocphan = ?",
                    studentId, row.getMaLophocphan()));
        }

        TimetableFilter teacherSeed = TimetableFilter.user(
                LocalDate.of(2026, 1, 5), teacherId, "TEACHER");
        LocalDate firstTeacherDate = repo.findNextDate(
                teacherSeed, LocalDate.of(2026, 1, 1));
        assertNotNull(firstTeacherDate);
        List<LichHoc> teacherRows = repo.findForTimetable(
                teacherSeed.withWeekStart(firstTeacherDate));
        assertFalse(teacherRows.isEmpty());
        for (LichHoc row : teacherRows) {
            assertEquals(1, scalar("SELECT COUNT(*) FROM dbo.lophocphan "
                    + "WHERE ma_lophocphan = ? AND ma_giangvien = ?",
                    row.getMaLophocphan(), teacherId));
        }
    }

    @Test
    void lichThiDungBienTuanVaPhamViHaiVaiTro() throws Exception {
        KyThiRepository repo = new KyThiRepository();
        TimetableFilter seed = TimetableFilter.admin(
                LocalDate.of(2026, 1, 5), null, null, null, null);
        LocalDate first = repo.findNextDate(seed, LocalDate.of(2026, 1, 1));
        assertNotNull(first);
        LocalDate examBound = first.plusDays(1);
        LocalDate nextExamDate = repo.findNextDate(seed, examBound);
        assertTrue(nextExamDate == null || !nextExamDate.isBefore(examBound));
        if (nextExamDate != null) {
            assertTrue(repo.findForTimetable(seed.withWeekStart(nextExamDate))
                    .stream().anyMatch(row -> row.getNgayThi().equals(nextExamDate)));
        }
        List<KyThi> rows = repo.findForTimetable(seed.withWeekStart(first));
        assertFalse(rows.isEmpty());
        assertTrue(rows.stream().allMatch(row ->
                !row.getNgayThi().isBefore(TimetableWeek.mondayOf(first))
                && !row.getNgayThi().isAfter(TimetableWeek.mondayOf(first).plusDays(6))));

        KyThi sample = rows.get(0);
        int termId = scalar("SELECT ma_hocky FROM dbo.lophocphan "
                + "WHERE ma_lophocphan = ?", sample.getMaLophocphan());
        int teacherId = scalar("SELECT ma_giangvien FROM dbo.lophocphan "
                + "WHERE ma_lophocphan = ?", sample.getMaLophocphan());
        List<KyThi> fullyFiltered = repo.findForTimetable(TimetableFilter.admin(first,
                termId, sample.getMaLophocphan(), teacherId, sample.getMaPhong()));
        assertFalse(fullyFiltered.isEmpty());
        for (KyThi row : fullyFiltered) {
            assertEquals(sample.getMaLophocphan(), row.getMaLophocphan());
            assertEquals(sample.getMaPhong(), row.getMaPhong());
            assertEquals(termId, scalar("SELECT ma_hocky FROM dbo.lophocphan "
                    + "WHERE ma_lophocphan = ?", row.getMaLophocphan()));
            assertEquals(teacherId, scalar("SELECT ma_giangvien FROM dbo.lophocphan "
                    + "WHERE ma_lophocphan = ?", row.getMaLophocphan()));
        }
        assertTrue(repo.findForTimetable(TimetableFilter.admin(first,
                different(termId), sample.getMaLophocphan(), teacherId,
                sample.getMaPhong())).isEmpty());
        assertTrue(repo.findForTimetable(TimetableFilter.admin(first, termId,
                sample.getMaLophocphan(), different(teacherId),
                sample.getMaPhong())).isEmpty());

        int studentId = scalar("SELECT ma_nguoidung FROM dbo.nguoidung "
                + "WHERE email = ?", "sv1@fpt.edu.vn");
        TimetableFilter studentSeed = TimetableFilter.user(
                LocalDate.of(2026, 1, 5), studentId, "STUDENT");
        LocalDate studentDate = repo.findNextDate(
                studentSeed, LocalDate.of(2026, 1, 1));
        assertNotNull(studentDate);
        List<KyThi> studentExams = repo.findForTimetable(
                studentSeed.withWeekStart(studentDate));
        assertFalse(studentExams.isEmpty());
        for (KyThi row : studentExams) {
            assertEquals(1, scalar("SELECT COUNT(*) "
                    + "FROM dbo.danhsach_sv_lhp "
                    + "WHERE ma_nguoidung = ? AND ma_lophocphan = ?",
                    studentId, row.getMaLophocphan()));
        }

        int teacherUserId = scalar("SELECT ma_nguoidung FROM dbo.nguoidung "
                + "WHERE email = ?", "gv1@fpt.edu.vn");
        TimetableFilter teacherSeed = TimetableFilter.user(
                LocalDate.of(2026, 1, 5), teacherUserId, "TEACHER");
        LocalDate teacherDate = repo.findNextDate(
                teacherSeed, LocalDate.of(2026, 1, 1));
        assertNotNull(teacherDate);
        List<KyThi> teacherExams = repo.findForTimetable(
                teacherSeed.withWeekStart(teacherDate));
        assertFalse(teacherExams.isEmpty());
        for (KyThi row : teacherExams) {
            assertEquals(1, scalar("SELECT COUNT(*) FROM dbo.lophocphan "
                    + "WHERE ma_lophocphan = ? AND ma_giangvien = ?",
                    row.getMaLophocphan(), teacherUserId));
        }
    }

    private int scalar(String sql, Object... values) throws Exception {
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int different(int value) {
        return value == Integer.MAX_VALUE ? value - 1 : value + 1;
    }
}
