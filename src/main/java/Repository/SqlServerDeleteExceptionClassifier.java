package Repository;

import java.sql.SQLException;

final class SqlServerDeleteExceptionClassifier {
    private static final int FOREIGN_KEY_CONFLICT = 547;

    private SqlServerDeleteExceptionClassifier() {
    }

    static RepositoryException classify(String message, SQLException cause) {
        SQLException current = cause;
        while (current != null) {
            if (current.getErrorCode() == FOREIGN_KEY_CONFLICT) {
                return new DeleteConflictException(message, cause);
            }
            SQLException next = current.getNextException();
            if (next == current) {
                break;
            }
            current = next;
        }
        return new RepositoryException(message, cause);
    }
}
