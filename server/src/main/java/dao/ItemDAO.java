package dao;

import model.item.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO - đã sửa cho MySQL (đổi INSERT OR IGNORE → INSERT IGNORE).
 */
public class ItemDAO {

    private final Connection conn;

    public ItemDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── INSERT ─────────────────────────────────────────────────────

    public void save(Item item) {
        // Sửa: INSERT IGNORE thay vì INSERT OR IGNORE (MySQL syntax)
        String sql = "INSERT IGNORE INTO items " +
                "(id, seller_id, name, description, starting_price, status, type, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
    public void saveWithImage(Item item, String imageBase64) {
        String sql = "INSERT IGNORE INTO items " +
                "(id, seller_id, name, description, starting_price, status, type, image, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getId());
            ps.setString(2, item.getSellerId());
            ps.setString(3, item.getName());
            ps.setString(4, item.getDescription());
            ps.setDouble(5, item.getStartPrice());
            ps.setString(6, item.getStatus().name());
            ps.setString(7, item.getClass().getSimpleName().toUpperCase());
            ps.setString(8, imageBase64);
            ps.setString(9, item.getCreatedAt().toString());
            ps.executeUpdate();
            System.out.println("[ItemDAO] Lưu item with image: " + item.getName());
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi saveWithImage: " + e.getMessage());
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

    public List<Item> findAll() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToItem(rs));
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi findAll: " + e.getMessage());
        }
        return list;
    }

    public List<Item> findByType(String type) {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.toUpperCase());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToItem(rs));
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi findByType: " + e.getMessage());
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

    public void update(String itemId, String name, String description,
                       double startPrice, String status) {
        String sql = "UPDATE items SET name = ?, description = ?, " +
                "starting_price = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDouble(3, startPrice);
            ps.setString(4, status);
            ps.setString(5, itemId);
            int rows = ps.executeUpdate();
            System.out.println("[ItemDAO] Update item: " + itemId + ", rows=" + rows);
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi update: " + e.getMessage());
        }
    }
    public void updateWithImage(String itemId, String name, String description,
                                double startPrice, String status, String imageBase64) {
        String sql = "UPDATE items SET name = ?, description = ?, " +
                "starting_price = ?, status = ?, image = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDouble(3, startPrice);
            ps.setString(4, status);
            ps.setString(5, imageBase64);
            ps.setString(6, itemId);
            ps.executeUpdate();
            System.out.println("[ItemDAO] UpdateWithImage item: " + itemId);
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi updateWithImage: " + e.getMessage());
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

        String image = null;
        try { image = rs.getString("image"); } catch (SQLException ignored) {}

        Item.ItemStatus status = Item.ItemStatus.valueOf(statusStr);
        Item.ItemType   type   = Item.ItemType.valueOf(typeStr);
        Item item = type.create(sellerId, name, id, description, startPrice, status);
        item.setImage(image);
        return item;
    }
}