package exception;

import model.enums.AuctionStatus;

/**
 * Ném khi cố thao tác (đặt giá, sửa...) trên phiên không ở trạng thái RUNNING.
 */
public class AuctionClosedException extends AuctionSystemException {

    private final String auctionId;
    private final AuctionStatus currentStatus;

    public AuctionClosedException(String auctionId, AuctionStatus currentStatus) {
        super("AUCTION_CLOSED",
                "Phiên '" + auctionId + "' không thể thao tác — trạng thái hiện tại: "
                        + currentStatus.name());
        this.auctionId     = auctionId;
        this.currentStatus = currentStatus;
    }

    public String getAuctionId()             { return auctionId; }
    public AuctionStatus getCurrentStatus()  { return currentStatus; }
}
