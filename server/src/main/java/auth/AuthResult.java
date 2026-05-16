package auth;

/**
 * AuthResult - kết quả trả về sau khi xác thực token trong mỗi request.
 * <p>
 * Thay vì ném exception khắp nơi, ClientHandler gọi
 * AuthResult r = TokenGuard.check(req) rồi kiểm tra r.isOk().
 */
public class AuthResult {

    private final boolean ok;
    private final String userId;
    private final String username;
    private final String role;
    private final String errorMessage;

    // Constructor thành công
    private AuthResult(String userId, String username, String role) {
        this.ok = true;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.errorMessage = null;
    }

    // Constructor thất bại
    private AuthResult(String errorMessage) {
        this.ok = false;
        this.userId = null;
        this.username = null;
        this.role = null;
        this.errorMessage = errorMessage;
    }

    public static AuthResult success(String userId, String username, String role) {
        return new AuthResult(userId, username, role);
    }

    public static AuthResult fail(String message) {
        return new AuthResult(message);
    }

    public boolean isOk() {
        return ok;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Kiểm tra quyền — ném lỗi nếu không đúng role
     */
    public boolean hasRole(String requiredRole) {
        return ok && requiredRole.equalsIgnoreCase(role);
    }
}
