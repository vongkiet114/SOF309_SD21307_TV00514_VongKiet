package Repository;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlServerDeleteExceptionClassifierTest {
    @Test void loiKhoaNgoai547DuocPhanLoaiThanhXungDotXoaVaGiuNguyenNhan() {
        SQLException cause = new SQLException("fk conflict", "23000", 547);

        RepositoryException error = SqlServerDeleteExceptionClassifier.classify(
                "Không thể xóa dữ liệu.", cause);

        assertTrue(error instanceof DeleteConflictException);
        assertSame(cause, error.getCause());
    }

    @Test void loi547TrongNextExceptionVanDuocPhanLoaiVaGiuLoiGoc() {
        SQLException cause = new SQLException("driver wrapper", "S0000", 0);
        cause.setNextException(new SQLException("fk conflict", "23000", 547));

        RepositoryException error = SqlServerDeleteExceptionClassifier.classify(
                "Không thể xóa dữ liệu.", cause);

        assertTrue(error instanceof DeleteConflictException);
        assertSame(cause, error.getCause());
    }

    @Test void loiSqlKhacVanLaLoiRepositoryChungVaGiuNguyenNhan() {
        SQLException cause = new SQLException("connection lost", "08S01", 0);

        RepositoryException error = SqlServerDeleteExceptionClassifier.classify(
                "Không thể xóa dữ liệu.", cause);

        assertEquals(RepositoryException.class, error.getClass());
        assertSame(cause, error.getCause());
    }
}
