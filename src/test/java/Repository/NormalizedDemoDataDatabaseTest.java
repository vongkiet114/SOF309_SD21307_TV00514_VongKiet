package Repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class NormalizedDemoDataDatabaseTest {
    @Test
    void coDungQuyMoDuLieuDemoDaChuanHoa() throws Exception {
        assertCount("SELECT COUNT(*) FROM dbo.nguoidung WHERE email = 'admin@fpt.edu.vn' " +
                "OR email LIKE 'gv%@fpt.edu.vn' OR email LIKE 'sv%@fpt.edu.vn'", 111);
        assertCount("SELECT COUNT(*) FROM dbo.nguoidung WHERE email NOT IN " +
                "('hakhoa@fpt.edu.vn') AND email <> 'admin@fpt.edu.vn' " +
                "AND email NOT LIKE 'gv%@fpt.edu.vn' AND email NOT LIKE 'sv%@fpt.edu.vn'", 0);
        assertCount("SELECT COUNT(*) FROM dbo.giangvien", 10);
        assertCount("SELECT COUNT(*) FROM dbo.monhoc", 10);
        assertCount("SELECT COUNT(*) FROM dbo.lophocphan", 20);
        assertCount("SELECT COUNT(*) FROM dbo.lichhoc", 600);
        assertCount("SELECT COUNT(*) FROM dbo.kythi", 60);

        int expectedEnrollments = scalar("SELECT SUM(student_count * course_count) " +
                "FROM (SELECT l.ma_chuyennganh, COUNT(*) student_count " +
                "FROM dbo.sinhvien sv JOIN dbo.lop l ON l.ma_lop = sv.ma_lop " +
                "GROUP BY l.ma_chuyennganh) s JOIN " +
                "(SELECT ma_chuyennganh, COUNT(*) course_count FROM dbo.monhoc " +
                "GROUP BY ma_chuyennganh) c ON c.ma_chuyennganh = s.ma_chuyennganh");
        assertCount("SELECT COUNT(*) FROM dbo.danhsach_sv_lhp", expectedEnrollments);
        assertCount("SELECT COUNT(*) FROM dbo.diemso", expectedEnrollments * 2);
        assertCount("SELECT COUNT(*) FROM dbo.diemdanh", expectedEnrollments * 30);
    }

    @Test
    void lichHocVaLichThiDeuNamTrongHocKy() throws Exception {
        assertCount("SELECT COUNT(*) FROM dbo.lichhoc lh "
                + "JOIN dbo.lophocphan lhp ON lhp.ma_lophocphan = lh.ma_lophocphan "
                + "JOIN dbo.hocky hk ON hk.ma_hocky = lhp.ma_hocky "
                + "WHERE lh.ngay_hoc < hk.ngay_batdau OR lh.ngay_hoc > hk.ngay_ketthuc", 0);
        assertCount("SELECT COUNT(*) FROM dbo.kythi kt "
                + "JOIN dbo.lophocphan lhp ON lhp.ma_lophocphan = kt.ma_lophocphan "
                + "JOIN dbo.hocky hk ON hk.ma_hocky = lhp.ma_hocky "
                + "WHERE kt.ngay_thi < hk.ngay_batdau OR kt.ngay_thi > hk.ngay_ketthuc", 0);
    }

    @Test
    void maHienThiVaQuanHeChuyenNganhDeuHopLe() throws Exception {
        assertCount("SELECT COUNT(*) FROM dbo.lop WHERE ma_lop_hienthi IS NULL " +
                "OR ma_lop_hienthi NOT LIKE '%-[0-9][0-9][0-9]'", 0);
        assertCount("SELECT COUNT(*) FROM dbo.lophocphan WHERE ma_lhp IS NULL", 0);
        assertCount("SELECT COUNT(*) FROM dbo.lop l JOIN dbo.chuyennganh cn " +
                "ON cn.ma_chuyennganh = l.ma_chuyennganh " +
                "WHERE l.ma_lop_hienthi NOT LIKE cn.ma_cn + '-%'", 0);
        assertCount("SELECT COUNT(*) FROM dbo.lophocphan lhp " +
                "JOIN dbo.monhoc mh ON mh.ma_monhoc = lhp.ma_monhoc " +
                "JOIN dbo.chuyennganh cn ON cn.ma_chuyennganh = mh.ma_chuyennganh " +
                "WHERE lhp.ma_lhp NOT LIKE cn.ma_cn + '-LHP-%'", 0);
        assertCount("SELECT COUNT(*) FROM dbo.lophocphan lhp " +
                "JOIN dbo.monhoc mh ON mh.ma_monhoc = lhp.ma_monhoc " +
                "JOIN dbo.giangvien gv ON gv.ma_nguoidung = lhp.ma_giangvien " +
                "WHERE mh.ma_chuyennganh <> gv.ma_chuyennganh", 0);
        assertCount("SELECT COUNT(*) FROM dbo.danhsach_sv_lhp ds " +
                "JOIN dbo.sinhvien sv ON sv.ma_nguoidung = ds.ma_nguoidung " +
                "JOIN dbo.lop l ON l.ma_lop = sv.ma_lop " +
                "JOIN dbo.lophocphan lhp ON lhp.ma_lophocphan = ds.ma_lophocphan " +
                "JOIN dbo.monhoc mh ON mh.ma_monhoc = lhp.ma_monhoc " +
                "WHERE l.ma_chuyennganh <> mh.ma_chuyennganh", 0);
    }

    @Test
    void khongConXungDotLichHocLichThi() throws Exception {
        assertCount("SELECT COUNT(*) FROM dbo.lichhoc a JOIN dbo.lichhoc b " +
                "ON a.ma_lichhoc < b.ma_lichhoc AND a.ngay_hoc = b.ngay_hoc " +
                "AND a.ma_ca = b.ma_ca AND a.ma_phong = b.ma_phong", 0);
        assertCount("SELECT COUNT(*) FROM dbo.lichhoc a JOIN dbo.lichhoc b " +
                "ON a.ma_lichhoc < b.ma_lichhoc AND a.ngay_hoc = b.ngay_hoc " +
                "AND a.ma_ca = b.ma_ca JOIN dbo.lophocphan la " +
                "ON la.ma_lophocphan = a.ma_lophocphan JOIN dbo.lophocphan lb " +
                "ON lb.ma_lophocphan = b.ma_lophocphan " +
                "WHERE la.ma_giangvien = lb.ma_giangvien", 0);
        assertCount("SELECT COUNT(*) FROM dbo.lichhoc a JOIN dbo.lichhoc b " +
                "ON a.ma_lichhoc < b.ma_lichhoc AND a.ngay_hoc = b.ngay_hoc " +
                "AND a.ma_ca = b.ma_ca JOIN dbo.danhsach_sv_lhp da " +
                "ON da.ma_lophocphan = a.ma_lophocphan JOIN dbo.danhsach_sv_lhp db " +
                "ON db.ma_lophocphan = b.ma_lophocphan " +
                "WHERE da.ma_nguoidung = db.ma_nguoidung", 0);
        assertCount("SELECT COUNT(*) FROM dbo.kythi a JOIN dbo.kythi b " +
                "ON a.ma_kythi < b.ma_kythi AND a.ngay_thi = b.ngay_thi " +
                "AND a.ma_ca = b.ma_ca AND a.ma_phong = b.ma_phong", 0);
        assertCount("SELECT COUNT(*) FROM dbo.kythi a JOIN dbo.kythi b " +
                "ON a.ma_kythi < b.ma_kythi AND a.ngay_thi = b.ngay_thi " +
                "AND a.ma_ca = b.ma_ca JOIN dbo.danhsach_sv_lhp da " +
                "ON da.ma_lophocphan = a.ma_lophocphan JOIN dbo.danhsach_sv_lhp db " +
                "ON db.ma_lophocphan = b.ma_lophocphan " +
                "WHERE da.ma_nguoidung = db.ma_nguoidung", 0);
    }

    @Test
    void diemVaDiemDanhKhongBiMoCoi() throws Exception {
        assertCount("SELECT COUNT(*) FROM dbo.diemso d " +
                "JOIN dbo.danhsach_sv_lhp ds ON ds.ma_danhsach = d.ma_danhsach " +
                "JOIN dbo.lophocphan lhp ON lhp.ma_lophocphan = ds.ma_lophocphan " +
                "JOIN dbo.thanhphan_diem tp ON tp.ma_thanhphan = d.ma_thanhphan " +
                "WHERE tp.ma_monhoc <> lhp.ma_monhoc", 0);
        assertCount("SELECT COUNT(*) FROM dbo.diemdanh dd " +
                "JOIN dbo.danhsach_sv_lhp ds ON ds.ma_danhsach = dd.ma_danhsach " +
                "JOIN dbo.lichhoc lh ON lh.ma_lichhoc = dd.ma_lichhoc " +
                "WHERE ds.ma_lophocphan <> lh.ma_lophocphan", 0);
    }

    @Test
    void haiLopHocPhanCuaMoiMonDuocChiaSiSoCanBang() throws Exception {
        assertCount("SELECT COUNT(*) FROM (" +
                "SELECT lhp.ma_monhoc, MIN(ISNULL(s.so_luong, 0)) AS nho_nhat, " +
                "MAX(ISNULL(s.so_luong, 0)) AS lon_nhat " +
                "FROM dbo.lophocphan lhp LEFT JOIN (" +
                "SELECT ma_lophocphan, COUNT(*) AS so_luong " +
                "FROM dbo.danhsach_sv_lhp GROUP BY ma_lophocphan" +
                ") s ON s.ma_lophocphan = lhp.ma_lophocphan " +
                "GROUP BY lhp.ma_monhoc" +
                ") x WHERE nho_nhat = 0 OR lon_nhat - nho_nhat > 1", 0);
    }

    @Test
    void dashboardPhanAnhDungSoLieuTrongCacBangHienCo() throws Exception {
        LocalDate today = LocalDate.now();
        DashboardRepository.AdminStatistics statistics =
                new DashboardRepository().loadAdminStatistics(today);

        assertEquals(scalar("SELECT COUNT(*) FROM dbo.sinhvien sv "
                        + "JOIN dbo.nguoidung nd ON nd.ma_nguoidung = sv.ma_nguoidung "
                        + "WHERE nd.hoat_dong = 1 AND sv.trang_thai = 'DangHoc'"),
                statistics.getSoSinhVienHoatDong());
        assertEquals(scalar("SELECT COUNT(*) FROM dbo.giangvien gv "
                        + "JOIN dbo.nguoidung nd ON nd.ma_nguoidung = gv.ma_nguoidung "
                        + "WHERE nd.hoat_dong = 1"),
                statistics.getSoGiangVienHoatDong());
        assertEquals(scalar("SELECT COUNT(*) FROM dbo.lophocphan "
                        + "WHERE trang_thai IN ('MoLop', 'DangHoc')"),
                statistics.getSoLopHocPhanMo());
        assertEquals(scalar("SELECT COUNT(*) FROM dbo.lichhoc "
                        + "WHERE ngay_hoc = '" + today
                        + "' AND trang_thai = 'BinhThuong'"),
                statistics.getSoBuoiHomNay());
    }

    private void assertCount(String sql, int expected) throws Exception {
        assertEquals(expected, scalar(sql), sql);
    }

    private int scalar(String sql) throws Exception {
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
