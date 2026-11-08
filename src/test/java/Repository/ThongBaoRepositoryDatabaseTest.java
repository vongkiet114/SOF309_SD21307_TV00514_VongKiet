package Repository;

import Models.ThongBao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class ThongBaoRepositoryDatabaseTest {
    @Test
    void thongBaoChiGuiSinhVienGiuDungPhamViKhiDocTheoNguoiNhan() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<ThongBao> notifications =
                    new ThongBaoRepository().findRecentByNguoiDung(fixture.studentId, 1000);

            ThongBao notification = notifications.stream()
                    .filter(value -> value.getMaThongbao() == fixture.notificationId)
                    .findFirst()
                    .orElse(null);

            assertNotNull(notification);
            assertEquals("Sinh viên", notification.getPhamVi());
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final String tag = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        private final Connection connection;
        private final int studentId;
        private final int notificationId;

        private Fixture() throws Exception {
            connection = DBConnect.getConnection();
            connection.setAutoCommit(true);
            studentId = scalar("SELECT TOP 1 n.ma_nguoidung "
                    + "FROM dbo.nguoidung n "
                    + "JOIN dbo.vaitro v ON v.ma_vaitro = n.ma_vaitro "
                    + "WHERE v.ten_vaitro = 'STUDENT' "
                    + "ORDER BY n.ma_nguoidung");
            notificationId = insertNotification();
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO dbo.thongbao_nguoinhan "
                            + "(ma_thongbao, ma_nguoidung, da_doc) VALUES (?, ?, 0)")) {
                statement.setInt(1, notificationId);
                statement.setInt(2, studentId);
                statement.executeUpdate();
            }
        }

        private int insertNotification() throws Exception {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO dbo.thongbao (tieu_de, noi_dung) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, "DB notification scope " + tag);
                statement.setString(2, "Recipient-only notification contract");
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new IllegalStateException("Không nhận được mã thông báo.");
                    }
                    return keys.getInt(1);
                }
            }
        }

        private int scalar(String sql) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Không có sinh viên cho DB contract.");
                }
                return resultSet.getInt(1);
            }
        }

        @Override
        public void close() throws Exception {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM dbo.thongbao WHERE ma_thongbao = ?")) {
                statement.setInt(1, notificationId);
                statement.executeUpdate();
            } finally {
                connection.close();
            }
        }
    }
}
