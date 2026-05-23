package dao;

import model.user.Admin;
import model.user.Bidder;
import model.user.Seller;
import model.user.User;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - đã sửa cho MySQL (đổi INSERT OR IGNORE → INSERT IGNORE).
 */
public class UserDAO {

    private final Connection conn;

    public UserDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── INSERT ─────────────────────────────────────────────────────

    public void save(User user) {
        String sql = "INSERT IGNORE INTO users (id, name, password, role, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            // Luôn hash mật khẩu trước khi lưu — không bao giờ lưu plain-text
            ps.setString(3, PasswordUtil.hash(user.getPassword()));
            ps.setString(4, user.getRole());
            ps.setString(5, user.getCreatedAt().toString());
            ps.executeUpdate();
            System.out.println("[UserDAO] Lưu user: " + user.getName());
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi save: " + e.getMessage());
        }
    }

    /**
     * Tìm user theo tên VÀ xác thực mật khẩu — dùng cho handleLogin.
     * Trả về null nếu sai tên hoặc sai mật khẩu.
     */
    public User findByCredentials(String name, String rawPassword) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashInDB = rs.getString("password");
                // Dùng BCrypt verify — không so sánh string trực tiếp
                if (PasswordUtil.verify(rawPassword, hashInDB)) {
                    return mapToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi findByCredentials: " + e.getMessage());
        }
        return null; // sai tên hoặc sai mật khẩu
    }

    // ── SELECT ─────────────────────────────────────────────────────

    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToUser(rs);
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi findById: " + e.getMessage());
        }
        return null;
    }

    public User findByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToUser(rs);
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi findByName: " + e.getMessage());
        }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToUser(rs));
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi findAll: " + e.getMessage());
        }
        return list;
    }

    public List<User> findByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapToUser(rs));
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi findByRole: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ─────────────────────────────────────────────────────

    public void updatePassword(String userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Hash mật khẩu mới trước khi cập nhật
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, userId);
            ps.executeUpdate();
            System.out.println("[UserDAO] Đổi mật khẩu: " + userId);
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi updatePassword: " + e.getMessage());
        }
    }

    /**
     * Đổi tên user - dùng cho handleChangeUsername
     */
    public void updateName(String userId, String newName) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, userId);
            ps.executeUpdate();
            System.out.println("[UserDAO] Đổi tên: " + userId + " → " + newName);
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi updateName: " + e.getMessage());
        }
    }

    /**
     * Đổi role user - dùng cho handleMakeAdmin
     */
    public void updateRole(String userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setString(2, userId);
            ps.executeUpdate();
            System.out.println("[UserDAO] Đổi role: " + userId + " → " + newRole);
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi updateRole: " + e.getMessage());
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────

    public void delete(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
            System.out.println("[UserDAO] Xóa user: " + userId);
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi delete: " + e.getMessage());
        }
    }

    // ── Helper ─────────────────────────────────────────────────────

    private User mapToUser(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String password = rs.getString("password");
        String role = rs.getString("role");

        return switch (role) {
            case "BIDDER" -> new Bidder(id, name, password);
            case "SELLER" -> new Seller(id, name, password);
            case "ADMIN" -> new Admin(id, name, password);
            default -> throw new SQLException("Role không hợp lệ: " + role);
        };
    }

    public void initAdminIfNotExists() {
        User existing = findByName("admin");
        if (existing != null) {
            System.out.println("[UserDAO] Admin đã tồn tại");
            return;
        }
        Admin admin = new Admin(
                java.util.UUID.randomUUID().toString(),
                "admin",
                "admin123"
        );
        save(admin);
        System.out.println("[UserDAO] Đã tạo admin mặc định");
    }
}