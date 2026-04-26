package dao;

import model.auction.Auction;
import model.item.Item;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AuctionDAO xử lý toàn bộ thao tác CRUD với bảng auctions.
 */
public class AuctionDAO {

    private final Connection conn;
    private final ItemDAO itemDAO;

    public AuctionDAO() {
        this.conn    = DatabaseConnection.getInstance().getConnection();
        this.itemDAO = new ItemDAO();
    }

    // ── INSERT ─────────────────────────────────────────────────────

    public void save(Auction auction) {
        // Lưu item trước
        itemDAO.save(auction.getItem());

        String sql = """
                INSERT OR IGNORE INTO auctions
                (id, item_id, start_price, current_price, min_increment,
                 current_winner, start_time, end_time, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auction.getAuctionId());
            ps.setString(2, auction.getItem().getId());
            ps.setDouble(3, auction.getStartPrice());
            ps.setDouble(4, auction.getCurrentPrice());
            ps.setDouble(5, auction.getMinIncrement());
            ps.setString(6, auction.getCurrentWinner());
            ps.setString(7, auction.getStartTime().toString());
            ps.setString(8, auction.getEndTime().toString());
            ps.setString(9, auction.getStatus());
            ps.setString(10, auction.getCreatedAt().toString());
            ps.executeUpdate();
            System.out.println("[AuctionDAO] Lưu phiên: " + auction.getAuctionId());
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi save: " + e.getMessage());
        }
    }

    // ── SELECT ─────────────────────────────────────────────────────

    public Auction findById(String auctionId) {
        String sql = "SELECT * FROM auctions WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToAuction(rs);
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi findById: " + e.getMessage());
        }
        return null;
    }

    public List<Auction> findAll() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToAuction(rs));
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi findAll: " + e.getMessage());
        }
        return list;
    }

    public List<Auction> findByStatus(String status) {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE status = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToAuction(rs));
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi findByStatus: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ─────────────────────────────────────────────────────

    public void updateStatus(String auctionId, String newStatus) {
        String sql = "UPDATE auctions SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, auctionId);
            ps.executeUpdate();
            System.out.println("[AuctionDAO] Cập nhật trạng thái: " + auctionId + " → " + newStatus);
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi updateStatus: " + e.getMessage());
        }
    }

    public void updateBid(String auctionId, double currentPrice, String currentWinner) {
        String sql = "UPDATE auctions SET current_price = ?, current_winner = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, currentPrice);
            ps.setString(2, currentWinner);
            ps.setString(3, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi updateBid: " + e.getMessage());
        }
    }

    public void updateEndTime(String auctionId, LocalDateTime newEndTime) {
        String sql = "UPDATE auctions SET end_time = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEndTime.toString());
            ps.setString(2, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi updateEndTime: " + e.getMessage());
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────

    public void delete(String auctionId) {
        String sql = "DELETE FROM auctions WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auctionId);
            ps.executeUpdate();
            System.out.println("[AuctionDAO] Xóa phiên: " + auctionId);
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi delete: " + e.getMessage());
        }
    }

    // ── Helper ─────────────────────────────────────────────────────

    private Auction mapToAuction(ResultSet rs) throws SQLException {
        String itemId = rs.getString("item_id");
        Item item = itemDAO.findById(itemId);

        Auction auction = new Auction(
                rs.getString("id"),
                item,
                rs.getDouble("start_price"),
                rs.getDouble("min_increment"),
                LocalDateTime.parse(rs.getString("start_time")),
                LocalDateTime.parse(rs.getString("end_time"))
        );
        auction.setStatus(rs.getString("status"));
        return auction;
    }
}