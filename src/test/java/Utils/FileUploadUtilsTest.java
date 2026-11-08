package Utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUploadUtilsTest {
    Path tempRoot;

    @BeforeEach
    void setUpWorkspaceTemp() throws Exception {
        Path workspaceTemp = Paths.get("target", "avatar-test-tmp")
                .toAbsolutePath().normalize();
        Files.createDirectories(workspaceTemp);
        tempRoot = Files.createTempDirectory(workspaceTemp, "storage-");
    }

    @Test
    void tuChoiDuoiFileThucThiVaNoiDungGiaAnh() {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        assertThrows(IllegalArgumentException.class, () -> FileUploadUtils.uploadImage(
                request(part("shell.jsp", "image/png", png, png.length)),
                "hinhAnhFile", "sinhvien"));

        byte[] fake = "not-an-image".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> FileUploadUtils.uploadImage(
                request(part("avatar.jpg", "image/jpeg", fake, fake.length)),
                "hinhAnhFile", "sinhvien"));

        assertThrows(IllegalArgumentException.class, () -> FileUploadUtils.uploadImage(
                request(part("avatar.png", "image/png", png, png.length)),
                "hinhAnhFile", "sinhvien"));

        byte[] webpHeader = new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
        assertThrows(IllegalArgumentException.class, () -> FileUploadUtils.uploadImage(
                request(part("avatar.webp", "image/webp", webpHeader, webpHeader.length)),
                "hinhAnhFile", "sinhvien"));
    }

    @Test
    void tuChoiAnhVuotQuaNamMb() {
        assertThrows(IllegalArgumentException.class, () -> FileUploadUtils.uploadImage(
                request(part("avatar.png", "image/png", new byte[0], 5L * 1024 * 1024 + 1)),
                "hinhAnhFile", "sinhvien"));
    }

    @Test
    void luuAnhHopLeBangTenTuSinhTrongThuMucChoPhep() throws Exception {
        byte[] png = validPng();
        Path uploadRoot = tempRoot.resolve("persistent-uploads");

        String stored = FileUploadUtils.uploadImage(
                request(part("anh ca nhan.png", "image/png", png, png.length),
                        uploadRoot, null),
                "hinhAnhFile", "sinhvien");

        assertTrue(stored.endsWith(".png"));
        assertFalse(stored.contains("anh_ca_nhan"));
        assertTrue(Files.isRegularFile(uploadRoot.resolve("sinhvien").resolve(stored)));
        assertTrue(ImageIO.read(uploadRoot.resolve("sinhvien").resolve(stored).toFile()) != null);
    }

    @Test
    void taiMaHoaLaiAnhDeLoaiBoDuLieuGanThem() throws Exception {
        byte[] png = validPng();
        Path uploadRoot = tempRoot.resolve("persistent-uploads");
        byte[] marker = "<script>khong-duoc-luu</script>"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayOutputStream polyglot = new ByteArrayOutputStream();
        polyglot.write(png);
        polyglot.write(marker);

        String stored = FileUploadUtils.uploadImage(
                request(part("avatar.png", "image/png", polyglot.toByteArray(), polyglot.size()),
                        uploadRoot, null),
                "hinhAnhFile", "giangvien");

        byte[] saved = Files.readAllBytes(uploadRoot.resolve("giangvien").resolve(stored));
        assertFalse(new String(saved, java.nio.charset.StandardCharsets.ISO_8859_1)
                .contains("khong-duoc-luu"));
    }

    @Test
    void tuChoiThuMucLuuNamTrongThuMucTrienKhai() throws Exception {
        Path deployedRoot = Files.createDirectories(tempRoot.resolve("deployed-webapp"));
        Path unsafeRoot = deployedRoot.resolve("assets").resolve("uploads");
        byte[] png = validPng();

        IOException error = assertThrows(IOException.class, () ->
                FileUploadUtils.uploadImage(
                        request(part("avatar.png", "image/png", png, png.length),
                                unsafeRoot, deployedRoot),
                        "hinhAnhFile", "sinhvien"));

        assertTrue(error.getMessage().contains("ngoài thư mục triển khai"));
        assertFalse(Files.exists(unsafeRoot));
    }

    @Test
    void chiChoPhepDocVaXoaMotTenAnhAnToanTrongThuMucDuocPhep() throws Exception {
        Path uploadRoot = tempRoot.resolve("persistent-uploads");
        byte[] png = validPng();
        HttpServletRequest request = request(
                part("avatar.png", "image/png", png, png.length),
                uploadRoot, null);
        String stored = FileUploadUtils.uploadImage(
                request, "hinhAnhFile", "sinhvien");
        ServletContext context = request.getServletContext();

        assertEquals(uploadRoot.resolve("sinhvien").resolve(stored),
                FileUploadUtils.resolveImage(context, "sinhvien", stored));
        assertNull(FileUploadUtils.resolveImage(context, "sinhvien", "missing.png"));
        assertThrows(IllegalArgumentException.class, () ->
                FileUploadUtils.resolveImage(context, "sinhvien", "../" + stored));
        assertThrows(IllegalArgumentException.class, () ->
                FileUploadUtils.resolveImage(context, "../sinhvien", stored));
        assertThrows(IllegalArgumentException.class, () ->
                FileUploadUtils.resolveImage(context, "sinhvien", "avatar.jsp"));

        assertTrue(FileUploadUtils.deleteImage(context, "sinhvien", stored));
        assertFalse(Files.exists(uploadRoot.resolve("sinhvien").resolve(stored)));
        assertFalse(FileUploadUtils.deleteImage(context, "sinhvien", stored));
    }

    @Test
    void tuChoiAncestorLinkTroVaoWebRootTruocKhiTaoBatKyThuMucNao()
            throws Exception {
        Path deployedRoot = Files.createDirectories(tempRoot.resolve("deployed-webapp"));
        Path outside = Files.createDirectories(tempRoot.resolve("outside"));
        Path ancestorLink = outside.resolve("ancestor-link");
        createDirectoryLink(ancestorLink, deployedRoot);
        Path configuredRoot = ancestorLink.resolve("persistent-uploads");
        byte[] png = validPng();

        IOException error = assertThrows(IOException.class, () ->
                FileUploadUtils.uploadImage(
                        request(part("avatar.png", "image/png", png, png.length),
                                configuredRoot, deployedRoot),
                        "hinhAnhFile", "sinhvien"));

        assertTrue(error.getMessage().contains("ngoài thư mục triển khai"));
        assertFalse(Files.exists(deployedRoot.resolve("persistent-uploads")),
                "Không được tạo upload root xuyên qua ancestor link.");
    }

    @Test
    void directRootLinkBiTuChoiTruocKhiTaoThuMucCon()
            throws Exception {
        Path deployedRoot = Files.createDirectories(tempRoot.resolve("deployed-webapp"));
        Path outside = Files.createDirectories(tempRoot.resolve("outside"));
        Path directLink = outside.resolve("direct-upload-root");
        createDirectoryLink(directLink, deployedRoot);
        byte[] png = validPng();

        IOException error = assertThrows(IOException.class, () ->
                FileUploadUtils.uploadImage(
                        request(part("avatar.png", "image/png", png, png.length),
                                directLink, deployedRoot),
                        "hinhAnhFile", "sinhvien"));

        assertTrue(error.getMessage().contains("ngoài thư mục triển khai"));
        assertFalse(Files.exists(deployedRoot.resolve("sinhvien")),
                "Không được tạo subfolder trước khi từ chối direct link.");
    }

    private static byte[] validPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x336699);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", output));
        return output.toByteArray();
    }

    private static void createDirectoryLink(Path link, Path target)
            throws Exception {
        Exception symbolicLinkFailure = null;
        try {
            Files.createSymbolicLink(link, target);
            return;
        } catch (IOException | UnsupportedOperationException
                 | SecurityException e) {
            symbolicLinkFailure = e;
        }

        if (System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT).contains("win")) {
            Process process = new ProcessBuilder(
                    "cmd.exe", "/d", "/c", "mklink", "/J",
                    link.toString(), target.toString())
                    .redirectErrorStream(true)
                    .start();
            String output;
            try (java.io.InputStream input = process.getInputStream()) {
                output = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            int exit = process.waitFor();
            if (exit == 0 && Files.isDirectory(link)) {
                return;
            }
            throw new AssertionError(
                    "Không thể tạo junction thật cho regression test. "
                            + "exit=" + exit + ", output=" + output,
                    symbolicLinkFailure);
        }

        throw new AssertionError(
                "Filesystem không tạo được symbolic link thật cho regression test.",
                symbolicLinkFailure);
    }

    private HttpServletRequest request(Part part) {
        return request(part, tempRoot.resolve("persistent-uploads"), null);
    }

    private HttpServletRequest request(Part part, Path uploadRoot, Path deployedRoot) {
        ServletContext context = proxy(ServletContext.class, (value, method, args) -> {
            if ("getInitParameter".equals(method.getName())
                    && "qlsv.uploadRoot".equals(args[0])) {
                return uploadRoot.toString();
            }
            if ("getRealPath".equals(method.getName())) {
                return deployedRoot == null ? null : deployedRoot.toString();
            }
            return defaultValue(method.getReturnType());
        });
        return proxy(HttpServletRequest.class, (value, method, args) -> {
            if ("getPart".equals(method.getName())) return part;
            if ("getServletContext".equals(method.getName())) return context;
            return defaultValue(method.getReturnType());
        });
    }

    private static Part part(String name, String contentType, byte[] bytes, long size) {
        return proxy(Part.class, (value, method, args) -> {
            if ("getSubmittedFileName".equals(method.getName())) return name;
            if ("getContentType".equals(method.getName())) return contentType;
            if ("getSize".equals(method.getName())) return size;
            if ("getInputStream".equals(method.getName())) return new ByteArrayInputStream(bytes);
            return defaultValue(method.getReturnType());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == Void.TYPE) return null;
        return type.isPrimitive() ? Array.get(Array.newInstance(type, 1), 0) : null;
    }
}
