package Repository;

import Models.KyThi;
import Models.TimetableFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KyThiRepository {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String SELECT_COLUMNS =
            "SELECT kt.*, lhp.ma_lhp, p.ten_phong, c.gio_batdau, "
            + "c.gio_ketthuc, m.ten_monhoc, nd.ho_ten AS ten_giangvien ";
    private static final String FROM_JOIN =
            "FROM dbo.kythi kt "
            + "JOIN dbo.phonghoc p ON kt.ma_phong = p.ma_phong "
            + "JOIN dbo.cahoc c ON kt.ma_ca = c.ma_ca "
            + "JOIN dbo.lophocphan lhp ON kt.ma_lophocphan = lhp.ma_lophocphan "
            + "JOIN dbo.monhoc m ON lhp.ma_monhoc = m.ma_monhoc "
            + "JOIN dbo.giangvien gv ON lhp.ma_giangvien = gv.ma_nguoidung "
            + "JOIN dbo.nguoidung nd ON gv.ma_nguoidung = nd.ma_nguoidung ";
    private static final String SELECT_JOIN = SELECT_COLUMNS + FROM_JOIN;

    public List<KyThi> findForTimetable(TimetableFilter filter) {
        TimetableSqlScope.Scope scope =
                TimetableSqlScope.week(filter, "kt", "ngay_thi");
        String sql = SELECT_COLUMNS + FROM_JOIN + scope.getSql()
                + "ORDER BY kt.ngay_thi, kt.ma_ca, lhp.ma_lhp, kt.ma_kythi";
        return queryMany(sql, scope.getParameters());
    }

    public LocalDate findNextDate(TimetableFilter filter, LocalDate fromDate) {
        TimetableSqlScope.Scope scope =
                TimetableSqlScope.next(filter, "kt", "ngay_thi", fromDate);
        String sql = "SELECT MIN(kt.ngay_thi) " + FROM_JOIN + scope.getSql();
        return queryDate(sql, scope.getParameters());
    }

    public List<KyThi> findByNguoiDung(int maNguoiDung, String vaiTro) {
        List<KyThi> list = new ArrayList<>();
        String sql;
        if ("STUDENT".equalsIgnoreCase(vaiTro)) {
            sql = SELECT_JOIN + "JOIN danhsach_sv_lhp dssv ON kt.ma_lophocphan = dssv.ma_lophocphan " +
                  "WHERE dssv.ma_nguoidung = ? ORDER BY kt.ngay_thi";
        } else if ("TEACHER".equalsIgnoreCase(vaiTro)) {
            sql = SELECT_JOIN + "WHERE lhp.ma_giangvien = ? ORDER BY kt.ngay_thi";
        } else {
            return list;
        }

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
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

    public List<KyThi> findByMaSinhVien(int maSinhVien) {
        return findByNguoiDung(maSinhVien, "STUDENT");
    }
    
    public List<KyThi> getAll() {
        List<KyThi> list = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "ORDER BY kt.ngay_thi");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<KyThi> findByLopHocPhan(int maLopHocPhan) {
        List<KyThi> list = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "WHERE kt.ma_lophocphan = ? ORDER BY kt.ngay_thi")) {
            ps.setInt(1, maLopHocPhan);
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

    public KyThi findById(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "WHERE kt.ma_kythi = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM kythi WHERE ma_kythi = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<KyThi> queryMany(String sql, List<Object> values) {
        List<KyThi> list = new ArrayList<>();
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải thời khóa biểu học/thi.", e);
        }
        return list;
    }

    private LocalDate queryDate(String sql, List<Object> values) {
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                java.sql.Date value = rs.getDate(1);
                return value == null ? null : value.toLocalDate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể tải thời khóa biểu học/thi.", e);
        }
    }

    private void bind(PreparedStatement statement, List<Object> values)
            throws SQLException {
        for (int index = 0; index < values.size(); index++) {
            Object value = values.get(index);
            if (value instanceof LocalDate) {
                statement.setDate(index + 1,
                        java.sql.Date.valueOf((LocalDate) value));
            } else if (value instanceof Integer) {
                statement.setInt(index + 1, (Integer) value);
            } else {
                throw new IllegalArgumentException(
                        "Kiểu tham số timetable không được hỗ trợ.");
            }
        }
    }

    private KyThi map(ResultSet rs) throws SQLException {
        KyThi kt = new KyThi(
                rs.getInt("ma_kythi"),
                rs.getInt("ma_lophocphan"),
                rs.getInt("ma_phong"),
                rs.getInt("ma_ca"),
                rs.getDate("ngay_thi").toLocalDate(),
                rs.getString("loai_kythi")
        );
        kt.setMaLhp(rs.getString("ma_lhp"));
        kt.setTenPhong(rs.getString("ten_phong"));
        kt.setTenCa(rs.getTime("gio_batdau").toLocalTime().format(TIME_FORMAT) + " - " +
                rs.getTime("gio_ketthuc").toLocalTime().format(TIME_FORMAT));
        kt.setTenMonHoc(rs.getString("ten_monhoc"));
        kt.setTenGiangVien(rs.getString("ten_giangvien"));
        return kt;
    }
}
