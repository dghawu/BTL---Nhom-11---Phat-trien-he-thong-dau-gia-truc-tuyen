package model.auction;

import exception.AuctionClosedException;
import exception.InvalidBidException;
import model.entity.Entity;
import model.item.Item;
import observer.AuctionObserver;
import observer.Subject;
import service.AuctionTimer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity implements Subject {
    private String auctionId;
    private Item item;
    private double startPrice;
    private double currentPrice;
    private double minIncrement;
    private String currentWinner;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // PENDING, APPROVED, RUNNING, FINISHED, PAYING, REJECTED, CANCELED
    private List<BidTransaction> bidHistory;
    private List<AuctionObserver> observers;

    public Auction(String id, Item item, double startPrice, double minIncrement,
                   LocalDateTime startTime, LocalDateTime endTime) {
        super(id);
        this.auctionId    = id;
        this.item         = item;
        this.startPrice   = startPrice;
        this.currentPrice = startPrice;
        this.minIncrement = minIncrement;
        this.startTime    = startTime;
        this.endTime      = endTime;
        this.status       = "PENDING";
        this.bidHistory   = new ArrayList<>();
        this.observers    = new ArrayList<>();
    }

    @Override
    public void printInfo() {
        System.out.println("Auction ID  : " + auctionId);
        System.out.println("Item        : " + item.getName());
        System.out.println("Giá hiện tại: " + currentPrice);
        System.out.println("Trạng thái  : " + status);
        System.out.println("Kết thúc    : " + endTime);
    }

    /**
     * Xử lý lượt đặt giá — throws exception thay vì return false.
     * Caller (Bidder.placeManualBid) cần try-catch để xử lý.
     */
    public synchronized void handleNewBid(BidTransaction bid) {
        // 1. Kiểm tra trạng thái phiên
        if (!status.equals("RUNNING")) {
            throw new AuctionClosedException(auctionId, status);
        }
        // 2. Kiểm tra hết giờ
        if (LocalDateTime.now().isAfter(endTime)) {
            closeAuction();
            throw new AuctionClosedException(auctionId, "FINISHED");
        }
        // 3. Kiểm tra giá hợp lệ
        double requiredPrice = currentPrice + minIncrement;
        if (bid.getAmount() < requiredPrice) {
            throw new InvalidBidException(bid.getAmount(), requiredPrice);
        }

        // 4. Anti-sniping: bid trong 30 giây cuối → gia hạn +60 giây
        if (LocalDateTime.now().isAfter(endTime.minusSeconds(30))) {
            endTime = endTime.plusSeconds(60);
            System.out.println("[ANTI-SNIPE] Gia hạn phiên đến: " + endTime);
            AuctionTimer.getInstance().reschedule(this);
        }

        // 5. Cập nhật
        this.currentPrice  = bid.getAmount();
        this.currentWinner = bid.getBidderId();
        this.bidHistory.add(bid);
        notifyObservers(this, currentPrice, bid.getBidderId());

        System.out.println("Đặt giá thành công! Giá hiện tại: " + currentPrice);
    }

    public void startAuction() {
        if (status.equals("APPROVED")) {
            this.status = "RUNNING";
            System.out.println("Phiên " + auctionId + " bắt đầu!");
            AuctionTimer.getInstance().schedule(this);
        } else {
            throw new AuctionClosedException(auctionId, status);
        }
    }

    public void closeAuction() {
        this.status = "FINISHED";
        if (currentWinner != null) {
            System.out.println("Phiên kết thúc. Người thắng: " + currentWinner
                    + " | Giá: " + currentPrice);
        } else {
            System.out.println("Phiên kết thúc. Không có người mua.");
        }
        notifyObservers(this, currentPrice, currentWinner != null ? currentWinner : "");
    }

    // Observer
    @Override
    public void addObserver(AuctionObserver o)    { observers.add(o); }
    @Override
    public void removeObserver(AuctionObserver o) { observers.remove(o); }
    @Override
    public void notifyObservers(Auction auction, double newPrice, String lastBidderId) {
        for (AuctionObserver o : observers) o.update(this, newPrice, lastBidderId);
    }

    // Getters & Setters
    public String getAuctionId()                { return auctionId; }
    public Item getItem()                       { return item; }
    public double getCurrentPrice()             { return currentPrice; }
    public double getStartPrice()               { return startPrice; }
    public LocalDateTime getEndTime()           { return endTime; }
    public String getCurrentWinner()            { return currentWinner; }
    public List<BidTransaction> getBidHistory() { return bidHistory; }
    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }
    public void setName(String name)            { item.setName(name); }
    public void setStartPrice(double price)     { item.setStartPrice(price); }
}