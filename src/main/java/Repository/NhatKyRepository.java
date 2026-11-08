package Repository;

import Models.NhatKyHoatDong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// repository nhat ky hoat dong (bang nhatky_hoatdong).
public class NhatKyRepository {

    // ghi 1 dong nhat ky (ban gon)
    public boolean ghi(int maNguoidung, String hanhDong) {
        String sql = "INSERT INTO nhatky_hoatdong (ma_nguoidung, hanh_dong) VALUES (?, ?)";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNguoidung);
            ps.setString(2, hanhDong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // signature cu van giu de compile nhung bo qua cac tham so thua
    public boolean ghi(int maNguoidung, String hanhDong, String doiTuong, Integer maDoituong) {
        return ghi(maNguoidung, hanhDong);
    }

    public boolean ghi(int maNguoidung, String hanhDong, String doiTuong, Integer maDoituong,
                       String ipAddress, String userAgent) {
        return ghi(maNguoidung, hanhDong);
    }

    // lay cac nhat ky gan day nhat (gioi han so dong)
    public List<NhatKyHoatDong> ganDay(int gioiHan) {
        String sql = "SELECT TOP (?) * FROM nhatky_hoatdong ORDER BY thoi_gian DESC";
        List<NhatKyHoatDong> ds = new ArrayList<>();
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gioiHan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhatKyHoatDong nk = new NhatKyHoatDong();
                    nk.setMaNhatky(rs.getInt("ma_nhatky"));
                    nk.setMaNguoidung(rs.getInt("ma_nguoidung"));
                    nk.setHanhDong(rs.getString("hanh_dong"));
                    Timestamp tg = rs.getTimestamp("thoi_gian");
                    nk.setThoiGian(tg == null ? null : tg.toLocalDateTime());
                    ds.add(nk);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}
