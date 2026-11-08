package Repository;

import Models.ThongKeGiangDay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GiangVienPortalRepository {

    public static class ThongBaoRow {
        private String tieuDe;
        private String noiDung;
        private LocalDateTime thoiGian;

        public ThongBaoRow(String tieuDe, String noiDung, LocalDateTime thoiGian) {
            this.tieuDe = tieuDe;
            this.noiDung = noiDung;
            this.thoiGian = thoiGian;
        }

        public String getTieuDe() { return tieuDe; }
        public String getNoiDung() { return noiDung; }
        public LocalDateTime getThoiGian() { return thoiGian; }
    }

    public List<ThongBaoRow> thongBaoCuaToi(int maNguoidung) {
        return new ArrayList<>();
    }

    // Thong ke giang day: tong so ca, so buoi (ngay rieng biet), tong so phut giang
    // tren cac buoi hoc binh thuong da dien ra (den hom nay) cua giang vien.
    public ThongKeGiangDay thongKeGiangDay(int maGiangVien) {
        String sql = "SELECT COUNT(*) AS so_ca, "
                + "COUNT(DISTINCT lh.ngay_hoc) AS so_buoi, "
                + "COALESCE(SUM(DATEDIFF(MINUTE, ca.gio_batdau, ca.gio_ketthuc)), 0) AS tong_phut "
                + "FROM dbo.lichhoc lh "
                + "JOIN dbo.lophocphan lhp ON lhp.ma_lophocphan = lh.ma_lophocphan "
                + "JOIN dbo.cahoc ca ON ca.ma_ca = lh.ma_ca "
                + "WHERE lhp.ma_giangvien = ? "
                + "AND lh.trang_thai = 'BinhThuong' "
                + "AND lh.ngay_hoc <= CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGiangVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ThongKeGiangDay(
                            rs.getInt("so_ca"),
                            rs.getInt("so_buoi"),
                            rs.getInt("tong_phut"));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải thống kê giảng dạy.", e);
        }
        return new ThongKeGiangDay(0, 0, 0);
    }
}
