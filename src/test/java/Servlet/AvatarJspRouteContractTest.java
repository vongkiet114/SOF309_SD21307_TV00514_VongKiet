package Servlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvatarJspRouteContractTest {
    @Test
    void baTrangAnhDaiDienDeuDungMediaRouteNgoaiWar() throws Exception {
        assertAvatarRoute(
                "src/main/webapp/WEB-INF/admin/sinhVienForm.jsp",
                "sinhvien");
        assertAvatarRoute(
                "src/main/webapp/WEB-INF/admin/giangVienForm.jsp",
                "giangvien");
        assertAvatarRoute(
                "src/main/webapp/WEB-INF/student/thongTinCaNhan.jsp",
                "sinhvien");
    }

    @Test
    void formQuanTriKhongGuiTenAnhCuCoTheBiGiaMao() throws Exception {
        assertNoHiddenPreviousImage(
                "src/main/webapp/WEB-INF/admin/sinhVienForm.jsp");
        assertNoHiddenPreviousImage(
                "src/main/webapp/WEB-INF/admin/giangVienForm.jsp");
    }

    private static void assertAvatarRoute(String path, String folder)
            throws Exception {
        String jsp = new String(
                Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

        assertTrue(jsp.contains("/media/avatar/" + folder + "/"), path);
        assertFalse(jsp.contains("/assets/img/" + folder + "/"), path);
    }

    private static void assertNoHiddenPreviousImage(String path)
            throws Exception {
        String jsp = new String(
                Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        assertFalse(jsp.contains("name=\"hinhAnhCu\""), path);
    }
}
