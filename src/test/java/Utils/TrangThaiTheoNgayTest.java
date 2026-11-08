package Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrangThaiTheoNgayTest {
    @Test
    void hienThiTrangThaiLopHocPhanBangTiengViet() {
        assertEquals("Mở lớp", TrangThaiTheoNgay.nhan("MoLop"));
        assertEquals("Đang học", TrangThaiTheoNgay.nhan("DangHoc"));
        assertEquals("Hoàn thành", TrangThaiTheoNgay.nhan("HoanThanh"));
        assertEquals("Đã hủy", TrangThaiTheoNgay.nhan("Huy"));
    }

    @Test
    void hienThiTrangThaiBuoiHocBangTiengViet() {
        assertEquals("Bình thường", TrangThaiTheoNgay.nhan("BinhThuong"));
        assertEquals("Xin nghỉ", TrangThaiTheoNgay.nhan("XinNghi"));
        assertEquals("Đã hủy", TrangThaiTheoNgay.nhan("DaHuy"));
    }
}
