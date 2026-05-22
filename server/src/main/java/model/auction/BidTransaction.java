package model.auction;

import model.entity.Entity;

import java.time.LocalDateTime;

/**
 * BidTransaction lưu một giao dịch đặt giá.
 * timestamp dùng làm trục X cho biểu đồ giá theo thời gian.
 */
public class BidTransaction extends Entity {
    private String bidderId;
    private String bidderName;
    private String auctionId;
    private double amount;
    private LocalDateTime timestamp;

    public BidTransaction(String bidderId, String auctionId, double amount) {
        super(generateId());
        this.bidderId = bidderId;
        this.auctionId = auctionId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    private static String generateId() {
        return "TXN-" + System.currentTimeMillis();
    }

    public String getBidderId() {
        return bidderId;
    }

    public String getBidderName() { return bidderName; }

    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public String getAuctionId() {
        return auctionId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void printInfo() {
        System.out.println("Transaction: " + getId()
                + " | Bidder: " + bidderId
                + " | Auction: " + auctionId
                + " | Amount: " + amount
                + " | Time: " + timestamp);
    }
}