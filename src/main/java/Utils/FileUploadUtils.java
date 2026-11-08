package Utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class FileUploadUtils {
    private static final String UPLOAD_ROOT_INIT_PARAM = "qlsv.uploadRoot";
    private static final String UPLOAD_ROOT_ENV = "QLSV_UPLOAD_ROOT";
    private static final String UPLOAD_ROOT_SYSTEM_PROPERTY = "qlsv.upload.root";
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 4096;
    private static final long MAX_IMAGE_PIXELS = 16L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif"));
    private static final Set<String> ALLOWED_FOLDERS = new HashSet<>(Arrays.asList(
            "sinhvien", "giangvien"));

    private FileUploadUtils() {
    }

    public static String uploadImage(HttpServletRequest request,
                                     String partName,
                                     String subFolder)
            throws IOException, ServletException {
        Part part = request.getPart(partName);
        if (part == null || part.getSize() <= 0) {
            return null;
        }
        if (part.getSize() > MAX_IMAGE_SIZE) {
            throw invalid("Ảnh tải lên không được vượt quá 5 MB.");
        }
        validateFolder(subFolder);

        String extension = extension(part.getSubmittedFileName());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw invalid("Chỉ chấp nhận ảnh JPG, PNG hoặc GIF.");
        }
        String contentType = part.getContentType();
        if (contentType != null
                && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw invalid("Tệp tải lên không phải là hình ảnh.");
        }

        byte[] content;
        try (InputStream input = part.getInputStream()) {
            content = readLimited(input);
        }
        if (!hasExpectedSignature(extension, content)) {
            throw invalid("Nội dung tệp không khớp định dạng hình ảnh.");
        }
        byte[] sanitized = decodeAndSanitize(extension, content);

        ServletContext context = request.getServletContext();
        Path imageRoot = uploadRoot(context);
        Files.createDirectories(imageRoot);
        Path canonicalRoot = imageRoot.toRealPath();
        rejectDeployedWebRoot(context, canonicalRoot);

        Path uploadDirectory = canonicalizePlannedPath(
                canonicalRoot.resolve(subFolder));
        if (!uploadDirectory.startsWith(canonicalRoot)) {
            throw invalid("Thư mục lưu ảnh không hợp lệ.");
        }
        rejectDeployedWebRoot(context, uploadDirectory);
        Files.createDirectories(uploadDirectory);
        Path canonicalUploadDirectory = uploadDirectory.toRealPath();
        if (!canonicalUploadDirectory.startsWith(canonicalRoot)) {
            throw invalid("Thư mục lưu ảnh không hợp lệ.");
        }
        rejectDeployedWebRoot(context, canonicalUploadDirectory);

        String storedName = UUID.randomUUID().toString() + extension;
        Path target = canonicalUploadDirectory.resolve(storedName).normalize();
        Files.write(target, sanitized, StandardOpenOption.CREATE_NEW);
        return storedName;
    }

    public static Path uploadRoot(ServletContext context) throws IOException {
        String configured = trimToNull(context == null
                ? null : context.getInitParameter(UPLOAD_ROOT_INIT_PARAM));
        if (configured == null) {
            configured = trimToNull(System.getenv(UPLOAD_ROOT_ENV));
        }
        if (configured == null) {
            configured = trimToNull(System.getProperty(UPLOAD_ROOT_SYSTEM_PROPERTY));
        }
        if (configured == null) {
            String userHome = trimToNull(System.getProperty("user.home"));
            if (userHome == null) {
                throw new IOException("Không thể xác định thư mục lưu hình ảnh.");
            }
            configured = Paths.get(userHome, ".qlsv", "uploads").toString();
        }

        final Path root;
        try {
            root = Paths.get(configured).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            throw new IOException("Cấu hình thư mục lưu hình ảnh không hợp lệ.", e);
        }
        Path canonicalRoot = canonicalizePlannedPath(root);
        rejectDeployedWebRoot(context, canonicalRoot);
        return canonicalRoot;
    }

    public static Path resolveImage(ServletContext context,
                                    String subFolder,
                                    String storedName) throws IOException {
        validateFolder(subFolder);
        validateStoredName(storedName);

        Path root = uploadRoot(context);
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        Path canonicalRoot = root.toRealPath();
        rejectDeployedWebRoot(context, canonicalRoot);

        Path folder = canonicalRoot.resolve(subFolder).normalize();
        if (!Files.exists(folder, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        Path canonicalFolder = folder.toRealPath();
        if (!canonicalFolder.startsWith(canonicalRoot)) {
            throw invalid("Tên ảnh không hợp lệ.");
        }
        rejectDeployedWebRoot(context, canonicalFolder);

        Path target = canonicalFolder.resolve(storedName).normalize();
        if (!target.startsWith(canonicalFolder)) {
            throw invalid("Tên ảnh không hợp lệ.");
        }
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        Path canonicalTarget = target.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!canonicalTarget.startsWith(canonicalFolder)
                || !canonicalTarget.startsWith(canonicalRoot)) {
            throw invalid("Tên ảnh không hợp lệ.");
        }
        rejectDeployedWebRoot(context, canonicalTarget);
        return canonicalTarget;
    }

    public static boolean deleteImage(ServletContext context,
                                      String subFolder,
                                      String storedName) throws IOException {
        if (trimToNull(storedName) == null) {
            return false;
        }
        Path image = resolveImage(context, subFolder, storedName);
        return image != null && Files.deleteIfExists(image);
    }

    public static void validateImageLocation(String subFolder, String storedName) {
        validateFolder(subFolder);
        validateStoredName(storedName);
    }

    public static String imageContentType(String storedName) {
        validateStoredName(storedName);
        String value = extension(storedName);
        if (".png".equals(value)) return "image/png";
        if (".gif".equals(value)) return "image/gif";
        return "image/jpeg";
    }

    private static void rejectDeployedWebRoot(ServletContext context, Path root)
            throws IOException {
        String realPath = context == null ? null : trimToNull(context.getRealPath(""));
        if (realPath == null) {
            return;
        }
        final Path deployedRoot;
        try {
            deployedRoot = canonicalizePlannedPath(
                    Paths.get(realPath).toAbsolutePath().normalize());
        } catch (RuntimeException e) {
            throw new IOException("Không thể xác định thư mục triển khai ứng dụng.", e);
        }
        if (root.startsWith(deployedRoot)) {
            throw new IOException(
                    "Thư mục lưu hình ảnh phải nằm ngoài thư mục triển khai ứng dụng.");
        }
    }

    private static Path canonicalizePlannedPath(Path path) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        Path existingAncestor = normalized;
        while (existingAncestor != null
                && !Files.exists(existingAncestor, LinkOption.NOFOLLOW_LINKS)) {
            existingAncestor = existingAncestor.getParent();
        }
        if (existingAncestor == null) {
            throw new IOException("Không thể xác định đường dẫn lưu hình ảnh.");
        }

        Path canonicalAncestor = existingAncestor.toRealPath();
        Path missingSuffix = existingAncestor.relativize(normalized);
        return canonicalAncestor.resolve(missingSuffix).normalize();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void validateFolder(String subFolder) {
        if (!ALLOWED_FOLDERS.contains(subFolder)) {
            throw invalid("Thư mục lưu ảnh không hợp lệ.");
        }
    }

    private static void validateStoredName(String storedName) {
        String value = trimToNull(storedName);
        if (value == null
                || value.length() > 128
                || !value.matches("[A-Za-z0-9][A-Za-z0-9._-]*")
                || value.contains("..")
                || !ALLOWED_EXTENSIONS.contains(extension(value))) {
            throw invalid("Tên ảnh không hợp lệ.");
        }
    }

    private static byte[] readLimited(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) >= 0) {
            total += read;
            if (total > MAX_IMAGE_SIZE) {
                throw invalid("Ảnh tải lên không được vượt quá 5 MB.");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static String extension(String submittedName) {
        if (submittedName == null) return "";
        String fileName = Paths.get(submittedName).getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot).toLowerCase(Locale.ROOT);
    }

    private static boolean hasExpectedSignature(String extension, byte[] value) {
        if (".png".equals(extension)) {
            byte[] signature = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
            return startsWith(value, signature);
        }
        if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
            return value.length >= 3 && (value[0] & 0xff) == 0xff
                    && (value[1] & 0xff) == 0xd8 && (value[2] & 0xff) == 0xff;
        }
        if (".gif".equals(extension)) {
            return startsWith(value, "GIF87a".getBytes(java.nio.charset.StandardCharsets.US_ASCII))
                    || startsWith(value, "GIF89a".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        }
        return false;
    }

    private static byte[] decodeAndSanitize(String extension, byte[] content) {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(
                new ByteArrayInputStream(content))) {
            if (imageInput == null) {
                throw invalid("Không thể đọc nội dung hình ảnh.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw invalid("Nội dung tệp không phải là hình ảnh hợp lệ.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixels = (long) width * height;
                if (width <= 0 || height <= 0
                        || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION
                        || pixels > MAX_IMAGE_PIXELS) {
                    throw invalid("Kích thước hình ảnh không hợp lệ hoặc quá lớn.");
                }

                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw invalid("Nội dung tệp không phải là hình ảnh hợp lệ.");
                }
                String format = ".png".equals(extension) ? "png"
                        : ".gif".equals(extension) ? "gif" : "jpg";
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!ImageIO.write(image, format, output)
                        || output.size() <= 0 || output.size() > MAX_IMAGE_SIZE) {
                    throw invalid("Không thể chuẩn hóa hình ảnh tải lên.");
                }
                return output.toByteArray();
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw invalid("Nội dung tệp không phải là hình ảnh hợp lệ.");
        }
    }

    private static boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int index = 0; index < prefix.length; index++) {
            if (value[index] != prefix[index]) return false;
        }
        return true;
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }
}
