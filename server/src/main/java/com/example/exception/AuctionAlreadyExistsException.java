package com.example.exception;

/**
 * Ném khi tạo phiên với ID đã tồn tại trong hệ thống.
 */
public class AuctionAlreadyExistsException extends AuctionSystemException {

    private final String auctionId;

    public AuctionAlreadyExistsException(String auctionId) {
        super("AUCTION_ALREADY_EXISTS",
                "Phiên đấu giá với ID '" + auctionId + "' đã tồn tại. "
                        + "Mỗi ID phải là duy nhất.");
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
