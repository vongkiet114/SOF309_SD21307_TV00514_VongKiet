package Repository;

import Models.CaHoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class CaHocRepository {
    private static final String SELECT_ALL =
            "SELECT ma_ca, gio_batdau, gio_ketthuc FROM cahoc ORDER BY gio_batdau";
    private static final String SELECT_ONE =
            "SELECT ma_ca, gio_batdau, gio_ketthuc FROM cahoc WHERE ma_ca = ?";
    private static final String INSERT =
            "INSERT INTO cahoc (gio_batdau, gio_ketthuc) VALUES (?, ?)";
    private static final String UPDATE =
            "UPDATE cahoc SET gio_batdau = ?, gio_ketthuc = ? WHERE ma_ca = ?";
    private static final String LICH_HOC_DEPENDENCY =
            "SELECT COUNT(*) FROM lichhoc WHERE ma_ca = ?";
    private static final String KY_THI_DEPENDENCY =
            "SELECT COUNT(*) FROM kythi WHERE ma_ca = ?";
    private static final String DELETE =
            "DELETE FROM cahoc WHERE ma_ca = ?";

    public List<CaHoc> getAll() {
        List<CaHoc> list = new ArrayList<>();
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

    public CaHoc getById(int id) {
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

    public boolean add(CaHoc value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setTime(1, Time.valueOf(value.getGioBatdau()));
            ps.setTime(2, Time.valueOf(value.getGioKetthuc()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(CaHoc value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setTime(1, Time.valueOf(value.getGioBatdau()));
            ps.setTime(2, Time.valueOf(value.getGioKetthuc()));
            ps.setInt(3, value.getMaCa());
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
                    "Không thể truy cập dữ liệu ca học.", e);
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

    private CaHoc map(ResultSet rs) throws SQLException {
        CaHoc caHoc = new CaHoc();
        caHoc.setMaCa(rs.getInt("ma_ca"));
        Time batDau = rs.getTime("gio_batdau");
        Time ketThuc = rs.getTime("gio_ketthuc");
        caHoc.setGioBatdau(batDau == null ? null : batDau.toLocalTime());
        caHoc.setGioKetthuc(ketThuc == null ? null : ketThuc.toLocalTime());
        return caHoc;
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu ca học.", cause);
    }
}
