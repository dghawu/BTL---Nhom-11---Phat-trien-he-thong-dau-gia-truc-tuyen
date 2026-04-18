package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton quản lý kết nối SQLite.
 * File database sẽ được tạo tự động tại: auction_system.db
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    // Đường dẫn file SQLite — tạo trong thư mục project
    private static final String DB_URL = "jdbc:sqlite:auction_system.db";

    private DatabaseConnection() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            System.out.println("[DB] Kết nối SQLite thành công!");
            initTables();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy SQLite JDBC driver!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối database!", e);
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
            // Tự động reconnect nếu connection bị đóng
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối database!", e);
        }
        return connection;
    }

    /**
     * Tạo các bảng nếu chưa tồn tại.
     * Gọi 1 lần khi khởi động ứng dụng.
     */
    private void initTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Bảng users
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id          TEXT PRIMARY KEY,
                    name        TEXT NOT NULL UNIQUE,
                    password    TEXT NOT NULL,
                    role        TEXT NOT NULL,
                    created_at  TEXT NOT NULL
                )
                """);

        // Bảng items
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id              TEXT PRIMARY KEY,
                    seller_id       TEXT NOT NULL,
                    name            TEXT NOT NULL,
                    description     TEXT,
                    starting_price  REAL NOT NULL,
                    status          TEXT NOT NULL,
                    type            TEXT NOT NULL,
                    created_at      TEXT NOT NULL
                )
                """);

        // Bảng auctions
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS auctions (
                    id              TEXT PRIMARY KEY,
                    item_id         TEXT NOT NULL,
                    start_price     REAL NOT NULL,
                    current_price   REAL NOT NULL,
                    min_increment   REAL NOT NULL,
                    current_winner  TEXT,
                    start_time      TEXT NOT NULL,
                    end_time        TEXT NOT NULL,
                    status          TEXT NOT NULL,
                    created_at      TEXT NOT NULL,
                    FOREIGN KEY (item_id) REFERENCES items(id)
                )
                """);

        // Bảng bid_transactions
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS bid_transactions (
                    id          TEXT PRIMARY KEY,
                    bidder_id   TEXT NOT NULL,
                    auction_id  TEXT NOT NULL,
                    amount      REAL NOT NULL,
                    timestamp   TEXT NOT NULL,
                    FOREIGN KEY (bidder_id)  REFERENCES users(id),
                    FOREIGN KEY (auction_id) REFERENCES auctions(id)
                )
                """);

        stmt.close();
        System.out.println("[DB] Khởi tạo bảng thành công!");
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Đã đóng kết nối.");
            }
        } catch (SQLException e) {
            System.out.println("[DB] Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }
}