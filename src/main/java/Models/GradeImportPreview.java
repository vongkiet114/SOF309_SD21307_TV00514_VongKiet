package Models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GradeImportPreview {
    private final int teacherId;
    private final int sectionId;
    private final String sectionCode;
    private final String fileName;
    private final String token;
    private final Instant createdAt;
    private final String rosterSignature;
    private final List<DiemSo> diemSoList;
    private final List<String> errors;
    private final int addedCount;
    private final int updatedCount;
    private final int deletedCount;
    private final int unchangedBlankCount;

    public GradeImportPreview(int teacherId,
                              int sectionId,
                              String sectionCode,
                              String fileName,
                              String token,
                              Instant createdAt,
                              String rosterSignature,
                              List<DiemSo> diemSoList,
                              List<String> errors,
                              int addedCount,
                              int updatedCount,
                              int deletedCount,
                              int unchangedBlankCount) {
        this.teacherId = teacherId;
        this.sectionId = sectionId;
        this.sectionCode = sectionCode == null ? "" : sectionCode;
        this.fileName = fileName == null ? "" : fileName;
        this.token = token == null ? "" : token;
        this.createdAt = createdAt;
        this.rosterSignature = rosterSignature == null ? "" : rosterSignature;
        this.diemSoList = copyScores(diemSoList);
        this.errors = Collections.unmodifiableList(new ArrayList<>(
                errors == null ? Collections.emptyList() : errors));
        this.addedCount = addedCount;
        this.updatedCount = updatedCount;
        this.deletedCount = deletedCount;
        this.unchangedBlankCount = unchangedBlankCount;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public String getFileName() {
        return fileName;
    }

    public String getToken() {
        return token;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getRosterSignature() {
        return rosterSignature;
    }

    public List<DiemSo> getDiemSoList() {
        return copyScores(diemSoList);
    }

    public List<String> getErrors() {
        return errors;
    }

    public int getAddedCount() {
        return addedCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public int getUnchangedBlankCount() {
        return unchangedBlankCount;
    }

    private static List<DiemSo> copyScores(List<DiemSo> source) {
        List<DiemSo> copy = new ArrayList<>();
        if (source != null) {
            for (DiemSo value : source) {
                if (value != null) copy.add(copyScore(value));
            }
        }
        return Collections.unmodifiableList(copy);
    }

    private static DiemSo copyScore(DiemSo source) {
        DiemSo copy = new DiemSo(source.getMaDiem(), source.getMaDanhsach(),
                source.getMaThanhphan(), source.getGiaTriDiem());
        copy.setTenThanhPhan(source.getTenThanhPhan());
        copy.setTrongSo(source.getTrongSo());
        return copy;
    }
}
