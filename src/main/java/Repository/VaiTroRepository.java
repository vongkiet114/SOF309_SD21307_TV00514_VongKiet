package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VaiTroRepository {
    public Integer findIdByName(String name) {
        String sql = "SELECT ma_vaitro FROM vaitro WHERE UPPER(ten_vaitro) = ?";
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.toUpperCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("ma_vaitro") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tra cứu vai trò", e);
        }
    }
}
