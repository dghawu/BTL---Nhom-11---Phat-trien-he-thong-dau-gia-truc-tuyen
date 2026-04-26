package service;

import model.auction.Auction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton chỉ quản lý phiên đấu giá.
 * Việc quản lý user được tách sang UserService.
 */
public class AuctionManager {

    private static volatile AuctionManager instance;
    private final List<Auction> auctions;

    private AuctionManager() {
        auctions = new ArrayList<>();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) instance = new AuctionManager();
            }
        }
        return instance;
    }

    // ── Quản lý phiên ─────────────────────────────────────────────

    public void addAuction(Auction auction) {
        auctions.add(auction);
        System.out.println("[AuctionManager] Thêm phiên: " + auction.getAuctionId());
    }

    public void removeAuction(String auctionId) {
        auctions.removeIf(a -> a.getAuctionId().equals(auctionId));
    }

    public Auction findAuction(String auctionId) {
        return auctions.stream()
                .filter(a -> a.getAuctionId().equals(auctionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy phiên: " + auctionId));
    }

    public List<Auction> getAllAuctions() { return auctions; }

    public List<Auction> getRunningAuctions() {
        return auctions.stream()
                .filter(a -> a.getStatus().equals("RUNNING"))
                .collect(Collectors.toList());
    }

    public List<Auction> getPendingAuctions() {
        return auctions.stream()
                .filter(a -> a.getStatus().equals("PENDING"))
                .collect(Collectors.toList());
    }

    // ── Duyệt / Từ chối (Admin) ───────────────────────────────────

    public void approveAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        if (a.getStatus().equals("PENDING")) {
            a.setStatus("APPROVED");
            System.out.println("[AuctionManager] Duyệt phiên: " + auctionId);
        } else {
            System.out.println("Phiên không ở trạng thái PENDING.");
        }
    }

    public void rejectAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        a.setStatus("REJECTED");
        System.out.println("[AuctionManager] Từ chối phiên: " + auctionId);
    }
}