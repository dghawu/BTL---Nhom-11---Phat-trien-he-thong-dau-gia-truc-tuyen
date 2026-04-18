import exception.UserNotFoundException;
import model.auction.Auction;
import model.item.Item;
import model.user.Bidder;
import model.user.Seller;
import model.user.User;
import service.AuctionManager;
import service.AuctionTimer;
import service.UserService;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        UserService  userService = UserService.getInstance();
        AuctionManager auctionMgr  = AuctionManager.getInstance();

        // 1. Đăng ký users qua UserService
        userService.registerAdmin("A01", "AdminRoot", "admin123");
        Seller seller  = userService.registerSeller("S01", "Nguyen Van A", "pass");
        Bidder bidder1 = userService.registerBidder("B01", "Tran Thi B", "pass");
        Bidder bidder2 = userService.registerBidder("B02", "Le Van C", "pass");

        // 2. Test đăng nhập
        try {
            User loggedIn = userService.login("Tran Thi B", "pass");
            System.out.println("Đăng nhập thành công: " + loggedIn.getName());

            // Test đổi mật khẩu
            userService.changePassword("B01", "pass", "newpass123");
        } catch (UserNotFoundException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }

        // 3. Test đăng nhập sai
        try {
            userService.login("Tran Thi B", "wrongpass");
        } catch (UserNotFoundException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }

        // 4. Test đăng ký trùng tên
        try {
            userService.registerBidder("B99", "Tran Thi B", "pass");
        } catch (IllegalArgumentException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }

        // 5. Seller tạo Item & phiên đấu giá
        Item laptop = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Dell XPS", "ITEM-001",
                "Core i7, 16GB RAM", 15_000_000.0, Item.ItemStatus.APPROVED);

        Auction auction = seller.createAuction(
                "AUC-001", laptop, 500_000.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(30)
        );
        auctionMgr.addAuction(auction);

        // 6. Admin duyệt → bắt đầu phiên
        auctionMgr.approveAuction("AUC-001");
        auction.startAuction();

        // 7. Bidders đặt giá
        bidder1.watchAuction(auction);
        bidder2.watchAuction(auction);
        bidder2.setupAutoBid(16_500_000.0);

        bidder1.placeManualBid(auction, 15_500_000.0); // hợp lệ
        bidder1.placeManualBid(auction, 14_000_000.0); // lỗi: thấp hơn
        bidder2.placeManualBid(auction, 16_000_000.0); // hợp lệ

        // 8. Test ban user
        try {
            userService.banUser("B01");
            userService.banUser("UNKNOWN"); // lỗi
        } catch (UserNotFoundException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }

        // 9. Shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                AuctionTimer.getInstance().shutdown()));
    }
}