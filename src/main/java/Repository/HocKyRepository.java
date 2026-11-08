package Repository;

import Models.HocKy;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HocKyRepository {
    private static final String SELECT_ALL =
            "SELECT ma_hocky, ten_hocky, block, nam_hoc, ngay_batdau, ngay_ketthuc "
                    + "FROM hocky ORDER BY ngay_batdau DESC, block DESC";
    private static final String SELECT_ONE =
            "SELECT ma_hocky, ten_hocky, block, nam_hoc, ngay_batdau, ngay_ketthuc "
                    + "FROM hocky WHERE ma_hocky = ?";
    private static final String INSERT =
            "INSERT INTO hocky (ten_hocky, block, nam_hoc, ngay_batdau, ngay_ketthuc) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE hocky SET ten_hocky = ?, block = ?, nam_hoc = ?, ngay_batdau = ?, ngay_ketthuc = ? WHERE ma_hocky = ?";
    private static final String LOP_HOC_PHAN_DEPENDENCY =
            "SELECT COUNT(*) FROM lophocphan WHERE ma_hocky = ?";
    private static final String DELETE = "DELETE FROM hocky WHERE ma_hocky = ?";

    public List<HocKy> getAll() {
        List<HocKy> list = new ArrayList<>();
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public HocKy getById(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ONE)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean add(HocKy value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            setValues(ps, value);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean update(HocKy value) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            setValues(ps, value);
            ps.setInt(6, value.getMaHocky());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public String getDeleteConstraint(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(LOP_HOC_PHAN_DEPENDENCY)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "đang có lớp học phần sử dụng";
                }
                return null;
            }
        } catch (SQLException e) {
            throw dataAccessError(e);
        }
    }

    public boolean delete(int id) {
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SqlServerDeleteExceptionClassifier.classify(
                    "Không thể truy cập dữ liệu học kỳ.", e);
        }
    }

    private void setValues(PreparedStatement ps, HocKy value) throws SQLException {
        ps.setString(1, value.getTenHocky());
        ps.setInt(2, value.getBlock());
        ps.setString(3, value.getNamHoc());
        ps.setDate(4, Date.valueOf(value.getNgayBatdau()));
        ps.setDate(5, Date.valueOf(value.getNgayKetthuc()));
    }

    private HocKy map(ResultSet rs) throws SQLException {
        HocKy hocKy = new HocKy();
        hocKy.setMaHocky(rs.getInt("ma_hocky"));
        hocKy.setTenHocky(rs.getString("ten_hocky"));
        hocKy.setBlock(rs.getInt("block"));
        hocKy.setNamHoc(rs.getString("nam_hoc"));

        Date ngayBatDau = rs.getDate("ngay_batdau");
        Date ngayKetThuc = rs.getDate("ngay_ketthuc");
        if (ngayBatDau != null) {
            hocKy.setNgayBatdau(ngayBatDau.toLocalDate());
        }
        if (ngayKetThuc != null) {
            hocKy.setNgayKetthuc(ngayKetThuc.toLocalDate());
        }
        hocKy.setTrangThai(computeStatus(hocKy, LocalDate.now()));
        return hocKy;
    }

    private String computeStatus(HocKy hocKy, LocalDate today) {
        if (hocKy.getNgayBatdau() == null || hocKy.getNgayKetthuc() == null) {
            return null;
        }
        if (today.isBefore(hocKy.getNgayBatdau())) {
            return "SapDienRa";
        }
        if (today.isAfter(hocKy.getNgayKetthuc())) {
            return "DaKetThuc";
        }
        return "DangDienRa";
    }

    private RepositoryException dataAccessError(SQLException cause) {
        return new RepositoryException("Không thể truy cập dữ liệu học kỳ.", cause);
    }
}
