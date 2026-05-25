package util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho PasswordUtil — mã hoá và xác thực mật khẩu bằng BCrypt.
 */
@DisplayName("PasswordUtil - BCrypt Password Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PasswordUtilTest {

    // ── Test 1: hash() ─────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("hash(): Mật khẩu hợp lệ → trả về BCrypt hash không null")
    void testHashNotNull() {
        String hashed = PasswordUtil.hash("myPassword123");
        assertNotNull(hashed, "Hash không được null");
        assertFalse(hashed.isBlank(), "Hash không được rỗng");
    }

    @Test
    @Order(2)
    @DisplayName("hash(): Hash bắt đầu bằng '$2a$' (định dạng BCrypt)")
    void testHashBCryptFormat() {
        String hashed = PasswordUtil.hash("testPass");
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$"),
                "Hash phải theo định dạng BCrypt ($2a$ hoặc $2b$)");
    }

    @Test
    @Order(3)
    @DisplayName("hash(): Cùng password → tạo hash khác nhau mỗi lần (do salt ngẫu nhiên)")
    void testHashUniquenessPerCall() {
        String pass = "samePassword";
        String h1 = PasswordUtil.hash(pass);
        String h2 = PasswordUtil.hash(pass);

        assertNotEquals(h1, h2, "Mỗi lần hash phải tạo ra giá trị khác nhau do salt");
    }

    @Test
    @Order(4)
    @DisplayName("hash(): Mật khẩu null → ném IllegalArgumentException")
    void testHashNullPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.hash(null),
                "Mật khẩu null phải ném IllegalArgumentException");
    }

    @Test
    @Order(5)
    @DisplayName("hash(): Mật khẩu rỗng → ném IllegalArgumentException")
    void testHashBlankPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.hash(""),
                "Mật khẩu rỗng phải ném IllegalArgumentException");
    }

    @Test
    @Order(6)
    @DisplayName("hash(): Mật khẩu chỉ có dấu cách → ném IllegalArgumentException")
    void testHashWhitespaceOnlyPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordUtil.hash("   "),
                "Mật khẩu chỉ có khoảng trắng phải ném IllegalArgumentException");
    }

    // ── Test 2: verify() ───────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("verify(): Mật khẩu đúng → trả về true")
    void testVerifyCorrectPassword() {
        String raw = "mySecurePass!123";
        String hashed = PasswordUtil.hash(raw);

        assertTrue(PasswordUtil.verify(raw, hashed),
                "Mật khẩu đúng phải xác thực thành công");
    }

    @Test
    @Order(8)
    @DisplayName("verify(): Mật khẩu sai → trả về false")
    void testVerifyWrongPassword() {
        String raw = "correctPass";
        String hashed = PasswordUtil.hash(raw);

        assertFalse(PasswordUtil.verify("wrongPass", hashed),
                "Mật khẩu sai phải trả về false");
    }

    @Test
    @Order(9)
    @DisplayName("verify(): Mật khẩu null → trả về false (không ném exception)")
    void testVerifyNullPassword() {
        String hashed = PasswordUtil.hash("somePass");
        assertFalse(PasswordUtil.verify(null, hashed),
                "Mật khẩu null phải trả về false");
    }

    @Test
    @Order(10)
    @DisplayName("verify(): Hash null → trả về false (không ném exception)")
    void testVerifyNullHash() {
        assertFalse(PasswordUtil.verify("somePass", null),
                "Hash null phải trả về false");
    }

    @Test
    @Order(11)
    @DisplayName("verify(): Hash không phải BCrypt format → trả về false (không crash)")
    void testVerifyInvalidHashFormat() {
        assertFalse(PasswordUtil.verify("pass", "not-a-bcrypt-hash"),
                "Hash không hợp lệ phải trả về false");
    }

    // ── Test 3: Trường hợp đặc biệt ───────────────────────────────

    @Test
    @Order(12)
    @DisplayName("hash và verify: Mật khẩu có ký tự đặc biệt")
    void testSpecialCharacterPassword() {
        String special = "P@$$w0rd!#%^&*()_+";
        String hashed = PasswordUtil.hash(special);

        assertTrue(PasswordUtil.verify(special, hashed));
        assertFalse(PasswordUtil.verify("P@$$w0rd", hashed));
    }

    @Test
    @Order(13)
    @DisplayName("hash và verify: Mật khẩu rất dài (72+ ký tự BCrypt xử lý)")
    void testLongPassword() {
        String longPass = "A".repeat(72);
        String hashed = PasswordUtil.hash(longPass);

        assertTrue(PasswordUtil.verify(longPass, hashed));
    }

    @Test
    @Order(14)
    @DisplayName("Luồng đầy đủ: Đăng ký → hash → lưu → đăng nhập → verify")
    void testFullRegistrationLoginFlow() {
        // Đăng ký: hash mật khẩu trước khi lưu
        String rawPassword = "user_password_2024";
        String storedHash = PasswordUtil.hash(rawPassword);

        // Đăng nhập: verify mật khẩu nhập vào với hash đã lưu
        assertTrue(PasswordUtil.verify(rawPassword, storedHash), "Đăng nhập đúng mật khẩu phải thành công");
        assertFalse(PasswordUtil.verify("wrong_attempt", storedHash), "Đăng nhập sai mật khẩu phải thất bại");
    }
}
