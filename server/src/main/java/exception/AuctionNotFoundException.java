package exception;

/**
 * Ném khi tra cứu phiên đấu giá theo ID nhưng không tìm thấy.
 */
public class AuctionNotFoundException extends AuctionSystemException {

    private final String auctionId;

    public AuctionNotFoundException(String auctionId) {
        super("AUCTION_NOT_FOUND",
                "Không tìm thấy phiên đấu giá với ID: '" + auctionId + "'");
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
