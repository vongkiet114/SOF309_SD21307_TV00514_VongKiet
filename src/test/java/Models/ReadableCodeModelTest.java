package Models;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadableCodeModelTest {
    @Test
    void lopHocLuuDuocMaHienThiRiengVoiIdNoiBo() throws Exception {
        Method setter = assertDoesNotThrow(
                () -> LopHoc.class.getMethod("setMaLopHienThi", String.class));
        Method getter = assertDoesNotThrow(
                () -> LopHoc.class.getMethod("getMaLopHienThi"));
        LopHoc lop = new LopHoc();

        setter.invoke(lop, "WEB-001");

        assertEquals("WEB-001", getter.invoke(lop));
    }

    @Test
    void lopHocPhanLuuDuocMaOnDinhTuDatabase() {
        LopHocPhan lhp = new LopHocPhan();

        lhp.setMaLhp("MKT-LHP-001");

        assertEquals("MKT-LHP-001", lhp.getMaLhp());
    }
}
