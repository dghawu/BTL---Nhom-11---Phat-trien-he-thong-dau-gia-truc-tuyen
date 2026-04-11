package model.auction;

import model.entity.Entity;
import model.item.Item;
import model.user.Bidder;
import observer.AuctionObserver;
import observer.Subject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp model.auction.Auction quản lý phiên đấu giá cho một sản phẩm cụ thể.
 * Đảm bảo logic về thời gian, giá cao nhất và trạng thái phiên.
 */
public class Auction extends Entity implements Subject {
    private String auctionId;
    private Item item;
    private double currentPrice;
    private double minIncrement;
    private String currentWinner;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // OPEN, RUNNING, FINISHED, PAID, CANCELED
    private List<BidTransaction> bidHistory;
    private List<AuctionObserver> observers;

    @Override
    public void printInfo()  {
        return;
    }

    public Auction(String id, Item item,double minIncrement, LocalDateTime startTime, LocalDateTime endTime) {
        super(id); // Kế thừa từ Entity
        this.auctionId = id;
        this.item = item;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentPrice = item.getStartingPrice();
        this.minIncrement = minIncrement;
        this.status = "OPEN";
        this.bidHistory = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Xử lý lượt đặt giá mới.
     * Cần được synchronized để tránh lỗi Concurrency (đấu giá đồng thời).
     */
    public synchronized boolean handleNewBid(BidTransaction bid) {
        // 1. Kiểm tra trạng thái phiên
        if (!status.equals("RUNNING")) {
            System.out.println("Lỗi: Phiên đấu giá không trong trạng thái hoạt động.");
            return false;
        }
        // 2. Kiểm tra thời gian
        if (LocalDateTime.now().isAfter(endTime)) {
            closeAuction();
            return false;
        }
        // 3. Kiểm tra tính hợp lệ của giá
        if (bid.getAmount() <= currentPrice) {
            System.out.println("Lỗi: Giá đặt phải cao hơn giá hiện tại (" + currentPrice + ").");
            return false;
        }
        // 4. Kiểm tra bước nhảy tối thiểu
        // Quy định: Giá mới >= Giá hiện tại + Bước giá tối thiểu
        double requiredPrice = currentPrice + minIncrement;
        if (bid.getAmount() < requiredPrice) {
            System.out.println("Lỗi: Giá đặt " + bid.getAmount() +
                    " chưa đủ. Bạn cần đặt tối thiểu: " + requiredPrice);
            return false;
        }
        // 5. Cập nhật thông tin
        // Nếu hợp lệ, cập nhật giá hiện tại
        this.currentPrice = bid.getAmount();
        this.currentWinner = bid.getBidderId();
        this.bidHistory.add(bid);
        notifyObservers(this, currentPrice, bid.getBidderId());

        System.out.println("Cập nhật thành công! Giá hiện tại: " + currentPrice);

        return true;
    }

    public void startAuction() {
        if (status.equals("OPEN")) {
            this.status = "RUNNING";
        }
    }

    public void closeAuction() {
        this.status = "FINISHED";
        if (currentWinner != null) {
            System.out.println("Phiên kết thúc. Người thắng cuộc: " + currentWinner);
        } else {
            System.out.println("Phiên kết thúc. Không có người mua.");
        }
    }
    @Override
    public void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Auction auction, double newPrice, String lastBidderId) {
        for (AuctionObserver observer : observers) {
            observer.update(this, newPrice, lastBidderId);
        }
    }

    // Getters và Setters
    public String getAuctionId() { return auctionId; }
    public Item getItem() {return item; }
    public double getCurrentPrice() { return currentPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

