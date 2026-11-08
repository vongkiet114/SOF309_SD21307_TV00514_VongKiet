package Repository;

import Models.HoatDongDashboard;

import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Repository lay so lieu tong quan cho trang dashboard cua quan tri vien.
public class DashboardRepository {

    public static class AdminStatistics {
        private final int soSinhVienHoatDong;
        private final int soGiangVienHoatDong;
        private final int soLopHocPhanMo;
        private final int soBuoiHomNay;

        public AdminStatistics(
                int soSinhVienHoatDong,
                int soGiangVienHoatDong,
                int soLopHocPhanMo,
                int soBuoiHomNay) {
            this.soSinhVienHoatDong = soSinhVienHoatDong;
            this.soGiangVienHoatDong = soGiangVienHoatDong;
            this.soLopHocPhanMo = soLopHocPhanMo;
            this.soBuoiHomNay = soBuoiHomNay;
        }

        public int getSoSinhVienHoatDong() {
            return soSinhVienHoatDong;
        }

        public int getSoGiangVienHoatDong() {
            return soGiangVienHoatDong;
        }

        public int getSoLopHocPhanMo() {
            return soLopHocPhanMo;
        }

        public int getSoBuoiHomNay() {
            return soBuoiHomNay;
        }
    }

    public AdminStatistics loadAdminStatistics(LocalDate today) {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM dbo.sinhvien sv " +
                "JOIN dbo.nguoidung nd ON nd.ma_nguoidung = sv.ma_nguoidung " +
                "WHERE nd.hoat_dong = 1 AND sv.trang_thai = 'DangHoc') " +
                "AS sinhvien_hoatdong, " +
                "(SELECT COUNT(*) FROM dbo.giangvien gv " +
                "JOIN dbo.nguoidung nd ON nd.ma_nguoidung = gv.ma_nguoidung " +
                "WHERE nd.hoat_dong = 1) AS giangvien_hoatdong, " +
                "(SELECT COUNT(*) FROM dbo.lophocphan " +
                "WHERE trang_thai IN ('MoLop', 'DangHoc')) AS lophocphan_mo, " +
                "(SELECT COUNT(*) FROM dbo.lichhoc " +
                "WHERE ngay_hoc = ? AND trang_thai = 'BinhThuong') AS buoi_homnay";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AdminStatistics(
                            rs.getInt("sinhvien_hoatdong"),
                            rs.getInt("giangvien_hoatdong"),
                            rs.getInt("lophocphan_mo"),
                            rs.getInt("buoi_homnay"));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Không thể tải số liệu dashboard quản trị.", e);
        }
        return new AdminStatistics(0, 0, 0, 0);
    }

    public List<HoatDongDashboard> findRecentActivities(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        String sql = "SELECT TOP (?) nk.hanh_dong, nk.thoi_gian, nd.ho_ten "
                + "FROM dbo.nhatky_hoatdong nk "
                + "JOIN dbo.nguoidung nd "
                + "ON nd.ma_nguoidung = nk.ma_nguoidung "
                + "ORDER BY nk.thoi_gian DESC, nk.ma_nhatky DESC";
        List<HoatDongDashboard> activities = new ArrayList<>();
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Timestamp timestamp = result.getTimestamp("thoi_gian");
                    activities.add(new HoatDongDashboard(
                            result.getString("ho_ten"),
                            result.getString("hanh_dong"),
                            timestamp == null ? null : timestamp.toLocalDateTime()));
                }
            }
            return activities;
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Không thể tải hoạt động gần đây.", e);
        }
    }

    public int countDistinctActiveStudentsForTeacher(int teacherId) {
        String sql = "SELECT COUNT(DISTINCT ds.ma_nguoidung) "
                + "FROM dbo.lophocphan lhp "
                + "JOIN dbo.danhsach_sv_lhp ds "
                + "ON ds.ma_lophocphan = lhp.ma_lophocphan "
                + "WHERE lhp.ma_giangvien = ? "
                + "AND lhp.trang_thai = 'DangHoc'";
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Không thể tải số liệu dashboard giảng viên.", e);
        }
    }

    public int countUnattendedSessionsForTeacher(int teacherId, LocalDate today) {
        String sql = "SELECT COUNT(*) "
                + "FROM dbo.lichhoc lh "
                + "JOIN dbo.lophocphan lhp "
                + "ON lhp.ma_lophocphan = lh.ma_lophocphan "
                + "WHERE lhp.ma_giangvien = ? "
                + "AND lh.ngay_hoc = ? "
                + "AND lh.trang_thai = 'BinhThuong' "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM dbo.diemdanh dd "
                + "WHERE dd.ma_lichhoc = lh.ma_lichhoc)";
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            statement.setDate(2, Date.valueOf(today));
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Không thể tải số liệu dashboard giảng viên.", e);
        }
    }
}
