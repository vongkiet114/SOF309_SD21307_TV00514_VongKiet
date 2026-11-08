package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DanhSachLopHocPhanContractTest {
    @Test
    void servletDungUrlChiChoAdminVaThucHienPostRedirectGet() throws Exception {
        String servlet = read("src/main/java/Servlet/DanhSachLopHocPhanServlet.java");

        assertTrue(servlet.contains("@WebServlet(\"/admin/lophocphan/sinhvien\")"));
        assertTrue(servlet.contains("protected void doGet"));
        assertTrue(servlet.contains("protected void doPost"));
        assertTrue(servlet.contains("\"ADMIN\".equalsIgnoreCase"));
        assertTrue(servlet.contains("SC_FORBIDDEN"));
        assertTrue(servlet.contains("getParameter(\"maLHP\")"));
        assertTrue(servlet.contains("service.getSection(maLHP)"));
        assertTrue(servlet.contains("service.getMembers(maLHP)"));
        assertTrue(servlet.contains("service.findCandidates(maLHP"));
        assertTrue(servlet.contains("\"add\".equals(action)"));
        assertTrue(servlet.contains("\"remove\".equals(action)"));
        assertTrue(servlet.contains("service.addStudent(maLHP"));
        assertTrue(servlet.contains("service.removeStudent(maLHP"));
        assertTrue(servlet.contains("sendRedirect"));
        assertTrue(servlet.contains("/WEB-INF/admin/lhpSinhVien.jsp"));
    }

    @Test
    void trangQuanLyCoLinkTimKiemCotDuLieuVaFormThemGo() throws Exception {
        String list = read("src/main/webapp/WEB-INF/admin/lhpList.jsp");
        String jsp = read("src/main/webapp/WEB-INF/admin/lhpSinhVien.jsp");

        assertTrue(list.contains("/admin/lophocphan/sinhvien?maLHP="));
        assertTrue(jsp.contains("name=\"q\""));
        assertTrue(jsp.contains("<th>MSSV</th>"));
        assertTrue(jsp.contains("<th>Họ tên</th>"));
        assertTrue(jsp.contains("<th>Lớp hành chính</th>"));
        assertTrue(jsp.contains("<form method=\"post\""));
        assertTrue(jsp.contains("name=\"action\" value=\"add\""));
        assertTrue(jsp.contains("name=\"action\" value=\"remove\""));
        assertTrue(jsp.contains("name=\"maNguoidung\""));
        assertTrue(jsp.contains("name=\"maDanhsach\""));
        assertTrue(jsp.contains("Không có sinh viên phù hợp"));
        assertTrue(jsp.contains("Thử thay đổi từ khóa"));
        assertTrue(jsp.contains("Html.escape"));
        assertTrue(jsp.contains("Html.escape(lhp.getMaLhp())"));
        assertTrue(jsp.contains("Html.escape(sv.getMssv())"));
        assertTrue(jsp.contains("Html.escape(sv.getHoTen())"));
        assertTrue(jsp.contains("Html.escape(sv.getTenLop())"));
        assertTrue(jsp.contains("<%@ include file=\"/WEB-INF/footer.jsp\" %>"));
    }

    @Test
    void repositoryKhoaVaKiemTraDuDieuKienTrongCungGiaoDich() throws Exception {
        String repository = read("src/main/java/Repository/DanhSachLopHocPhanRepository.java");

        assertTrue(repository.contains("enum AddResult"));
        assertTrue(repository.contains("SECTION_NOT_FOUND"));
        assertTrue(repository.contains("SECTION_CLOSED"));
        assertTrue(repository.contains("STUDENT_NOT_ACTIVE"));
        assertTrue(repository.contains("WRONG_SPECIALIZATION"));
        assertTrue(repository.contains("ALREADY_ENROLLED"));
        assertTrue(repository.contains("DUPLICATE_SUBJECT"));
        assertTrue(repository.contains("SCHEDULE_CONFLICT"));
        assertTrue(repository.contains("enum RemoveResult"));
        assertTrue(repository.contains("HAS_GRADES"));
        assertTrue(repository.contains("HAS_ATTENDANCE"));
        assertTrue(repository.contains("Connection.TRANSACTION_SERIALIZABLE"));
        assertTrue(repository.contains("setAutoCommit(false)"));
        assertTrue(repository.contains("conn.commit()"));
        assertTrue(repository.contains("conn.rollback()"));
        assertTrue(repository.contains("WITH (UPDLOCK, HOLDLOCK)"));
        assertTrue(repository.contains("trang_thai = 'BinhThuong'"));
        assertTrue(repository.contains("INSERT INTO danhsach_sv_lhp"));
        assertTrue(repository.contains("DELETE FROM danhsach_sv_lhp"));
        assertTrue(repository.contains("ma_danhsach = ? AND ma_lophocphan = ?"));
        assertTrue(repository.contains("ORDER BY sv.mssv"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
