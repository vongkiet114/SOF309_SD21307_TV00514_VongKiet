package Servlet;

import Models.GiangVien;
import Models.SinhVien;
import Models.TaiKhoan;
import Services.GiangVienService;
import Services.SinhVienService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvatarLifecycleServletTest {
    private Path uploadRoot;
    private byte[] png;

    @BeforeEach
    void setUp() throws Exception {
        Path workspaceTemp = Paths.get("target", "avatar-test-tmp")
                .toAbsolutePath().normalize();
        Files.createDirectories(workspaceTemp);
        uploadRoot = Files.createTempDirectory(workspaceTemp, "lifecycle-");
        png = validPng();
    }

    @Test
    void taoSinhVienThatBaiThiXoaAnhVuaGhi() throws Exception {
        FakeSinhVienService service = new FakeSinhVienService();
        service.failCreate = true;
        ResponseCapture response = new ResponseCapture();

        new SinhVienServlet(service).doPost(
                request(studentParams("create"), imagePart(), uploadRoot),
                response.proxy());

        assertTrue(service.createCalled);
        assertEquals(0, fileCount(uploadRoot.resolve("sinhvien")));
        assertTrue(response.redirect.contains("error="));
    }

    @Test
    void thamSoThuongKhongHopLeDuocChanTruocKhiGhiAnhSinhVien()
            throws Exception {
        FakeSinhVienService service = new FakeSinhVienService();
        Map<String, String> params = studentParams("create");
        params.put("maLop", "khong-hop-le");

        new SinhVienServlet(service).doPost(
                request(params, imagePart(), uploadRoot),
                new ResponseCapture().proxy());

        assertFalse(service.createCalled);
        assertEquals(0, fileCount(uploadRoot.resolve("sinhvien")));
    }

    @Test
    void capNhatSinhVienThanhCongXoaAnhCuVaGiuAnhMoi() throws Exception {
        FakeSinhVienService service = new FakeSinhVienService();
        service.existingStudent = student(17, "old-student.png");
        writeStored("sinhvien", "old-student.png");
        Map<String, String> params = studentParams("update");
        params.put("id", "17");

        new SinhVienServlet(service).doPost(
                request(params, imagePart(), uploadRoot),
                new ResponseCapture().proxy());

        assertNotNull(service.savedStudent);
        assertNotEquals("old-student.png", service.savedStudent.getHinhAnh());
        assertFalse(Files.exists(
                uploadRoot.resolve("sinhvien").resolve("old-student.png")));
        assertTrue(Files.isRegularFile(uploadRoot.resolve("sinhvien")
                .resolve(service.savedStudent.getHinhAnh())));
    }

    @Test
    void capNhatSinhVienKhongTaiAnhThiBoQuaHiddenGiaMaoVaGiuAnhDb()
            throws Exception {
        FakeSinhVienService service = new FakeSinhVienService();
        service.existingStudent = student(17, "trusted-student.png");
        writeStored("sinhvien", "trusted-student.png");
        writeStored("sinhvien", "attacker.png");
        Map<String, String> params = studentParams("update");
        params.put("id", "17");
        params.put("hinhAnhCu", "attacker.png");

        new SinhVienServlet(service).doPost(
                request(params, null, uploadRoot),
                new ResponseCapture().proxy());

        assertEquals("trusted-student.png", service.savedStudent.getHinhAnh());
        assertTrue(Files.exists(uploadRoot.resolve("sinhvien")
                .resolve("trusted-student.png")));
        assertTrue(Files.exists(uploadRoot.resolve("sinhvien")
                .resolve("attacker.png")));
    }

    @Test
    void xoaSinhVienThanhCongMoiXoaAnhDaiDien() throws Exception {
        FakeSinhVienService service = new FakeSinhVienService();
        service.existingStudent = student(17, "deleted-student.png");
        writeStored("sinhvien", "deleted-student.png");
        Map<String, String> params = studentParams("delete");
        params.put("id", "17");

        new SinhVienServlet(service).doPost(
                request(params, null, uploadRoot),
                new ResponseCapture().proxy());

        assertTrue(service.deleteCalled);
        assertFalse(Files.exists(uploadRoot.resolve("sinhvien")
                .resolve("deleted-student.png")));
    }

    @Test
    void capNhatGiangVienThatBaiXoaAnhMoiVaGiuAnhCu() throws Exception {
        FakeGiangVienService service = new FakeGiangVienService();
        service.existingTeacher = teacher(23, "old-teacher.png");
        service.failUpdate = true;
        writeStored("giangvien", "old-teacher.png");
        Map<String, String> params = teacherParams("update");
        params.put("id", "23");

        new GiangVienServlet(service).doPost(
                request(params, imagePart(), uploadRoot),
                new ResponseCapture().proxy());

        assertTrue(service.updateCalled);
        assertTrue(Files.exists(uploadRoot.resolve("giangvien")
                .resolve("old-teacher.png")));
        assertEquals(1, fileCount(uploadRoot.resolve("giangvien")));
    }

    @Test
    void capNhatGiangVienThanhCongXoaAnhCuVaKhongTinHidden()
            throws Exception {
        FakeGiangVienService service = new FakeGiangVienService();
        service.existingTeacher = teacher(23, "old-teacher.png");
        writeStored("giangvien", "old-teacher.png");
        writeStored("giangvien", "hidden-tamper.png");
        Map<String, String> params = teacherParams("update");
        params.put("id", "23");
        params.put("hinhAnhCu", "hidden-tamper.png");

        new GiangVienServlet(service).doPost(
                request(params, imagePart(), uploadRoot),
                new ResponseCapture().proxy());

        assertNotNull(service.savedTeacher);
        assertNotEquals("old-teacher.png", service.savedTeacher.getHinhAnh());
        assertFalse(Files.exists(uploadRoot.resolve("giangvien")
                .resolve("old-teacher.png")));
        assertTrue(Files.exists(uploadRoot.resolve("giangvien")
                .resolve("hidden-tamper.png")));
        assertTrue(Files.exists(uploadRoot.resolve("giangvien")
                .resolve(service.savedTeacher.getHinhAnh())));
    }

    @Test
    void giangVienKhongTaiAnhGiuAnhDbVaXoaHoSoMoiXoaAnh()
            throws Exception {
        FakeGiangVienService service = new FakeGiangVienService();
        service.existingTeacher = teacher(23, "trusted-teacher.png");
        writeStored("giangvien", "trusted-teacher.png");
        Map<String, String> update = teacherParams("update");
        update.put("id", "23");
        update.put("hinhAnhCu", "../attacker.png");

        new GiangVienServlet(service).doPost(
                request(update, null, uploadRoot),
                new ResponseCapture().proxy());

        assertEquals("trusted-teacher.png", service.savedTeacher.getHinhAnh());
        Map<String, String> delete = teacherParams("delete");
        delete.put("id", "23");
        new GiangVienServlet(service).doPost(
                request(delete, null, uploadRoot),
                new ResponseCapture().proxy());
        assertTrue(service.deleteCalled);
        assertFalse(Files.exists(uploadRoot.resolve("giangvien")
                .resolve("trusted-teacher.png")));
    }

    private HttpServletRequest request(Map<String, String> parameters,
                                       Part part,
                                       Path root) {
        ServletContext context = proxy(ServletContext.class, (value, method, args) -> {
            if ("getInitParameter".equals(method.getName())
                    && "qlsv.uploadRoot".equals(args[0])) {
                return root.toString();
            }
            if ("getRealPath".equals(method.getName())) return null;
            return defaultValue(method.getReturnType());
        });
        TaiKhoan admin = new TaiKhoan();
        admin.setTenVaiTro("ADMIN");
        HttpSession session = proxy(HttpSession.class, (value, method, args) -> {
            if ("getAttribute".equals(method.getName())
                    && "user".equals(args[0])) {
                return admin;
            }
            return defaultValue(method.getReturnType());
        });
        return proxy(HttpServletRequest.class, (value, method, args) -> {
            switch (method.getName()) {
                case "getParameter":
                    return parameters.get(args[0]);
                case "getPart":
                    return part;
                case "getServletContext":
                    return context;
                case "getSession":
                    return session;
                case "getContextPath":
                    return "";
                default:
                    return defaultValue(method.getReturnType());
            }
        });
    }

    private Part imagePart() {
        return proxy(Part.class, (value, method, args) -> {
            switch (method.getName()) {
                case "getSubmittedFileName":
                    return "avatar.png";
                case "getContentType":
                    return "image/png";
                case "getSize":
                    return (long) png.length;
                case "getInputStream":
                    return new ByteArrayInputStream(png);
                default:
                    return defaultValue(method.getReturnType());
            }
        });
    }

    private static Map<String, String> studentParams(String action) {
        Map<String, String> values = new HashMap<>();
        values.put("action", action);
        values.put("hoTen", "Sinh viên Test");
        values.put("email", "student@example.test");
        values.put("mssv", "SV-TEST");
        values.put("gioiTinh", "Nam");
        values.put("ngaySinh", "2005-01-01");
        values.put("diaChi", "TP.HCM");
        values.put("cccd", "012345678901");
        values.put("danToc", "Kinh");
        values.put("khoaHoc", "K20");
        values.put("ngayNhapHoc", "2024-09-01");
        values.put("maLop", "2");
        values.put("trangThai", "DangHoc");
        return values;
    }

    private static Map<String, String> teacherParams(String action) {
        Map<String, String> values = new HashMap<>();
        values.put("action", action);
        values.put("hoTen", "Giảng viên Test");
        values.put("email", "teacher@example.test");
        values.put("maChuyenNganh", "3");
        values.put("hocVi", "Thạc sĩ");
        values.put("gioiTinh", "Nữ");
        values.put("ngaySinh", "1985-03-02");
        values.put("diaChi", "Đà Nẵng");
        return values;
    }

    private static SinhVien student(int id, String image) {
        SinhVien value = new SinhVien();
        value.setMaNguoidung(id);
        value.setHinhAnh(image);
        return value;
    }

    private static GiangVien teacher(int id, String image) {
        GiangVien value = new GiangVien();
        value.setMaNguoidung(id);
        value.setHinhAnh(image);
        return value;
    }

    private void writeStored(String folder, String name) throws Exception {
        Path directory = uploadRoot.resolve(folder);
        Files.createDirectories(directory);
        Files.write(directory.resolve(name), png);
    }

    private static long fileCount(Path directory) throws Exception {
        if (!Files.isDirectory(directory)) return 0;
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile).count();
        }
    }

    private static byte[] validPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x336699);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", output));
        return output.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == Void.TYPE) return null;
        return type.isPrimitive()
                ? Array.get(Array.newInstance(type, 1), 0) : null;
    }

    private static final class ResponseCapture {
        String redirect;

        HttpServletResponse proxy() {
            return AvatarLifecycleServletTest.proxy(HttpServletResponse.class,
                    (value, method, args) -> {
                        if ("sendRedirect".equals(method.getName())) {
                            redirect = (String) args[0];
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class FakeSinhVienService extends SinhVienService {
        SinhVien existingStudent;
        SinhVien savedStudent;
        boolean failCreate;
        boolean createCalled;
        boolean deleteCalled;

        @Override public int create(SinhVien value) throws Exception {
            createCalled = true;
            if (failCreate) throw new Exception("DB create failed");
            savedStudent = value;
            return 101;
        }

        @Override public void update(SinhVien value) {
            savedStudent = value;
        }

        @Override public void delete(int id) {
            deleteCalled = true;
        }

        @Override public SinhVien findByMaNguoidung(int id) {
            return existingStudent;
        }
    }

    private static final class FakeGiangVienService extends GiangVienService {
        GiangVien existingTeacher;
        GiangVien savedTeacher;
        boolean failUpdate;
        boolean updateCalled;
        boolean deleteCalled;

        @Override public int create(GiangVien value) {
            savedTeacher = value;
            return 202;
        }

        @Override public void update(GiangVien value) throws Exception {
            updateCalled = true;
            if (failUpdate) throw new Exception("DB update failed");
            savedTeacher = value;
        }

        @Override public void delete(int id) {
            deleteCalled = true;
        }

        @Override public GiangVien findByMaNguoidung(int id) {
            return existingTeacher;
        }
    }
}
