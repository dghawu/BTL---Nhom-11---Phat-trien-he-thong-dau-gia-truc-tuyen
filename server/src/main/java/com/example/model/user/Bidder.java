package com.example.model.user;

import com.example.exception.AuctionClosedException;
import com.example.exception.InvalidBidException;
import com.example.model.auction.Auction;
import com.example.model.auction.BidTransaction;
import com.example.observer.AuctionObserver;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User implements AuctionObserver {
    private List<Auction> watchlist;
    private double maxAutoBidAmount;
    private boolean isAutoBidEnabled;

    public Bidder(String id, String name, String password) {
        super(id, name, password, "BIDDER");
        this.watchlist = new ArrayList<>();
        this.isAutoBidEnabled = false;
        this.maxAutoBidAmount = 0.0;
    }

    /**
     * Đặt giá thủ công — bắt exception và thông báo lý do thất bại
     */
    public void placeManualBid(Auction auction, double amount) {
        System.out.println("[BIDDER] " + getName() + " yêu cầu đặt giá: " + amount);
        try {
            BidTransaction newBid = new BidTransaction(this.getId(), auction.getAuctionId(), amount);
            auction.handleNewBid(newBid);

            // Tự động theo dõi phiên khi đặt giá thành công
            if (!watchlist.contains(auction)) {
                watchAuction(auction);
            }
        } catch (AuctionClosedException e) {
            System.out.println("[LỖI] " + e.getMessage());
        } catch (InvalidBidException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }
    }

    public void watchAuction(Auction auction) {
        watchlist.add(auction);
        auction.addObserver(this);
        System.out.println("[SYSTEM] " + getName() + " theo dõi: " + auction.getAuctionId());
    }

    public void setupAutoBid(double maxAmount) {
        this.maxAutoBidAmount = maxAmount;
        this.isAutoBidEnabled = true;
        System.out.println("[AUTO-BID] " + getName() + " đặt giá trần: " + maxAmount);
    }

    @Override
    public void update(Auction auction, double newPrice, String lastBidderId) {
        if (!lastBidderId.equals(this.getId())) {
            System.out.println("[NOTIFY → " + getName() + "] Giá '"
                    + auction.getItem().getName() + "' tăng lên: " + newPrice);

            if (isAutoBidEnabled) {
                processAutoBid(auction, newPrice);
            }
        }
    }

    private void processAutoBid(Auction auction, double currentPrice) {
        double nextBid = currentPrice + 10.0;
        if (nextBid <= maxAutoBidAmount) {
            System.out.println("[AUTO-BID] Tự động nâng giá cho " + getName() + ": " + nextBid);
            placeManualBid(auction, nextBid); // exception đã được bắt bên trong
        } else {
            System.out.println("[AUTO-BID] " + getName() + " vượt giá trần, tắt auto-bid.");
            this.isAutoBidEnabled = false;
        }
    }

    @Override
    public void printInfo() {
        System.out.println("Bidder: " + getName() + " | ID: " + getId());
    }

    @Override
    public void showMenu() {
        System.out.println("\n--- MENU NGƯỜI MUA (" + getName() + ") ---");
        System.out.println("1. Xem danh sách phiên đang diễn ra");
        System.out.println("2. Đặt giá thủ công");
        System.out.println("3. Cài đặt auto-bid");
        System.out.println("4. Xem watchlist");
        System.out.println("0. Đăng xuất");
    }
}