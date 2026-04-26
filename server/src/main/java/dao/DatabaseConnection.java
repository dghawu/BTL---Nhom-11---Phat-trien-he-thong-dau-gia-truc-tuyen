package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton quản lý kết nối MySQL.
 * Cần tạo database trước: CREATE DATABASE auction_db;
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    // ── Cấu hình kết nối MySQL ─────────────────────────────────────
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/auction_db" +
            "?useSSL=false" +
            "&serverTimezone=Asia/Ho_Chi_Minh" +
            "&allowPublicKeyRetrieval=true" +
            "&characterEncoding=UTF-8";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "your_password"; // ← đổi thành password của bạn

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(true);
            System.out.println("[DB] Kết nối MySQL thành công!");
            initTables();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy MySQL JDBC driver!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối MySQL: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) instance = new DatabaseConnection();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối MySQL!", e);
        }
        return connection;
    }

    /**
     * Tạo các bảng nếu chưa tồn tại.
     */
    private void initTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Bảng users
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id          VARCHAR(100) PRIMARY KEY,
                    name        VARCHAR(100) NOT NULL UNIQUE,
                    password    VARCHAR(255) NOT NULL,
                    role        VARCHAR(20)  NOT NULL,
                    created_at  DATETIME     NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

        // Bảng items
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id              VARCHAR(100) PRIMARY KEY,
                    seller_id       VARCHAR(100) NOT NULL,
                    name            VARCHAR(255) NOT NULL,
                    description     TEXT,
                    starting_price  DOUBLE       NOT NULL,
                    status          VARCHAR(20)  NOT NULL,
                    type            VARCHAR(20)  NOT NULL,
                    created_at      DATETIME     NOT NULL,
                    FOREIGN KEY (seller_id) REFERENCES users(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

        // Bảng auctions
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS auctions (
                    id              VARCHAR(100) PRIMARY KEY,
                    item_id         VARCHAR(100) NOT NULL,
                    start_price     DOUBLE       NOT NULL,
                    current_price   DOUBLE       NOT NULL,
                    min_increment   DOUBLE       NOT NULL,
                    current_winner  VARCHAR(100),
                    start_time      DATETIME     NOT NULL,
                    end_time        DATETIME     NOT NULL,
                    status          VARCHAR(20)  NOT NULL,
                    created_at      DATETIME     NOT NULL,
                    FOREIGN KEY (item_id) REFERENCES items(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

        // Bảng bid_transactions
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS bid_transactions (
                    id          VARCHAR(100) PRIMARY KEY,
                    bidder_id   VARCHAR(100) NOT NULL,
                    auction_id  VARCHAR(100) NOT NULL,
                    amount      DOUBLE       NOT NULL,
                    timestamp   DATETIME     NOT NULL,
                    FOREIGN KEY (bidder_id)  REFERENCES users(id),
                    FOREIGN KEY (auction_id) REFERENCES auctions(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

        stmt.close();
        System.out.println("[DB] Khởi tạo bảng MySQL thành công!");
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Đã đóng kết nối MySQL.");
            }
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi đóng kết nối: " + e.getMessage());
        }
    }
}