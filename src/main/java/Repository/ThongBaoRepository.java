package Repository;

import Models.ThongBao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Repository quan ly thong bao va nguoi nhan thong bao.
public class ThongBaoRepository {
    private static final String RECIPIENT_SUMMARY =
            "(SELECT COUNT(*) FROM thongbao_nguoinhan x " +
            " WHERE x.ma_thongbao = t.ma_thongbao) AS so_nguoinhan, " +
            "(SELECT COUNT(*) FROM thongbao_nguoinhan x " +
            " JOIN nguoidung n ON n.ma_nguoidung = x.ma_nguoidung " +
            " JOIN vaitro v ON v.ma_vaitro = n.ma_vaitro " +
            " WHERE x.ma_thongbao = t.ma_thongbao AND v.ten_vaitro = 'STUDENT') AS so_sinhvien, " +
            "(SELECT COUNT(*) FROM thongbao_nguoinhan x " +
            " JOIN nguoidung n ON n.ma_nguoidung = x.ma_nguoidung " +
            " JOIN vaitro v ON v.ma_vaitro = n.ma_vaitro " +
            " WHERE x.ma_thongbao = t.ma_thongbao AND v.ten_vaitro = 'TEACHER') AS so_giangvien, " +
            "(SELECT COUNT(*) FROM thongbao_nguoinhan x " +
            " JOIN nguoidung n ON n.ma_nguoidung = x.ma_nguoidung " +
            " JOIN vaitro v ON v.ma_vaitro = n.ma_vaitro " +
            " WHERE x.ma_thongbao = t.ma_thongbao AND v.ten_vaitro = 'ADMIN') AS so_quantri, " +
            "(SELECT COUNT(*) FROM nguoidung WHERE hoat_dong = 1) AS so_danghoatdong ";

    public List<ThongBao> getAllWithRecipientScope() {
        String sql = "SELECT t.*, " + RECIPIENT_SUMMARY
                + "FROM thongbao t ORDER BY t.ngay_tao DESC, t.ma_thongbao DESC";
        List<ThongBao> notifications = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ThongBao notification = map(rs);
                setRecipientScope(notification, rs);
                notifications.add(notification);
            }
            return notifications;
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải thông báo.", e);
        }
    }

    public List<ThongBao> findByNguoiDung(int maNguoiDung) {
        return findRecentByNguoiDung(maNguoiDung, Integer.MAX_VALUE);
    }

    public List<ThongBao> findRecentByNguoiDung(int maNguoiDung, int limit) {
        if (maNguoiDung <= 0 || limit <= 0) {
            return Collections.emptyList();
        }

        String sql = "SELECT TOP (?) t.*, tnn.da_doc, " + RECIPIENT_SUMMARY
                + "FROM dbo.thongbao t "
                + "JOIN dbo.thongbao_nguoinhan tnn "
                + "ON tnn.ma_thongbao = t.ma_thongbao "
                + "WHERE tnn.ma_nguoidung = ? "
                + "ORDER BY t.ngay_tao DESC, t.ma_thongbao DESC";
        List<ThongBao> notifications = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThongBao notification = map(rs);
                    notification.setDaDoc(rs.getBoolean("da_doc"));
                    setRecipientScope(notification, rs);
                    notifications.add(notification);
                }
            }
            return notifications;
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải thông báo.", e);
        }
    }

    public void insertForAllActiveUsers(ThongBao notification) {
        String insertThongBao = "INSERT INTO thongbao (tieu_de, noi_dung) VALUES (?, ?)";
        String insertNguoiNhan = "INSERT INTO thongbao_nguoinhan "
                + "(ma_thongbao, ma_nguoidung, da_doc) "
                + "SELECT ?, ma_nguoidung, 0 FROM nguoidung WHERE hoat_dong = 1";

        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int maThongBao = insertThongBao(conn, insertThongBao, notification);
                insertRecipients(conn, insertNguoiNhan, maThongBao);
                conn.commit();
            } catch (SQLException e) {
                rollback(conn, e);
                throw new RepositoryException("Không thể gửi thông báo.", e);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể gửi thông báo.", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM thongbao WHERE ma_thongbao = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Không thể xóa thông báo.", e);
        }
    }

    private int insertThongBao(Connection conn, String sql, ThongBao notification) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, notification.getTieuDe());
            ps.setString(2, notification.getNoiDung());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không lấy được mã thông báo vừa tạo.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertRecipients(Connection conn, String sql, int maThongBao) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThongBao);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Không có tài khoản hoạt động để nhận thông báo.");
            }
        }
    }

    private void rollback(Connection conn, SQLException original) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private ThongBao map(ResultSet rs) throws SQLException {
        ThongBao notification = new ThongBao();
        notification.setMaThongbao(rs.getInt("ma_thongbao"));
        notification.setTieuDe(rs.getString("tieu_de"));
        notification.setNoiDung(rs.getString("noi_dung"));
        Timestamp createdAt = rs.getTimestamp("ngay_tao");
        notification.setNgayTao(createdAt == null ? null : createdAt.toLocalDateTime());
        return notification;
    }

    private void setRecipientScope(ThongBao notification, ResultSet rs) throws SQLException {
        notification.setNguoiGui("Phòng Đào tạo");
        notification.setPhamVi(determineScope(
                rs.getInt("so_nguoinhan"),
                rs.getInt("so_sinhvien"),
                rs.getInt("so_giangvien"),
                rs.getInt("so_quantri"),
                rs.getInt("so_danghoatdong")));
    }

    private String determineScope(int total, int students, int teachers,
                                  int admins, int activeAccounts) {
        if (total <= 0) return "Chưa có người nhận";
        if (activeAccounts > 0 && total == activeAccounts) return "Toàn trường";
        if (total == students) return "Sinh viên";
        if (total == teachers) return "Giảng viên";
        if (total == admins) return "Quản trị viên";
        return "Nhóm người nhận";
    }
}
