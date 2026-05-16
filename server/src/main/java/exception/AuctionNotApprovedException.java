package exception;

/**
 * Ném khi Seller cố start phiên nhưng Admin chưa duyệt (status != APPROVED).
 * Phân biệt với AuctionClosedException — đây là lỗi luồng duyệt, không phải
 * lỗi đặt giá.
 */
public class AuctionNotApprovedException extends AuctionSystemException {

    private final String auctionId;

    public AuctionNotApprovedException(String auctionId) {
        super("AUCTION_NOT_APPROVED",
                "Phiên '" + auctionId + "' chưa được Admin duyệt. "
                        + "Không thể bắt đầu phiên ở trạng thái PENDING hoặc REJECTED.");
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
