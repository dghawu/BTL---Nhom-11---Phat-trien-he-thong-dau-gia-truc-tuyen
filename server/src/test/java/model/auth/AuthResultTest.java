package model.auth;

import auth.AuthResult;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho AuthResult — kết quả xác thực token JWT.
 */
@DisplayName("AuthResult - Authentication Result Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthResultTest {

    // ── Test 1: Success result ─────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("success(): isOk() = true, lấy đúng thông tin user")
    void testSuccessResult() {
        AuthResult result = AuthResult.success("U01", "alice", "BIDDER");

        assertTrue(result.isOk());
        assertEquals("U01", result.getUserId());
        assertEquals("alice", result.getUsername());
        assertEquals("BIDDER", result.getRole());
        assertNull(result.getErrorMessage(), "Kết quả thành công không có error message");
    }

    @Test
    @Order(2)
    @DisplayName("success(): hasRole() khớp đúng role")
    void testHasRoleSuccess() {
        AuthResult result = AuthResult.success("U01", "alice", "BIDDER");

        assertTrue(result.hasRole("BIDDER"));
        assertTrue(result.hasRole("bidder"), "hasRole phải case-insensitive");
        assertFalse(result.hasRole("ADMIN"), "Sai role phải trả về false");
        assertFalse(result.hasRole("SELLER"), "Sai role phải trả về false");
    }

    // ── Test 2: Fail result ────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("fail(): isOk() = false, có error message")
    void testFailResult() {
        AuthResult result = AuthResult.fail("Token hết hạn");

        assertFalse(result.isOk());
        assertEquals("Token hết hạn", result.getErrorMessage());
        assertNull(result.getUserId(), "Kết quả lỗi không có userId");
        assertNull(result.getUsername(), "Kết quả lỗi không có username");
        assertNull(result.getRole(), "Kết quả lỗi không có role");
    }

    @Test
    @Order(4)
    @DisplayName("fail(): hasRole() luôn trả về false")
    void testHasRoleOnFailResult() {
        AuthResult result = AuthResult.fail("Không có quyền");

        assertFalse(result.hasRole("ADMIN"), "hasRole trên fail result phải trả về false");
        assertFalse(result.hasRole("BIDDER"), "hasRole trên fail result phải trả về false");
    }

    // ── Test 3: Các loại role khác nhau ───────────────────────────

    @Test
    @Order(5)
    @DisplayName("success() với ADMIN role")
    void testAdminResult() {
        AuthResult result = AuthResult.success("A01", "admin", "ADMIN");

        assertTrue(result.isOk());
        assertTrue(result.hasRole("ADMIN"));
        assertFalse(result.hasRole("BIDDER"));
    }

    @Test
    @Order(6)
    @DisplayName("success() với SELLER role")
    void testSellerResult() {
        AuthResult result = AuthResult.success("S01", "seller", "SELLER");

        assertTrue(result.isOk());
        assertTrue(result.hasRole("SELLER"));
        assertTrue(result.hasRole("seller"), "Case-insensitive check");
    }

    // ── Test 4: Edge cases ─────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("fail() với message rỗng vẫn hoạt động")
    void testFailEmptyMessage() {
        AuthResult result = AuthResult.fail("");
        assertFalse(result.isOk());
        assertEquals("", result.getErrorMessage());
    }

    @Test
    @Order(8)
    @DisplayName("Hai AuthResult.success cùng data là độc lập nhau")
    void testTwoSuccessResultsAreIndependent() {
        AuthResult r1 = AuthResult.success("U01", "alice", "BIDDER");
        AuthResult r2 = AuthResult.success("U02", "bob", "ADMIN");

        assertNotSame(r1, r2);
        assertNotEquals(r1.getUserId(), r2.getUserId());
        assertNotEquals(r1.getRole(), r2.getRole());
    }
}
