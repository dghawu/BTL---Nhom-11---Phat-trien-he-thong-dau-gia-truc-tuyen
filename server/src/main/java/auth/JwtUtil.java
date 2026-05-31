package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil - tạo, ký và xác thực JWT token.
 * <p>
 * Cấu trúc JWT gồm 3 phần: header.payload.signature
 * - header:    thuật toán ký (HS256)
 * - payload:   userId, username, role, thời hạn (claims)
 * - signature: ký bằng SECRET_KEY → client không thể giả mạo
 * <p>
 * Luồng sử dụng:
 * Login thành công → JwtUtil.generateToken(user)
 * → trả token về client
 * Request tiếp theo → client gửi token
 * → JwtUtil.validate(token) → lấy thông tin user
 */
public final class JwtUtil {

    // ── Cấu hình ──────────────────────────────────────────────────

    /**
     * SECRET KEY — THAY BẰNG GIÁ TRỊ NGẪU NHIÊN DÀI ÍT NHẤT 32 KÝ TỰ
     * TRONG PRODUCTION.
     * Trong thực tế nên đọc từ biến môi trường:
     * System.getenv("JWT_SECRET")
     */
    private static final String SECRET_STRING =
            "AuctionSystem_SuperSecretKey_2024_DoNotShare!";

    /**
     * Token hết hạn sau 8 tiếng (tính bằng millisecond).
     */
    private static final long EXPIRATION_MS = 8 * 60 * 60 * 1000L;

    /**
     * Khóa ký JWT từ chuỗi bí mật.
     */
    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {
    }

    // ── Public API ─────────────────────────────────────────────────

    /**
     * Tạo JWT token cho user sau khi đăng nhập thành công.
     *
     * @param userId   ID của user
     * @param username tên đăng nhập
     * @param role     BIDDER / SELLER / ADMIN
     * @return JWT string dạng "xxxxx.yyyyy.zzzzz"
     */
    public static String generateToken(final String userId,
                                       final String username,
                                       final String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Xác thực token và trả về Claims (thông tin bên trong).
     * Ném JwtException nếu token không hợp lệ hoặc hết hạn.
     *
     * @param token JWT string từ client
     * @return Claims chứa userId, username, role
     * @throws JwtException nếu token sai chữ ký, hết hạn, hoặc bị chỉnh sửa
     */
    public static Claims validate(final String token) {
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
     * @param token JWT string từ client
     * @return true nếu token hợp lệ và chưa hết hạn
     */
    public static boolean isValid(final String token) {
        try {
            validate(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Getters tiện lợi ──────────────────────────────────────────

    /**
     * Lấy userId từ token (không cần validate lại nếu đã validate trước đó).
     *
     * @param token JWT string từ client
     * @return userId trong token
     */
    public static String getUserId(final String token) {
        return validate(token).getSubject();
    }

    /**
     * Lấy username từ token.
     *
     * @param token JWT string từ client
     * @return username trong token
     */
    public static String getUsername(final String token) {
        return validate(token).get("username", String.class);
    }

    /**
     * Lấy role từ token.
     *
     * @param token JWT string từ client
     * @return role trong token
     */
    public static String getRole(final String token) {
        return validate(token).get("role", String.class);
    }
}
