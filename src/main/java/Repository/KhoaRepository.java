package Repository;

import Models.Khoa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhoaRepository {
    private static final String SELECT_ALL =
            "SELECT ma_khoa, ten_khoa FROM khoa ORDER BY ten_khoa";
    private static final String SELECT_ONE =
            "SELECT ma_khoa, ten_khoa FROM khoa WHERE ma_khoa = ?";
    private static final String INSERT =
            "INSERT INTO khoa (ten_khoa) VALUES (?)";
    private static final String UPDATE =
            "UPDATE khoa SET ten_khoa = ? WHERE ma_khoa = ?";
    private static final String EXISTS_NAME =
            "SELECT COUNT(*) FROM khoa WHERE LOWER(ten_khoa) = LOWER(?) AND ma_khoa <> ?";
    private static final String DEPENDENCY =
            "SELECT COUNT(*) FROM chuyennganh WHERE ma_khoa = ?";
    private static final String DELETE =
            "DELETE FROM khoa WHERE ma_khoa = ?";

    public List<Khoa> getAll() {
        List<Khoa> list = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public Khoa getById(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ONE)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean existsByTenKhoa(String value, int excludeId) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_NAME)) {
            ps.setString(1, value);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean add(Khoa value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, value.getTenKhoa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(Khoa value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, value.getTenKhoa());
            ps.setInt(2, value.getMaKhoa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public String getDeleteConstraint(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DEPENDENCY)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0
                        ? "đang có chuyên ngành sử dụng"
                        : null;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean delete(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SqlServerDeleteExceptionClassifier.classify(
                    "Không thể truy cập dữ liệu khoa.", e);
        }
    }

    private Khoa map(ResultSet rs) throws SQLException {
        Khoa khoa = new Khoa();
        khoa.setMaKhoa(rs.getInt("ma_khoa"));
        khoa.setTenKhoa(rs.getString("ten_khoa"));
        return khoa;
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu khoa.", cause);
    }
}
