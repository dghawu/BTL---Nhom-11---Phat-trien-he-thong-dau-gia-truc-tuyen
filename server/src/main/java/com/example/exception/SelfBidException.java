package com.example.exception;

/**
 * Ném khi người dùng đang dẫn đầu lại cố đặt thêm giá trên phiên của chính họ.
 * Tránh tình trạng tự đấu giá với bản thân để đội giá ảo.
 */
public class SelfBidException extends AuctionSystemException {

    private final String bidderId;
    private final String auctionId;

    public SelfBidException(String bidderId, String auctionId) {
        super("SELF_BID",
                "Người dùng '" + bidderId + "' đang là người dẫn đầu phiên '"
                        + auctionId + "'. Không thể tự đặt giá khi đang thắng.");
        this.bidderId = bidderId;
        this.auctionId = auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
