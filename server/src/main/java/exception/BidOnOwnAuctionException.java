package exception;

/**
 * Ném khi Seller cố đặt giá trên phiên do chính họ tạo ra.
 * Ngăn Seller tự làm giá leo thang trên sản phẩm của mình.
 */
public class BidOnOwnAuctionException extends AuctionSystemException {

    private final String sellerId;
    private final String auctionId;

    public BidOnOwnAuctionException(String sellerId, String auctionId) {
        super("BID_ON_OWN_AUCTION",
              "Seller '" + sellerId + "' không được phép đặt giá trên phiên '"
              + auctionId + "' do chính mình tạo ra.");
        this.sellerId  = sellerId;
        this.auctionId = auctionId;
    }

    public String getSellerId()  { return sellerId; }
    public String getAuctionId() { return auctionId; }
}
