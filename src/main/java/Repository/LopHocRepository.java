package Repository;

import Models.LopHoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LopHocRepository {
    private static final String SELECT_JOIN =
            "SELECT l.ma_lop, l.ma_lop_hienthi, l.ten_lop, l.ma_chuyennganh, "
                    + "cn.ten_chuyennganh, k.ten_khoa "
                    + "FROM lop l "
                    + "LEFT JOIN chuyennganh cn ON cn.ma_chuyennganh = l.ma_chuyennganh "
                    + "LEFT JOIN khoa k ON k.ma_khoa = cn.ma_khoa ";
    private static final String SELECT_ALL = SELECT_JOIN + "ORDER BY l.ten_lop";
    private static final String SELECT_ONE = SELECT_JOIN + "WHERE l.ma_lop = ?";
    private static final String EXISTS_NAME =
            "SELECT COUNT(*) FROM lop WHERE LOWER(ten_lop) = LOWER(?) AND ma_lop <> ?";
    private static final String NEXT_CODE =
            "SELECT UPPER(ma_cn) AS ma_cn, "
                    + "NEXT VALUE FOR dbo.seq_lop_hienthi AS next_value "
                    + "FROM dbo.chuyennganh WHERE ma_chuyennganh = ?";
    private static final String INSERT =
            "INSERT INTO lop (ma_lop_hienthi, ten_lop, ma_chuyennganh) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE l SET l.ten_lop = ?, l.ma_chuyennganh = cn.ma_chuyennganh, "
                    + "l.ma_lop_hienthi = UPPER(cn.ma_cn) "
                    + "+ SUBSTRING(l.ma_lop_hienthi, CHARINDEX('-', l.ma_lop_hienthi), "
                    + "LEN(l.ma_lop_hienthi)) "
                    + "FROM dbo.lop l "
                    + "JOIN dbo.chuyennganh cn ON cn.ma_chuyennganh = ? "
                    + "WHERE l.ma_lop = ?";
    private static final String SINH_VIEN_DEPENDENCY =
            "SELECT COUNT(*) FROM sinhvien WHERE ma_lop = ?";
    private static final String DELETE = "DELETE FROM lop WHERE ma_lop = ?";

    public List<LopHoc> getAll() {
        List<LopHoc> list = new ArrayList<>();
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

    public LopHoc getById(int id) {
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

    public boolean existsByTenLop(String value, int excludeId) {
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

    public int add(LopHoc value) {
        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement codeStatement = conn.prepareStatement(NEXT_CODE)) {
                    codeStatement.setInt(1, value.getMaChuyenNganh());
                    try (ResultSet rs = codeStatement.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return 0;
                        }
                        value.setMaLopHienThi(rs.getString("ma_cn").toUpperCase(Locale.ROOT)
                                + "-" + String.format("%03d", rs.getInt("next_value")));
                    }
                }

                int id;
                try (PreparedStatement ps = conn.prepareStatement(
                        INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, value.getMaLopHienThi());
                    ps.setString(2, value.getTenLop());
                    ps.setInt(3, value.getMaChuyenNganh());
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return 0;
                    }
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        id = rs.next() ? rs.getInt(1) : 0;
                    }
                }

                if (id <= 0) {
                    conn.rollback();
                    return 0;
                }
                conn.commit();
                return id;
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(LopHoc value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, value.getTenLop());
            ps.setInt(2, value.getMaChuyenNganh());
            ps.setInt(3, value.getMaLop());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public String getDeleteConstraint(int id) {
        if (dependencyCount(SINH_VIEN_DEPENDENCY, id) > 0) {
            return "đang có sinh viên sử dụng";
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
                    "Không thể truy cập dữ liệu lớp.", e);
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

    private LopHoc map(ResultSet rs) throws SQLException {
        LopHoc lop = new LopHoc();
        lop.setMaLop(rs.getInt("ma_lop"));
        lop.setMaLopHienThi(rs.getString("ma_lop_hienthi"));
        lop.setTenLop(rs.getString("ten_lop"));
        lop.setMaChuyenNganh(rs.getInt("ma_chuyennganh"));
        lop.setTenChuyenNganh(rs.getString("ten_chuyennganh"));
        lop.setTenKhoa(rs.getString("ten_khoa"));
        return lop;
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu lớp.", cause);
    }

    private void rollback(Connection connection, SQLException cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }
}
