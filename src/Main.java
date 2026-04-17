import exception.UserNotFoundException;
import model.auction.Auction;
import model.item.Item;
import model.user.Admin;
import model.user.Bidder;
import model.user.Seller;
import model.user.User;
import service.AuctionManager;
import service.AuctionTimer;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        AuctionManager manager = AuctionManager.getInstance();

        // 1. Tạo và đăng ký users
        Admin  admin   = new Admin("A01", "AdminRoot", "admin123");
        Seller seller  = new Seller("S01", "Nguyen Van A", "pass");
        Bidder bidder1 = new Bidder("B01", "Tran Thi B", "pass");
        Bidder bidder2 = new Bidder("B02", "Le Van C", "pass");

        manager.addUser(admin);
        manager.addUser(seller);
        manager.addUser(bidder1);
        manager.addUser(bidder2);

        // 2. Test login hợp lệ
        try {
            User loggedIn = manager.login("Tran Thi B", "pass");
            System.out.println("Đăng nhập thành công: " + loggedIn.getName());
        } catch (UserNotFoundException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }

        // 3. Test login sai mật khẩu
        try {
            manager.login("Tran Thi B", "wrongpass");
        } catch (UserNotFoundException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }

        // 4. Seller tạo Item & phiên đấu giá
        Item laptop = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Dell XPS", "ITEM-001",
                "Core i7, 16GB RAM", 15_000_000.0, Item.ItemStatus.APPROVED);

        Auction auction = seller.createAuction(
                "AUC-001", laptop, 500_000.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(30)
        );
        manager.addAuction(auction);

        // 5. Admin duyệt → bắt đầu phiên
        manager.approveAuction("AUC-001");
        auction.startAuction();

        // 6. Bidders theo dõi & đặt giá
        bidder1.watchAuction(auction);
        bidder2.watchAuction(auction);
        bidder2.setupAutoBid(16_500_000.0);

        bidder1.placeManualBid(auction, 15_500_000.0); // hợp lệ
        bidder1.placeManualBid(auction, 14_000_000.0); // lỗi: thấp hơn hiện tại
        bidder2.placeManualBid(auction, 16_000_000.0); // hợp lệ → trigger auto-bid

        // 7. Test ban user
        try {
            manager.banUser("B01");
            manager.banUser("UNKNOWN"); // lỗi: không tồn tại
        } catch (UserNotFoundException e) {
            System.out.println("[LỖI] " + e.getMessage());
        }

        // 8. Shutdown khi thoát
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                AuctionTimer.getInstance().shutdown()));
    }
}