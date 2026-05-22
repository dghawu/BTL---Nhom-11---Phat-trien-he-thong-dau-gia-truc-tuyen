package dao;

import model.auction.BidTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BidTransactionDAO xử lý CRUD với bảng bid_transactions.
 */
public class BidTransactionDAO {

    private final Connection conn;

    public BidTransactionDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── INSERT ─────────────────────────────────────────────────────

    public void save(BidTransaction bid) {
        String sql = """
                INSERT INTO bid_transactions (id, bidder_id, auction_id, amount, timestamp)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bid.getId());
            ps.setString(2, bid.getBidderId());
            ps.setString(3, bid.getAuctionId());
            ps.setDouble(4, bid.getAmount());
            ps.setString(5, bid.getTimestamp().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[BidTransactionDAO] Lỗi save: " + e.getMessage());
        }
    }

    // ── SELECT ─────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ lịch sử bid của một phiên — dùng cho biểu đồ giá
     */
    public List<BidTransaction> findByAuctionId(String auctionId) {
        List<BidTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY timestamp ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToBid(rs));
        } catch (SQLException e) {
            System.out.println("[BidTransactionDAO] Lỗi findByAuctionId: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy toàn bộ lịch sử bid của một bidder
     */
    public List<BidTransaction> findByBidderId(String bidderId) {
        List<BidTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM bid_transactions WHERE bidder_id = ? ORDER BY timestamp DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bidderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToBid(rs));
        } catch (SQLException e) {
            System.out.println("[BidTransactionDAO] Lỗi findByBidderId: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy bid cao nhất của một phiên
     */
    public BidTransaction findHighestBid(String auctionId) {
        String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY amount DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToBid(rs);
        } catch (SQLException e) {
            System.out.println("[BidTransactionDAO] Lỗi findHighestBid: " + e.getMessage());
        }
        return null;
    }

    // ── Helper ─────────────────────────────────────────────────────

    private BidTransaction mapToBid(ResultSet rs) throws SQLException {
        BidTransaction tx = new BidTransaction(
                rs.getString("bidder_id"),
                rs.getString("auction_id"),
                rs.getDouble("amount")
        );
        String ts = rs.getString("timestamp");
        if (ts != null) {
            tx.setTimestamp(java.time.LocalDateTime.parse(
                    ts.replace(" ", "T"),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        }
        return tx;
    }
}