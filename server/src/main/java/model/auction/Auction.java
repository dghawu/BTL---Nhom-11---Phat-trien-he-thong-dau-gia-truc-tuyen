package model.auction;

import exception.*;
import model.entity.Entity;
import model.enums.AuctionStatus;
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
    private AuctionStatus status;
    private List<BidTransaction> bidHistory;
    private List<AuctionObserver> observers;

    public Auction(String id, Item item, double startPrice, double minIncrement,
                   LocalDateTime startTime, LocalDateTime endTime) {
        super(id);
        if (startPrice <= 0) throw new InvalidItemPriceException("startPrice", startPrice);
        if (minIncrement <= 0) throw new InvalidItemPriceException("minIncrement", minIncrement);
        this.auctionId = id;
        this.item = item;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.minIncrement = minIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.PENDING;
        this.bidHistory = new ArrayList<>();
        this.observers = new ArrayList<>();
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
     * Đặt giá — kiểm tra đầy đủ trạng thái, thời gian, và mức giá
     */
    public synchronized void handleNewBid(BidTransaction bid) {
        if (status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException(auctionId, status);
        }
        if (LocalDateTime.now().isAfter(endTime)) {
            closeAuction();
            throw new AuctionClosedException(auctionId, AuctionStatus.FINISHED);
        }
        // Chặn tự đặt giá khi đang thắng
        if (bid.getBidderId().equals(currentWinner)) {
            throw new SelfBidException(bid.getBidderId(), auctionId);
        }
        double requiredPrice = currentPrice + minIncrement;
        if (bid.getAmount() < requiredPrice) {
            throw new InvalidBidException(bid.getAmount(), requiredPrice);
        }
        // Anti-sniping
        if (LocalDateTime.now().isAfter(endTime.minusSeconds(30))) {
            endTime = endTime.plusSeconds(60);
            System.out.println("[ANTI-SNIPE] Gia hạn phiên đến: " + endTime);
            AuctionTimer.getInstance().reschedule(this);
        }
        this.currentPrice = bid.getAmount();
        this.currentWinner = bid.getBidderId();
        this.bidHistory.add(bid);
        notifyObservers(this, currentPrice, bid.getBidderId());
        System.out.println("Đặt giá thành công! Giá hiện tại: " + currentPrice);
    }

    /**
     * Bắt đầu phiên — yêu cầu status == APPROVED
     */
    public void startAuction() {
        if (status != AuctionStatus.APPROVED) {
            throw new AuctionNotApprovedException(auctionId);
        }
        this.status = AuctionStatus.RUNNING;
        System.out.println("Phiên " + auctionId + " bắt đầu!");
        AuctionTimer.getInstance().schedule(this);
    }

    public void closeAuction() {
        this.status = AuctionStatus.FINISHED;
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
    public void addObserver(AuctionObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(AuctionObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(Auction auction, double newPrice, String lastBidderId) {
        for (AuctionObserver o : observers) o.update(this, newPrice, lastBidderId);
    }

    // Getters & Setters
    public String getAuctionId() {
        return auctionId;
    }

    public Item getItem() {
        return item;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getMinIncrement() {
        return minIncrement;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getCurrentWinner() {
        return currentWinner;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setName(String name) {
        item.setName(name);
    }

    public void setStartPrice(double price) {
        item.setStartPrice(price);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setMinIncrement(double minIncrement) {
        this.minIncrement = minIncrement;
    }
}
