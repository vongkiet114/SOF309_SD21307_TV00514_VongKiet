package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeeklyTimetablePresentationTest {
    @Test
    void sauTrangDeuDungFragmentVaKhongConMaTranCu() throws Exception {
        String[] pages = {
                "src/main/webapp/WEB-INF/admin/lichHocList.jsp",
                "src/main/webapp/WEB-INF/admin/kyThiList.jsp",
                "src/main/webapp/WEB-INF/student/lichHocCaNhan.jsp",
                "src/main/webapp/WEB-INF/student/xemKyThi.jsp",
                "src/main/webapp/WEB-INF/teacher/lichDayCaNhan.jsp",
                "src/main/webapp/WEB-INF/teacher/xemKyThi.jsp"
        };
        for (String page : pages) {
            String jsp = read(page);
            assertTrue(jsp.contains("/WEB-INF/common/timetableWeek.jspf"), page);
            assertEquals(1, count(jsp, "/WEB-INF/common/timetableWeek.jspf"), page);
            assertFalse(jsp.contains("list.get(0)"), page);
            assertFalse(jsp.contains("for (int ca = 1; ca <= 10; ca++)"), page);
        }
    }

    @Test
    void adminCoBonBoLocVaBonTrangCaNhanKhongCoQuanLy() throws Exception {
        String adminClass = read("src/main/webapp/WEB-INF/admin/lichHocList.jsp");
        String adminExam = read("src/main/webapp/WEB-INF/admin/kyThiList.jsp");
        for (String admin : new String[]{adminClass, adminExam}) {
            assertTrue(admin.contains("name=\"maHocKy\""));
            assertTrue(admin.contains("name=\"maLhp\""));
            assertTrue(admin.contains("name=\"maGiangVien\""));
            assertTrue(admin.contains("name=\"maPhong\""));
        }
        String[][] personalPages = {
                {"src/main/webapp/WEB-INF/student/lichHocCaNhan.jsp",
                        "Lịch học của tôi",
                        "Các buổi học của những lớp học phần bạn đã đăng ký."},
                {"src/main/webapp/WEB-INF/student/xemKyThi.jsp",
                        "Lịch thi của tôi",
                        "Các kỳ thi của những lớp học phần bạn đã đăng ký."},
                {"src/main/webapp/WEB-INF/teacher/lichDayCaNhan.jsp",
                        "Lịch dạy của tôi", "Các buổi dạy đã được xếp cho bạn."},
                {"src/main/webapp/WEB-INF/teacher/xemKyThi.jsp",
                        "Lịch thi lớp phụ trách",
                        "Các kỳ thi của những lớp học phần bạn đang giảng dạy."}
        };
        String[] managementMarkers = {
                "name=\"maHocKy\"", "name=\"maLhp\"", "name=\"maGiangVien\"",
                "name=\"maPhong\"", "action=edit", "data-detail-edit",
                "data-detail-delete", "name=\"action\" value=\"delete\"",
                "action=add", "+ Thêm buổi học", "+ Thêm kỳ thi", "canManage"
        };
        for (String[] personalPage : personalPages) {
            String jsp = read(personalPage[0]);
            int header = jsp.indexOf("/WEB-INF/header.jsp");
            int fragment = jsp.indexOf("/WEB-INF/common/timetableWeek.jspf");
            int footer = jsp.indexOf("/WEB-INF/footer.jsp");
            assertTrue(header >= 0, personalPage[0]);
            assertTrue(fragment > header, personalPage[0]);
            assertTrue(footer > fragment, personalPage[0]);
            assertTrue(jsp.contains(personalPage[1]), personalPage[0]);
            assertTrue(jsp.contains(personalPage[2]), personalPage[0]);
            for (String marker : managementMarkers) {
                assertFalse(jsp.contains(marker), personalPage[0] + ": " + marker);
            }
        }
    }

    @Test
    void fragmentCoDieuHuongBayNgayChiTietVaQuanLyTheoQuyen() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/common/timetableWeek.jspf");

        assertTrue(jsp.contains("getPreviousWeekStart()"));
        assertTrue(jsp.contains("getNextWeekStart()"));
        assertTrue(jsp.contains("getDays()"));
        assertTrue(jsp.contains("getShifts()"));
        assertTrue(jsp.contains("getEvents("));
        assertTrue(jsp.contains("getNearestEventWeekStart()"));
        assertTrue(jsp.contains("Tuần này chưa có lịch"));
        assertTrue(jsp.contains("timetable-legend"));
        assertTrue(jsp.contains("timetable-week-range"));
        assertTrue(jsp.contains("data-timetable-event"));
        assertTrue(jsp.contains("data-timetable-drawer"));
        assertTrue(jsp.contains("data-detail-day"));
        assertTrue(jsp.contains("data-detail-time"));
        assertTrue(jsp.contains("canManage"));
        assertTrue(jsp.contains("action\" value=\"delete"));
        assertTrue(jsp.contains("Html.escape"));
    }

    @Test
    void fragmentEscapeDuLieuDongVaGioiHanThaoTacQuanLy() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/common/timetableWeek.jspf");

        assertTrue(jsp.contains("filterQuery = normalizeFilterQuery"));
        assertTrue(jsp.contains("data-id=\"<%= Html.escape(event.getId()) %>\""));
        assertTrue(jsp.contains("data-code=\"<%= Html.escape(event.getSectionCode()) %>\""));
        assertTrue(jsp.contains("data-edit-url=\"<%= Html.escape(editUrl) %>\""));
        assertTrue(jsp.contains("<% if (canManage) { %>"));
        assertTrue(jsp.indexOf("<% if (canManage) { %>")
                < jsp.indexOf("data-detail-delete"));
        assertFalse(jsp.contains("data-edit-url=\"<%= editUrl %>\""));
        assertEditUrlGuarded(jsp,
                "for (TimetableEvent event : timetable.getEvents(day, shift.getMaCa())) {");
        assertEditUrlGuarded(jsp,
                "for (TimetableEvent event : dayEvents) {");
    }

    @Test
    void agendaDienThoaiNhomSuKienTheoTungNgayTheoThuTuTuan() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/common/timetableWeek.jspf");

        int agendaStart = jsp.indexOf(
                "<div class=\"timetable-agenda\" aria-label=\"Lịch tuần dạng danh sách\">");
        int drawerStart = jsp.indexOf("<div class=\"timetable-drawer\"", agendaStart);
        assertTrue(agendaStart >= 0);
        assertTrue(drawerStart > agendaStart);
        String agenda = jsp.substring(agendaStart, drawerStart);

        assertTrue(agenda.contains(
                "for (LocalDate agendaDay : timetable.getDays()) {"));
        assertTrue(agenda.contains(
                "for (TimetableEvent candidate : timetable.getAllEvents()) {"));
        assertTrue(agenda.contains("candidate.getDate().equals(agendaDay)"));
        assertTrue(agenda.contains("if (!dayEvents.isEmpty()) {"));
        assertTrue(agenda.contains(
                "<section class=\"timetable-agenda-day\""));
        assertTrue(agenda.contains(
                "<h3 class=\"timetable-agenda-heading\">"));
        assertTrue(agenda.contains("timetableDayLabel(agendaDay)"));
        assertTrue(agenda.contains("agendaDay.format(dateFormat)"));
        assertTrue(agenda.contains(
                "for (TimetableEvent event : dayEvents) {"));
        assertFalse(agenda.contains("timetable-agenda-date"),
                "Ngày phải nằm ở tiêu đề nhóm, không lặp lại trên từng thẻ");
    }

    @Test
    void moiTheSuKienCoTrangThaiHienThiVaTenTruyCapDayDu() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/common/timetableWeek.jspf");

        assertTrue(count(jsp, "timetable-event-state") >= 2);
        assertTrue(jsp.contains("Html.escape(timetableEventState(event))"));
        assertTrue(jsp.contains("event.getCategoryLabel()"));
        assertTrue(jsp.contains("event.getStatusLabel()"));
        assertTrue(jsp.contains("aria-label=\"<%= Html.escape(timetableEventAccessibleLabel(event)) %>\""));
        assertTrue(jsp.contains("agendaDay.format(dateFormat)"));
        assertTrue(jsp.contains("event.getShiftLabel()"));
        assertTrue(jsp.contains("event.getTimeLabel()"));
        assertTrue(jsp.contains("event.getRoomName()"));
    }

    @Test
    void dialogGiuFocusVaKhoiPhucNenKhiDong() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/common/timetableWeek.jspf");
        String js = read("src/main/webapp/assets/js/timetable.js");

        assertTrue(jsp.contains("data-timetable-drawer role=\"dialog\""));
        assertTrue(jsp.contains("tabindex=\"-1\""));
        assertTrue(js.contains("event.key !== 'Tab'"));
        assertTrue(js.contains("event.shiftKey"));
        assertTrue(js.contains("getFocusableElements"));
        assertTrue(js.contains("focusable.length === 1"));
        assertTrue(js.contains("backgroundState"));
        assertTrue(js.contains("setAttribute('inert', '')"));
        assertTrue(js.contains("setAttribute('aria-hidden', 'true')"));
        assertTrue(js.contains("restoreBackground"));
        assertTrue(js.contains("removeAttribute('inert')"));
        assertTrue(js.contains("document.addEventListener('focusin'"));
    }

    @Test
    void luoiThuongTrucKhongDungCacVaiTroBangAriaKhongHopLe() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/common/timetableWeek.jspf");

        assertTrue(jsp.contains("class=\"timetable-grid\" role=\"region\" aria-label=\"Lịch theo ca và ngày\""));
        assertFalse(jsp.contains("role=\"table\""));
        assertFalse(jsp.contains("role=\"columnheader\""));
        assertFalse(jsp.contains("role=\"rowheader\""));
        assertFalse(jsp.contains("role=\"cell\""));
        assertTrue(jsp.contains("aria-label=\"<%= Html.escape(timetableEventAccessibleLabel(event)) %>\""));
    }

    @Test
    void assetsCoResponsiveVaHoTroBanPhim() throws Exception {
        String css = read("src/main/webapp/assets/css/timetable.css");
        String js = read("src/main/webapp/assets/js/timetable.js");
        String header = read("src/main/webapp/WEB-INF/header.jsp");
        String footer = read("src/main/webapp/WEB-INF/footer.jsp");

        assertTrue(css.contains("@media (max-width:"));
        assertTrue(css.contains(".timetable-agenda"));
        assertTrue(css.contains(".event-cancelled"));
        assertTrue(css.contains("grid-template-columns: 58px repeat(7, minmax(110px, 1fr))"));
        assertTrue(js.contains("data-timetable-event"));
        assertTrue(js.contains("data-timetable-drawer"));
        assertTrue(js.contains("data-timetable-close"));
        assertTrue(js.contains("setDetail('[data-detail-day]', eventButton.dataset.day)"));
        assertTrue(js.contains("setDetail('[data-detail-time]', eventButton.dataset.time)"));
        assertTrue(js.contains("Escape"));
        assertTrue(js.contains("aria-hidden"));
        assertTrue(js.contains("previousFocus.focus()"));
        assertTrue(header.contains("/assets/css/timetable.css"));
        assertTrue(footer.contains("/assets/js/timetable.js"));
    }

    @Test
    void sidebarGiangVienCoLichThiVaMoNhomKhiDangXemLichThi() throws Exception {
        String sidebar = read("src/main/webapp/WEB-INF/sidebar.jsp");
        String teacherSection = sidebar.substring(
                sidebar.indexOf("\"TEACHER\".equals(userRole)"),
                sidebar.indexOf("\"STUDENT\".equals(userRole)"));
        String openGiangDay = lineContaining(teacherSection, "boolean openGiangDay");

        assertTrue(openGiangDay.contains("\"kythi_gv\""));
        assertTrue(teacherSection.contains(
                "<a class=\"muc-menu muc-menu-con <%= \"kythi_gv\".equals(currentPage) ? \"active\" : \"\" %>\""
                        + " href=\"<%= contextPath %>/giangvien/kythi\">Lịch thi</a>"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private String lineContaining(String text, String marker) {
        int markerIndex = text.indexOf(marker);
        assertTrue(markerIndex >= 0, marker);
        int lineStart = text.lastIndexOf('\n', markerIndex);
        int lineEnd = text.indexOf('\n', markerIndex);
        return text.substring(lineStart + 1, lineEnd >= 0 ? lineEnd : text.length());
    }

    private int count(String text, String value) {
        int count = 0;
        int index = text.indexOf(value);
        while (index >= 0) {
            count++;
            index = text.indexOf(value, index + value.length());
        }
        return count;
    }

    private void assertEditUrlGuarded(String jsp, String eventLoop) {
        int loop = jsp.indexOf(eventLoop);
        int nextLoop = jsp.indexOf("for (TimetableEvent event :", loop + eventLoop.length());
        int regionEnd = nextLoop >= 0 ? nextLoop : jsp.length();
        int guard = jsp.indexOf("<% if (canManage) {", loop);
        int build = jsp.indexOf("String editUrl = timetableUrl + \"?action=edit&id=\" + event.getId();", loop);
        int attribute = jsp.indexOf("data-edit-url=\"<%= Html.escape(editUrl) %>\"", loop);
        int guardEnd = jsp.indexOf("<% } %>", guard);

        assertTrue(loop >= 0, eventLoop);
        assertTrue(guard > loop && guard < regionEnd, eventLoop);
        assertTrue(build > guard && build < regionEnd, eventLoop);
        assertTrue(attribute > build && attribute < regionEnd, eventLoop);
        assertTrue(guardEnd > attribute && guardEnd < regionEnd, eventLoop);
    }
}
