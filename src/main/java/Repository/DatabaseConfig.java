package Repository;

import java.util.Map;
import java.util.Properties;

public final class DatabaseConfig {
    private final String host;
    private final String port;
    private final String databaseName;
    private final String user;
    private final String password;
    private final boolean encrypt;
    private final boolean trustServerCertificate;

    private DatabaseConfig(String host,
                           String port,
                           String databaseName,
                           String user,
                           String password,
                           boolean encrypt,
                           boolean trustServerCertificate) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        this.encrypt = encrypt;
        this.trustServerCertificate = trustServerCertificate;
    }

    public static DatabaseConfig from(Properties properties, Map<String, String> environment) {
        return new DatabaseConfig(
                value(properties, environment, "db.host", "QLSV_DB_HOST", "localhost"),
                value(properties, environment, "db.port", "QLSV_DB_PORT", "1433"),
                value(properties, environment, "db.name", "QLSV_DB_NAME", "QL_DaoTao"),
                value(properties, environment, "db.user", "QLSV_DB_USER", ""),
                value(properties, environment, "db.password", "QLSV_DB_PASSWORD", ""),
                Boolean.parseBoolean(value(
                        properties, environment, "db.encrypt", "QLSV_DB_ENCRYPT", "false")),
                Boolean.parseBoolean(value(
                        properties, environment, "db.trustServerCertificate",
                        "QLSV_DB_TRUST_SERVER_CERTIFICATE", "true")));
    }

    private static String value(Properties properties,
                                Map<String, String> environment,
                                String propertyKey,
                                String environmentKey,
                                String defaultValue) {
        String environmentValue = environment.get(environmentKey);
        return environmentValue != null
                ? environmentValue
                : properties.getProperty(propertyKey, defaultValue);
    }

    public String getJdbcUrl() {
        return "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=" + databaseName
                + ";encrypt=" + encrypt
                + ";trustServerCertificate=" + trustServerCertificate;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
