package exception;

/**
 * Base exception của toàn bộ hệ thống đấu giá.
 * Mọi custom exception đều kế thừa từ đây để có thể catch chung
 * bằng một khối duy nhất: catch (AuctionSystemException e)
 * <p>
 * Có thêm field errorCode để client dễ xử lý theo loại lỗi
 * mà không cần parse chuỗi message.
 */
public abstract class AuctionSystemException extends RuntimeException {

    private final String errorCode;

    protected AuctionSystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected AuctionSystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Mã lỗi ngắn gọn để client switch-case, VD: "AUCTION_CLOSED", "BID_TOO_LOW"
     */
    public String getErrorCode() {
        return errorCode;
    }
}
