import model.auction.Auction;
import model.item.Item;
import model.user.Admin;
import model.user.Bidder;
import model.user.Seller;
import service.AuctionManager;
import service.AuctionTimer;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        AuctionManager manager = AuctionManager.getInstance();

        // 1. Tạo users
        Admin  admin   = new Admin("A01", "AdminRoot", "admin123");
        Seller seller  = new Seller("S01", "Nguyen Van A", "pass");
        Bidder bidder1 = new Bidder("B01", "Tran Thi B", "pass");
        Bidder bidder2 = new Bidder("B02", "Le Van C",   "pass");

        manager.addUser(admin);
        manager.addUser(seller);
        manager.addUser(bidder1);
        manager.addUser(bidder2);

        // 2. Seller tạo Item & phiên đấu giá
        Item laptop = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Dell XPS", "ITEM-001",
                "Core i7, 16GB RAM", 15_000_000.0, Item.ItemStatus.APPROVED);

        Auction auction = seller.createAuction(
                "AUC-001", laptop, 500_000.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(30) // kết thúc sau 30 giây để test
        );
        manager.addAuction(auction);

        // 3. Admin duyệt → Seller bắt đầu phiên
        manager.approveAuction("AUC-001");
        auction.startAuction(); // AuctionTimer tự lên lịch đóng sau 30 giây

        // 4. Bidders theo dõi & đặt giá
        bidder1.watchAuction(auction);
        bidder2.watchAuction(auction);

        bidder2.setupAutoBid(16_500_000.0);

        bidder1.placeManualBid(auction, 15_500_000.0); // hợp lệ
        bidder1.placeManualBid(auction, 14_000_000.0); // lỗi: thấp hơn hiện tại
        bidder2.placeManualBid(auction, 16_000_000.0); // hợp lệ → trigger auto-bid bidder1 nếu có

        // 5. Tắt scheduler khi thoát (quan trọng — không có dòng này app không thoát được)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AuctionTimer.getInstance().shutdown();
        }));
    }
}