package Services;

import Models.NhatKyHoatDong;
import Repository.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhatKyService {
    private final NhatKyRepository repo = new NhatKyRepository();

    public List<NhatKyHoatDong> getAll() {
        return repo.getAll();
    }

    public void log(int maNguoiDung, String hanhDong) {
        repo.ghi(maNguoiDung, hanhDong, null);
    }

    public void log(int maNguoiDung, String hanhDong, String ipAddress) {
        repo.ghi(maNguoiDung, hanhDong, ipAddress);
    }
}

class NhatKyRepository {
    public void ghi(int maNguoiDung, String hanhDong, String ipAddress) {
        String sql = "INSERT INTO nhatky_hoatdong (ma_nguoidung, hanh_dong, ip_address) VALUES (?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            ps.setString(2, hanhDong);
            ps.setString(3, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<NhatKyHoatDong> getAll() {
        List<NhatKyHoatDong> list = new ArrayList<>();
        String sql = "SELECT nk.*, nd.ho_ten, vt.ten_vaitro "
                + "FROM nhatky_hoatdong nk "
                + "JOIN nguoidung nd ON nk.ma_nguoidung = nd.ma_nguoidung "
                + "JOIN vaitro vt ON nd.ma_vaitro = vt.ma_vaitro "
                + "ORDER BY nk.thoi_gian DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhatKyHoatDong nk = new NhatKyHoatDong();
                nk.setMaNhatky(rs.getInt("ma_nhatky"));
                nk.setMaNguoidung(rs.getInt("ma_nguoidung"));
                nk.setHanhDong(rs.getString("hanh_dong"));
                nk.setThoiGian(rs.getTimestamp("thoi_gian").toLocalDateTime());
                nk.setHoTenNguoiDung(rs.getString("ho_ten"));
                nk.setTenVaiTro(rs.getString("ten_vaitro"));
                nk.setIpAddress(rs.getString("ip_address"));
                list.add(nk);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
