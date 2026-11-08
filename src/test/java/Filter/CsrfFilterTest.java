package Filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfFilterTest {
    @Test
    void chiKiemTraCacHttpMethodLamThayDoiDuLieu() {
        assertFalse(CsrfFilter.requiresValidation("GET"));
        assertFalse(CsrfFilter.requiresValidation("HEAD"));
        assertTrue(CsrfFilter.requiresValidation("POST"));
        assertTrue(CsrfFilter.requiresValidation("PUT"));
        assertTrue(CsrfFilter.requiresValidation("PATCH"));
        assertTrue(CsrfFilter.requiresValidation("DELETE"));
    }

    @Test
    void boQuaTaiNguyenTinh() {
        assertTrue(CsrfFilter.isStaticAsset("/QLSV/assets/css/admin.css", "/QLSV"));
        assertFalse(CsrfFilter.isStaticAsset("/QLSV/admin/sinhvien", "/QLSV"));
    }
}
