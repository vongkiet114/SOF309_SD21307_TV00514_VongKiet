package Repository;

import Models.SinhVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SinhVienRepository {

    private static final String COUNT_BASE = "SELECT COUNT(*) FROM sinhvien sv " +
        "JOIN nguoidung nd ON sv.ma_nguoidung = nd.ma_nguoidung " +
        "JOIN lop l ON sv.ma_lop = l.ma_lop " +
        "JOIN chuyennganh cn ON l.ma_chuyennganh = cn.ma_chuyennganh ";

    public long count(String keyword, Integer maChuyenNganh, Integer maLop) {
        StringBuilder sql = new StringBuilder(COUNT_BASE);
        List<Object> params = new ArrayList<>();
        sql.append("WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (sv.mssv LIKE ? OR nd.ho_ten LIKE ? OR nd.email LIKE ?) ");
            String keywordParam = "%" + keyword.trim() + "%";
            params.add(keywordParam);
            params.add(keywordParam);
            params.add(keywordParam);
        }
        if (maChuyenNganh != null && maChuyenNganh > 0) {
            sql.append("AND cn.ma_chuyennganh = ? ");
            params.add(maChuyenNganh);
        }
        if (maLop != null && maLop > 0) {
            sql.append("AND sv.ma_lop = ? ");
            params.add(maLop);
        }

        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    // ... (rest of the existing methods)
    
    private static final String BASE_SELECT =
        "SELECT sv.*, nd.ho_ten, nd.email, l.ten_lop, cn.ten_chuyennganh " +
        "FROM sinhvien sv " +
        "JOIN nguoidung nd ON sv.ma_nguoidung = nd.ma_nguoidung " +
        "JOIN lop l ON sv.ma_lop = l.ma_lop " +
        "JOIN chuyennganh cn ON l.ma_chuyennganh = cn.ma_chuyennganh ";

    public List<SinhVien> search(String keyword, Integer maChuyenNganh, Integer maLop, int page, int pageSize) {
        List<SinhVien> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> params = new ArrayList<>();
        
        sql.append("WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (sv.mssv LIKE ? OR nd.ho_ten LIKE ? OR nd.email LIKE ?) ");
            String keywordParam = "%" + keyword.trim() + "%";
            params.add(keywordParam);
            params.add(keywordParam);
            params.add(keywordParam);
        }
        if (maChuyenNganh != null && maChuyenNganh > 0) {
            sql.append("AND cn.ma_chuyennganh = ? ");
            params.add(maChuyenNganh);
        }
        if (maLop != null && maLop > 0) {
            sql.append("AND sv.ma_lop = ? ");
            params.add(maLop);
        }

        sql.append("ORDER BY sv.mssv OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);

        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(map(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return danhSach;
    }
    
    public List<SinhVien> getAll(Integer maChuyenNganh, Integer maLop) {
        return search(null, maChuyenNganh, maLop, 1, Integer.MAX_VALUE);
    }

    public SinhVien findByMaNguoidung(int maNguoidung) {
        String sql = BASE_SELECT + "WHERE sv.ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNguoidung);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs, false) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public SinhVien findByMssv(String mssv) {
        String sql = BASE_SELECT + "WHERE sv.mssv = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, mssv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs, false) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Sinh ma so sinh vien tu dong tang dan: SV001, SV002, SV003...
    // Lay so lon nhat trong cac ma dang 'SVxxx' hien co roi cong 1.
    public String nextMssv() {
        String sql = "SELECT mssv FROM sinhvien WHERE mssv LIKE 'SV%'";
        int max = 0;
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString("mssv");
                if (code == null || code.length() <= 2) continue;
                try {
                    int n = Integer.parseInt(code.substring(2).trim());
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {
                    // bo qua cac ma khong theo dinh dang so (vd du lieu cu)
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return String.format("SV%03d", max + 1);
    }

    public List<SinhVien> findByLopHocPhan(int maLopHocPhan) {
        List<SinhVien> danhSach = new ArrayList<>();
        String sql = "SELECT sv.*, nd.ho_ten, nd.email, l.ten_lop, cn.ten_chuyennganh, dssv.ma_danhsach " +
                     "FROM sinhvien sv " +
                     "JOIN nguoidung nd ON sv.ma_nguoidung = nd.ma_nguoidung " +
                     "JOIN lop l ON sv.ma_lop = l.ma_lop " +
                     "JOIN chuyennganh cn ON l.ma_chuyennganh = cn.ma_chuyennganh " +
                     "JOIN danhsach_sv_lhp dssv ON sv.ma_nguoidung = dssv.ma_nguoidung " +
                     "WHERE dssv.ma_lophocphan = ? " +
                     "ORDER BY sv.mssv";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maLopHocPhan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(map(rs, true));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return danhSach;
    }

    public boolean insert(SinhVien sv) {
        String sql = "INSERT INTO sinhvien " +
                "(ma_nguoidung, ma_lop, mssv, gioi_tinh, ngay_sinh, dia_chi, cccd, dan_toc, khoa_hoc, ngay_nhap_hoc, hinh_anh, trang_thai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sv.getMaNguoidung());
            ps.setInt(2, sv.getMaLop());
            ps.setString(3, sv.getMssv());
            ps.setString(4, sv.getGioiTinh());
            ps.setString(5, blankToNull(sv.getNgaySinh()));
            ps.setString(6, sv.getDiaChi());
            ps.setString(7, sv.getCccd());
            ps.setString(8, sv.getDanToc());
            ps.setString(9, sv.getKhoaHoc());
            ps.setString(10, blankToNull(sv.getNgayNhapHoc()));
            ps.setString(11, blankToNull(sv.getHinhAnh()));
            ps.setString(12, sv.getTrangThai() == null ? "DangHoc" : sv.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(SinhVien sv) {
        String sql = "UPDATE sinhvien SET ma_lop = ?, mssv = ?, gioi_tinh = ?, ngay_sinh = ?, dia_chi = ?, " +
                "cccd = ?, dan_toc = ?, khoa_hoc = ?, ngay_nhap_hoc = ?, hinh_anh = ?, trang_thai = ? " +
                "WHERE ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sv.getMaLop());
            ps.setString(2, sv.getMssv());
            ps.setString(3, sv.getGioiTinh());
            ps.setString(4, blankToNull(sv.getNgaySinh()));
            ps.setString(5, sv.getDiaChi());
            ps.setString(6, sv.getCccd());
            ps.setString(7, sv.getDanToc());
            ps.setString(8, sv.getKhoaHoc());
            ps.setString(9, blankToNull(sv.getNgayNhapHoc()));
            ps.setString(10, blankToNull(sv.getHinhAnh()));
            ps.setString(11, sv.getTrangThai());
            ps.setInt(12, sv.getMaNguoidung());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int maNguoidung) {
        String sql = "DELETE FROM sinhvien WHERE ma_nguoidung = ?";
        try (Connection c = DBConnect.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNguoidung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SinhVien map(ResultSet rs, boolean includeMaDanhSach) throws SQLException {
        SinhVien sv = new SinhVien();
        sv.setMaNguoidung(rs.getInt("ma_nguoidung"));
        sv.setMaLop(rs.getInt("ma_lop"));
        sv.setMssv(rs.getString("mssv"));
        sv.setGioiTinh(rs.getString("gioi_tinh"));
        sv.setNgaySinh(rs.getString("ngay_sinh"));
        sv.setDiaChi(rs.getString("dia_chi"));
        sv.setCccd(rs.getString("cccd"));
        sv.setDanToc(rs.getString("dan_toc"));
        sv.setKhoaHoc(rs.getString("khoa_hoc"));
        sv.setNgayNhapHoc(rs.getString("ngay_nhap_hoc"));
        sv.setHinhAnh(rs.getString("hinh_anh"));
        sv.setTrangThai(rs.getString("trang_thai"));
        
        sv.setHoTen(rs.getString("ho_ten"));
        sv.setEmail(rs.getString("email"));
        sv.setTenLop(rs.getString("ten_lop"));
        sv.setTenChuyenNganh(rs.getString("ten_chuyennganh"));
        
        if (includeMaDanhSach) {
            sv.setMaDanhSach(rs.getInt("ma_danhsach"));
        }
        
        return sv;
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
