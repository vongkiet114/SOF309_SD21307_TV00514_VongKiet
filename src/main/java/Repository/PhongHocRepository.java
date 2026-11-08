package Repository;

import Models.PhongHoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhongHocRepository {
    private static final String SELECT_ALL =
            "SELECT ma_phong, ten_phong FROM phonghoc ORDER BY ten_phong";
    private static final String SELECT_ONE =
            "SELECT ma_phong, ten_phong FROM phonghoc WHERE ma_phong = ?";
    private static final String INSERT =
            "INSERT INTO phonghoc (ten_phong) VALUES (?)";
    private static final String UPDATE =
            "UPDATE phonghoc SET ten_phong = ? WHERE ma_phong = ?";
    private static final String EXISTS_NAME =
            "SELECT COUNT(*) FROM phonghoc WHERE LOWER(ten_phong) = LOWER(?) AND ma_phong <> ?";
    private static final String LICH_HOC_DEPENDENCY =
            "SELECT COUNT(*) FROM lichhoc WHERE ma_phong = ?";
    private static final String KY_THI_DEPENDENCY =
            "SELECT COUNT(*) FROM kythi WHERE ma_phong = ?";
    private static final String DELETE =
            "DELETE FROM phonghoc WHERE ma_phong = ?";

    public List<PhongHoc> getAll() {
        List<PhongHoc> list = new ArrayList<>();
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

    public PhongHoc getById(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ONE)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean existsByTenPhong(String value, int excludeId) {
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

    public boolean add(PhongHoc value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, value.getTenPhong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(PhongHoc value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, value.getTenPhong());
            ps.setInt(2, value.getMaPhong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public String getDeleteConstraint(int id) {
        try (Connection conn = DBConnect.getConnection()) {
            if (hasDependency(conn, LICH_HOC_DEPENDENCY, id)) {
                return "đang được dùng trong lịch học";
            }
            if (hasDependency(conn, KY_THI_DEPENDENCY, id)) {
                return "đang được dùng trong kỳ thi";
            }
            return null;
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
                    "Không thể truy cập dữ liệu phòng học.", e);
        }
    }

    private boolean hasDependency(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private PhongHoc map(ResultSet rs) throws SQLException {
        PhongHoc phongHoc = new PhongHoc();
        phongHoc.setMaPhong(rs.getInt("ma_phong"));
        phongHoc.setTenPhong(rs.getString("ten_phong"));
        return phongHoc;
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu phòng học.", cause);
    }
}
