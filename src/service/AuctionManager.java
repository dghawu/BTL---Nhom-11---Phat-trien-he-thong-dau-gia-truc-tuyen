package service;

import model.auction.Auction;
import model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Singleton quản lý toàn bộ phiên đấu giá và người dùng trong hệ thống.
 * Dùng double-checked locking để thread-safe.
 */
public class AuctionManager {

    private static volatile AuctionManager instance;

    private final List<Auction> auctions;
    private final List<User>    users;

    private AuctionManager() {
        auctions = new ArrayList<>();
        users    = new ArrayList<>();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // ── Auction management ─────────────────────────────────────────

    public void addAuction(Auction auction) {
        auctions.add(auction);
        System.out.println("[AuctionManager] Thêm phiên: " + auction.getAuctionId());
    }

    public void removeAuction(String auctionId) {
        auctions.removeIf(a -> a.getAuctionId().equals(auctionId));
    }

    public Optional<Auction> findAuction(String auctionId) {
        return auctions.stream()
                .filter(a -> a.getAuctionId().equals(auctionId))
                .findFirst();
    }

    public List<Auction> getAllAuctions() { return auctions; }

    public List<Auction> getRunningAuctions() {
        return auctions.stream()
                .filter(a -> a.getStatus().equals("RUNNING"))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Admin duyệt phiên đấu giá PENDING → APPROVED */
    public void approveAuction(String auctionId) {
        findAuction(auctionId).ifPresentOrElse(a -> {
            if (a.getStatus().equals("PENDING")) {
                a.setStatus("APPROVED");
                System.out.println("[Admin] Duyệt phiên: " + auctionId);
            } else {
                System.out.println("Phiên không ở trạng thái PENDING.");
            }
        }, () -> System.out.println("Không tìm thấy phiên: " + auctionId));
    }

    /** Admin từ chối phiên đấu giá */
    public void rejectAuction(String auctionId) {
        findAuction(auctionId).ifPresentOrElse(a -> {
            a.setStatus("REJECTED");
            System.out.println("[Admin] Từ chối phiên: " + auctionId);
        }, () -> System.out.println("Không tìm thấy phiên: " + auctionId));
    }

    // ── User management ────────────────────────────────────────────

    public void addUser(User user) {
        users.add(user);
        System.out.println("[AuctionManager] Đăng ký user: " + user.getName());
    }

    public Optional<User> findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public Optional<User> login(String name, String password) {
        return users.stream()
                .filter(u -> u.login(name, password))
                .findFirst();
    }

    public List<User> getAllUsers() { return users; }

    /** Xóa user (ban) */
    public void banUser(String userId) {
        users.removeIf(u -> u.getId().equals(userId));
        System.out.println("[Admin] Đã ban user: " + userId);
    }
}