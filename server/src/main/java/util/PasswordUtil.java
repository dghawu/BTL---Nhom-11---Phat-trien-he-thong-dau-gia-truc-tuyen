package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil - tiện ích mã hoá và xác thực mật khẩu bằng BCrypt.
 * <p>
 * KHÔNG bao giờ lưu mật khẩu plain-text vào DB.
 * Luồng:
 * Đăng ký → hash(rawPassword) → lưu hash vào DB
 * Đăng nhập → verify(rawPassword, hashFromDB) → true/false
 */
public class PasswordUtil {

    // Cost factor: 12 là mức khuyến nghị (cân bằng giữa bảo mật và tốc độ)
    private static final int BCRYPT_COST = 12;

    private PasswordUtil() {
    } // utility class, không khởi tạo

    /**
     * Hash mật khẩu trước khi lưu vào DB.
     * Mỗi lần gọi tạo ra salt ngẫu nhiên mới → cùng password cho hash khác nhau.
     *
     * @param rawPassword mật khẩu gốc người dùng nhập
     * @return chuỗi hash BCrypt (60 ký tự), an toàn để lưu thẳng vào DB
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống!");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập có khớp với hash trong DB không.
     *
     * @param rawPassword    mật khẩu người dùng vừa nhập
     * @param hashedPassword hash đã lưu trong DB
     * @return true nếu khớp
     */
    public static boolean verify(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (Exception e) {
            // BCrypt ném lỗi nếu hashedPassword không phải BCrypt hash hợp lệ
            return false;
        }
    }
}
