package Repository;

import Models.MonHoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MonHocRepository {
    private static final String SELECT_JOIN =
            "SELECT mh.ma_monhoc, mh.ma_mon, mh.ten_monhoc, mh.so_tinchi, mh.ma_chuyennganh, "
                    + "cn.ten_chuyennganh, cn.ma_khoa, k.ten_khoa "
                    + "FROM monhoc mh "
                    + "LEFT JOIN chuyennganh cn ON cn.ma_chuyennganh = mh.ma_chuyennganh "
                    + "LEFT JOIN khoa k ON k.ma_khoa = cn.ma_khoa ";
    private static final String SELECT_ALL = SELECT_JOIN + "ORDER BY mh.ma_mon";
    private static final String SELECT_ONE = SELECT_JOIN + "WHERE mh.ma_monhoc = ?";
    private static final String EXISTS_CODE =
            "SELECT COUNT(*) FROM monhoc WHERE UPPER(ma_mon) = UPPER(?) AND ma_monhoc <> ?";
    private static final String LOP_HOC_PHAN_DEPENDENCY =
            "SELECT COUNT(*) FROM lophocphan WHERE ma_monhoc = ?";
    private static final String TIEN_QUYET_DEPENDENCY =
            "SELECT COUNT(*) FROM monhoc_tienquyet WHERE ma_monhoc = ? OR ma_monhoc_tienquyet = ?";
    private static final String THANH_PHAN_DIEM_DEPENDENCY =
            "SELECT COUNT(*) FROM thanhphan_diem WHERE ma_monhoc = ?";
    private static final String INSERT =
            "INSERT INTO monhoc (ma_mon, ten_monhoc, so_tinchi, ma_chuyennganh) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE monhoc SET ma_mon = ?, ten_monhoc = ?, so_tinchi = ?, ma_chuyennganh = ? WHERE ma_monhoc = ?";
    private static final String DELETE = "DELETE FROM monhoc WHERE ma_monhoc = ?";

    public List<MonHoc> getAll() {
        return getAll(null);
    }

    public List<MonHoc> getAll(String keyword) {
        List<MonHoc> list = new ArrayList<>();
        String sql = SELECT_ALL;
        boolean hasSearch = keyword != null && !keyword.trim().isEmpty();
        if (hasSearch) {
            sql = SELECT_JOIN + "WHERE mh.ma_mon LIKE ? OR mh.ten_monhoc LIKE ? ORDER BY mh.ma_mon";
        }
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasSearch) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public MonHoc getById(int id) {
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

    public boolean existsByMaMon(String value, int excludeId) {
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

    public boolean add(MonHoc value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, value.getMaMon());
            ps.setString(2, value.getTenMonhoc());
            ps.setInt(3, value.getSoTinchi());
            ps.setInt(4, value.getMaChuyenNganh());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(MonHoc value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, value.getMaMon());
            ps.setString(2, value.getTenMonhoc());
            ps.setInt(3, value.getSoTinchi());
            ps.setInt(4, value.getMaChuyenNganh());
            ps.setInt(5, value.getMaMonhoc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public String getDeleteConstraint(int id) {
        if (dependencyCount(LOP_HOC_PHAN_DEPENDENCY, id, false) > 0) {
            return "đang có lớp học phần sử dụng";
        }
        if (dependencyCount(TIEN_QUYET_DEPENDENCY, id, true) > 0) {
            return "đang có quan hệ môn học tiên quyết sử dụng";
        }
        if (dependencyCount(THANH_PHAN_DIEM_DEPENDENCY, id, false) > 0) {
            return "đang có thành phần điểm sử dụng";
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
                    "Không thể truy cập dữ liệu môn học.", e);
        }
    }

    private int dependencyCount(String sql, int id, boolean bindTwice) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (bindTwice) {
                ps.setInt(2, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    private MonHoc map(ResultSet rs) throws SQLException {
        MonHoc mon = new MonHoc();
        mon.setMaMonhoc(rs.getInt("ma_monhoc"));
        mon.setMaMon(rs.getString("ma_mon"));
        mon.setTenMonhoc(rs.getString("ten_monhoc"));
        mon.setSoTinchi(rs.getInt("so_tinchi"));
        mon.setMaChuyenNganh(rs.getInt("ma_chuyennganh"));
        mon.setTenChuyenNganh(rs.getString("ten_chuyennganh"));
        mon.setMaKhoa(rs.getInt("ma_khoa"));
        mon.setTenKhoa(rs.getString("ten_khoa"));
        return mon;
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu môn học.", cause);
    }
}
