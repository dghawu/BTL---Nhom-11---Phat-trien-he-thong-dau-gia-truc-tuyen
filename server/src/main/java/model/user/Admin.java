package model.user;

import model.auction.Auction;
import model.auction.BidTransaction;

import java.util.List;
public class Admin extends User {
    public Admin(String id, String name, String password) {
        super(id, name, password, "ADMIN");
    }
    // Hiển thị menu
    @Override
    public void showMenu() {
        System.out.println("\n===== ADMIN MENU =====");
        System.out.println("1. Xem danh sách user");
        System.out.println("2. Ban user");
        System.out.println("3. Kiểm duyệt sản phẩm");
        System.out.println("4. Xóa phiên đấu giá");
        System.out.println("5. Xem tất cả giao dịch");
        System.out.println("0. Thoát");
    }
    // Override từ Entity
    @Override
    public void printInfo() {
        System.out.println("ID: " + getId());
        System.out.println("Tên: " + name);
        System.out.println("Role: " + role);
        System.out.println("Ngày tạo: " + getCreatedAt());
    }

    //1. Quản lý người dùng
    public void banUser(List<User> users, String username) {
        for (User u : users) {
            if (u.getName().equals(username)) {
                users.remove(u);
                System.out.println("Đã ban user: " + username);
                return;
            }
        }
        System.out.println("Không tìm thấy user!");
    }

    //2. Quản lý sản phẩm
    public void moderateItem(List<Auction> items, int index) {
        if (index >= 0 && index < items.size()) {
            Auction item = items.get(index);
            items.remove(index);
            System.out.println("Đã xóa sản phẩm vi phạm: " + item);
        } else {
            System.out.println("Không tìm thấy sản phẩm!");
        }
    }

    //3. Quản lý phiên đấu giá
    public void removeInvalidAuction(List<Auction> auctions, int index) {
        if (index >= 0 && index < auctions.size()) {
            Auction item = auctions.remove(index);
            System.out.println("Đã xóa phiên đấu giá: " + item);
        } else {
            System.out.println("Không tìm thấy phiên đấu giá!");
        }
    }

    // 4. Xem tất cả giao dịch
    public void reviewAllTransactions(List<BidTransaction> transactions) {
        System.out.println("\n===== DANH SÁCH GIAO DỊCH =====");
        for (BidTransaction t : transactions) {
            t.printInfo();
        }
    }
}
