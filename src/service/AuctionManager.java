package service;

import exception.UserNotFoundException;
import model.auction.Auction;
import model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton quản lý toàn bộ phiên đấu giá và người dùng.
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
                if (instance == null) instance = new AuctionManager();
            }
        }
        return instance;
    }

    // ── Auction ────────────────────────────────────────────────────

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

    public void approveAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        if (a.getStatus().equals("PENDING")) {
            a.setStatus("APPROVED");
            System.out.println("[Admin] Duyệt phiên: " + auctionId);
        } else {
            System.out.println("Phiên không ở trạng thái PENDING.");
        }
    }

    public void rejectAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        a.setStatus("REJECTED");
        System.out.println("[Admin] Từ chối phiên: " + auctionId);
    }

    // ── User ───────────────────────────────────────────────────────

    public void addUser(User user) {
        users.add(user);
        System.out.println("[AuctionManager] Đăng ký: " + user.getName());
    }

    /**
     * Tìm user theo ID.
     * @throws UserNotFoundException nếu không tìm thấy
     */
    public User findUserById(String id) throws UserNotFoundException {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Đăng nhập.
     * @throws UserNotFoundException nếu sai tên/mật khẩu
     */
    public User login(String name, String password) throws UserNotFoundException {
        return users.stream()
                .filter(u -> u.login(name, password))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(name));
    }

    public List<User> getAllUsers() { return users; }

    /**
     * Ban user theo ID.
     * @throws UserNotFoundException nếu không tìm thấy user
     */
    public void banUser(String userId) throws UserNotFoundException {
        findUserById(userId); // throw nếu không tìm thấy
        users.removeIf(u -> u.getId().equals(userId));
        System.out.println("[Admin] Đã ban user: " + userId);
    }
}