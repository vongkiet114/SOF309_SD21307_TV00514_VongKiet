package Repository;

import java.sql.SQLException;

public class DeleteConflictException extends RepositoryException {
    public DeleteConflictException(String message, SQLException cause) {
        super(message, cause);
    }
}
