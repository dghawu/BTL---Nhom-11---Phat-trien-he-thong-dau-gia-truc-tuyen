package auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil - tạo, ký và xác thực JWT token.
 *
 * Cấu trúc JWT gồm 3 phần: header.payload.signature
 *   - header:    thuật toán ký (HS256)
 *   - payload:   userId, username, role, thời hạn (claims)
 *   - signature: ký bằng SECRET_KEY → client không thể giả mạo
 *
 * Luồng sử dụng:
 *   Login thành công → JwtUtil.generateToken(user) → trả token về client
 *   Request tiếp theo → client gửi token → JwtUtil.validate(token) → lấy thông tin user
 */
public class JwtUtil {

    // ── Cấu hình ──────────────────────────────────────────────────

    /**
     * SECRET KEY — THAY BẰNG GIÁ TRỊ NGẪU NHIÊN DÀI ÍT NHẤT 32 KÝ TỰ TRONG PRODUCTION.
     * Trong thực tế nên đọc từ biến môi trường: System.getenv("JWT_SECRET")
     */
    private static final String SECRET_STRING =
            "AuctionSystem_SuperSecretKey_2024_DoNotShare!";

    /** Token hết hạn sau 8 tiếng (tính bằng millisecond) */
    private static final long EXPIRATION_MS = 8 * 60 * 60 * 1000L;

    // SecretKey được tạo một lần duy nhất từ SECRET_STRING
    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {} // utility class

    // ── Public API ─────────────────────────────────────────────────

    /**
     * Tạo JWT token cho user sau khi đăng nhập thành công.
     *
     * @param userId   ID của user
     * @param username tên đăng nhập
     * @param role     BIDDER / SELLER / ADMIN
     * @return JWT string dạng "xxxxx.yyyyy.zzzzz"
     */
    public static String generateToken(String userId, String username, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId)                    // subject = userId
                .claim("username", username)         // thêm username vào payload
                .claim("role",     role)             // thêm role vào payload
                .issuedAt(now)                       // thời điểm tạo
                .expiration(expiry)                  // thời điểm hết hạn
                .signWith(SECRET_KEY)                // ký bằng HS256
                .compact();                          // tạo chuỗi JWT
    }

    /**
     * Xác thực token và trả về Claims (thông tin bên trong).
     * Ném JwtException nếu token không hợp lệ hoặc hết hạn.
     *
     * @param token JWT string từ client
     * @return Claims chứa userId, username, role
     * @throws JwtException nếu token sai chữ ký, hết hạn, hoặc bị chỉnh sửa
     */
    public static Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Kiểm tra token có hợp lệ không (không ném exception).
     * Tiện dụng cho các điểm kiểm tra nhanh.
     *
     * @return true nếu token hợp lệ và chưa hết hạn
     */
    public static boolean isValid(String token) {
        try {
            validate(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Getters tiện lợi ──────────────────────────────────────────

    /** Lấy userId từ token (không cần validate lại nếu đã validate trước đó) */
    public static String getUserId(String token) {
        return validate(token).getSubject();
    }

    /** Lấy username từ token */
    public static String getUsername(String token) {
        return validate(token).get("username", String.class);
    }

    /** Lấy role từ token */
    public static String getRole(String token) {
        return validate(token).get("role", String.class);
    }
}
