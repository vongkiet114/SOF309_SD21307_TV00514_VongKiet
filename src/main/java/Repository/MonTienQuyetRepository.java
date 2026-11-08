package Repository;

import Models.MonHocTienQuyet;
import Repository.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MonTienQuyetRepository {

    // Lấy tất cả
    public List<MonHocTienQuyet> getAll() {
        List<MonHocTienQuyet> list = new ArrayList<>();

        String sql = "SELECT * FROM monhoc_tienquyet";

        try (
                Connection conn = DBConnect.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                MonHocTienQuyet mhtq = new MonHocTienQuyet();

                mhtq.setMaMonhoc(rs.getInt("ma_monhoc"));
                mhtq.setMaMonhocTienquyet(rs.getInt("ma_monhoc_tienquyet"));

                list.add(mhtq);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // thêm
    public boolean add(MonHocTienQuyet mhtq) {

        String sql = " INSERT INTO monhoc_tienquyet (ma_monhoc, ma_monhoc_tienquyet) VALUES (?, ?) ";

        try (
                Connection conn = DBConnect.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, mhtq.getMaMonhoc());
            ps.setInt(2, mhtq.getMaMonhocTienquyet());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // xóa
    public boolean delete(int maMonhoc, int maMonhocTienquyet) {

        String sql = "DELETE FROM monhoc_tienquyet WHERE ma_monhoc = ? AND ma_monhoc_tienquyet = ? ";

        try (
                Connection conn = DBConnect.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, maMonhoc);
            ps.setInt(2, maMonhocTienquyet);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // tìm theo khóa chính
    public MonHocTienQuyet getById(int maMonhoc, int maMonhocTienquyet) {

        String sql = "SELECT *FROM monhoc_tienquyet WHERE ma_monhoc = ? AND ma_monhoc_tienquyet = ? ";

        try (
                Connection conn = DBConnect.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, maMonhoc);
            ps.setInt(2, maMonhocTienquyet);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                MonHocTienQuyet m = new MonHocTienQuyet();

                m.setMaMonhoc(rs.getInt("ma_monhoc"));
                m.setMaMonhocTienquyet(rs.getInt("ma_monhoc_tienquyet"));

                return m;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

