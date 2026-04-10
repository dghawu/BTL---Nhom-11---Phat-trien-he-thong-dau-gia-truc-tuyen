import java.time.LocalDateTime;

/**
 * Lớp BidTransaction lưu trữ thông tin về một giao dịch đặt giá cụ thể.
 * Đáp ứng yêu cầu về lưu trữ lịch sử và hiển thị biểu đồ giá theo thời gian.
 */
public class BidTransaction extends Entity {
    private String bidderId;      // ID của người đặt giá
    private String auctionId;     // ID của phiên đấu giá
    private double amount;        // Số tiền đặt giá
    private LocalDateTime timestamp; // Thời điểm đặt giá (Trục X cho biểu đồ)

    public BidTransaction(String bidderId, String auctionId, double amount) {
        super(generateId()); // Gọi constructor của Entity để tạo ID giao dịch
        this.bidderId = bidderId;
        this.auctionId = auctionId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now(); // Tự động ghi lại thời gian thực
    }

    /**
     * Phương thức tĩnh để tạo ID ngẫu nhiên cho giao dịch (ví dụ minh họa)
     */
    private static String generateId() {
        return "TXN-" + System.currentTimeMillis();
    }

    // Getters - Cần thiết cho việc Visualization (Biểu đồ) và Server xử lý
    public String getBidderId() {
        return bidderId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getAmount() {
        return amount; // Giá trị này sẽ dùng làm trục Y trên biểu đồ
    }

    public LocalDateTime getTimestamp() {
        return timestamp; // Giá trị này sẽ dùng làm trục X trên biểu đồ
    }

    @Override
    public void printInfo() {
        System.out.println("Transaction: " + getId() +
                " | Bidder: " + bidderId +
                " | Amount: " + amount +
                " | Time: " + timestamp);
    }
}

