package model.user;

import exception.*;
import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import service.AuctionTimer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import dao.AuctionDAO;

public class Seller extends User {
    private List<Item> myItems;
    private List<Auction> myAuctions;

    public Seller(String id, String name, String password) {
        super(id, name, password, "SELLER");
        this.myItems = new ArrayList<>();
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

    /**
     * Tạo phiên đấu giá mới.
     *
     * @throws ItemNotApprovedException  nếu sản phẩm chưa được duyệt
     * @throws InvalidItemPriceException nếu giá không hợp lệ
     */
    public Auction createAuction(String auctionId, Item item, double minIncrement,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        if (item.getStatus() != Item.ItemStatus.APPROVED) {
            throw new ItemNotApprovedException(item.getId(), item.getStatus().name());
        }
        // Auction constructor tự kiểm tra giá > 0
        Auction auction = new Auction(auctionId, item, item.getStartPrice(),
                minIncrement, startTime, endTime);
        myAuctions.add(auction);
        System.out.println("Seller " + getName() + " tạo phiên: " + auctionId);
        return auction;
    }

    /**
     * Sửa thông tin phiên — chỉ được khi PENDING.
     *
     * @throws AuctionNotFoundException       nếu không tìm thấy
     * @throws AuctionEditNotAllowedException nếu phiên không ở trạng thái PENDING
     */
    public void editAuction(String auctionId, String newName, double newPrice) {
        Auction a = findMyAuction(auctionId);
        if (a.getStatus() != AuctionStatus.PENDING) {
            throw new AuctionEditNotAllowedException(auctionId, a.getStatus());
        }
        a.setName(newName);
        a.setStartPrice(newPrice);
        System.out.println("Cập nhật thành công!");
    }

    /**
     * Hủy phiên — chỉ được khi PENDING hoặc APPROVED.
     *
     * @throws AuctionNotFoundException         nếu không tìm thấy
     * @throws AuctionCancelNotAllowedException nếu phiên đang RUNNING hoặc FINISHED
     */
    public void cancelAuction(String auctionId) {

        for (Auction a : myAuctions) {

            if (a.getAuctionId().equals(auctionId)) {

                // Không cho hủy nếu đã kết thúc hoặc đã thanh toán
                if (a.getStatus() == AuctionStatus.FINISHED
                        || a.getStatus() == AuctionStatus.PAID) {
                    throw new AuctionCancelNotAllowedException(auctionId, a.getStatus());
                }

                // Đổi trạng thái
                a.setStatus(AuctionStatus.CANCELED);

                // Update database
                new AuctionDAO().updateStatus(
                        auctionId,
                        AuctionStatus.CANCELED
                );

                // Dừng timer
                AuctionTimer.getInstance().cancelTask(auctionId);

                System.out.println("Đã hủy phiên: " + auctionId);

                return;
            }
        }

        throw new AuctionNotFoundException(auctionId);
    }

    public void viewMyAuctions() {
        System.out.println("\n===== PHIÊN CỦA TÔI =====");
        for (Auction a : myAuctions) {
            System.out.println("ID: " + a.getAuctionId()
                    + " | Item: " + a.getItem().getName()
                    + " | Giá: " + a.getCurrentPrice()
                    + " | Trạng thái: " + a.getStatus());
        }
    }

    public void viewAuctionDetail(String auctionId) {
        findMyAuction(auctionId).printInfo();
    }

    /**
     * @throws AuctionNotFoundException nếu không tìm thấy trong danh sách của Seller này
     */
    private Auction findMyAuction(String auctionId) {
        return myAuctions.stream()
                .filter(a -> a.getAuctionId().equals(auctionId))
                .findFirst()
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
    }
}
