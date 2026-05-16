package exception;

/**
 * Wrapper exception cho mọi lỗi liên quan đến database.
 * Thay vì để SQLException lan ra ngoài tầng DAO, wrap lại
 * thành checked exception có nghĩa rõ ràng hơn cho tầng service.
 * <p>
 * Lưu giữ original cause để log đầy đủ stack trace khi cần debug.
 */
public class DatabaseException extends AuctionSystemException {

    private final String operation; // "save", "findById", "update"...

    public DatabaseException(String operation, Throwable cause) {
        super("DATABASE_ERROR",
                "Lỗi database khi thực hiện '" + operation + "': " + cause.getMessage(),
                cause);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
