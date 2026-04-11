package model.user;

import model.auction.Auction;
import model.item.Item;
import service.AuctionTimer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private List<Item> myItems;
    private List<Auction> myAuctions;

    public Seller(String id, String name, String password) {
        super(id, name, password, "SELLER");
        this.myItems    = new ArrayList<>();
        this.myAuctions = new ArrayList<>();
    }

    @Override
    public void showMenu() {
        System.out.println("\n===== SELLER MENU =====");
        System.out.println("1. Tạo phiên đấu giá");
        System.out.println("2. Sửa phiên đấu giá (trước khi duyệt)");
        System.out.println("3. Xem phiên đấu giá của tôi");
        System.out.println("4. Hủy phiên đấu giá");
        System.out.println("0. Thoát");
    }

    @Override
    public void printInfo() {
        System.out.println("ID      : " + getId());
        System.out.println("Tên     : " + name);
        System.out.println("Role    : " + role);
        System.out.println("Ngày tạo: " + getCreatedAt());
    }

    public Auction createAuction(String auctionId, Item item, double minIncrement,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        Auction auction = new Auction(auctionId, item, item.getStartingPrice(),
                minIncrement, startTime, endTime);
        myAuctions.add(auction);
        System.out.println("Seller " + getName() + " tạo phiên: " + auctionId);
        return auction;
    }

    public void editAuction(String auctionId, String newName, double newPrice) {
        for (Auction a : myAuctions) {
            if (a.getAuctionId().equals(auctionId)) {
                if (a.getStatus().equals("RUNNING")) {
                    System.out.println("Không thể sửa: phiên đang chạy!");
                    return;
                }
                a.setName(newName);
                a.setStartPrice(newPrice);
                System.out.println("Cập nhật thành công!");
                return;
            }
        }
        System.out.println("Không tìm thấy phiên: " + auctionId);
    }

    public void cancelAuction(String auctionId) {
        for (Auction a : myAuctions) {
            if (a.getAuctionId().equals(auctionId)) {
                if (a.getStatus().equals("RUNNING") || a.getStatus().equals("FINISHED")) {
                    System.out.println("Không thể hủy phiên đang chạy/đã kết thúc!");
                    return;
                }
                a.setStatus("CANCELED");
                // Hủy task timer nếu đang chạy
                AuctionTimer.getInstance().cancelTask(auctionId);
                System.out.println("Đã hủy phiên: " + auctionId);
                return;
            }
        }
        System.out.println("Không tìm thấy phiên: " + auctionId);
    }

    public void viewMyAuctions() {
        System.out.println("\n===== PHIÊN CỦA TÔI =====");
        for (Auction a : myAuctions) {
            System.out.println("ID: " + a.getAuctionId()
                    + " | Item: " + a.getItem().getName()
                    + " | Giá: "  + a.getCurrentPrice()
                    + " | Trạng thái: " + a.getStatus());
        }
    }

    public void viewAuctionDetail(String auctionId) {
        for (Auction a : myAuctions) {
            if (a.getAuctionId().equals(auctionId)) {
                a.printInfo();
                return;
            }
        }
        System.out.println("Không tìm thấy phiên: " + auctionId);
    }
}