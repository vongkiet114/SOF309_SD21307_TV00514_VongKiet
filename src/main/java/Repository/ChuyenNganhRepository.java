package Repository;

import Models.ChuyenNganh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChuyenNganhRepository {
    private static final String SELECT_ALL =
            "SELECT cn.ma_chuyennganh, cn.ma_cn, cn.ten_chuyennganh, cn.ma_khoa, k.ten_khoa "
                    + "FROM chuyennganh cn JOIN khoa k ON k.ma_khoa = cn.ma_khoa "
                    + "ORDER BY cn.ma_cn";
    private static final String SELECT_ONE =
            "SELECT cn.ma_chuyennganh, cn.ma_cn, cn.ten_chuyennganh, cn.ma_khoa, k.ten_khoa "
                    + "FROM chuyennganh cn JOIN khoa k ON k.ma_khoa = cn.ma_khoa "
                    + "WHERE cn.ma_chuyennganh = ?";
    private static final String INSERT =
            "INSERT INTO chuyennganh (ma_cn, ten_chuyennganh, ma_khoa) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE chuyennganh SET ma_cn = ?, ten_chuyennganh = ?, ma_khoa = ? WHERE ma_chuyennganh = ?";
    private static final String EXISTS_CODE =
            "SELECT COUNT(*) FROM chuyennganh WHERE UPPER(ma_cn) = UPPER(?) AND ma_chuyennganh <> ?";
    private static final String LOP_DEPENDENCY =
            "SELECT COUNT(*) FROM lop WHERE ma_chuyennganh = ?";
    private static final String MON_HOC_DEPENDENCY =
            "SELECT COUNT(*) FROM monhoc WHERE ma_chuyennganh = ?";
    private static final String GIANG_VIEN_DEPENDENCY =
            "SELECT COUNT(*) FROM giangvien WHERE ma_chuyennganh = ?";
    private static final String DELETE =
            "DELETE FROM chuyennganh WHERE ma_chuyennganh = ?";

    public List<ChuyenNganh> getAll() {
        return search(null, null);
    }

    public List<ChuyenNganh> search(String keyword, Integer maKhoa) {
        List<ChuyenNganh> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT cn.ma_chuyennganh, cn.ma_cn, cn.ten_chuyennganh, cn.ma_khoa, k.ten_khoa "
                        + "FROM chuyennganh cn JOIN khoa k ON k.ma_khoa = cn.ma_khoa WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (cn.ma_cn LIKE ? OR cn.ten_chuyennganh LIKE ? OR k.ten_khoa LIKE ?) ");
            String value = "%" + keyword.trim() + "%";
            params.add(value);
            params.add(value);
            params.add(value);
        }
        if (maKhoa != null && maKhoa > 0) {
            sql.append("AND cn.ma_khoa = ? ");
            params.add(maKhoa);
        }
        sql.append("ORDER BY cn.ma_cn");
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public ChuyenNganh getById(int id) {
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

    public boolean existsByMaCn(String value, int excludeId) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_CODE)) {
            ps.setString(1, value);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean add(ChuyenNganh value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, value.getMaCn());
            ps.setString(2, value.getTenChuyenNganh());
            ps.setInt(3, value.getMaKhoa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(ChuyenNganh value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, value.getMaCn());
            ps.setString(2, value.getTenChuyenNganh());
            ps.setInt(3, value.getMaKhoa());
            ps.setInt(4, value.getMaChuyenNganh());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public String getDeleteConstraint(int id) {
        if (dependencyCount(LOP_DEPENDENCY, id) > 0) {
            return "đang có lớp sử dụng";
        }
        if (dependencyCount(MON_HOC_DEPENDENCY, id) > 0) {
            return "đang có môn học sử dụng";
        }
        if (dependencyCount(GIANG_VIEN_DEPENDENCY, id) > 0) {
            return "đang có giảng viên sử dụng";
        }
        return null;
    }

    public boolean delete(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SqlServerDeleteExceptionClassifier.classify(
                    "Không thể truy cập dữ liệu chuyên ngành.", e);
        }
    }

    private int dependencyCount(String sql, int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    private ChuyenNganh map(ResultSet rs) throws SQLException {
        ChuyenNganh cn = new ChuyenNganh();
        cn.setMaChuyenNganh(rs.getInt("ma_chuyennganh"));
        cn.setMaCn(rs.getString("ma_cn"));
        cn.setTenChuyenNganh(rs.getString("ten_chuyennganh"));
        cn.setMaKhoa(rs.getInt("ma_khoa"));
        cn.setTenKhoa(rs.getString("ten_khoa"));
        return cn;
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu chuyên ngành.", cause);
    }
}
