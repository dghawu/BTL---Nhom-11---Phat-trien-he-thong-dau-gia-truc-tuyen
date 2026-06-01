package model.auth;

import com.example.auth.AuthResult;
import com.example.auth.JwtUtil;
import com.example.auth.TokenGuard;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho TokenGuard — middleware xác thực JWT trong mỗi request JSON.
 */
@DisplayName("TokenGuard - JWT Middleware Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TokenGuardTest {

    // Tạo token hợp lệ để tái sử dụng trong nhiều test
    private static String validBidderToken;
    private static String validAdminToken;
    private static String validSellerToken;

    @BeforeAll
    static void setUpAll() {
        validBidderToken = JwtUtil.generateToken("B-001", "bidderUser", "BIDDER");
        validAdminToken = JwtUtil.generateToken("A-001", "adminUser", "ADMIN");
        validSellerToken = JwtUtil.generateToken("S-001", "sellerUser", "SELLER");
    }

    // Helper: tạo JSONObject request với token
    private JSONObject requestWith(String token) {
        JSONObject req = new JSONObject();
        req.put("action", "placeBid");
        req.put("token", token);
        return req;
    }

    // ── Test 1: check() với token hợp lệ ──────────────────────────

    @Test
    @Order(1)
    @DisplayName("check(): Token BIDDER hợp lệ → AuthResult thành công với đúng thông tin")
    void testCheckValidBidderToken() {
        JSONObject req = requestWith(validBidderToken);
        AuthResult result = TokenGuard.check(req);

        assertTrue(result.isOk());
        assertEquals("B-001", result.getUserId());
        assertEquals("bidderUser", result.getUsername());
        assertEquals("BIDDER", result.getRole());
    }

    @Test
    @Order(2)
    @DisplayName("check(): Token ADMIN hợp lệ → AuthResult thành công")
    void testCheckValidAdminToken() {
        AuthResult result = TokenGuard.check(requestWith(validAdminToken));

        assertTrue(result.isOk());
        assertEquals("A-001", result.getUserId());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    @Order(3)
    @DisplayName("check(): Token SELLER hợp lệ → AuthResult thành công")
    void testCheckValidSellerToken() {
        AuthResult result = TokenGuard.check(requestWith(validSellerToken));

        assertTrue(result.isOk());
        assertEquals("SELLER", result.getRole());
    }

    // ── Test 2: check() với token không hợp lệ ────────────────────

    @Test
    @Order(4)
    @DisplayName("check(): Thiếu field 'token' → fail với message hữu ích")
    void testCheckMissingToken() {
        JSONObject req = new JSONObject();
        req.put("action", "placeBid");
        // Không có field "token"

        AuthResult result = TokenGuard.check(req);

        assertFalse(result.isOk());
        assertNotNull(result.getErrorMessage());
        assertFalse(result.getErrorMessage().isBlank());
    }

    @Test
    @Order(5)
    @DisplayName("check(): Token rỗng (blank) → fail")
    void testCheckBlankToken() {
        JSONObject req = new JSONObject();
        req.put("token", "   ");

        AuthResult result = TokenGuard.check(req);

        assertFalse(result.isOk());
    }

    @Test
    @Order(6)
    @DisplayName("check(): Token giả mạo → fail với message")
    void testCheckInvalidToken() {
        JSONObject req = requestWith("this.is.not.a.jwt");

        AuthResult result = TokenGuard.check(req);

        assertFalse(result.isOk());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @Order(7)
    @DisplayName("check(): Token bị chỉnh sửa (tampered) → fail")
    void testCheckTamperedToken() {
        // Lấy token hợp lệ và thêm ký tự vào cuối
        String tampered = validBidderToken + "tampered";
        AuthResult result = TokenGuard.check(requestWith(tampered));

        assertFalse(result.isOk());
    }

    // ── Test 3: checkRole() ────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("checkRole(): Token ADMIN + yêu cầu ADMIN → thành công")
    void testCheckRoleMatchAdmin() {
        AuthResult result = TokenGuard.checkRole(requestWith(validAdminToken), "ADMIN");

        assertTrue(result.isOk());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    @Order(9)
    @DisplayName("checkRole(): Token BIDDER + yêu cầu ADMIN → fail (không đủ quyền)")
    void testCheckRoleMismatch() {
        AuthResult result = TokenGuard.checkRole(requestWith(validBidderToken), "ADMIN");

        assertFalse(result.isOk());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @Order(10)
    @DisplayName("checkRole(): Token SELLER + yêu cầu SELLER → thành công")
    void testCheckRoleMatchSeller() {
        AuthResult result = TokenGuard.checkRole(requestWith(validSellerToken), "SELLER");

        assertTrue(result.isOk());
    }

    @Test
    @Order(11)
    @DisplayName("checkRole(): Thiếu token + yêu cầu ADMIN → fail (không có token)")
    void testCheckRoleMissingToken() {
        JSONObject req = new JSONObject();
        req.put("action", "approve");

        AuthResult result = TokenGuard.checkRole(req, "ADMIN");

        assertFalse(result.isOk());
    }

    // ── Test 4: TOKEN_FIELD constant ──────────────────────────────

    @Test
    @Order(12)
    @DisplayName("TOKEN_FIELD constant phải là 'token'")
    void testTokenFieldConstant() {
        assertEquals("token", TokenGuard.TOKEN_FIELD);
    }
}
