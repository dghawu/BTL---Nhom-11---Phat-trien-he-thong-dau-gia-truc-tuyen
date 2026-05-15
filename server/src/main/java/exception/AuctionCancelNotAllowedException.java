package exception;

import model.enums.AuctionStatus;

/**
 * Ném khi cố hủy phiên đang chạy hoặc đã kết thúc.
 * Chỉ hủy được phiên ở trạng thái PENDING hoặc APPROVED.
 */
public class AuctionCancelNotAllowedException extends AuctionSystemException {

    private final String auctionId;
    private final AuctionStatus currentStatus;

    public AuctionCancelNotAllowedException(String auctionId, AuctionStatus currentStatus) {
        super("AUCTION_CANCEL_NOT_ALLOWED",
              "Không thể hủy phiên '" + auctionId + "' — trạng thái hiện tại: "
              + currentStatus.name() + ". Chỉ hủy được khi PENDING hoặc APPROVED.");
        this.auctionId     = auctionId;
        this.currentStatus = currentStatus;
    }

    public String getAuctionId()             { return auctionId; }
    public AuctionStatus getCurrentStatus()  { return currentStatus; }
}
