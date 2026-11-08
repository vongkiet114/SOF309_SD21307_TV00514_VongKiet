package Repository;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SqlServerVerificationDatabaseTest {

    @Test
    void xacMinhKetNoiVaTruyVanReadOnlyThanhCong() throws Exception {
        try (Connection conn = DBConnect.getConnection()) {
            // Verify connection is valid
            assertTrue(conn.isValid(5), "Kết nối phải hợp lệ");
            
            // Verify we can read data (read-only verification)
            try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 AS result");
                 ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("result"));
            }
        }
    }
}
