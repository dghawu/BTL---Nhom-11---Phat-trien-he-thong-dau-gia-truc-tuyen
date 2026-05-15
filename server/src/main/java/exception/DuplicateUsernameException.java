package exception;

/**
 * Ném khi đăng ký tài khoản với tên đã tồn tại trong hệ thống.
 * Thay thế cho IllegalArgumentException mơ hồ trong UserService.registerBidder/Seller.
 */
public class DuplicateUsernameException extends AuctionSystemException {

    private final String username;

    public DuplicateUsernameException(String username) {
        super("DUPLICATE_USERNAME",
              "Tên đăng nhập '" + username + "' đã được sử dụng. "
              + "Vui lòng chọn tên khác.");
        this.username = username;
    }

    public String getUsername() { return username; }
}
