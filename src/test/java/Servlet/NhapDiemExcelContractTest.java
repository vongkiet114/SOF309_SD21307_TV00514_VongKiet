package Servlet;

import Models.DiemSo;
import Models.GradeImportPreview;
import Models.TaiKhoan;
import Services.AcademicException;
import Services.GradeExcelService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NhapDiemExcelContractTest {
    @Test
    void servletKhaiBaoDungUrlVaGioiHanMultipart() {
        WebServlet mapping = NhapDiemExcelServlet.class.getAnnotation(WebServlet.class);
        assertTrue(Arrays.asList(mapping.value()).contains("/giangvien/nhapdiem/excel"));

        MultipartConfig multipart = NhapDiemExcelServlet.class.getAnnotation(MultipartConfig.class);
        assertEquals(5L * 1024 * 1024, multipart.maxFileSize());
        assertEquals(5L * 1024 * 1024 + 256 * 1024, multipart.maxRequestSize());
        assertTrue(multipart.fileSizeThreshold() >= multipart.maxFileSize(),
                "file hợp lệ không được bị servlet container ghi tạm xuống đĩa");
        assertEquals("gradeImportPreview", NhapDiemExcelServlet.PREVIEW_KEY);
    }

    @Test
    void jspCoTaiMauUploadXlsxCsrfVaTrangPreviewAnToan() throws Exception {
        String gradebook = read("src/main/webapp/WEB-INF/teacher/nhapDiem.jsp");
        String preview = read("src/main/webapp/WEB-INF/teacher/nhapDiemExcelPreview.jsp");

        assertTrue(gradebook.contains("/giangvien/nhapdiem/excel?action=template"));
        assertTrue(gradebook.contains("Tải mẫu Excel"));
        assertTrue(gradebook.contains("enctype=\"multipart/form-data\""));
        assertTrue(gradebook.contains("accept=\".xlsx\""));
        assertTrue(gradebook.contains("name=\"_csrf\""));
        assertTrue(preview.contains("getAddedCount"));
        assertTrue(preview.contains("getUpdatedCount"));
        assertTrue(preview.contains("getDeletedCount"));
        assertTrue(preview.contains("getUnchangedBlankCount"));
        assertTrue(preview.contains("!preview.getErrors().isEmpty()"));
        assertTrue(preview.contains("name=\"token\""));
        assertTrue(preview.contains("Utils.Html.escape"));
    }

    @Test
    void chiTeacherDuocDungConKhachVeLoginVaVaiTroKhacBiCam() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        ResponseCapture anonymousResponse = new ResponseCapture();
        RequestCapture anonymous = new RequestCapture((TaiKhoan) null);
        anonymous.params.put("action", "template");
        anonymous.params.put("maLHP", "12");

        new NhapDiemExcelServlet(service)
                .doGet(anonymous.proxy(), anonymousResponse.proxy());

        assertEquals("/QLSV/login", anonymousResponse.redirect);
        assertEquals(0, service.writeCount);

        RequestCapture student = new RequestCapture(account(9, "STUDENT"));
        student.params.put("action", "template");
        student.params.put("maLHP", "12");
        ResponseCapture studentResponse = new ResponseCapture();
        new NhapDiemExcelServlet(service).doGet(student.proxy(), studentResponse.proxy());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, studentResponse.errorStatus);
        assertEquals(0, service.writeCount);
    }

    @Test
    void getTemplateLayTeacherTuSessionVaTraXlsxVoiTenFileAnToan() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        RequestCapture request = new RequestCapture(account(7, "TEACHER"));
        request.params.put("action", "template");
        request.params.put("maLHP", "12");
        ResponseCapture response = new ResponseCapture();

        new NhapDiemExcelServlet(service).doGet(request.proxy(), response.proxy());

        assertEquals(7, service.writeTeacher);
        assertEquals(12, service.writeSection);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.contentType);
        String disposition = response.headers.get("Content-Disposition");
        assertTrue(disposition.startsWith("attachment;"));
        assertTrue(disposition.contains("bang-diem-lhp-12.xlsx"));
        assertFalse(disposition.contains("\r"));
        assertFalse(disposition.contains("\n"));
        assertArrayEquals(new byte[]{1, 2, 3}, response.output.toByteArray());
    }

    @Test
    void previewDocPartTrongBoNhoForwardVaChiLuuKetQuaKhongLoi() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        GradeImportPreview valid = preview(Collections.emptyList(), "valid-token");
        service.previewResult = valid;
        RequestCapture request = new RequestCapture(account(7, "TEACHER"));
        request.params.put("action", "preview");
        request.part = new PartCapture("diem.xlsx", new byte[]{4, 5, 6});

        new NhapDiemExcelServlet(service)
                .doPost(request.proxy(), new ResponseCapture().proxy());

        assertEquals(7, service.previewTeacher);
        assertEquals("diem.xlsx", service.previewFileName);
        assertEquals(3, service.previewSize);
        assertArrayEquals(new byte[]{4, 5, 6}, service.previewBytes);
        assertFalse(request.part.writeCalled);
        assertEquals(valid, request.session.attributes.get(NhapDiemExcelServlet.PREVIEW_KEY));
        assertEquals(valid, request.attributes.get("preview"));
        assertEquals("/WEB-INF/teacher/nhapDiemExcelPreview.jsp", request.forwardPath);

        GradeImportPreview invalid = preview(Collections.singletonList("Ô D5 lỗi."),
                "invalid-token");
        service.previewResult = invalid;
        RequestCapture second = new RequestCapture(request.session);
        second.params.put("action", "preview");
        second.part = new PartCapture("diem.xlsx", new byte[]{7});

        new NhapDiemExcelServlet(service)
                .doPost(second.proxy(), new ResponseCapture().proxy());

        assertNull(second.session.attributes.get(NhapDiemExcelServlet.PREVIEW_KEY));
        assertEquals(invalid, second.attributes.get("preview"));
    }

    @Test
    void confirmSoTokenTuRequestVoiSessionSaiThiKhongLuu() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        RequestCapture request = confirmRequest(preview(Collections.emptyList(), "right-token"));
        request.params.put("token", "wrong-token");
        ResponseCapture response = new ResponseCapture();

        new NhapDiemExcelServlet(service).doPost(request.proxy(), response.proxy());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.errorStatus);
        assertEquals(0, service.confirmCount);
        assertTrue(request.session.attributes.containsKey(NhapDiemExcelServlet.PREVIEW_KEY));
    }

    @Test
    void confirmThanhCongXoaSessionRedirectVaTokenKhongDungLaiDuoc() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        GradeImportPreview preview = preview(Collections.emptyList(), "single-use-token");
        RequestCapture request = confirmRequest(preview);
        ResponseCapture response = new ResponseCapture();

        NhapDiemExcelServlet servlet = new NhapDiemExcelServlet(service);
        servlet.doPost(request.proxy(), response.proxy());

        assertEquals(1, service.confirmCount);
        assertEquals(7, service.confirmTeacher);
        assertEquals(preview, service.confirmPreview);
        assertNull(request.session.attributes.get(NhapDiemExcelServlet.PREVIEW_KEY));
        assertTrue(response.redirect.startsWith("/QLSV/giangvien/nhapdiem?maLHP=12&msg="));

        RequestCapture reuse = new RequestCapture(request.session);
        reuse.params.put("action", "confirm");
        reuse.params.put("token", "single-use-token");
        ResponseCapture reuseResponse = new ResponseCapture();
        servlet.doPost(reuse.proxy(), reuseResponse.proxy());

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, reuseResponse.errorStatus);
        assertEquals(1, service.confirmCount);
    }

    @Test
    void haiConfirmDongThoiChiMotRequestDuocLuu() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        service.blockFirstConfirm = true;
        GradeImportPreview preview = preview(Collections.emptyList(), "concurrent-token");
        RequestCapture first = confirmRequest(preview);
        RequestCapture second = new RequestCapture(first.session);
        second.params.put("action", "confirm");
        second.params.put("token", preview.getToken());
        ResponseCapture firstResponse = new ResponseCapture();
        ResponseCapture secondResponse = new ResponseCapture();
        NhapDiemExcelServlet servlet = new NhapDiemExcelServlet(service);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread firstThread = requestThread(() ->
                servlet.doPost(first.proxy(), firstResponse.proxy()), failure);
        Thread secondThread = requestThread(() ->
                servlet.doPost(second.proxy(), secondResponse.proxy()), failure);
        firstThread.start();
        assertTrue(service.firstConfirmEntered.await(2, TimeUnit.SECONDS));
        secondThread.start();
        boolean secondReachedService = service.secondConfirmEntered.await(
                250, TimeUnit.MILLISECONDS);
        service.releaseFirstConfirm.countDown();
        firstThread.join(2000);
        secondThread.join(2000);

        assertFalse(secondReachedService,
                "request thứ hai không được vượt qua token đang xác nhận");
        assertEquals(1, service.confirmCount);
        assertNull(failure.get());
        assertTrue((firstResponse.redirect != null
                && secondResponse.errorStatus == HttpServletResponse.SC_BAD_REQUEST)
                || (secondResponse.redirect != null
                && firstResponse.errorStatus == HttpServletResponse.SC_BAD_REQUEST));
    }

    @Test
    void previewMoiChoConfirmCuVaSauDoVanConDeXacNhan() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        service.blockFirstConfirm = true;
        GradeImportPreview firstPreview = preview(Collections.emptyList(), "token-a");
        RequestCapture confirmA = confirmRequest(firstPreview);
        ResponseCapture confirmAResponse = new ResponseCapture();
        NhapDiemExcelServlet servlet = new NhapDiemExcelServlet(service);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread confirmThread = requestThread(() ->
                servlet.doPost(confirmA.proxy(), confirmAResponse.proxy()), failure);
        confirmThread.start();
        assertTrue(service.firstConfirmEntered.await(2, TimeUnit.SECONDS));

        GradeImportPreview secondPreview = preview(Collections.emptyList(), "token-b");
        service.previewResult = secondPreview;
        RequestCapture previewB = new RequestCapture(confirmA.session);
        previewB.params.put("action", "preview");
        previewB.part = new PartCapture("diem-b.xlsx", new byte[]{7, 8, 9});
        Thread previewThread = requestThread(() ->
                servlet.doPost(previewB.proxy(), new ResponseCapture().proxy()), failure);
        previewThread.start();
        assertTrue(service.previewEntered.await(2, TimeUnit.SECONDS));

        boolean previewCompletedWhileConfirming =
                previewB.forwardCompleted.await(250, TimeUnit.MILLISECONDS);
        Object storedWhileConfirming = confirmA.session.attributes.get(
                NhapDiemExcelServlet.PREVIEW_KEY);
        service.releaseFirstConfirm.countDown();
        confirmThread.join(2000);
        previewThread.join(2000);

        assertFalse(previewCompletedWhileConfirming,
                "preview B phải chờ confirm A hoàn tất trên cùng session");
        assertEquals(firstPreview, storedWhileConfirming,
                "preview đang xác nhận không được thay giữa giao dịch confirm");
        assertNull(failure.get());
        assertFalse(confirmThread.isAlive());
        assertFalse(previewThread.isAlive());
        assertEquals(secondPreview, confirmA.session.attributes.get(
                NhapDiemExcelServlet.PREVIEW_KEY),
                "confirm A không được xóa nhầm preview B");

        RequestCapture confirmB = new RequestCapture(confirmA.session);
        confirmB.params.put("action", "confirm");
        confirmB.params.put("token", secondPreview.getToken());
        ResponseCapture confirmBResponse = new ResponseCapture();
        servlet.doPost(confirmB.proxy(), confirmBResponse.proxy());

        assertEquals(2, service.confirmCount);
        assertEquals(secondPreview, service.confirmPreview);
        assertNull(confirmA.session.attributes.get(NhapDiemExcelServlet.PREVIEW_KEY));
        assertTrue(confirmBResponse.redirect != null);
    }

    @Test
    void previewLoiDinhDangVanForwardThongBaoNhungKhongLoNoiBo() throws Exception {
        FakeGradeExcelService service = new FakeGradeExcelService();
        service.previewError = new AcademicException(AcademicException.Code.INVALID_INPUT,
                "Chỉ chấp nhận file Excel .xlsx.");
        RequestCapture request = new RequestCapture(account(7, "TEACHER"));
        request.params.put("action", "preview");
        request.part = new PartCapture("diem.txt", new byte[]{1});
        ResponseCapture response = new ResponseCapture();

        new NhapDiemExcelServlet(service).doPost(request.proxy(), response.proxy());

        assertEquals("Chỉ chấp nhận file Excel .xlsx.", request.attributes.get("error"));
        assertEquals("/WEB-INF/teacher/nhapDiemExcelPreview.jsp", request.forwardPath);
        assertEquals(0, response.errorStatus);
        assertNull(request.session.attributes.get(NhapDiemExcelServlet.PREVIEW_KEY));
    }

    private static RequestCapture confirmRequest(GradeImportPreview preview) {
        RequestCapture request = new RequestCapture(account(7, "TEACHER"));
        request.params.put("action", "confirm");
        request.params.put("token", preview.getToken());
        request.session.attributes.put(NhapDiemExcelServlet.PREVIEW_KEY, preview);
        return request;
    }

    private static GradeImportPreview preview(List<String> errors, String token) {
        List<DiemSo> scores = new ArrayList<>();
        DiemSo score = new DiemSo();
        score.setMaDanhsach(101);
        score.setMaThanhphan(201);
        scores.add(score);
        return new GradeImportPreview(7, 12, "CNTT-LHP-00012", "diem.xlsx",
                token, Instant.parse("2026-07-15T06:00:00Z"), "signature",
                scores, errors, 1, 2, 3, 4);
    }

    private static TaiKhoan account(int id, String role) {
        TaiKhoan value = new TaiKhoan();
        value.setMaNguoidung(id);
        value.setTenVaiTro(role);
        return value;
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static Thread requestThread(ThrowingRequest request,
                                        AtomicReference<Throwable> failure) {
        return new Thread(() -> {
            try {
                request.run();
            } catch (Throwable error) {
                failure.compareAndSet(null, error);
            }
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

    private static final class SessionCapture {
        private final Map<String, Object> attributes =
                Collections.synchronizedMap(new HashMap<>());
        private final HttpSession proxy = NhapDiemExcelContractTest.proxy(
                HttpSession.class, this::invoke);

        private HttpSession proxy() {
            return proxy;
        }

        private Object invoke(Object value, java.lang.reflect.Method method, Object[] args) {
            if ("getAttribute".equals(method.getName())) {
                return attributes.get(args[0]);
            }
            if ("setAttribute".equals(method.getName())) {
                attributes.put((String) args[0], args[1]);
                return null;
            }
            if ("removeAttribute".equals(method.getName())) {
                attributes.remove(args[0]);
                return null;
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static final class RequestCapture {
        private final Map<String, String> params = new LinkedHashMap<>();
        private final Map<String, Object> attributes = new HashMap<>();
        private final SessionCapture session;
        private PartCapture part;
        private String forwardPath;
        private final CountDownLatch forwardCompleted = new CountDownLatch(1);

        private RequestCapture(TaiKhoan account) {
            this.session = account == null ? null : new SessionCapture();
            if (session != null) session.attributes.put("user", account);
        }

        private RequestCapture(SessionCapture session) {
            this.session = session;
        }

        private HttpServletRequest proxy() {
            return NhapDiemExcelContractTest.proxy(HttpServletRequest.class,
                    (value, method, args) -> {
                        if ("getSession".equals(method.getName())) {
                            return session == null ? null : session.proxy();
                        }
                        if ("getParameter".equals(method.getName())) {
                            return params.get(args[0]);
                        }
                        if ("getPart".equals(method.getName())) return part == null ? null : part.proxy();
                        if ("setAttribute".equals(method.getName())) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        }
                        if ("getContextPath".equals(method.getName())) return "/QLSV";
                        if ("getRequestDispatcher".equals(method.getName())) {
                            String path = (String) args[0];
                            return NhapDiemExcelContractTest.proxy(RequestDispatcher.class,
                                    (dispatcher, dispatchMethod, dispatchArgs) -> {
                                        if ("forward".equals(dispatchMethod.getName())) {
                                            forwardPath = path;
                                            forwardCompleted.countDown();
                                        }
                                        return defaultValue(dispatchMethod.getReturnType());
                                    });
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class PartCapture {
        private final String submittedFileName;
        private final byte[] bytes;
        private boolean writeCalled;

        private PartCapture(String submittedFileName, byte[] bytes) {
            this.submittedFileName = submittedFileName;
            this.bytes = bytes;
        }

        private Part proxy() {
            return NhapDiemExcelContractTest.proxy(Part.class,
                    (value, method, args) -> {
                        if ("getSubmittedFileName".equals(method.getName())) {
                            return submittedFileName;
                        }
                        if ("getSize".equals(method.getName())) return (long) bytes.length;
                        if ("getInputStream".equals(method.getName())) {
                            return new ByteArrayInputStream(bytes);
                        }
                        if ("write".equals(method.getName())) {
                            writeCalled = true;
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class ResponseCapture {
        private final Map<String, String> headers = new HashMap<>();
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private String contentType;
        private String redirect;
        private int errorStatus;

        private HttpServletResponse proxy() {
            ServletOutputStream stream = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                }

                @Override
                public void write(int value) {
                    output.write(value);
                }
            };
            return NhapDiemExcelContractTest.proxy(HttpServletResponse.class,
                    (value, method, args) -> {
                        if ("setContentType".equals(method.getName())) {
                            contentType = (String) args[0];
                            return null;
                        }
                        if ("setHeader".equals(method.getName())) {
                            headers.put((String) args[0], (String) args[1]);
                            return null;
                        }
                        if ("getOutputStream".equals(method.getName())) return stream;
                        if ("sendRedirect".equals(method.getName())) {
                            redirect = (String) args[0];
                            return null;
                        }
                        if ("sendError".equals(method.getName())) {
                            errorStatus = (Integer) args[0];
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class FakeGradeExcelService extends GradeExcelService {
        private int writeCount;
        private int writeTeacher;
        private int writeSection;
        private int previewTeacher;
        private String previewFileName;
        private long previewSize;
        private byte[] previewBytes;
        private GradeImportPreview previewResult;
        private AcademicException previewError;
        private int confirmCount;
        private int confirmTeacher;
        private GradeImportPreview confirmPreview;
        private boolean blockFirstConfirm;
        private final CountDownLatch firstConfirmEntered = new CountDownLatch(1);
        private final CountDownLatch secondConfirmEntered = new CountDownLatch(1);
        private final CountDownLatch releaseFirstConfirm = new CountDownLatch(1);
        private final CountDownLatch previewEntered = new CountDownLatch(1);

        @Override
        public void writeTemplate(int teacherId, int sectionId, OutputStream output)
                throws AcademicException, IOException {
            writeCount++;
            writeTeacher = teacherId;
            writeSection = sectionId;
            output.write(new byte[]{1, 2, 3});
        }

        @Override
        public GradeImportPreview preview(int teacherId, String fileName, long size,
                                          InputStream input, Instant now)
                throws AcademicException, IOException {
            previewTeacher = teacherId;
            previewFileName = fileName;
            previewSize = size;
            previewBytes = input.readAllBytes();
            previewEntered.countDown();
            if (previewError != null) throw previewError;
            return previewResult;
        }

        @Override
        public void confirm(int teacherId, GradeImportPreview preview, Instant now)
                throws AcademicException {
            int call;
            synchronized (this) {
                call = ++confirmCount;
            }
            confirmTeacher = teacherId;
            confirmPreview = preview;
            if (blockFirstConfirm && call == 1) {
                firstConfirmEntered.countDown();
                try {
                    if (!releaseFirstConfirm.await(2, TimeUnit.SECONDS)) {
                        throw new AssertionError("Timeout chờ request confirm thứ hai.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
            } else if (blockFirstConfirm) {
                secondConfirmEntered.countDown();
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRequest {
        void run() throws Exception;
    }
}
