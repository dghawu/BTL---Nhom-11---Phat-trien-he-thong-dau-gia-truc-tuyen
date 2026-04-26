package service;

import exception.UserNotFoundException;
import model.user.Admin;
import model.user.Bidder;
import model.user.Seller;
import model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService xử lý toàn bộ nghiệp vụ liên quan đến người dùng:
 * đăng ký, đăng nhập, tìm kiếm, ban user.
 * Tách biệt khỏi AuctionManager để đúng Single Responsibility.
 */
public class UserService {

    private static volatile UserService instance;
    private final List<User> users;

    private UserService() {
        users = new ArrayList<>();
    }

    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) instance = new UserService();
            }
        }
        return instance;
    }

    // ── Đăng ký ───────────────────────────────────────────────────

    /**
     * Đăng ký Bidder mới.
     * @throws IllegalArgumentException nếu tên đã tồn tại
     */
    public Bidder registerBidder(String id, String name, String password) {
        checkDuplicateName(name);
        Bidder bidder = new Bidder(id, name, password);
        users.add(bidder);
        System.out.println("[UserService] Đăng ký Bidder: " + name);
        return bidder;
    }

    /**
     * Đăng ký Seller mới.
     * @throws IllegalArgumentException nếu tên đã tồn tại
     */
    public Seller registerSeller(String id, String name, String password) {
        checkDuplicateName(name);
        Seller seller = new Seller(id, name, password);
        users.add(seller);
        System.out.println("[UserService] Đăng ký Seller: " + name);
        return seller;
    }

    /**
     * Thêm Admin (chỉ dùng khi khởi tạo hệ thống).
     */
    public Admin registerAdmin(String id, String name, String password) {
        checkDuplicateName(name);
        Admin admin = new Admin(id, name, password);
        users.add(admin);
        System.out.println("[UserService] Đăng ký Admin: " + name);
        return admin;
    }

    // ── Đăng nhập ─────────────────────────────────────────────────

    /**
     * Đăng nhập bằng tên và mật khẩu.
     * @throws UserNotFoundException nếu sai tên hoặc mật khẩu
     */
    public User login(String name, String password) throws UserNotFoundException {
        return users.stream()
                .filter(u -> u.login(name, password))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(name));
    }

    // ── Tìm kiếm ──────────────────────────────────────────────────

    /**
     * Tìm user theo ID.
     * @throws UserNotFoundException nếu không tìm thấy
     */
    public User findById(String id) throws UserNotFoundException {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Tìm user theo tên.
     * @throws UserNotFoundException nếu không tìm thấy
     */
    public User findByName(String name) throws UserNotFoundException {
        return users.stream()
                .filter(u -> u.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(name));
    }

    public List<User> getAllUsers()    { return users; }

    public List<Bidder> getAllBidders() {
        return users.stream()
                .filter(u -> u instanceof Bidder)
                .map(u -> (Bidder) u)
                .collect(Collectors.toList());
    }

    public List<Seller> getAllSellers() {
        return users.stream()
                .filter(u -> u instanceof Seller)
                .map(u -> (Seller) u)
                .collect(Collectors.toList());
    }

    // ── Quản lý ───────────────────────────────────────────────────

    /**
     * Ban (xóa) user khỏi hệ thống.
     * @throws UserNotFoundException nếu không tìm thấy
     */
    public void banUser(String userId) throws UserNotFoundException {
        findById(userId); // throw nếu không tồn tại
        users.removeIf(u -> u.getId().equals(userId));
        System.out.println("[UserService] Đã ban user: " + userId);
    }

    /**
     * Đổi mật khẩu.
     * @throws UserNotFoundException nếu không tìm thấy user
     */
    public void changePassword(String userId, String oldPassword, String newPassword)
            throws UserNotFoundException {
        User user = findById(userId);
        if (!user.login(user.getName(), oldPassword)) {
            System.out.println("[LỖI] Mật khẩu cũ không đúng!");
            return;
        }
        user.setPassword(newPassword);
        System.out.println("[UserService] Đổi mật khẩu thành công cho: " + user.getName());
    }

    // ── Private helpers ───────────────────────────────────────────

    private void checkDuplicateName(String name) {
        boolean exists = users.stream().anyMatch(u -> u.getName().equals(name));
        if (exists) {
            throw new IllegalArgumentException("Tên '" + name + "' đã được sử dụng!");
        }
    }
}