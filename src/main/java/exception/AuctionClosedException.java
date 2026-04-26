package exception;

public class AuctionClosedException  extends  RuntimeException{
    private final String auctionId;
    public AuctionClosedException(String auctionId, String status) {
        super("Phiên đấu giá '" + auctionId + "' không thể đặt giá (trạng thái: " + status + ")");
        this.auctionId = auctionId;
    }
    public String getAuctionId() {return auctionId;}
}
