package Repository;

import Models.ThanhPhanDiem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ThanhPhanDiemRepository {

    public List<ThanhPhanDiem> findByMaMonHoc(int maMonHoc) {
        List<ThanhPhanDiem> list = new ArrayList<>();
        String sql = "SELECT * FROM thanhphan_diem WHERE ma_monhoc = ? ORDER BY ma_thanhphan";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maMonHoc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public ThanhPhanDiem findById(int maThanhphan) {
        String sql = "SELECT * FROM thanhphan_diem WHERE ma_thanhphan = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThanhphan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Tong trong so cua mot mon hoc, khong tinh thanh phan dang sua (exceptId).
    public BigDecimal sumTrongSo(int maMonHoc, int exceptId) {
        String sql = "SELECT COALESCE(SUM(trong_so), 0) FROM thanhphan_diem "
                + "WHERE ma_monhoc = ? AND ma_thanhphan <> ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maMonHoc);
            ps.setInt(2, exceptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean coDiem(int maThanhphan) {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM diemso WHERE ma_thanhphan = ?) "
                + "THEN 1 ELSE 0 END";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThanhphan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean insert(ThanhPhanDiem tp) {
        String sql = "INSERT INTO thanhphan_diem (ten_thanhphan, trong_so, ma_monhoc) "
                + "VALUES (?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tp.getTenThanhphan());
            ps.setBigDecimal(2, tp.getTrongSo());
            ps.setInt(3, tp.getMaMonhoc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(ThanhPhanDiem tp) {
        String sql = "UPDATE thanhphan_diem SET ten_thanhphan = ?, trong_so = ? "
                + "WHERE ma_thanhphan = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tp.getTenThanhphan());
            ps.setBigDecimal(2, tp.getTrongSo());
            ps.setInt(3, tp.getMaThanhphan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int maThanhphan) {
        String sql = "DELETE FROM thanhphan_diem WHERE ma_thanhphan = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThanhphan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ThanhPhanDiem map(ResultSet rs) throws SQLException {
        return new ThanhPhanDiem(
                rs.getInt("ma_thanhphan"),
                rs.getString("ten_thanhphan"),
                rs.getBigDecimal("trong_so"),
                rs.getInt("ma_monhoc")
        );
    }
}
