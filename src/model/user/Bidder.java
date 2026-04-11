package model.user;

import model.auction.Auction;
import model.auction.BidTransaction;
import observer.AuctionObserver;

import java.util.ArrayList;
import java.util.List;


public class Bidder extends User implements AuctionObserver {
    private List<Auction> watchlist;      // Danh sách sản phẩm đang theo dõi
    private double maxAutoBidAmount;      // Ngưỡng giá tối đa cho Auto-bid
    private boolean isAutoBidEnabled;     // Trạng thái bật/tắt tự động đấu giá


    public Bidder(String id, String name, String password) {
        super(id, name, password, "BIDDER");
        this.watchlist = new ArrayList<>();
        this.isAutoBidEnabled = false;
        this.maxAutoBidAmount = 0.0;
    }


    // --- CHỨC NĂNG CƠ BẢN ---


    /**
     * Thực hiện đặt giá thủ công
     */
    public void placeManualBid(Auction auction, double amount) {
        System.out.println("[BIDDER] " + getName() + " yêu cầu đặt giá: " + amount);


        // Tạo đối tượng model.auction.BidTransaction mới
        BidTransaction newBid = new BidTransaction(this.getName(), amount);


        // Gửi yêu cầu đến model.auction.Auction
        String response = auction.handleNewBid(newBid);
        System.out.println("Hệ thống phản hồi: " + response);


        // Nếu đặt giá thành công, tự động thêm vào danh sách theo dõi
        if (response.contains("THÀNH CÔNG")) {
            if (!watchlist.contains(auction)) {
                watchAuction(auction);
            }
        }
    }


    /**
     * Theo dõi một phiên đấu giá
     */
    public void watchAuction(Auction auction) {
        this.watchlist.add(auction);
        System.out.println("[SYSTEM] " + getName() + " đã bắt đầu theo dõi: " + auction.getAuctionId());
    }


    // --- AUTO BID ---


    public void setupAutoBid(double maxAmount) {
        this.maxAutoBidAmount = maxAmount;
        this.isAutoBidEnabled = true;
        System.out.println("[AUTO-BID] " + getName() + " đã thiết lập giá trần: " + maxAmount);
    }

    public void update(Auction auction, double newPrice, String lastBidderId) {
        if (!lastBidderId.equals(this.getName())) {
            System.out.println("[NOTIFY to " + getName() + "] Giá '"
                    + auction.getItem() + "' đã tăng lên: " + newPrice);


            if (isAutoBidEnabled) {
                processAutoBid(auction, newPrice);
            }
        }
    }

    /**
     * Logic xử lý tự động nâng giá
     */
    private void processAutoBid(Auction auction, double currentPrice) {
        double nextBid = currentPrice + 10.0;


        if (nextBid <= maxAutoBidAmount) {
            System.out.println("[AUTO-BID] Hệ thống tự động nâng giá cho " + getName());
            placeManualBid(auction, nextBid);
        } else {
            System.out.println("[AUTO-BID] " + getName() + ": Giá vượt ngưỡng tối đa");
            this.isAutoBidEnabled = false;
        }
    }


    @Override
    public void printInfo() {
        System.out.println("Bidder: " + getName());
    }


    @Override
    public void showMenu() {
        System.out.println("\n--- MENU NGƯỜI MUA (" + getName() + ") ---");
        System.out.println("1. Xem danh sách sản phẩm đang diễn ra");
        System.out.println("2. Đặt giá thủ công");
        System.out.println("3. Cài đặt đấu giá tự động (Auto-bid)");
        System.out.println("4. Xem danh sách đang theo dõi");
        System.out.println("5. Đăng xuất");
    }

    @Override
    public void update(String message) {
        System.out.println("New update: " + message);
    }
}
