package Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfTokenTest {
    @Test
    void tokenNgauNhienVaSoSanhAnToan() {
        String first = CsrfToken.generate();
        String second = CsrfToken.generate();

        assertNotEquals(first, second);
        assertTrue(CsrfToken.matches(first, first));
        assertFalse(CsrfToken.matches(first, second));
        assertFalse(CsrfToken.matches(first, null));
        assertFalse(CsrfToken.matches(null, first));
    }
}
