package exception;

/**
 * Ném khi user đã bị ban cố thực hiện hành động trên hệ thống.
 */
public class UserBannedException extends AuctionSystemException {

    private final String userId;

    public UserBannedException(String userId) {
        super("USER_BANNED",
                "Tài khoản '" + userId + "' đã bị khóa và không thể thực hiện thao tác này. "
                        + "Liên hệ Admin để biết thêm chi tiết.");
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
