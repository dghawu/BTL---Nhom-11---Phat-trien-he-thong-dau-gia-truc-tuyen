package auth;

/**
 * AuthResult - kết quả trả về sau khi xác thực token trong mỗi request.
 * <p>
 * Thay vì ném exception khắp nơi, ClientHandler gọi
 * AuthResult r = TokenGuard.check(req) rồi kiểm tra
 * r.isOk().
 */
public final class AuthResult {

    /**
     * True nếu token hợp lệ và request được phép.
     */
    private final boolean ok;

    /**
     * ID người dùng từ token.
     */
    private final String userId;

    /**
     * Username từ token.
     */
    private final String username;

    /**
     * Role từ token.
     */
    private final String role;

    /**
     * Thông báo lỗi khi xác thực thất bại.
     */
    private final String errorMessage;

    private AuthResult(final String id,
                       final String usernameParam,
                       final String roleParam) {
        this.ok = true;
        this.userId = id;
        this.username = usernameParam;
        this.role = roleParam;
        this.errorMessage = null;
    }

    private AuthResult(final String errorMessageParam) {
        this.ok = false;
        this.userId = null;
        this.username = null;
        this.role = null;
        this.errorMessage = errorMessageParam;
    }

    /**
     * Tạo AuthResult thành công.
     *
     * @param userId   id người dùng
     * @param username tên người dùng
     * @param role     role người dùng
     * @return AuthResult thành công
     */
    public static AuthResult success(final String userId,
                                     final String username,
                                     final String role) {
        return new AuthResult(userId, username, role);
    }

    /**
     * Tạo AuthResult thất bại.
     *
     * @param message thông báo lỗi
     * @return AuthResult thất bại
     */
    public static AuthResult fail(final String message) {
        return new AuthResult(message);
    }

    /**
     * @return true nếu xác thực thành công
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * @return userId trong token nếu auth thành công, hoặc null nếu thất bại
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return username trong token nếu auth thành công
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return role trong token nếu auth thành công
     */
    public String getRole() {
        return role;
    }

    /**
     * @return thông báo lỗi khi auth thất bại
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Kiểm tra quyền — ném lỗi nếu không đúng role.
     *
     * @param requiredRole role yêu cầu
     * @return true nếu role khớp
     */
    public boolean hasRole(final String requiredRole) {
        return ok && requiredRole.equalsIgnoreCase(role);
    }
}
