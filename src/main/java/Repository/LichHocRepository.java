package Repository;

import Models.LichHoc;
import Models.TimetableFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LichHocRepository {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String SELECT_COLUMNS =
            "SELECT lh.*, lhp.ma_lhp, p.ten_phong, c.gio_batdau, "
            + "c.gio_ketthuc, m.ten_monhoc, nd.ho_ten AS ten_giangvien ";
    private static final String FROM_JOIN =
            "FROM dbo.lichhoc lh "
            + "JOIN dbo.phonghoc p ON lh.ma_phong = p.ma_phong "
            + "JOIN dbo.cahoc c ON lh.ma_ca = c.ma_ca "
            + "JOIN dbo.lophocphan lhp ON lh.ma_lophocphan = lhp.ma_lophocphan "
            + "JOIN dbo.monhoc m ON lhp.ma_monhoc = m.ma_monhoc "
            + "JOIN dbo.giangvien gv ON lhp.ma_giangvien = gv.ma_nguoidung "
            + "JOIN dbo.nguoidung nd ON gv.ma_nguoidung = nd.ma_nguoidung ";
    private static final String SELECT_JOIN = SELECT_COLUMNS + FROM_JOIN;

    public List<LichHoc> findForTimetable(TimetableFilter filter) {
        TimetableSqlScope.Scope scope =
                TimetableSqlScope.week(filter, "lh", "ngay_hoc");
        String sql = SELECT_COLUMNS + FROM_JOIN + scope.getSql()
                + "ORDER BY lh.ngay_hoc, lh.ma_ca, lhp.ma_lhp, lh.ma_lichhoc";
        return queryMany(sql, scope.getParameters());
    }

    public LocalDate findNextDate(TimetableFilter filter, LocalDate fromDate) {
        TimetableSqlScope.Scope scope =
                TimetableSqlScope.next(filter, "lh", "ngay_hoc", fromDate);
        String sql = "SELECT MIN(lh.ngay_hoc) " + FROM_JOIN + scope.getSql();
        return queryDate(sql, scope.getParameters());
    }

    public List<LichHoc> findByNguoiDung(int maNguoiDung, String vaiTro) {
        List<LichHoc> list = new ArrayList<>();
        String sql;
        if ("STUDENT".equalsIgnoreCase(vaiTro)) {
            sql = SELECT_JOIN + "JOIN danhsach_sv_lhp dssv ON lh.ma_lophocphan = dssv.ma_lophocphan " +
                  "WHERE dssv.ma_nguoidung = ? ORDER BY lh.ngay_hoc, lh.ma_ca";
        } else if ("TEACHER".equalsIgnoreCase(vaiTro)) {
            sql = SELECT_JOIN + "WHERE lhp.ma_giangvien = ? ORDER BY lh.ngay_hoc, lh.ma_ca";
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

    public List<LichHoc> findByMaSinhVien(int maSinhVien) {
        return findByNguoiDung(maSinhVien, "STUDENT");
    }
    
    public List<LichHoc> getAll() {
        List<LichHoc> list = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "ORDER BY lh.ngay_hoc, lh.ma_ca");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public LichHoc findById(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "WHERE lh.ma_lichhoc = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<LichHoc> findByLopHocPhan(int maLopHocPhan) {
        List<LichHoc> list = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE lh.ma_lophocphan = ? ORDER BY lh.ngay_hoc, lh.ma_ca";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    public boolean delete(int id) {
        String sql = "DELETE FROM lichhoc WHERE ma_lichhoc = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasAttendance(int id) {
        String sql = "SELECT 1 FROM dbo.diemdanh WHERE ma_lichhoc = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Không thể kiểm tra điểm danh của buổi học.", e);
        }
    }

    private List<LichHoc> queryMany(String sql, List<Object> values) {
        List<LichHoc> list = new ArrayList<>();
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

    private LichHoc map(ResultSet rs) throws SQLException {
        LichHoc lh = new LichHoc(
                rs.getInt("ma_lichhoc"),
                rs.getInt("ma_lophocphan"),
                rs.getInt("ma_phong"),
                rs.getInt("ma_ca"),
                rs.getDate("ngay_hoc").toLocalDate(),
                rs.getString("loai_lich"),
                rs.getString("trang_thai")
        );
        lh.setMaLhp(rs.getString("ma_lhp"));
        lh.setTenPhong(rs.getString("ten_phong"));
        lh.setTenCa(rs.getTime("gio_batdau").toLocalTime().format(TIME_FORMAT) + " - " +
                rs.getTime("gio_ketthuc").toLocalTime().format(TIME_FORMAT));
        lh.setTenMonHoc(rs.getString("ten_monhoc"));
        lh.setTenGiangVien(rs.getString("ten_giangvien"));
        return lh;
    }
}
