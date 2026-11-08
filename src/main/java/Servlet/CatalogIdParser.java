package Servlet;

final class CatalogIdParser {
    private static final int INVALID_ID = -1;

    private CatalogIdParser() {
    }

    static int parseSaveId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return INVALID_ID;
        }
    }
}
