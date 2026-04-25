package service;

import dao.AuctionDAO;
import model.auction.Auction;
import model.item.Item;
import observer.BidHistoryLogger;
import observer.SocketBroadcaster;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * AuctionService xử lý nghiệp vụ đấu giá.
 * Tự động đăng ký BidHistoryLogger và SocketBroadcaster vào mỗi phiên.
 */
public class AuctionService {

    private static volatile AuctionService instance;
    private final AuctionDAO auctionDAO;

    // Map auctionId → SocketBroadcaster để client có thể subscribe
    private final Map<String, SocketBroadcaster> broadcasters = new ConcurrentHashMap<>();

    private AuctionService() {
        this.auctionDAO = new AuctionDAO();
    }

    public static AuctionService getInstance() {
        if (instance == null) {
            synchronized (AuctionService.class) {
                if (instance == null) instance = new AuctionService();
            }
        }
        return instance;
    }

    /**
     * Tạo phiên đấu giá mới và lưu vào database.
     */
    public Auction createAuction(String auctionId, Item item, double minIncrement,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        Auction auction = new Auction(auctionId, item, item.getStartPrice(),
                minIncrement, startTime, endTime);
        auctionDAO.save(auction);
        System.out.println("[AuctionService] Tạo phiên: " + auctionId);
        return auction;
    }

    /**
     * Bắt đầu phiên — đăng ký observers tự động.
     *
     * Luồng:
     * auction.handleNewBid()
     *   └── notifyObservers()
     *         ├── BidHistoryLogger → lưu vào database
     *         └── SocketBroadcaster → gửi đến tất cả client
     */
    public void startAuction(Auction auction) {
        // 1. Đăng ký BidHistoryLogger — tự động lưu mỗi bid vào database
        auction.addObserver(new BidHistoryLogger());

        // 2. Tạo và đăng ký SocketBroadcaster — broadcast realtime đến client
        SocketBroadcaster broadcaster = new SocketBroadcaster(auction.getAuctionId());
        auction.addObserver(broadcaster);
        broadcasters.put(auction.getAuctionId(), broadcaster);

        // 3. Bắt đầu phiên (đổi status → RUNNING, lên lịch AuctionTimer)
        auction.startAuction();

        // 4. Cập nhật database
        auctionDAO.updateStatus(auction.getAuctionId(), "RUNNING");

        System.out.println("[AuctionService] Phiên " + auction.getAuctionId()
                + " đã bắt đầu với " + "BidHistoryLogger + SocketBroadcaster.");
    }

    /**
     * Đóng phiên và broadcast kết quả đến tất cả client.
     */
    public void closeAuction(Auction auction) {
        auction.closeAuction();
        auctionDAO.updateStatus(auction.getAuctionId(), "FINISHED");

        // Broadcast kết quả đến tất cả client đang xem
        SocketBroadcaster broadcaster = broadcasters.get(auction.getAuctionId());
        if (broadcaster != null) {
            broadcaster.broadcastClose(
                    auction.getAuctionId(),
                    auction.getCurrentWinner(),
                    auction.getCurrentPrice()
            );
            broadcasters.remove(auction.getAuctionId());
        }
    }

    /**
     * Lấy SocketBroadcaster của một phiên.
     * Server gọi khi client gửi lệnh "WATCH:AUC-001".
     */
    public SocketBroadcaster getBroadcaster(String auctionId) {
        return broadcasters.get(auctionId);
    }
}