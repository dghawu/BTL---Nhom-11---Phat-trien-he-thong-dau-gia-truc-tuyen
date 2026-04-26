package observer;

import dao.BidTransactionDAO;
import model.auction.Auction;
import model.auction.BidTransaction;

/**
 * BidHistoryLogger lắng nghe bid mới và tự động lưu vào dao.
 * Là một Observer — được đăng ký vào Auction khi phiên bắt đầu.
 */
public class BidHistoryLogger implements AuctionObserver {

    private final BidTransactionDAO bidTransactionDAO;

    public BidHistoryLogger() {
        this.bidTransactionDAO = new BidTransactionDAO();
    }

    @Override
    public void update(Auction auction, double newPrice, String lastBidderId) {
        // Lấy bid mới nhất từ lịch sử (vừa được thêm vào trong handleNewBid)
        if (auction.getBidHistory().isEmpty()) return;

        BidTransaction latestBid = auction.getBidHistory()
                .get(auction.getBidHistory().size() - 1);

        // Lưu vào database
        bidTransactionDAO.save(latestBid);

        System.out.println("[BidHistoryLogger] Đã lưu bid: "
                + lastBidderId + " → " + newPrice);
    }
}