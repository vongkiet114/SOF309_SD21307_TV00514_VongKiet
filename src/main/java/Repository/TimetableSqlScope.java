package Repository;

import Models.TimetableFilter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TimetableSqlScope {
    private TimetableSqlScope() {
    }

    public static Scope week(TimetableFilter filter, String alias, String dateColumn) {
        validateTarget(alias, dateColumn);
        Objects.requireNonNull(filter, "Bộ lọc thời khóa biểu không được để trống.");

        List<Object> parameters = new ArrayList<>();
        StringBuilder sql = roleJoin(filter, alias);
        sql.append("WHERE ").append(alias).append('.').append(dateColumn)
                .append(" BETWEEN ? AND ? ");
        parameters.add(filter.getWeekStart());
        parameters.add(filter.getWeekEnd());
        appendConditions(sql, parameters, filter, alias);
        return new Scope(sql.toString(), parameters);
    }

    public static Scope next(TimetableFilter filter, String alias, String dateColumn,
                             LocalDate fromDate) {
        validateTarget(alias, dateColumn);
        Objects.requireNonNull(filter, "Bộ lọc thời khóa biểu không được để trống.");
        Objects.requireNonNull(fromDate, "Ngày bắt đầu tìm kiếm không được để trống.");

        List<Object> parameters = new ArrayList<>();
        StringBuilder sql = roleJoin(filter, alias);
        sql.append("WHERE ").append(alias).append('.').append(dateColumn)
                .append(" >= ? ");
        parameters.add(fromDate);
        appendConditions(sql, parameters, filter, alias);
        return new Scope(sql.toString(), parameters);
    }

    private static StringBuilder roleJoin(TimetableFilter filter, String alias) {
        String role = filter.getRole();
        if ("ADMIN".equals(role)) {
            return new StringBuilder();
        }
        if ("STUDENT".equals(role)) {
            requireUserId(filter);
            return new StringBuilder("JOIN dbo.danhsach_sv_lhp dssv ON ")
                    .append(alias).append(".ma_lophocphan = dssv.ma_lophocphan ");
        }
        if ("TEACHER".equals(role)) {
            requireUserId(filter);
            return new StringBuilder();
        }
        throw new IllegalArgumentException("Phạm vi thời khóa biểu không hợp lệ.");
    }

    private static void appendConditions(StringBuilder sql, List<Object> parameters,
                                         TimetableFilter filter, String alias) {
        if ("STUDENT".equals(filter.getRole())) {
            sql.append("AND dssv.ma_nguoidung = ? ");
            parameters.add(filter.getUserId());
        } else if ("TEACHER".equals(filter.getRole())) {
            sql.append("AND lhp.ma_giangvien = ? ");
            parameters.add(filter.getUserId());
        }
        append(sql, parameters, "lhp.ma_hocky", filter.getTermId());
        append(sql, parameters, alias + ".ma_lophocphan", filter.getSectionId());
        append(sql, parameters, "lhp.ma_giangvien", filter.getTeacherId());
        append(sql, parameters, alias + ".ma_phong", filter.getRoomId());
    }

    private static void append(StringBuilder sql, List<Object> parameters,
                               String column, Integer value) {
        if (value != null) {
            sql.append("AND ").append(column).append(" = ? ");
            parameters.add(value);
        }
    }

    private static void requireUserId(TimetableFilter filter) {
        if (filter.getUserId() == null || filter.getUserId() <= 0) {
            throw new IllegalArgumentException("Phạm vi thời khóa biểu không hợp lệ.");
        }
    }

    private static void validateTarget(String alias, String dateColumn) {
        boolean classSchedule = "lh".equals(alias) && "ngay_hoc".equals(dateColumn);
        boolean examSchedule = "kt".equals(alias) && "ngay_thi".equals(dateColumn);
        if (!classSchedule && !examSchedule) {
            throw new IllegalArgumentException("Phạm vi SQL thời khóa biểu không hợp lệ.");
        }
    }

    public static final class Scope {
        private final String sql;
        private final List<Object> parameters;

        private Scope(String sql, List<Object> parameters) {
            this.sql = sql;
            this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        }

        public String getSql() {
            return sql;
        }

        public List<Object> getParameters() {
            return parameters;
        }
    }
}
