package Utils;

import Models.VaiTroHeThong;

public final class RoleAccess {
    private RoleAccess() {
    }

    public static VaiTroHeThong requiredRole(String path) {
        if (path == null) {
            return null;
        }
        if (isArea(path, "/admin")) {
            return VaiTroHeThong.ADMIN;
        }
        if (isArea(path, "/giangvien")) {
            return VaiTroHeThong.TEACHER;
        }
        if (isArea(path, "/sinhvien")) {
            return VaiTroHeThong.STUDENT;
        }
        return null;
    }

    private static boolean isArea(String path, String area) {
        return path.equals(area) || path.startsWith(area + "/");
    }
}
