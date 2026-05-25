package model.auth;

import auth.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho JwtUtil — tạo, xác thực, và trích xuất thông tin JWT token.
 */
@DisplayName("JwtUtil - JWT Token Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtUtilTest {

    private static String validToken;

    @BeforeAll
    static void setUpAll() {
        // Tạo một token hợp lệ dùng chung cho các test
        validToken = JwtUtil.generateToken("USER-001", "testUser", "BIDDER");
    }

    // ── Test 1: Tạo token ──────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("generateToken: Tạo token không null và có đúng 3 phần (header.payload.signature)")
    void testGenerateTokenNotNull() {
        String token = JwtUtil.generateToken("U01", "alice", "SELLER");
        assertNotNull(token, "Token không được null");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT phải có đúng 3 phần");
    }

    @Test
    @Order(2)
    @DisplayName("generateToken: Mỗi lần gọi với cùng input vẫn tạo token hợp lệ")
    void testGenerateMultipleTokens() {
        String t1 = JwtUtil.generateToken("U01", "bob", "ADMIN");
        String t2 = JwtUtil.generateToken("U01", "bob", "ADMIN");
        assertNotNull(t1);
        assertNotNull(t2);
        // Cả hai đều hợp lệ
        assertTrue(JwtUtil.isValid(t1));
        assertTrue(JwtUtil.isValid(t2));
    }

    // ── Test 2: Validate và trích xuất claims ──────────────────────

    @Test
    @Order(3)
    @DisplayName("validate: Token hợp lệ → trả về Claims đúng")
    void testValidateValidToken() {
        Claims claims = JwtUtil.validate(validToken);
        assertNotNull(claims);
        assertEquals("USER-001", claims.getSubject());
        assertEquals("testUser", claims.get("username", String.class));
        assertEquals("BIDDER", claims.get("role", String.class));
    }

    @Test
    @Order(4)
    @DisplayName("validate: Token giả mạo → ném JwtException")
    void testValidateFakeToken() {
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.fake.signature";
        assertThrows(JwtException.class, () -> JwtUtil.validate(fakeToken),
                "Token giả mạo phải ném JwtException");
    }

    @Test
    @Order(5)
    @DisplayName("validate: Token rỗng → ném exception")
    void testValidateEmptyToken() {
        assertThrows(Exception.class, () -> JwtUtil.validate(""),
                "Token rỗng phải ném exception");
    }

    // ── Test 3: isValid ────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("isValid: Token hợp lệ → trả về true")
    void testIsValidTrue() {
        assertTrue(JwtUtil.isValid(validToken));
    }

    @Test
    @Order(7)
    @DisplayName("isValid: Token giả mạo → trả về false (không ném exception)")
    void testIsValidFakeToken() {
        assertFalse(JwtUtil.isValid("not.a.valid.jwt.token"));
    }

    @Test
    @Order(8)
    @DisplayName("isValid: Token null → trả về false")
    void testIsValidNullToken() {
        assertFalse(JwtUtil.isValid(null));
    }

    @Test
    @Order(9)
    @DisplayName("isValid: Token rỗng → trả về false")
    void testIsValidEmptyToken() {
        assertFalse(JwtUtil.isValid(""));
    }

    // ── Test 4: Getter methods ─────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("getUserId: Lấy đúng userId từ token")
    void testGetUserId() {
        assertEquals("USER-001", JwtUtil.getUserId(validToken));
    }

    @Test
    @Order(11)
    @DisplayName("getUsername: Lấy đúng username từ token")
    void testGetUsername() {
        assertEquals("testUser", JwtUtil.getUsername(validToken));
    }

    @Test
    @Order(12)
    @DisplayName("getRole: Lấy đúng role từ token")
    void testGetRole() {
        assertEquals("BIDDER", JwtUtil.getRole(validToken));
    }

    // ── Test 5: Các roles khác nhau ────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("Tạo và xác thực token với role ADMIN")
    void testAdminRoleToken() {
        String token = JwtUtil.generateToken("ADMIN-01", "adminUser", "ADMIN");
        assertTrue(JwtUtil.isValid(token));
        assertEquals("ADMIN", JwtUtil.getRole(token));
        assertEquals("ADMIN-01", JwtUtil.getUserId(token));
    }

    @Test
    @Order(14)
    @DisplayName("Tạo và xác thực token với role SELLER")
    void testSellerRoleToken() {
        String token = JwtUtil.generateToken("SELLER-01", "sellerUser", "SELLER");
        assertTrue(JwtUtil.isValid(token));
        assertEquals("SELLER", JwtUtil.getRole(token));
    }
}
