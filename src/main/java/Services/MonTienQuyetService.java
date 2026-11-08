package Services;

import Models.MonHocTienQuyet;
import Repository.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MonTienQuyetService {
    private final MonTienQuyetRepository repo = new MonTienQuyetRepository();

    public List<MonHocTienQuyet> getAll() {
        return repo.getAll();
    }

    public void create(MonHocTienQuyet mtq) throws Exception {
        if (mtq.getMaMonhoc() == mtq.getMaMonhocTienquyet()) {
            throw new Exception("Môn học không thể là tiên quyết của chính nó.");
        }
        if (!repo.insert(mtq)) {
            throw new Exception("Thêm môn tiên quyết thất bại (có thể đã tồn tại).");
        }
    }

    public void delete(int maMonHoc, int maMonHocTienQuyet) throws Exception {
        if (!repo.delete(maMonHoc, maMonHocTienQuyet)) {
            throw new Exception("Xóa môn tiên quyết thất bại.");
        }
    }
}

class MonTienQuyetRepository {
    public List<MonHocTienQuyet> getAll() {
        List<MonHocTienQuyet> list = new ArrayList<>();
        String sql = "SELECT * FROM monhoc_tienquyet";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new MonHocTienQuyet(
                    rs.getInt("ma_monhoc"),
                    rs.getInt("ma_monhoc_tienquyet")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public boolean insert(MonHocTienQuyet mtq) {
        String sql = "INSERT INTO monhoc_tienquyet (ma_monhoc, ma_monhoc_tienquyet) VALUES (?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mtq.getMaMonhoc());
            ps.setInt(2, mtq.getMaMonhocTienquyet());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Handle unique constraint violation
            return false;
        }
    }

    public boolean delete(int maMonHoc, int maMonHocTienQuyet) {
        String sql = "DELETE FROM monhoc_tienquyet WHERE ma_monhoc = ? AND ma_monhoc_tienquyet = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maMonHoc);
            ps.setInt(2, maMonHocTienQuyet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
