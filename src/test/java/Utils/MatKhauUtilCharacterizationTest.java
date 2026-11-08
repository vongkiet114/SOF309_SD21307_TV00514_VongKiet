package Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatKhauUtilCharacterizationTest {
    private static final String LEGACY_HASH_123 =
            "3A4B766F18B7E187D65162B5D705C843B79E571DFDD888EFA8E782045AC6B283";

    @Test
    void legacyHashVanDangNhapDuoc() {
        assertTrue(MatKhauUtil.khop("123", LEGACY_HASH_123));
        assertFalse(MatKhauUtil.khop("sai-mat-khau", LEGACY_HASH_123));
    }

    @Test
    void hashMoiCoSaltNgauNhienVaKiemTraDuoc() {
        String first = MatKhauUtil.bam("123456");
        String second = MatKhauUtil.bam("123456");

        assertTrue(first.startsWith("pbkdf2$"));
        assertNotEquals(first, second);
        assertTrue(MatKhauUtil.khop("123456", first));
        assertFalse(MatKhauUtil.khop("sai", first));
    }

    @Test
    void nhanBietHashCuCanNangCap() {
        assertTrue(MatKhauUtil.canNangCap(LEGACY_HASH_123));
        assertFalse(MatKhauUtil.canNangCap(MatKhauUtil.bam("123456")));
    }

    @Test
    void hashPbkdf2HongKhongLamVoDangNhap() {
        assertFalse(MatKhauUtil.khop("123456", "pbkdf2$sai-dinh-dang"));
    }
}
