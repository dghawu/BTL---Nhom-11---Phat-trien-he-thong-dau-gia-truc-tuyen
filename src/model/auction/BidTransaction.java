package model.auction;

import model.entity.Entity;

import java.time.LocalDateTime;

/**
 * BidTransaction lưu một giao dịch đặt giá.
 * timestamp dùng làm trục X cho biểu đồ giá theo thời gian.
 */
public class BidTransaction extends Entity {
    private String bidderId;
    private String auctionId;
    private double amount;
    private LocalDateTime timestamp;

    // FIX: constructor đầy đủ 3 tham số — Bidder.java cần truyền auctionId
    public BidTransaction(String bidderId, String auctionId, double amount) {
        super(generateId());
        this.bidderId  = bidderId;
        this.auctionId = auctionId;
        this.amount    = amount;
        this.timestamp = LocalDateTime.now();
    }

    private static String generateId() {
        return "TXN-" + System.currentTimeMillis();
    }

    public String getBidderId()         { return bidderId; }
    public String getAuctionId()        { return auctionId; }
    public double getAmount()           { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public void printInfo() {
        System.out.println("Transaction: " + getId()
                + " | Bidder: " + bidderId
                + " | Auction: " + auctionId
                + " | Amount: " + amount
                + " | Time: " + timestamp);
    }
}