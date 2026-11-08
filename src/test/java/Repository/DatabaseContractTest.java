package Repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "QLSV_DB_CONTRACT", matches = "true")
class DatabaseContractTest {
    private static final Set<String> EXPECTED_TABLES = new HashSet<>(Arrays.asList(
            "cahoc", "chuyennganh", "danhsach_sv_lhp", "diemdanh", "diemso",
            "giangvien", "hocky", "khoa", "kythi", "lichhoc", "lop",
            "lophocphan", "monhoc", "monhoc_tienquyet", "nguoidung", "nhatky_hoatdong",
            "phonghoc", "sinhvien", "thanhphan_diem", "thongbao", "thongbao_nguoinhan", "vaitro"
    ));

    private static final Map<String, List<String>> EXPECTED_PLAN_C0_INDEXES = expectedPlanC0Indexes();
    private static final Map<String, String> EXPECTED_PLAN_C0_DEFAULTS = expectedPlanC0Defaults();
    private static final Set<String> EXPECTED_FOREIGN_KEYS = new HashSet<>(Arrays.asList(
            "fk_nd_vaitro", "fk_nk_nd", "fk_tbnn_tb", "fk_tbnn_nd", "fk_cn_khoa",
            "fk_lop_cn", "fk_mh_cn", "fk_mhtq_mh", "fk_mhtq_tq", "fk_gv_nd",
            "fk_gv_cn", "fk_sv_nd", "fk_sv_lop", "fk_lhp_mh", "fk_lhp_gv",
            "fk_lhp_hk", "fk_ds_sv", "fk_ds_lhp", "fk_tpd_mh", "fk_diem_ds",
            "fk_diem_tp", "fk_lh_lhp", "fk_lh_phong", "fk_lh_ca", "fk_dd_ds",
            "fk_dd_lh", "fk_kt_lhp", "fk_kt_phong", "fk_kt_ca"
    ));
    private static final Set<String> EXPECTED_NAMED_UNIQUES = new HashSet<>(Arrays.asList(
            "uq_ds_sv_lhp", "uq_diemso", "uq_diemdanh"
    ));
    private static final Set<String> EXPECTED_NAMED_CHECKS = new HashSet<>(Arrays.asList(
            "ck_mhtq_diff", "ck_hocky_ngay", "ck_cahoc_gio", "ck_diemdanh_trang_thai"
    ));

    @Test
    void databaseCoDung22BangNenTang() throws Exception {
        Set<String> actual = new HashSet<>();
        try (Connection connection = DBConnect.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getTables(
                    connection.getCatalog(), "dbo", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    actual.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        assertEquals(EXPECTED_TABLES, actual);
    }

    @Test
    void bangNguoiDungCoDuCotNenTang() throws Exception {
        Set<String> columns = new HashSet<>();
        try (Connection connection = DBConnect.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getColumns(
                    connection.getCatalog(), "dbo", "nguoidung", "%")) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }
        }

        assertTrue(columns.containsAll(Arrays.asList(
                "ma_nguoidung", "email", "mat_khau", "ho_ten", "ma_vaitro", "hoat_dong")));
    }

    @Test
    void bangDanhMucCoDungCotTheoSchema() throws Exception {
        try (Connection connection = DBConnect.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertEquals(Set.of("ma_khoa", "ten_khoa"), columns(metadata, "khoa"));
            assertTrue(columns(metadata, "chuyennganh").containsAll(
                    Set.of("ma_chuyennganh", "ma_cn", "ten_chuyennganh", "ma_khoa")));
            assertTrue(columns(metadata, "lop").containsAll(
                    Set.of("ma_lop", "ten_lop", "ma_chuyennganh")));
            assertTrue(columns(metadata, "monhoc").containsAll(
                    Set.of("ma_monhoc", "ma_mon", "ten_monhoc", "so_tinchi", "ma_chuyennganh")));
            assertTrue(columns(metadata, "hocky").containsAll(
                    Set.of("ma_hocky", "ten_hocky", "block", "nam_hoc", "ngay_batdau", "ngay_ketthuc")));
            assertEquals(Set.of("ma_phong", "ten_phong"), columns(metadata, "phonghoc"));
            assertEquals(Set.of("ma_ca", "gio_batdau", "gio_ketthuc"), columns(metadata, "cahoc"));
            assertFalse(columns(metadata, "khoa").contains("ma_kh"));
            assertFalse(columns(metadata, "phonghoc").contains("suc_chua"));
            assertFalse(columns(metadata, "phonghoc").contains("loai_phong"));
            assertFalse(columns(metadata, "cahoc").contains("ten_ca"));
        }
    }

    @Test
    void databaseCoDungHaiMuoiIndexPlanC0TheoThuTuCot() throws Exception {
        Map<String, List<String>> actual = new HashMap<>();
        String sql = "SELECT i.name AS index_name, ic.key_ordinal, c.name AS column_name, " +
                "ic.is_descending_key " +
                "FROM sys.indexes i " +
                "JOIN sys.index_columns ic ON ic.object_id = i.object_id " +
                " AND ic.index_id = i.index_id AND ic.key_ordinal > 0 " +
                "JOIN sys.columns c ON c.object_id = ic.object_id " +
                " AND c.column_id = ic.column_id " +
                "WHERE i.name IS NOT NULL ORDER BY i.name, ic.key_ordinal";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String index = rs.getString("index_name").toLowerCase(Locale.ROOT);
                if (!EXPECTED_PLAN_C0_INDEXES.containsKey(index)) continue;
                String column = rs.getString("column_name").toLowerCase(Locale.ROOT);
                if (rs.getBoolean("is_descending_key")) column += " desc";
                actual.computeIfAbsent(index, ignored -> new ArrayList<>()).add(column);
            }
        }

        for (Map.Entry<String, List<String>> expected : EXPECTED_PLAN_C0_INDEXES.entrySet()) {
            assertEquals(expected.getValue(), actual.get(expected.getKey()),
                    "Sai hoặc thiếu index " + expected.getKey());
        }
    }

    @Test
    void tamCotPlanC0LaNotNullVaCoDefaultDung() throws Exception {
        String sql = "SELECT c.is_nullable, dc.definition " +
                "FROM sys.tables t " +
                "JOIN sys.schemas s ON s.schema_id = t.schema_id " +
                "JOIN sys.columns c ON c.object_id = t.object_id " +
                "LEFT JOIN sys.default_constraints dc " +
                " ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id " +
                "WHERE s.name = N'dbo' AND t.name = ? AND c.name = ?";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, String> expected : EXPECTED_PLAN_C0_DEFAULTS.entrySet()) {
                String[] parts = expected.getKey().split("\\.", 2);
                statement.setString(1, parts[0]);
                statement.setString(2, parts[1]);
                try (ResultSet rs = statement.executeQuery()) {
                    assertTrue(rs.next(), "Thiếu cột " + expected.getKey());
                    assertFalse(rs.getBoolean("is_nullable"),
                            expected.getKey() + " vẫn cho phép NULL");
                    String definition = rs.getString("definition");
                    assertNotNull(definition, expected.getKey() + " chưa có default");
                    assertEquals(expected.getValue(), normalizeDefault(definition),
                            "Sai default của " + expected.getKey());
                }
            }
        }
    }

    @Test
    void cacRangBuocKhoaNgoaiUniqueVaCheckNenTangVanTonTai() throws Exception {
        try (Connection connection = DBConnect.getConnection()) {
            Set<String> foreignKeys = constraintNames(connection,
                    "SELECT fk.name FROM sys.foreign_keys fk " +
                            "JOIN sys.tables t ON t.object_id = fk.parent_object_id " +
                            "WHERE t.schema_id = SCHEMA_ID(N'dbo')");
            Set<String> uniqueConstraints = constraintNames(connection,
                    "SELECT kc.name FROM sys.key_constraints kc " +
                            "JOIN sys.tables t ON t.object_id = kc.parent_object_id " +
                            "WHERE t.schema_id = SCHEMA_ID(N'dbo') AND kc.type = 'UQ'");
            Set<String> checkConstraints = constraintNames(connection,
                    "SELECT cc.name FROM sys.check_constraints cc " +
                            "JOIN sys.tables t ON t.object_id = cc.parent_object_id " +
                            "WHERE t.schema_id = SCHEMA_ID(N'dbo')");

            assertTrue(foreignKeys.containsAll(EXPECTED_FOREIGN_KEYS),
                    "Thiếu foreign key nền tảng: " + missing(EXPECTED_FOREIGN_KEYS, foreignKeys));
            assertTrue(uniqueConstraints.containsAll(EXPECTED_NAMED_UNIQUES),
                    "Thiếu unique constraint nền tảng: " +
                            missing(EXPECTED_NAMED_UNIQUES, uniqueConstraints));
            assertTrue(checkConstraints.containsAll(EXPECTED_NAMED_CHECKS),
                    "Thiếu check constraint nền tảng: " +
                            missing(EXPECTED_NAMED_CHECKS, checkConstraints));
        }
    }

    @Test
    void rangBuocDiemDanhChiChoHaiTrangThaiVaDuocTinCay() throws Exception {
        String sql = "SELECT cc.is_disabled, cc.is_not_trusted, cc.definition " +
                "FROM sys.check_constraints cc " +
                "WHERE cc.parent_object_id = OBJECT_ID(N'dbo.diemdanh') " +
                "AND cc.name = N'ck_diemdanh_trang_thai'";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            assertTrue(rs.next(), "Thiếu constraint ck_diemdanh_trang_thai");
            assertFalse(rs.getBoolean("is_disabled"),
                    "Constraint điểm danh đang bị vô hiệu hóa");
            assertFalse(rs.getBoolean("is_not_trusted"),
                    "Constraint điểm danh chưa được SQL Server tin cậy");

            String definition = rs.getString("definition")
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("\\s+", "");
            assertTrue(definition.contains("'comat'"));
            assertTrue(definition.contains("'vang'"));
            assertFalse(definition.contains("'tre'"));
            assertFalse(rs.next(), "Constraint điểm danh bị khai báo trùng");
        }
    }

    private Set<String> columns(DatabaseMetaData metadata, String table) throws SQLException {
        Set<String> result = new HashSet<>();
        try (ResultSet rs = metadata.getColumns(
                metadata.getConnection().getCatalog(), "dbo", table, "%")) {
            while (rs.next()) result.add(rs.getString("COLUMN_NAME").toLowerCase());
        }
        return result;
    }

    private Set<String> constraintNames(Connection connection, String sql) throws SQLException {
        Set<String> result = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString(1).toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    private Set<String> missing(Set<String> expected, Set<String> actual) {
        Set<String> result = new HashSet<>(expected);
        result.removeAll(actual);
        return result;
    }

    private static Map<String, List<String>> expectedPlanC0Indexes() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("ix_nguoidung_vaitro_hoatdong", Arrays.asList("ma_vaitro", "hoat_dong"));
        result.put("ix_nhatky_hoatdong_nguoidung_thoigian", Arrays.asList("ma_nguoidung", "thoi_gian desc"));
        result.put("ix_thongbao_nguoinhan_nguoidung_dadoc", Arrays.asList("ma_nguoidung", "da_doc", "ma_thongbao"));
        result.put("ix_chuyennganh_khoa", Arrays.asList("ma_khoa"));
        result.put("ix_lop_chuyennganh", Arrays.asList("ma_chuyennganh"));
        result.put("ix_monhoc_chuyennganh", Arrays.asList("ma_chuyennganh"));
        result.put("ix_monhoc_tienquyet_reverse", Arrays.asList("ma_monhoc_tienquyet", "ma_monhoc"));
        result.put("ix_giangvien_chuyennganh", Arrays.asList("ma_chuyennganh"));
        result.put("ix_sinhvien_lop", Arrays.asList("ma_lop"));
        result.put("ix_lophocphan_monhoc", Arrays.asList("ma_monhoc"));
        result.put("ix_lophocphan_giangvien_hocky", Arrays.asList("ma_giangvien", "ma_hocky"));
        result.put("ix_lophocphan_hocky", Arrays.asList("ma_hocky"));
        result.put("ix_danhsach_sv_lhp_lophocphan", Arrays.asList("ma_lophocphan", "ma_nguoidung"));
        result.put("ix_thanhphan_diem_monhoc", Arrays.asList("ma_monhoc"));
        result.put("ix_diemso_thanhphan", Arrays.asList("ma_thanhphan", "ma_danhsach"));
        result.put("ix_diemdanh_lichhoc", Arrays.asList("ma_lichhoc", "ma_danhsach"));
        result.put("ix_lichhoc_lophocphan_ngay", Arrays.asList("ma_lophocphan", "ngay_hoc", "ma_ca"));
        result.put("ix_lichhoc_ngay_ca_phong", Arrays.asList("ngay_hoc", "ma_ca", "ma_phong"));
        result.put("ix_kythi_lophocphan_ngay", Arrays.asList("ma_lophocphan", "ngay_thi", "ma_ca"));
        result.put("ix_kythi_ngay_ca_phong", Arrays.asList("ngay_thi", "ma_ca", "ma_phong"));
        return result;
    }

    private static Map<String, String> expectedPlanC0Defaults() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("nguoidung.hoat_dong", "1");
        result.put("nhatky_hoatdong.thoi_gian", "getdate");
        result.put("thongbao.ngay_tao", "getdate");
        result.put("thongbao_nguoinhan.da_doc", "0");
        result.put("sinhvien.trang_thai", "'danghoc'");
        result.put("lophocphan.trang_thai", "'molop'");
        result.put("lichhoc.loai_lich", "'hocchinh'");
        result.put("lichhoc.trang_thai", "'binhthuong'");
        return result;
    }

    private static String normalizeDefault(String definition) {
        String result = definition.toLowerCase(Locale.ROOT)
                .replace("(", "")
                .replace(")", "")
                .replace(" ", "");
        return result.startsWith("n'") ? result.substring(1) : result;
    }
}
