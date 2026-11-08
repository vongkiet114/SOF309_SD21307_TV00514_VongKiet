package Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnect {

    private static final String CONFIG_FILE = "db.properties";
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Class.forName(DRIVER);
            loadConfig();
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Khong tim thay SQL Server JDBC Driver: " + e.getMessage());
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Khong doc duoc file " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    private DBConnect() {
    }

    private static void loadConfig() throws IOException {
        Properties props = new Properties();
        boolean fileFound;
        try (InputStream in = DBConnect.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            fileFound = in != null;
            if (fileFound) {
                props.load(in);
            }
        }

        DatabaseConfig config = DatabaseConfig.from(props, System.getenv());
        url = config.getJdbcUrl();
        user = config.getUser();
        password = config.getPassword();

        if (user == null || user.trim().isEmpty()) {
            throw new IOException(
                    "Thieu cau hinh ket noi CSDL: khong tim thay 'db.user'.\n"
                    + "  - File '" + CONFIG_FILE + "' tren classpath: "
                    + (fileFound ? "co nhung thieu db.user" : "KHONG co")
                    + " (luu y: Maven co y loai file nay khoi build, xem pom.xml).\n"
                    + "  - Bien moi truong QLSV_DB_USER: chua duoc dat.\n"
                    + "Cach khac phuc: dat cac bien moi truong QLSV_DB_HOST/QLSV_DB_USER/"
                    + "QLSV_DB_PASSWORD/... trong Run Configuration, "
                    + "hoac cung cap file " + CONFIG_FILE + " tren classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void closeAll(Connection conn, Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
        close(conn);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Ket noi SQL Server thanh cong!");
                System.out.println("URL: " + url);
            }
        } catch (SQLException e) {
            System.err.println("Ket noi that bai: " + e.getMessage());
        }
    }
}
