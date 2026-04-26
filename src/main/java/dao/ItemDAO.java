package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO xử lý CRUD với bảng items.
 */
public class ItemDAO {

    private final Connection conn;

    public ItemDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── INSERT ─────────────────────────────────────────────────────

    public void save(Item item) {
        String sql = """
                INSERT OR IGNORE INTO items
                (id, seller_id, name, description, starting_price, status, type, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getId());
            ps.setString(2, item.getSellerId());
            ps.setString(3, item.getName());
            ps.setString(4, item.getDescription());
            ps.setDouble(5, item.getStartPrice());
            ps.setString(6, item.getStatus().name());
            ps.setString(7, item.getClass().getSimpleName().toUpperCase());
            ps.setString(8, item.getCreatedAt().toString());
            ps.executeUpdate();
            System.out.println("[ItemDAO] Lưu item: " + item.getName());
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi save: " + e.getMessage());
        }
    }

    // ── SELECT ─────────────────────────────────────────────────────

    public Item findById(String id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToItem(rs);
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi findById: " + e.getMessage());
        }
        return null;
    }

    public List<Item> findBySellerId(String sellerId) {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sellerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToItem(rs));
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi findBySellerId: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ─────────────────────────────────────────────────────

    public void updateStatus(String itemId, String newStatus) {
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi updateStatus: " + e.getMessage());
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────

    public void delete(String itemId) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi delete: " + e.getMessage());
        }
    }

    // ── Helper ─────────────────────────────────────────────────────

    private Item mapToItem(ResultSet rs) throws SQLException {
        String id          = rs.getString("id");
        String sellerId    = rs.getString("seller_id");
        String name        = rs.getString("name");
        String description = rs.getString("description");
        double startPrice  = rs.getDouble("starting_price");
        String statusStr   = rs.getString("status");
        String typeStr     = rs.getString("type");

        Item.ItemStatus status = Item.ItemStatus.valueOf(statusStr);
        Item.ItemType   type   = Item.ItemType.valueOf(typeStr);

        return type.create(sellerId, name, id, description, startPrice, status);
    }
}