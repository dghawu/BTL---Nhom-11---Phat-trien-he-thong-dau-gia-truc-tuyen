package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.json.JSONObject;

/**
 * TokenGuard - middleware xác thực JWT cho mọi request trừ login/register.
 * <p>
 * Mỗi request cần auth phải có field "token" trong JSON:
 * {"action":"placeBid", "token":"eyJhbGc...", "auctionId":"AUC-01",
 * "amount":1500}
 * <p>
 * Cách dùng trong ClientHandler:
 * AuthResult auth = TokenGuard.check(req);
 * if (!auth.isOk()) return fail(auth.getErrorMessage());
 * String userId = auth.getUserId();
 */
public final class TokenGuard {

    /**
     * Tên field chứa token trong JSON request
     */
    public static final String TOKEN_FIELD = "token";

    private TokenGuard() {
    }

    /**
     * Kiểm tra token trong request JSON.
     *
     * @param req JSONObject request từ client
     * @return AuthResult.success(...) hoặc AuthResult.fail(...)
     */
    public static AuthResult check(final JSONObject req) {
        boolean missingToken = !req.has(TOKEN_FIELD)
                || req.getString(TOKEN_FIELD).isBlank();
        if (missingToken) {
            return AuthResult.fail("Yêu cầu xác thực: thiếu token.");
        }

        String token = req.getString(TOKEN_FIELD);

        // Xác thực và giải mã token
        try {
            Claims claims = JwtUtil.validate(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            return AuthResult.success(userId, username, role);

        } catch (JwtException e) {
            // Token sai chữ ký, hết hạn, bị chỉnh sửa...
            return AuthResult.fail(
                    "Token không hợp lệ hoặc đã hết hạn. "
                            + "Vui lòng đăng nhập lại.");
        }
    }

    /**
     * Kiểm tra token VÀ yêu cầu đúng role.
     *
     * @param req          JSONObject request
     * @param requiredRole role yêu cầu (VD: "ADMIN")
     * @return AuthResult
     */
    public static AuthResult checkRole(final JSONObject req,
                                       final String requiredRole) {
        AuthResult result = check(req);
        if (!result.isOk()) {
            return result;
        }

        boolean hasRequiredRole = result.hasRole(requiredRole);
        if (!hasRequiredRole) {
            String message = "Không có quyền truy cập. Yêu cầu role: "
                    + requiredRole;
            return AuthResult.fail(message);
        }
        return result;
    }
}
