package model.auction;

import com.example.model.auction.BidTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BidTransaction Model Tests")
class BidTransactionTest {

    private final String bidderId = "BIDDER-001";
    private final String auctionId = "AUC-999";
    private final double amount = 1500.50;

    // ── Test 1: Khởi tạo giao dịch ────────────────────────────────

    @Test
    @DisplayName("Constructor: Khởi tạo giao dịch và kiểm tra dữ liệu")
    void testBidTransactionCreation() {
        BidTransaction txn = new BidTransaction(bidderId, auctionId, amount);

        assertNotNull(txn.getId(), "ID giao dịch không được null");
        assertTrue(txn.getId().startsWith("TXN-"), "ID phải có tiền tố TXN-");

        assertEquals(bidderId, txn.getBidderId());
        assertEquals(auctionId, txn.getAuctionId());
        assertEquals(amount, txn.getAmount());

        // Kiểm tra timestamp không được null và phải là thời điểm hiện tại (xấp xỉ)
        assertNotNull(txn.getTimestamp());
        assertTrue(txn.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    // ── Test 2: Tính duy nhất của ID ──────────────────────────────

    @Test
    @DisplayName("ID: Đảm bảo các giao dịch khác nhau có ID khác nhau")
    void testUniqueId() throws InterruptedException {
        BidTransaction txn1 = new BidTransaction(bidderId, auctionId, 1000.0);
        // Ngủ 1ms để đảm bảo System.currentTimeMillis() thay đổi (nếu logic dựa trên thời gian)
        Thread.sleep(1);
        BidTransaction txn2 = new BidTransaction(bidderId, auctionId, 2000.0);

        assertNotEquals(txn1.getId(), txn2.getId(), "Mỗi giao dịch phải có một mã định danh duy nhất");
    }

    // ── Test 3: Hiển thị thông tin ────────────────────────────────

    @Test
    @DisplayName("PrintInfo: Không gây lỗi khi in log giao dịch")
    void testPrintInfo() {
        BidTransaction txn = new BidTransaction(bidderId, auctionId, amount);

        // Đảm bảo hàm in thông tin (thường dùng để debug/log) hoạt động bình thường
        assertDoesNotThrow(() -> txn.printInfo());
    }

    // ── Test 4: Kiểm tra tính toàn vẹn thời gian ──────────────────

    @Test
    @DisplayName("Timestamp: Thời gian giao dịch không được ở quá khứ xa hoặc tương lai")
    void testTimestampIntegrity() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        BidTransaction txn = new BidTransaction(bidderId, auctionId, amount);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertTrue(txn.getTimestamp().isAfter(beforeCreation) && txn.getTimestamp().isBefore(afterCreation),
                "Timestamp giao dịch phải nằm trong khoảng thời gian vừa tạo");
    }
}