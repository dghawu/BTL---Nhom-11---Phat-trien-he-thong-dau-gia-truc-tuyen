package service;

import exception.DuplicateUsernameException;
import exception.UserBannedException;
import exception.UserNotFoundException;
import exception.WrongPasswordException;
import model.user.Admin;
import model.user.Bidder;
import model.user.Seller;
import model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * @throws DuplicateUsernameException nếu tên đã tồn tại
     */
    public Bidder registerBidder(String id, String name, String password) {
        checkDuplicateName(name);
        Bidder bidder = new Bidder(id, name, password);
        users.add(bidder);
        System.out.println("[UserService] Đăng ký Bidder: " + name);
        return bidder;
    }

    /**
     * @throws DuplicateUsernameException nếu tên đã tồn tại
     */
    public Seller registerSeller(String id, String name, String password) {
        checkDuplicateName(name);
        Seller seller = new Seller(id, name, password);
        users.add(seller);
        System.out.println("[UserService] Đăng ký Seller: " + name);
        return seller;
    }

    public Admin registerAdmin(String id, String name, String password) {
        checkDuplicateName(name);
        Admin admin = new Admin(id, name, password);
        users.add(admin);
        System.out.println("[UserService] Đăng ký Admin: " + name);
        return admin;
    }

    // ── Đăng nhập ─────────────────────────────────────────────────

    /**
     * @throws UserNotFoundException  nếu tên không tồn tại
     * @throws WrongPasswordException nếu mật khẩu sai
     * @throws UserBannedException    nếu tài khoản bị khóa
     */
    public User login(String name, String password) {
        User user = users.stream()
                .filter(u -> u.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(name));

        if (user.isBanned()) throw new UserBannedException(user.getId());
        if (!user.login(name, password)) throw new WrongPasswordException();
        return user;
    }

    // ── Tìm kiếm ──────────────────────────────────────────────────

    /**
     * @throws UserNotFoundException nếu không tìm thấy
     */
    public User findById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * @throws UserNotFoundException nếu không tìm thấy
     */
    public User findByName(String name) {
        return users.stream()
                .filter(u -> u.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(name));
    }

    public List<User> getAllUsers() {
        return users;
    }

    public List<Bidder> getAllBidders() {
        return users.stream().filter(u -> u instanceof Bidder)
                .map(u -> (Bidder) u).collect(Collectors.toList());
    }

    public List<Seller> getAllSellers() {
        return users.stream().filter(u -> u instanceof Seller)
                .map(u -> (Seller) u).collect(Collectors.toList());
    }

    // ── Quản lý ───────────────────────────────────────────────────

    /**
     * @throws UserNotFoundException nếu không tìm thấy
     */
    public void banUser(String userId) {
        findById(userId);
        users.removeIf(u -> u.getId().equals(userId));
        System.out.println("[UserService] Đã ban user: " + userId);
    }

    /**
     * @throws UserNotFoundException  nếu không tìm thấy
     * @throws WrongPasswordException nếu mật khẩu cũ sai
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (!user.login(user.getName(), oldPassword)) {
            throw new WrongPasswordException();
        }
        user.setPassword(newPassword);
        System.out.println("[UserService] Đổi mật khẩu thành công cho: " + user.getName());
    }

    // ── Private helpers ───────────────────────────────────────────

    private void checkDuplicateName(String name) {
        boolean exists = users.stream().anyMatch(u -> u.getName().equals(name));
        if (exists) throw new DuplicateUsernameException(name);
    }
}
