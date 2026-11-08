package Services;

import Models.DiemSo;
import Models.GradeImportPreview;
import Models.LopHocPhan;
import Models.SinhVien;
import Models.ThanhPhanDiem;
import Repository.LopHocPhanRepository;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradeExcelServiceTest {
    private static final Instant CREATED_AT = Instant.parse("2026-07-15T06:00:00Z");

    private FakeNhapDiemService gradebook;
    private FakeSectionRepository sections;
    private GradeExcelService service;

    @BeforeEach
    void setUp() {
        gradebook = new FakeNhapDiemService();
        sections = new FakeSectionRepository();
        sections.section = section(12, 7, "CNTT-LHP-00012", "Lập trình Java");
        gradebook.students.add(student(101, "SV001", "Nguyễn An"));
        gradebook.students.add(student(102, "SV002", "Trần Bình"));
        gradebook.components.add(component(201, "Chuyên cần"));
        gradebook.components.add(component(202, "Cuối kỳ"));
        gradebook.existing.put(101, new ArrayList<>(Collections.singletonList(
                score(101, 201, new BigDecimal("7.00")))));
        service = new GradeExcelService(gradebook, sections, new SecureRandom());
    }

    @Test
    void xuatVaNhapLaiCungMotMau() throws Exception {
        byte[] workbook = fillScores(template(), new double[][]{
                {8.25, 9.0},
                {Double.NaN, Double.NaN}
        });

        GradeImportPreview preview = service.preview(7, "diem.xlsx", workbook.length,
                new ByteArrayInputStream(workbook), CREATED_AT);

        assertTrue(preview.getErrors().isEmpty());
        assertEquals(1, preview.getAddedCount());
        assertEquals(1, preview.getUpdatedCount());
        assertEquals(0, preview.getDeletedCount());
        assertEquals(2, preview.getUnchangedBlankCount());
        assertEquals(4, preview.getDiemSoList().size());
        assertEquals(new BigDecimal("8.25"), find(preview, 101, 201));
        assertEquals(new BigDecimal("9.0"), find(preview, 101, 202));
    }

    @Test
    void mauExcelCoCauTrucCoDinhKhoaDanhTinhVaMetadataAn() throws Exception {
        byte[] bytes = template();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("Bang diem", workbook.getSheetName(0));
            assertEquals("_meta", workbook.getSheetName(1));
            assertTrue(workbook.isSheetHidden(1));

            XSSFSheet sheet = workbook.getSheet("Bang diem");
            assertEquals("STT", sheet.getRow(3).getCell(0).getStringCellValue());
            assertEquals("MSSV", sheet.getRow(3).getCell(1).getStringCellValue());
            assertEquals("Họ tên", sheet.getRow(3).getCell(2).getStringCellValue());
            assertEquals("Chuyên cần", sheet.getRow(3).getCell(3).getStringCellValue());
            assertTrue(sheet.getRow(4).getCell(1).getCellStyle().getLocked());
            assertFalse(sheet.getRow(4).getCell(3).getCellStyle().getLocked());
            assertTrue(sheet.getProtect());

            PaneInformation pane = sheet.getPaneInformation();
            assertNotNull(pane);
            assertTrue(pane.isFreezePane());
            assertEquals(3, pane.getVerticalSplitPosition());
            assertEquals(4, pane.getHorizontalSplitPosition());
            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertFalse(validations.isEmpty());

            Map<String, String> metadata = metadata(workbook);
            assertEquals(Set.of("templateVersion", "sectionId", "teacherId",
                    "componentIds", "rosterSignature"), metadata.keySet());
            assertEquals("1", metadata.get("templateVersion"));
            assertEquals("12", metadata.get("sectionId"));
            assertEquals("7", metadata.get("teacherId"));
            assertEquals("201,202", metadata.get("componentIds"));
            assertFalse(metadata.get("rosterSignature").isEmpty());
        }
    }

    @Test
    void tuChoiMssvBiSuaVaDongBiTrung() throws Exception {
        byte[] tampered = editWorkbook(template(), workbook -> {
            XSSFSheet sheet = workbook.getSheet("Bang diem");
            sheet.getRow(4).getCell(1).setCellValue("SV999");
            sheet.getRow(5).getCell(1).setCellValue("SV999");
        });

        GradeImportPreview preview = preview(tampered);

        assertContainsError(preview, "MSSV");
        assertContainsError(preview, "trùng");
    }

    @Test
    void tuChoiKhiDanhSachSinhVienDaThayDoiSauLucTaiMau() throws Exception {
        byte[] original = template();
        gradebook.students.add(student(103, "SV003", "Lê Chi"));

        GradeImportPreview preview = preview(original);

        assertContainsError(preview, "hết hiệu lực");
    }

    @Test
    void tuChoiThanhPhanBiDoiVaSoCotKhongDung() throws Exception {
        byte[] tampered = editWorkbook(template(), workbook -> {
            XSSFSheet sheet = workbook.getSheet("Bang diem");
            sheet.getRow(3).getCell(3).setCellValue("Thành phần lạ");
            sheet.getRow(3).createCell(5).setCellValue("Cột lạ");
        });

        GradeImportPreview preview = preview(tampered);

        assertContainsError(preview, "thành phần");
        assertContainsError(preview, "số cột");
    }

    @Test
    void tuChoiCotDuBiChenVaoDongDuLieu() throws Exception {
        byte[] tampered = editWorkbook(template(), workbook ->
                workbook.getSheet("Bang diem").getRow(4)
                        .createCell(5).setCellFormula("1+1"));

        GradeImportPreview preview = preview(tampered);

        assertContainsError(preview, "số cột");
    }

    @Test
    void tuChoiCongThucTrongODiem() throws Exception {
        byte[] formula = editWorkbook(template(), workbook ->
                workbook.getSheet("Bang diem").getRow(4).getCell(3)
                        .setCellFormula("1+1"));

        GradeImportPreview preview = preview(formula);

        assertContainsError(preview, "công thức");
    }

    @Test
    void tuChoiChuoiVaDiemNgoaiKhoangHoacQuaHaiSoThapPhan() throws Exception {
        byte[] invalid = editWorkbook(template(), workbook -> {
            Row first = workbook.getSheet("Bang diem").getRow(4);
            first.getCell(3).setCellValue("8.5");
            first.getCell(4).setCellValue(10.123);
        });

        GradeImportPreview preview = preview(invalid);

        assertContainsError(preview, "phải là số");
        assertContainsError(preview, "tối đa hai");
    }

    @Test
    void tuChoiSaiDinhDangQuaDungLuongVaMacroPart() throws Exception {
        byte[] bytes = template();
        AcademicException extension = assertThrows(AcademicException.class,
                () -> service.preview(7, "diem.xls", bytes.length,
                        new ByteArrayInputStream(bytes), CREATED_AT));
        assertEquals(AcademicException.Code.INVALID_INPUT, extension.getCode());

        AcademicException size = assertThrows(AcademicException.class,
                () -> service.preview(7, "diem.xlsx", 5L * 1024 * 1024 + 1,
                        new ByteArrayInputStream(bytes), CREATED_AT));
        assertEquals(AcademicException.Code.INVALID_INPUT, size.getCode());

        byte[] macro = addMacroPart(bytes);
        AcademicException macroError = assertThrows(AcademicException.class,
                () -> service.preview(7, "diem.xlsx", macro.length,
                        new ByteArrayInputStream(macro), CREATED_AT));
        assertTrue(macroError.getMessage().toLowerCase().contains("macro"));
    }

    @Test
    void previewCoTokenHaiTramNamMuoiSauBitUrlSafeVaSaoChepPhongThu() throws Exception {
        GradeImportPreview preview = preview(template());
        byte[] tokenBytes = Base64.getUrlDecoder().decode(preview.getToken());
        assertEquals(32, tokenBytes.length);
        assertFalse(preview.getToken().contains("="));
        assertFalse(preview.getToken().contains("+"));
        assertFalse(preview.getToken().contains("/"));

        List<DiemSo> exposed = preview.getDiemSoList();
        BigDecimal original = exposed.get(0).getGiaTriDiem();
        exposed.get(0).setGiaTriDiem(BigDecimal.TEN);
        assertEquals(original, preview.getDiemSoList().get(0).getGiaTriDiem());
        assertThrows(UnsupportedOperationException.class,
                () -> preview.getDiemSoList().add(score(1, 1, BigDecimal.ONE)));
        assertThrows(UnsupportedOperationException.class,
                () -> preview.getErrors().add("sửa"));

        GradeImportPreview second = preview(template());
        assertNotEquals(preview.getToken(), second.getToken());
    }

    @Test
    void previewDemDungThemSuaXoaVaGiuTrong() throws Exception {
        gradebook.existing.put(102, new ArrayList<>(Collections.singletonList(
                score(102, 202, new BigDecimal("6.5")))));
        byte[] values = fillScores(template(), new double[][]{
                {8.25, 9.0},
                {Double.NaN, Double.NaN}
        });

        GradeImportPreview preview = preview(values);

        assertEquals(1, preview.getAddedCount());
        assertEquals(1, preview.getUpdatedCount());
        assertEquals(1, preview.getDeletedCount());
        assertEquals(1, preview.getUnchangedBlankCount());
    }

    @Test
    void confirmTrongHanTaiLaiQuyenVaChuKyRoiLuuMotBatch() throws Exception {
        GradeImportPreview preview = preview(fillScores(template(), new double[][]{
                {8.25, 9.0}, {7.5, 8.0}
        }));

        service.confirm(7, preview, CREATED_AT.plusSeconds(15 * 60));

        assertEquals(7, gradebook.savedTeacher);
        assertEquals(12, gradebook.savedSection);
        assertEquals(4, gradebook.saved.size());
        assertTrue(gradebook.readCount >= 2,
                "preview và confirm đều phải tải lại quyền/danh sách");
    }

    @Test
    void confirmQuaMuoiLamPhutBiTuChoi() throws Exception {
        GradeImportPreview preview = preview(template());

        AcademicException error = assertThrows(AcademicException.class,
                () -> service.confirm(7, preview, CREATED_AT.plusSeconds(15 * 60 + 1)));

        assertEquals(AcademicException.Code.EXPIRED_TEMPLATE, error.getCode());
        assertTrue(gradebook.saved == null);
    }

    @Test
    void confirmTuChoiSaiGiangVienPreviewCoLoiVaRosterDaDoi() throws Exception {
        GradeImportPreview valid = preview(template());
        AcademicException teacher = assertThrows(AcademicException.class,
                () -> service.confirm(8, valid, CREATED_AT.plusSeconds(60)));
        assertEquals(AcademicException.Code.FORBIDDEN, teacher.getCode());

        GradeImportPreview invalid = preview(editWorkbook(template(), workbook ->
                workbook.getSheet("Bang diem").getRow(4).getCell(3)
                        .setCellFormula("1+1")));
        AcademicException errors = assertThrows(AcademicException.class,
                () -> service.confirm(7, invalid, CREATED_AT.plusSeconds(60)));
        assertEquals(AcademicException.Code.TAMPERED_TEMPLATE, errors.getCode());

        gradebook.students.get(0).setMssv("SV-CHANGED");
        AcademicException changed = assertThrows(AcademicException.class,
                () -> service.confirm(7, valid, CREATED_AT.plusSeconds(60)));
        assertEquals(AcademicException.Code.TAMPERED_TEMPLATE, changed.getCode());
        assertTrue(changed.getMessage().contains("hết hiệu lực"));
        assertTrue(gradebook.saved == null);
    }

    @Test
    void previewBaoLoiRoVaKhongChoConfirmKhiDanhSachSinhVienRong() throws Exception {
        byte[] original = template();
        gradebook.students.clear();

        GradeImportPreview preview = preview(original);

        assertContainsError(preview, "chưa có sinh viên");
        AcademicException error = assertThrows(AcademicException.class,
                () -> service.confirm(7, preview, CREATED_AT.plusSeconds(60)));
        assertEquals(AcademicException.Code.TAMPERED_TEMPLATE, error.getCode());
        assertTrue(gradebook.saved == null);
    }

    @Test
    void previewBaoLoiRoVaKhongChoConfirmKhiThanhPhanDiemRong() throws Exception {
        byte[] original = template();
        gradebook.components.clear();

        GradeImportPreview preview = preview(original);

        assertContainsError(preview, "chưa có thành phần điểm");
        AcademicException error = assertThrows(AcademicException.class,
                () -> service.confirm(7, preview, CREATED_AT.plusSeconds(60)));
        assertEquals(AcademicException.Code.TAMPERED_TEMPLATE, error.getCode());
        assertTrue(gradebook.saved == null);
    }

    private byte[] template() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.writeTemplate(7, 12, output);
        return output.toByteArray();
    }

    private GradeImportPreview preview(byte[] workbook) throws Exception {
        return service.preview(7, "diem.xlsx", workbook.length,
                new ByteArrayInputStream(workbook), CREATED_AT);
    }

    private static byte[] fillScores(byte[] template, double[][] values) throws Exception {
        return editWorkbook(template, workbook -> {
            XSSFSheet sheet = workbook.getSheet("Bang diem");
            for (int row = 0; row < values.length; row++) {
                for (int column = 0; column < values[row].length; column++) {
                    Cell cell = sheet.getRow(4 + row).getCell(3 + column);
                    if (Double.isNaN(values[row][column])) {
                        cell.setBlank();
                    } else {
                        cell.setCellValue(values[row][column]);
                    }
                }
            }
        });
    }

    private static byte[] editWorkbook(byte[] original, WorkbookEdit edit) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(original));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            edit.apply(workbook);
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static byte[] addMacroPart(byte[] original) throws Exception {
        Path file = Files.createTempFile("grade-import-macro", ".xlsx");
        try {
            Files.write(file, original);
            try (OPCPackage pkg = OPCPackage.open(file.toFile(), PackageAccess.READ_WRITE)) {
                PackagePart part = pkg.createPart(
                        PackagingURIHelper.createPartName("/xl/vbaProject.bin"),
                        "application/vnd.ms-office.vbaProject");
                try (OutputStream output = part.getOutputStream()) {
                    output.write(new byte[]{1, 2, 3});
                }
            }
            return Files.readAllBytes(file);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static Map<String, String> metadata(XSSFWorkbook workbook) {
        Map<String, String> result = new LinkedHashMap<>();
        XSSFSheet meta = workbook.getSheet("_meta");
        for (Row row : meta) {
            Cell key = row.getCell(0);
            Cell value = row.getCell(1);
            if (key != null && key.getCellType() == CellType.STRING) {
                result.put(key.getStringCellValue(), value.getStringCellValue());
            }
        }
        return result;
    }

    private static BigDecimal find(GradeImportPreview preview,
                                   int membership, int component) {
        return preview.getDiemSoList().stream()
                .filter(value -> value.getMaDanhsach() == membership
                        && value.getMaThanhphan() == component)
                .findFirst().orElseThrow(AssertionError::new).getGiaTriDiem();
    }

    private static void assertContainsError(GradeImportPreview preview, String expected) {
        String all = preview.getErrors().stream().collect(Collectors.joining(" | "));
        assertTrue(all.toLowerCase().contains(expected.toLowerCase()), all);
    }

    private static LopHocPhan section(int id, int teacherId, String code, String subject) {
        LopHocPhan value = new LopHocPhan();
        value.setMaLophocphan(id);
        value.setMaGiangvien(teacherId);
        value.setMaMonhoc(301);
        value.setMaLhp(code);
        value.setTenMonHoc(subject);
        return value;
    }

    private static SinhVien student(int membership, String mssv, String name) {
        SinhVien value = new SinhVien();
        value.setMaDanhSach(membership);
        value.setMssv(mssv);
        value.setHoTen(name);
        return value;
    }

    private static ThanhPhanDiem component(int id, String name) {
        return new ThanhPhanDiem(id, name, new BigDecimal("0.5"), 301);
    }

    private static DiemSo score(int membership, int component, BigDecimal value) {
        DiemSo score = new DiemSo();
        score.setMaDanhsach(membership);
        score.setMaThanhphan(component);
        score.setGiaTriDiem(value);
        return score;
    }

    @FunctionalInterface
    private interface WorkbookEdit {
        void apply(XSSFWorkbook workbook) throws Exception;
    }

    private static final class FakeSectionRepository extends LopHocPhanRepository {
        private LopHocPhan section;

        @Override
        public LopHocPhan findById(int id) {
            return section != null && section.getMaLophocphan() == id ? section : null;
        }
    }

    private static final class FakeNhapDiemService extends NhapDiemService {
        private final List<SinhVien> students = new ArrayList<>();
        private final List<ThanhPhanDiem> components = new ArrayList<>();
        private final Map<Integer, List<DiemSo>> existing = new LinkedHashMap<>();
        private int readCount;
        private int savedTeacher;
        private int savedSection;
        private List<DiemSo> saved;

        @Override
        public NhapDiemData getNhapDiemData(int teacherId, int sectionId)
                throws AcademicException {
            readCount++;
            if (teacherId != 7 || sectionId != 12) {
                throw new AcademicException(AcademicException.Code.FORBIDDEN,
                        "Không có quyền.");
            }
            return new NhapDiemData(students, components, existing);
        }

        @Override
        public void saveDiem(int teacherId, int sectionId, List<DiemSo> values) {
            savedTeacher = teacherId;
            savedSection = sectionId;
            saved = new ArrayList<>(values);
        }
    }
}
