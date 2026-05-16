package exception;

import model.enums.AuctionStatus;

/**
 * Ném khi Seller cố sửa thông tin phiên sau khi đã được duyệt hoặc đang chạy.
 * Phiên chỉ có thể sửa ở trạng thái PENDING.
 */
public class AuctionEditNotAllowedException extends AuctionSystemException {

    private final String auctionId;
    private final AuctionStatus currentStatus;

    public AuctionEditNotAllowedException(String auctionId, AuctionStatus currentStatus) {
        super("AUCTION_EDIT_NOT_ALLOWED",
                "Không thể sửa phiên '" + auctionId + "' — chỉ được sửa khi PENDING. "
                        + "Trạng thái hiện tại: " + currentStatus.name());
        this.auctionId = auctionId;
        this.currentStatus = currentStatus;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public AuctionStatus getCurrentStatus() {
        return currentStatus;
    }
}
