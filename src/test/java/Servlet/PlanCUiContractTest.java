package Servlet;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class PlanCUiContractTest {
    @Test void cacBangLonCoTimKiemVaPhanTrang() throws Exception {
        String lhp = read("src/main/webapp/WEB-INF/admin/lhpList.jsp");
        String roster = read("src/main/webapp/WEB-INF/admin/lhpSinhVien.jsp");
        String schedules = read("src/main/webapp/WEB-INF/admin/lichHocList.jsp");
        assertTrue(lhp.contains("data-filter-table=\"bangLopHocPhan\""));
        assertTrue(lhp.contains("data-filter-text"));
        assertTrue(roster.contains("data-filter-table=\"roster-table\""));
        assertTrue(roster.contains("data-filter-text"));
        assertTrue(schedules.contains("/WEB-INF/common/timetableWeek.jspf"));
        assertFalse(schedules.contains("thoi-khoa-bieu-matrix"));
    }

    @Test void bangDiemCoCotDinhVaPreviewCoDuThongKe() throws Exception {
        String grade = read("src/main/webapp/WEB-INF/teacher/nhapDiem.jsp");
        String preview = read("src/main/webapp/WEB-INF/teacher/nhapDiemExcelPreview.jsp");
        assertTrue(grade.contains("bang-diem-lon"));
        assertTrue(grade.contains("cot-dinh"));
        assertTrue(preview.contains("getAddedCount()"));
        assertTrue(preview.contains("getUpdatedCount()"));
        assertTrue(preview.contains("getDeletedCount()"));
    }

    @Test void scriptFilterDuocNapChoAdminVaGiangVien() throws Exception {
        String footer = read("src/main/webapp/WEB-INF/footer.jsp");
        String teacher = read("src/main/webapp/WEB-INF/teacher/diemDanh.jsp");
        assertTrue(footer.contains("/assets/js/bolocbang.js"));
        assertTrue(teacher.contains("/WEB-INF/footer.jsp"));
        assertTrue(teacher.contains("data-filter-table=\"attendance-table\""));
        assertTrue(teacher.contains("data-filter-text"));
        assertFalse(teacher.contains("data-table-filter"));
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
