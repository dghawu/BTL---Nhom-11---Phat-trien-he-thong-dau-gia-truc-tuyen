package com.example.dao;

import com.example.model.item.*;
import com.example.model.item.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO - đã sửa cho MySQL, hỗ trợ attributes cho Fashion, Art, Vehicle, Electronics
 */
public class ItemDAO {

    private final Connection conn;

    public ItemDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── INSERT ─────────────────────────────────────────────────────

    public void save(Item item) {
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

            // Lưu attributes
            saveAttributes(item);
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

            // Lưu attributes
            saveAttributes(item);
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi saveWithImage: " + e.getMessage());
        }
    }

    /**
     * Lưu attributes vào bảng riêng theo loại item
     */
    private void saveAttributes(Item item) {
        if (item instanceof Fashion fashion) {
            String sql = "INSERT IGNORE INTO fashion (id, item_id, brand, size) VALUES (UUID(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ps.setString(2, fashion.getBrand());
                ps.setString(3, fashion.getSize());
                ps.executeUpdate();
                System.out.println("[ItemDAO]  Saved Fashion: " + fashion.getBrand() + " - " + fashion.getSize());
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi save Fashion attributes: " + e.getMessage());
            }
        } else if (item instanceof Art art) {
            String sql = "INSERT IGNORE INTO art (id, item_id, artist, medium) VALUES (UUID(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ps.setString(2, art.getArtist());
                ps.setString(3, art.getMedium());
                ps.executeUpdate();
                System.out.println("[ItemDAO]  Saved Art: " + art.getArtist() + " - " + art.getMedium());
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi save Art attributes: " + e.getMessage());
            }
        } else if (item instanceof Vehicle vehicle) {
            String sql = "INSERT IGNORE INTO vehicle (id, item_id, brand, mileage) VALUES (UUID(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ps.setString(2, vehicle.getBrand());
                ps.setLong(3, vehicle.getMileage());
                ps.executeUpdate();
                System.out.println("[ItemDAO]  Saved Vehicle: " + vehicle.getBrand() + " - " + vehicle.getMileage());
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi save Vehicle attributes: " + e.getMessage());
            }
        } else if (item instanceof Electronics electronics) {
            String sql = "INSERT IGNORE INTO electronics (id, item_id, brand, warranty_months) VALUES (UUID(), ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ps.setString(2, electronics.getBrand());
                ps.setInt(3, electronics.getWarrantyMonths());
                ps.executeUpdate();
                System.out.println("[ItemDAO] Saved Electronics: " + electronics.getBrand() + " - " + electronics.getWarrantyMonths());
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi save Electronics attributes: " + e.getMessage());
            }
        }
    }

    /**
     * Load attributes từ bảng riêng vào item
     */
    private void loadAttributes(Item item) {
        if (item instanceof Fashion fashion) {
            String sql = "SELECT brand, size FROM fashion WHERE item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    fashion.setBrand(rs.getString("brand"));
                    fashion.setSize(rs.getString("size"));
                    System.out.println("[ItemDAO]  Loaded Fashion: " + fashion.getBrand() + " - " + fashion.getSize());
                }
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi load Fashion attributes: " + e.getMessage());
            }
        } else if (item instanceof Art art) {
            String sql = "SELECT artist, medium FROM art WHERE item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    art.setArtist(rs.getString("artist"));
                    art.setMedium(rs.getString("medium"));
                    System.out.println("[ItemDAO]  Loaded Art: " + art.getArtist() + " - " + art.getMedium());
                }
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi load Art attributes: " + e.getMessage());
            }
        } else if (item instanceof Vehicle vehicle) {
            String sql = "SELECT brand, mileage FROM vehicle WHERE item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    vehicle.setBrand(rs.getString("brand"));
                    vehicle.setMileage(rs.getLong("mileage"));
                    System.out.println("[ItemDAO]  Loaded Vehicle: " + vehicle.getBrand() + " - " + vehicle.getMileage());
                }
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi load Vehicle attributes: " + e.getMessage());
            }
        } else if (item instanceof Electronics electronics) {
            String sql = "SELECT brand, warranty_months FROM electronics WHERE item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    electronics.setBrand(rs.getString("brand"));
                    electronics.setWarrantyMonths(rs.getInt("warranty_months"));
                    System.out.println("[ItemDAO]  Loaded Electronics: " + electronics.getBrand() + " - " + electronics.getWarrantyMonths());
                }
            } catch (SQLException e) {
                System.out.println("[ItemDAO] Lỗi load Electronics attributes: " + e.getMessage());
            }
        }
    }

    // ── SELECT ─────────────────────────────────────────────────────

    public Item findById(String id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Item item = mapToItem(rs);
                loadAttributes(item);  // Load attributes
                return item;
            }
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
            while (rs.next()) {
                Item item = mapToItem(rs);
                loadAttributes(item);  // Load attributes
                list.add(item);
            }
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
            while (rs.next()) {
                Item item = mapToItem(rs);
                loadAttributes(item);  // Load attributes
                list.add(item);
            }
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
            while (rs.next()) {
                Item item = mapToItem(rs);
                loadAttributes(item);  // Load attributes
                list.add(item);
            }
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi findByType: " + e.getMessage());
        }
        return list;
    }

    public String getImageByItemId(String itemId) {
        String sql = "SELECT image FROM items WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("image");
            }
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi getImageByItemId: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ─────────────────────────────────────────────────────

    public void updateStatus(String itemId, String newStatus) {
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, itemId);
            ps.executeUpdate();
            System.out.println("[ItemDAO] Updated status for item: " + itemId + " -> " + newStatus);
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
            System.out.println("[ItemDAO] Deleted item: " + itemId);
        } catch (SQLException e) {
            System.out.println("[ItemDAO] Lỗi delete: " + e.getMessage());
        }
    }

    // ── Helper ─────────────────────────────────────────────────────

    private Item mapToItem(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String sellerId = rs.getString("seller_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startPrice = rs.getDouble("starting_price");
        String statusStr = rs.getString("status");
        String typeStr = rs.getString("type");

        String image = null;
        try {
            image = rs.getString("image");
        } catch (SQLException ignored) {
        }

        Item.ItemStatus status = Item.ItemStatus.valueOf(statusStr);
        Item.ItemType type = Item.ItemType.valueOf(typeStr);
        Item item = type.create(sellerId, name, id, description, startPrice, status);
        item.setImage(image);

        try {
            String sellerName = rs.getString("seller_name");
            item.setSellerName(sellerName);
        } catch (SQLException ignored) {
            // fallback: query riêng
            try {
                PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT name FROM users WHERE id = ?");
                ps2.setString(1, sellerId);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) item.setSellerName(rs2.getString("name"));
                ps2.close();
            } catch (SQLException e2) {
                item.setSellerName(sellerId); // fallback cuối
            }
        }

        return item;
    }
}