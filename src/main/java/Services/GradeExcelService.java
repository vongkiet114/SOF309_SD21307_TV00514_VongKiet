package Services;

import Models.DiemSo;
import Models.GradeImportPreview;
import Models.LopHocPhan;
import Models.SinhVien;
import Models.ThanhPhanDiem;
import Repository.LopHocPhanRepository;
import Repository.RepositoryException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GradeExcelService {
    private static final String GRADE_SHEET = "Bang diem";
    private static final String META_SHEET = "_meta";
    private static final String TEMPLATE_VERSION = "1";
    private static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024;
    private static final int HEADER_ROW = 3;
    private static final int FIRST_DATA_ROW = 4;
    private static final int FIRST_SCORE_COLUMN = 3;
    private static final Duration PREVIEW_TTL = Duration.ofMinutes(15);
    private static final List<String> META_KEYS = Collections.unmodifiableList(Arrays.asList(
            "templateVersion", "sectionId", "teacherId", "componentIds",
            "rosterSignature"));

    private final NhapDiemService gradebookService;
    private final LopHocPhanRepository sectionRepository;
    private final SecureRandom secureRandom;

    public GradeExcelService() {
        this(new NhapDiemService(), new LopHocPhanRepository(), new SecureRandom());
    }

    GradeExcelService(NhapDiemService gradebookService,
                      LopHocPhanRepository sectionRepository,
                      SecureRandom secureRandom) {
        this.gradebookService = gradebookService;
        this.sectionRepository = sectionRepository;
        this.secureRandom = secureRandom;
    }

    public void writeTemplate(int teacherId, int sectionId, OutputStream output)
            throws AcademicException, IOException {
        if (output == null) throw invalid("Không thể tạo file Excel.");
        Snapshot snapshot = loadSnapshot(teacherId, sectionId);
        String signature = rosterSignature(snapshot);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(GRADE_SHEET);
            writeVisibleSheet(workbook, sheet, snapshot);
            writeMetadata(workbook, snapshot, signature);
            workbook.write(output);
        }
    }

    public GradeImportPreview preview(int teacherId,
                                      String fileName,
                                      long size,
                                      InputStream input,
                                      Instant now)
            throws AcademicException, IOException {
        validateUpload(teacherId, fileName, size, input, now);
        byte[] upload = readUpload(input);

        try (OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(upload))) {
            rejectMacroPart(pkg);
            try (XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {
                return parsePreview(teacherId, safeUploadName(fileName), workbook, now);
            }
        } catch (AcademicException e) {
            throw e;
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw invalid("File Excel không hợp lệ hoặc đã bị hỏng.");
        }
    }

    public void confirm(int teacherId, GradeImportPreview preview, Instant now)
            throws AcademicException {
        if (teacherId <= 0 || preview == null || now == null
                || preview.getCreatedAt() == null || preview.getToken().isEmpty()) {
            throw tampered("Phiên xem trước không hợp lệ.");
        }
        if (teacherId != preview.getTeacherId()) {
            throw new AcademicException(AcademicException.Code.FORBIDDEN,
                    "Bạn không được xác nhận bảng điểm này.");
        }
        if (now.isBefore(preview.getCreatedAt())) {
            throw tampered("Phiên xem trước không hợp lệ.");
        }
        if (now.isAfter(preview.getCreatedAt().plus(PREVIEW_TTL))) {
            throw new AcademicException(AcademicException.Code.EXPIRED_TEMPLATE,
                    "Phiên xem trước đã hết hạn. Vui lòng tải lại file Excel.");
        }
        if (!preview.getErrors().isEmpty()) {
            throw tampered("File Excel còn lỗi nên không thể xác nhận.");
        }

        Snapshot current = loadSnapshot(teacherId, preview.getSectionId());
        String currentSignature = rosterSignature(current);
        if (!constantEquals(currentSignature, preview.getRosterSignature())) {
            throw tampered("Mẫu Excel đã hết hiệu lực vì danh sách lớp hoặc thành phần điểm đã thay đổi.");
        }
        validatePreviewGrid(preview.getDiemSoList(), current);
        gradebookService.saveDiem(teacherId, preview.getSectionId(),
                preview.getDiemSoList());
    }

    private void writeVisibleSheet(XSSFWorkbook workbook,
                                   XSSFSheet sheet,
                                   Snapshot snapshot) {
        int lastColumn = FIRST_SCORE_COLUMN + snapshot.components.size() - 1;
        int mergedLastColumn = Math.max(FIRST_SCORE_COLUMN, lastColumn);
        CellStyle titleStyle = titleStyle(workbook);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle lockedStyle = lockedStyle(workbook);
        CellStyle scoreStyle = scoreStyle(workbook);

        Row title = sheet.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("BẢNG ĐIỂM LỚP HỌC PHẦN");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, mergedLastColumn));

        Row section = sheet.createRow(1);
        section.createCell(0).setCellValue("Lớp học phần");
        section.createCell(1).setCellValue(safe(snapshot.section.getMaLhp()));
        Row subject = sheet.createRow(2);
        subject.createCell(0).setCellValue("Môn học");
        subject.createCell(1).setCellValue(safe(snapshot.section.getTenMonHoc()));

        Row header = sheet.createRow(HEADER_ROW);
        writeHeader(header, 0, "STT", headerStyle);
        writeHeader(header, 1, "MSSV", headerStyle);
        writeHeader(header, 2, "Họ tên", headerStyle);
        for (int i = 0; i < snapshot.components.size(); i++) {
            writeHeader(header, FIRST_SCORE_COLUMN + i,
                    safe(snapshot.components.get(i).getTenThanhphan()), headerStyle);
        }

        for (int rowIndex = 0; rowIndex < snapshot.students.size(); rowIndex++) {
            SinhVien student = snapshot.students.get(rowIndex);
            Row row = sheet.createRow(FIRST_DATA_ROW + rowIndex);
            Cell ordinal = row.createCell(0);
            ordinal.setCellValue(rowIndex + 1);
            ordinal.setCellStyle(lockedStyle);
            Cell mssv = row.createCell(1);
            mssv.setCellValue(safe(student.getMssv()));
            mssv.setCellStyle(lockedStyle);
            Cell name = row.createCell(2);
            name.setCellValue(safe(student.getHoTen()));
            name.setCellStyle(lockedStyle);
            for (int componentIndex = 0;
                 componentIndex < snapshot.components.size(); componentIndex++) {
                ThanhPhanDiem component = snapshot.components.get(componentIndex);
                Cell score = row.createCell(FIRST_SCORE_COLUMN + componentIndex);
                BigDecimal existing = existingScore(snapshot, student.getMaDanhSach(),
                        component.getMaThanhphan());
                if (existing != null) score.setCellValue(existing.doubleValue());
                else score.setBlank();
                score.setCellStyle(scoreStyle);
            }
        }

        sheet.setColumnWidth(0, 8 * 256);
        sheet.setColumnWidth(1, 16 * 256);
        sheet.setColumnWidth(2, 28 * 256);
        for (int column = FIRST_SCORE_COLUMN; column <= lastColumn; column++) {
            sheet.setColumnWidth(column, 18 * 256);
        }
        sheet.createFreezePane(FIRST_SCORE_COLUMN, FIRST_DATA_ROW);
        addScoreValidation(sheet, snapshot.students.size(), snapshot.components.size());
        sheet.protectSheet("QLSV-grade-template-v1");
    }

    private void writeMetadata(XSSFWorkbook workbook,
                               Snapshot snapshot,
                               String signature) {
        XSSFSheet metadata = workbook.createSheet(META_SHEET);
        Map<String, String> values = new LinkedHashMap<>();
        values.put("templateVersion", TEMPLATE_VERSION);
        values.put("sectionId", Integer.toString(snapshot.section.getMaLophocphan()));
        values.put("teacherId", Integer.toString(snapshot.teacherId));
        values.put("componentIds", componentIds(snapshot.components));
        values.put("rosterSignature", signature);
        int rowIndex = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Row row = metadata.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        workbook.setSheetHidden(workbook.getSheetIndex(metadata), true);
    }

    private GradeImportPreview parsePreview(int teacherId,
                                            String fileName,
                                            XSSFWorkbook workbook,
                                            Instant now)
            throws AcademicException {
        Map<String, String> metadata = readMetadata(workbook);
        if (!TEMPLATE_VERSION.equals(metadata.get("templateVersion"))) {
            throw tampered("Phiên bản mẫu Excel không được hỗ trợ.");
        }
        int sectionId = positiveMetadata(metadata.get("sectionId"));
        int metadataTeacher = positiveMetadata(metadata.get("teacherId"));
        if (metadataTeacher != teacherId) {
            throw new AcademicException(AcademicException.Code.FORBIDDEN,
                    "Mẫu Excel không thuộc giảng viên đang đăng nhập.");
        }

        Snapshot snapshot = loadSnapshot(teacherId, sectionId);
        List<String> errors = new ArrayList<>();
        if (snapshot.students.isEmpty()) {
            errors.add("Lớp học phần chưa có sinh viên để nhập điểm.");
        }
        if (snapshot.components.isEmpty()) {
            errors.add("Lớp học phần chưa có thành phần điểm để nhập điểm.");
        }
        String currentComponentIds = componentIds(snapshot.components);
        String currentSignature = rosterSignature(snapshot);
        if (!currentComponentIds.equals(metadata.get("componentIds"))
                || !constantEquals(currentSignature, metadata.get("rosterSignature"))) {
            errors.add("Mẫu Excel đã hết hiệu lực vì danh sách lớp hoặc thành phần điểm đã thay đổi.");
        }

        List<DiemSo> values = parseVisibleSheet(workbook, snapshot, errors);
        Counts counts = countChanges(values, snapshot.existingScores);
        return new GradeImportPreview(teacherId, sectionId,
                safe(snapshot.section.getMaLhp()), fileName, token(), now,
                currentSignature, values, errors, counts.added, counts.updated,
                counts.deleted, counts.unchangedBlank);
    }

    private List<DiemSo> parseVisibleSheet(XSSFWorkbook workbook,
                                           Snapshot snapshot,
                                           List<String> errors) {
        XSSFSheet sheet = workbook.getSheet(GRADE_SHEET);
        if (sheet == null || workbook.getNumberOfSheets() != 2
                || workbook.getSheet(META_SHEET) == null
                || !workbook.isSheetHidden(workbook.getSheetIndex(META_SHEET))) {
            errors.add("Mẫu Excel không đúng cấu trúc sheet quy định.");
            return Collections.emptyList();
        }

        Row header = sheet.getRow(HEADER_ROW);
        int expectedColumns = FIRST_SCORE_COLUMN + snapshot.components.size();
        if (header == null) {
            errors.add("Mẫu Excel thiếu dòng tiêu đề.");
            return Collections.emptyList();
        }
        if (header.getLastCellNum() != expectedColumns) {
            errors.add("Mẫu Excel có số cột không đúng.");
        }
        checkText(header.getCell(0), "STT", "Tiêu đề STT đã bị thay đổi.", errors);
        checkText(header.getCell(1), "MSSV", "Tiêu đề MSSV đã bị thay đổi.", errors);
        checkText(header.getCell(2), "Họ tên", "Tiêu đề Họ tên đã bị thay đổi.", errors);
        for (int i = 0; i < snapshot.components.size(); i++) {
            checkText(header.getCell(FIRST_SCORE_COLUMN + i),
                    safe(snapshot.components.get(i).getTenThanhphan()),
                    "Tên thành phần điểm ở cột "
                            + (FIRST_SCORE_COLUMN + i + 1) + " đã bị thay đổi.", errors);
        }

        int expectedLastRow = FIRST_DATA_ROW + snapshot.students.size() - 1;
        if (snapshot.students.isEmpty()) expectedLastRow = HEADER_ROW;
        if (sheet.getLastRowNum() != expectedLastRow) {
            errors.add("Số dòng sinh viên không khớp danh sách lớp hiện tại.");
        }

        List<DiemSo> values = new ArrayList<>();
        Set<String> visibleMssv = new HashSet<>();
        for (int i = 0; i < snapshot.students.size(); i++) {
            int rowNumber = FIRST_DATA_ROW + i;
            Row row = sheet.getRow(rowNumber);
            SinhVien student = snapshot.students.get(i);
            if (row == null) {
                errors.add("Thiếu dòng sinh viên " + safe(student.getMssv()) + ".");
                addBlankRow(values, student, snapshot.components);
                continue;
            }
            if (row.getLastCellNum() > expectedColumns) {
                errors.add("Dòng " + (rowNumber + 1) + " có số cột không đúng.");
            }
            checkOrdinal(row.getCell(0), i + 1, rowNumber + 1, errors);
            String mssv = stringValue(row.getCell(1));
            if (mssv == null || !mssv.equals(safe(student.getMssv()))) {
                errors.add("MSSV tại dòng " + (rowNumber + 1)
                        + " không khớp danh sách lớp.");
            }
            if (mssv != null && !visibleMssv.add(mssv)) {
                errors.add("MSSV " + mssv + " bị trùng trong file Excel.");
            }
            String name = stringValue(row.getCell(2));
            if (name == null || !name.equals(safe(student.getHoTen()))) {
                errors.add("Họ tên tại dòng " + (rowNumber + 1)
                        + " không khớp danh sách lớp.");
            }

            for (int componentIndex = 0;
                 componentIndex < snapshot.components.size(); componentIndex++) {
                ThanhPhanDiem component = snapshot.components.get(componentIndex);
                DiemSo value = new DiemSo();
                value.setMaDanhsach(student.getMaDanhSach());
                value.setMaThanhphan(component.getMaThanhphan());
                Cell cell = row.getCell(FIRST_SCORE_COLUMN + componentIndex);
                value.setGiaTriDiem(score(cell,
                        address(rowNumber, FIRST_SCORE_COLUMN + componentIndex), errors));
                values.add(value);
            }
        }
        return values;
    }

    private BigDecimal score(Cell cell, String address, List<String> errors) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.FORMULA) {
            errors.add("Ô " + address + " không được chứa công thức.");
            return null;
        }
        if (cell.getCellType() != CellType.NUMERIC) {
            errors.add("Ô " + address + " phải là số từ 0 đến 10.");
            return null;
        }
        BigDecimal value = BigDecimal.valueOf(cell.getNumericCellValue());
        if (value.compareTo(BigDecimal.ZERO) < 0
                || value.compareTo(BigDecimal.TEN) > 0
                || Math.max(0, value.stripTrailingZeros().scale()) > 2) {
            errors.add("Ô " + address
                    + " phải từ 0 đến 10 và tối đa hai chữ số thập phân.");
        }
        return value;
    }

    private void validateUpload(int teacherId,
                                String fileName,
                                long size,
                                InputStream input,
                                Instant now) throws AcademicException {
        if (teacherId <= 0 || input == null || now == null) {
            throw invalid("Dữ liệu tải lên không hợp lệ.");
        }
        if (fileName == null
                || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw invalid("Chỉ chấp nhận file Excel .xlsx.");
        }
        if (size <= 0 || size > MAX_UPLOAD_BYTES) {
            throw invalid("File Excel phải nhỏ hơn hoặc bằng 5 MB.");
        }
    }

    private byte[] readUpload(InputStream input) throws IOException, AcademicException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        long total = 0;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > MAX_UPLOAD_BYTES) {
                throw invalid("File Excel phải nhỏ hơn hoặc bằng 5 MB.");
            }
            output.write(buffer, 0, read);
        }
        if (total == 0) throw invalid("File Excel không được để trống.");
        return output.toByteArray();
    }

    private void rejectMacroPart(OPCPackage pkg)
            throws AcademicException, InvalidFormatException {
        for (PackagePart part : pkg.getParts()) {
            String name = part.getPartName().getName();
            if ("/xl/vbaProject.bin".equalsIgnoreCase(name)) {
                throw invalid("File Excel có macro nên không được chấp nhận.");
            }
        }
    }

    private Map<String, String> readMetadata(XSSFWorkbook workbook)
            throws AcademicException {
        Sheet sheet = workbook.getSheet(META_SHEET);
        if (sheet == null || !workbook.isSheetHidden(workbook.getSheetIndex(sheet))) {
            throw tampered("Mẫu Excel thiếu metadata hợp lệ.");
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (Row row : sheet) {
            String key = stringValue(row.getCell(0));
            String value = stringValue(row.getCell(1));
            if (key == null || value == null || values.put(key, value) != null) {
                throw tampered("Metadata của mẫu Excel không hợp lệ.");
            }
        }
        if (!new LinkedHashSet<>(META_KEYS).equals(values.keySet())) {
            throw tampered("Metadata của mẫu Excel không đúng cấu trúc.");
        }
        return values;
    }

    private Snapshot loadSnapshot(int teacherId, int sectionId)
            throws AcademicException {
        if (teacherId <= 0 || sectionId <= 0) {
            throw invalid("Lớp học phần không hợp lệ.");
        }
        NhapDiemService.NhapDiemData data =
                gradebookService.getNhapDiemData(teacherId, sectionId);
        LopHocPhan section = sectionRepository.findById(sectionId);
        if (section == null) {
            throw new AcademicException(AcademicException.Code.NOT_FOUND,
                    "Không tìm thấy lớp học phần.");
        }
        if (section.getMaGiangvien() != teacherId) {
            throw new AcademicException(AcademicException.Code.FORBIDDEN,
                    "Bạn không được truy cập lớp học phần này.");
        }

        List<SinhVien> students = new ArrayList<>(data.getSinhViens());
        students.sort(Comparator.comparing((SinhVien value) -> safe(value.getMssv()))
                .thenComparingInt(SinhVien::getMaDanhSach));
        List<ThanhPhanDiem> components = new ArrayList<>(data.getThanhPhanDiems());
        components.sort(Comparator.comparingInt(ThanhPhanDiem::getMaThanhphan));
        validateSnapshot(students, components);
        Map<Integer, List<DiemSo>> existing = data.getExistingScores() == null
                ? Collections.emptyMap() : data.getExistingScores();
        return new Snapshot(teacherId, section, students, components, existing);
    }

    private void validateSnapshot(List<SinhVien> students,
                                  List<ThanhPhanDiem> components)
            throws AcademicException {
        Set<Integer> memberships = new HashSet<>();
        Set<String> mssvs = new HashSet<>();
        for (SinhVien student : students) {
            if (student == null || student.getMaDanhSach() <= 0
                    || safe(student.getMssv()).isEmpty()
                    || !memberships.add(student.getMaDanhSach())
                    || !mssvs.add(student.getMssv())) {
                throw operationFailed("Danh sách lớp học phần không hợp lệ.");
            }
        }
        Set<Integer> componentIds = new HashSet<>();
        for (ThanhPhanDiem component : components) {
            if (component == null || component.getMaThanhphan() <= 0
                    || safe(component.getTenThanhphan()).isEmpty()
                    || !componentIds.add(component.getMaThanhphan())) {
                throw operationFailed("Thành phần điểm không hợp lệ.");
            }
        }
    }

    private void validatePreviewGrid(List<DiemSo> values, Snapshot snapshot)
            throws AcademicException {
        int expectedSize = snapshot.students.size() * snapshot.components.size();
        if (values == null || values.size() != expectedSize || expectedSize == 0) {
            throw tampered("Dữ liệu xem trước không còn hợp lệ.");
        }
        Set<Long> allowed = new HashSet<>();
        for (SinhVien student : snapshot.students) {
            for (ThanhPhanDiem component : snapshot.components) {
                allowed.add(pair(student.getMaDanhSach(), component.getMaThanhphan()));
            }
        }
        Set<Long> seen = new HashSet<>();
        for (DiemSo value : values) {
            if (value == null) throw tampered("Dữ liệu xem trước không còn hợp lệ.");
            long pair = pair(value.getMaDanhsach(), value.getMaThanhphan());
            BigDecimal score = value.getGiaTriDiem();
            if (!allowed.contains(pair) || !seen.add(pair)
                    || (score != null && (score.compareTo(BigDecimal.ZERO) < 0
                    || score.compareTo(BigDecimal.TEN) > 0
                    || Math.max(0, score.stripTrailingZeros().scale()) > 2))) {
                throw tampered("Dữ liệu xem trước không còn hợp lệ.");
            }
        }
    }

    private Counts countChanges(List<DiemSo> values,
                                Map<Integer, List<DiemSo>> existingScores) {
        Map<Long, BigDecimal> existing = new HashMap<>();
        if (existingScores != null) {
            for (List<DiemSo> scores : existingScores.values()) {
                if (scores == null) continue;
                for (DiemSo score : scores) {
                    if (score != null) existing.put(
                            pair(score.getMaDanhsach(), score.getMaThanhphan()),
                            score.getGiaTriDiem());
                }
            }
        }
        Counts counts = new Counts();
        for (DiemSo value : values) {
            BigDecimal before = existing.get(pair(
                    value.getMaDanhsach(), value.getMaThanhphan()));
            BigDecimal after = value.getGiaTriDiem();
            if (before == null && after == null) counts.unchangedBlank++;
            else if (before == null) counts.added++;
            else if (after == null) counts.deleted++;
            else if (before.compareTo(after) != 0) counts.updated++;
        }
        return counts;
    }

    private String rosterSignature(Snapshot snapshot) {
        StringBuilder canonical = new StringBuilder()
                .append(snapshot.section.getMaLophocphan());
        for (SinhVien student : snapshot.students) {
            canonical.append('|').append(student.getMaDanhSach())
                    .append('|').append(safe(student.getMssv()));
        }
        canonical.append('|').append(componentIds(snapshot.components));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) hex.append(String.format("%02x", value & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private String token() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String componentIds(List<ThanhPhanDiem> components) {
        StringBuilder value = new StringBuilder();
        for (ThanhPhanDiem component : components) {
            if (value.length() > 0) value.append(',');
            value.append(component.getMaThanhphan());
        }
        return value.toString();
    }

    private static BigDecimal existingScore(Snapshot snapshot,
                                            int membershipId,
                                            int componentId) {
        List<DiemSo> scores = snapshot.existingScores.get(membershipId);
        if (scores == null) return null;
        for (DiemSo score : scores) {
            if (score != null && score.getMaThanhphan() == componentId) {
                return score.getGiaTriDiem();
            }
        }
        return null;
    }

    private static void addBlankRow(List<DiemSo> result,
                                    SinhVien student,
                                    List<ThanhPhanDiem> components) {
        for (ThanhPhanDiem component : components) {
            DiemSo value = new DiemSo();
            value.setMaDanhsach(student.getMaDanhSach());
            value.setMaThanhphan(component.getMaThanhphan());
            result.add(value);
        }
    }

    private static void checkText(Cell cell,
                                  String expected,
                                  String error,
                                  List<String> errors) {
        String actual = stringValue(cell);
        if (!expected.equals(actual)) errors.add(error);
    }

    private static void checkOrdinal(Cell cell,
                                     int expected,
                                     int displayRow,
                                     List<String> errors) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC
                || cell.getNumericCellValue() != expected) {
            errors.add("STT tại dòng " + displayRow + " không hợp lệ.");
        }
    }

    private static String stringValue(Cell cell) {
        return cell != null && cell.getCellType() == CellType.STRING
                ? cell.getStringCellValue() : null;
    }

    private static int positiveMetadata(String value) throws AcademicException {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) return parsed;
        } catch (Exception ignored) {
        }
        throw tampered("Metadata của mẫu Excel không hợp lệ.");
    }

    private static String safeUploadName(String fileName) {
        String value = fileName.replace('\\', '/');
        int slash = value.lastIndexOf('/');
        if (slash >= 0) value = value.substring(slash + 1);
        value = value.replace("\r", "").replace("\n", "");
        return value.length() <= 255 ? value : value.substring(value.length() - 255);
    }

    private static String address(int zeroBasedRow, int zeroBasedColumn) {
        int value = zeroBasedColumn + 1;
        StringBuilder column = new StringBuilder();
        while (value > 0) {
            int remainder = (value - 1) % 26;
            column.insert(0, (char) ('A' + remainder));
            value = (value - 1) / 26;
        }
        return column.append(zeroBasedRow + 1).toString();
    }

    private static long pair(int membership, int component) {
        return (((long) membership) << 32) ^ (component & 0xffffffffL);
    }

    private static boolean constantEquals(String left, String right) {
        if (left == null || right == null) return false;
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void writeHeader(Row row,
                                    int column,
                                    String value,
                                    CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void addScoreValidation(XSSFSheet sheet,
                                           int studentCount,
                                           int componentCount) {
        if (studentCount == 0 || componentCount == 0) return;
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createDecimalConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "0", "10");
        CellRangeAddressList range = new CellRangeAddressList(
                FIRST_DATA_ROW, FIRST_DATA_ROW + studentCount - 1,
                FIRST_SCORE_COLUMN, FIRST_SCORE_COLUMN + componentCount - 1);
        DataValidation validation = helper.createValidation(constraint, range);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Điểm không hợp lệ",
                "Chỉ nhập điểm từ 0 đến 10.");
        sheet.addValidationData(validation);
    }

    private static CellStyle titleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = bordered(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setLocked(true);
        return style;
    }

    private static CellStyle lockedStyle(XSSFWorkbook workbook) {
        CellStyle style = bordered(workbook);
        style.setLocked(true);
        return style;
    }

    private static CellStyle scoreStyle(XSSFWorkbook workbook) {
        CellStyle style = bordered(workbook);
        style.setLocked(false);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        return style;
    }

    private static CellStyle bordered(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static AcademicException invalid(String message) {
        return new AcademicException(AcademicException.Code.INVALID_INPUT, message);
    }

    private static AcademicException tampered(String message) {
        return new AcademicException(AcademicException.Code.TAMPERED_TEMPLATE, message);
    }

    private static AcademicException operationFailed(String message) {
        return new AcademicException(AcademicException.Code.OPERATION_FAILED, message);
    }

    private static final class Snapshot {
        private final int teacherId;
        private final LopHocPhan section;
        private final List<SinhVien> students;
        private final List<ThanhPhanDiem> components;
        private final Map<Integer, List<DiemSo>> existingScores;

        private Snapshot(int teacherId,
                         LopHocPhan section,
                         List<SinhVien> students,
                         List<ThanhPhanDiem> components,
                         Map<Integer, List<DiemSo>> existingScores) {
            this.teacherId = teacherId;
            this.section = section;
            this.students = students;
            this.components = components;
            this.existingScores = existingScores;
        }
    }

    private static final class Counts {
        private int added;
        private int updated;
        private int deleted;
        private int unchangedBlank;
    }
}
