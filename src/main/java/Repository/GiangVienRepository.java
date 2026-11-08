package Repository;

import Models.GiangVien;
import Models.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GiangVienRepository {

    private static final String SELECT_JOIN =
            "SELECT gv.*, nd.ho_ten, nd.email, cn.ten_chuyennganh, k.ten_khoa " +
            "FROM giangvien gv " +
            "JOIN nguoidung nd ON gv.ma_nguoidung = nd.ma_nguoidung " +
            "JOIN chuyennganh cn ON gv.ma_chuyennganh = cn.ma_chuyennganh " +
            "JOIN khoa k ON cn.ma_khoa = k.ma_khoa ";

    public List<GiangVien> getAll() {
        return search(null, null, null);
    }

    public List<GiangVien> search(String keyword, Integer maChuyenNganh, String hocVi) {
        List<GiangVien> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_JOIN);
        List<Object> params = new ArrayList<>();
        sql.append("WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (nd.ho_ten LIKE ? OR nd.email LIKE ? OR CAST(gv.ma_nguoidung AS VARCHAR(20)) LIKE ?) ");
            String value = "%" + keyword.trim() + "%";
            params.add(value);
            params.add(value);
            params.add(value);
        }
        if (maChuyenNganh != null && maChuyenNganh > 0) {
            sql.append("AND gv.ma_chuyennganh = ? ");
            params.add(maChuyenNganh);
        }
        if (hocVi != null && !hocVi.trim().isEmpty()) {
            sql.append("AND gv.hoc_vi = ? ");
            params.add(hocVi.trim());
        }
        sql.append("ORDER BY gv.ma_nguoidung");
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public GiangVien findByMaNguoidung(int maNguoidung) {
        String sql = SELECT_JOIN + "WHERE gv.ma_nguoidung = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maNguoidung);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean insert(GiangVien gv) {
        String sql = "INSERT INTO giangvien (ma_nguoidung, ma_chuyennganh, hoc_vi, gioi_tinh, ngay_sinh, dia_chi, hinh_anh) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gv.getMaNguoidung());
            stmt.setInt(2, gv.getMaChuyenNganh());
            stmt.setString(3, gv.getHocVi());
            stmt.setString(4, gv.getGioiTinh());
            stmt.setString(5, blankToNull(gv.getNgaySinh()));
            stmt.setString(6, gv.getDiaChi());
            stmt.setString(7, blankToNull(gv.getHinhAnh()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(GiangVien gv) {
        String sql = "UPDATE giangvien SET ma_chuyennganh = ?, hoc_vi = ?, gioi_tinh = ?, ngay_sinh = ?, dia_chi = ?, hinh_anh = ? " +
                "WHERE ma_nguoidung = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gv.getMaChuyenNganh());
            stmt.setString(2, gv.getHocVi());
            stmt.setString(3, gv.getGioiTinh());
            stmt.setString(4, blankToNull(gv.getNgaySinh()));
            stmt.setString(5, gv.getDiaChi());
            stmt.setString(6, blankToNull(gv.getHinhAnh()));
            stmt.setInt(7, gv.getMaNguoidung());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDeleteConstraint(int maNguoidung) {
        String checkLopHocPhan = "SELECT COUNT(*) FROM lophocphan WHERE ma_giangvien = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkLopHocPhan)) {
            ps.setInt(1, maNguoidung);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Giảng viên đang phụ trách " + rs.getInt(1) + " lớp học phần.";
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean delete(int maNguoidung) {
        // The database schema uses ON DELETE CASCADE from giangvien to nguoidung.
        // Deleting the nguoidung record will automatically delete the giangvien record.
        String sql = "DELETE FROM giangvien WHERE ma_nguoidung = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maNguoidung);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TaiKhoan> getPotentialUsersForTeacherRole() {
        List<TaiKhoan> list = new ArrayList<>();
        // Select users who are not already a student or a teacher
        String sql = "SELECT nd.ma_nguoidung, nd.ho_ten, nd.email " +
                     "FROM nguoidung nd " +
                     "WHERE nd.hoat_dong = 1 " +
                     "  AND NOT EXISTS (SELECT 1 FROM giangvien gv WHERE gv.ma_nguoidung = nd.ma_nguoidung) " +
                     "  AND NOT EXISTS (SELECT 1 FROM sinhvien sv WHERE sv.ma_nguoidung = nd.ma_nguoidung) " +
                     "ORDER BY nd.ma_nguoidung";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                TaiKhoan tk = new TaiKhoan();
                tk.setMaNguoidung(rs.getInt("ma_nguoidung"));
                tk.setHoTen(rs.getString("ho_ten"));
                tk.setEmail(rs.getString("email"));
                list.add(tk);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private GiangVien map(ResultSet rs) throws SQLException {
        GiangVien gv = new GiangVien();
        gv.setMaNguoidung(rs.getInt("ma_nguoidung"));
        gv.setMaChuyenNganh(rs.getInt("ma_chuyennganh"));
        gv.setHocVi(rs.getString("hoc_vi"));
        gv.setGioiTinh(rs.getString("gioi_tinh"));
        gv.setNgaySinh(rs.getString("ngay_sinh"));
        gv.setDiaChi(rs.getString("dia_chi"));
        gv.setHinhAnh(rs.getString("hinh_anh"));

        // Populate transient fields from JOINs
        gv.setHoTen(rs.getString("ho_ten"));
        gv.setEmail(rs.getString("email"));
        gv.setTenChuyenNganh(rs.getString("ten_chuyennganh"));
        gv.setTenKhoa(rs.getString("ten_khoa"));

        return gv;
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
