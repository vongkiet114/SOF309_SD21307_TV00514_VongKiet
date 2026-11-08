package Repository;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogDeleteClassificationContractTest {
    @Test void bayRepositoryDanhMucPhanLoaiRiengLoiLenhXoa() throws Exception {
        String[] repositories = {
                "KhoaRepository.java", "ChuyenNganhRepository.java", "LopHocRepository.java",
                "MonHocRepository.java", "HocKyRepository.java", "PhongHocRepository.java",
                "CaHocRepository.java"
        };

        for (String file : repositories) {
            String path = "src/main/java/Repository/" + file;
            String source = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            assertTrue(source.contains("throw SqlServerDeleteExceptionClassifier.classify("), path);
        }
    }
}
