package Servlet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogIdParserTest {
    @Test void idRongHoacBangKhongLaTaoMoi() {
        assertEquals(0, CatalogIdParser.parseSaveId(null));
        assertEquals(0, CatalogIdParser.parseSaveId(""));
        assertEquals(0, CatalogIdParser.parseSaveId("   "));
        assertEquals(0, CatalogIdParser.parseSaveId("0"));
        assertEquals(0, CatalogIdParser.parseSaveId(" 0 "));
    }

    @Test void idDuongLaCapNhat() {
        assertEquals(12, CatalogIdParser.parseSaveId(" 12 "));
    }

    @Test void idAmHoacSaiDinhDangDuocDanhDauKhongHopLe() {
        assertEquals(-3, CatalogIdParser.parseSaveId("-3"));
        assertEquals(-1, CatalogIdParser.parseSaveId("abc"));
        assertEquals(-1, CatalogIdParser.parseSaveId("2147483648"));
    }
}
