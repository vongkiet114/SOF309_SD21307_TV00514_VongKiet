package Repository;

import Models.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanRepository {

    /**
     * Lấy tất cả tài khoản kèm vai trò và mã số (MSSV hoặc mã GV).
     * Dùng cho màn hình quản lý người dùng của Admin.
     */
    public List<TaiKhoan> getAll() {
        String sql = "SELECT nd.*, vt.ten_vaitro, sv.mssv " +
                     "FROM nguoidung nd " +
                     "JOIN vaitro vt ON nd.ma_vaitro = vt.ma_vaitro " +
                     "LEFT JOIN sinhvien sv ON sv.ma_nguoidung = nd.ma_nguoidung " +
                     "ORDER BY nd.ma_nguoidung";
        List<TaiKhoan> danhSach = new ArrayList<>();
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaiKhoan tk = map(rs);
                tk.setMatKhau(null); // Never send password hash to the client list
                tk.setTenVaiTro(rs.getString("ten_vaitro"));
                String mssv = rs.getString("mssv");
                if (mssv != null) {
                    tk.setMaSo(mssv);
                } else if ("TEACHER".equalsIgnoreCase(rs.getString("ten_vaitro"))) {
                    tk.setMaSo(Utils.MaGiangVien.format(tk.getMaNguoidung()));
                }
                danhSach.add(tk);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return danhSach;
    }

    /**
     * Tìm tài khoản theo email. Kèm theo tên vai trò.
     * Dùng cho chức năng đăng nhập.
     */
    public TaiKhoan findByEmail(String email) {
        String sql = "SELECT nd.*, vt.ten_vaitro " +
                     "FROM nguoidung nd " +
                     "JOIN vaitro vt ON nd.ma_vaitro = vt.ma_vaitro " +
                     "WHERE nd.email = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan tk = map(rs);
                    tk.setTenVaiTro(rs.getString("ten_vaitro"));
                    return tk;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tìm tài khoản theo mã người dùng.
     */
    public TaiKhoan findByMaNguoidung(int maNguoidung) {
        String sql = "SELECT * FROM nguoidung WHERE ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNguoidung);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tạo một tài khoản mới.
     * @param taiKhoan Đối tượng tài khoản chứa thông tin cần thiết.
     * @return ID của người dùng mới được tạo, hoặc 0 nếu thất bại.
     */
    public int insert(TaiKhoan taiKhoan) {
        String sql = "INSERT INTO nguoidung(email, mat_khau, ho_ten, ma_vaitro, hoat_dong) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, taiKhoan.getEmail());
            ps.setString(2, taiKhoan.getMatKhau());
            ps.setString(3, taiKhoan.getHoTen());
            ps.setInt(4, taiKhoan.getMaVaitro());
            ps.setBoolean(5, taiKhoan.isHoatDong());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Cập nhật thông tin cơ bản của người dùng.
     */
    public boolean update(TaiKhoan taiKhoan) {
        String sql = "UPDATE nguoidung SET email = ?, ho_ten = ?, ma_vaitro = ?, hoat_dong = ? WHERE ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, taiKhoan.getEmail());
            ps.setString(2, taiKhoan.getHoTen());
            ps.setInt(3, taiKhoan.getMaVaitro());
            ps.setBoolean(4, taiKhoan.isHoatDong());
            ps.setInt(5, taiKhoan.getMaNguoidung());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cập nhật mật khẩu đã được băm cho người dùng.
     */
    public boolean capNhatMatKhau(int maNguoidung, String matKhauBam) {
        String sql = "UPDATE nguoidung SET mat_khau = ? WHERE ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, matKhauBam);
            ps.setInt(2, maNguoidung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cập nhật trạng thái hoạt động (khóa/mở khóa) của tài khoản.
     */
    public boolean capNhatTrangThai(int maNguoidung, boolean hoatDong) {
        String sql = "UPDATE nguoidung SET hoat_dong = ? WHERE ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, hoatDong);
            ps.setInt(2, maNguoidung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Xóa một tài khoản người dùng.
     */
    public boolean delete(int maNguoidung) {
        String sql = "DELETE FROM nguoidung WHERE ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNguoidung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to map a ResultSet row to a TaiKhoan object.
     */
    private TaiKhoan map(ResultSet rs) throws SQLException {
        TaiKhoan t = new TaiKhoan();
        t.setMaNguoidung(rs.getInt("ma_nguoidung"));
        t.setEmail(rs.getString("email"));
        t.setMatKhau(rs.getString("mat_khau"));
        t.setHoTen(rs.getString("ho_ten"));
        t.setMaVaitro(rs.getInt("ma_vaitro"));
        t.setHoatDong(rs.getBoolean("hoat_dong"));
        return t;
    }
}
