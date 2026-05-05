package model.user;

import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Seller Logic Tests")
class SellerTest {

    private Seller seller;
    private Item laptop;

    @BeforeEach
    void setUp() {
        // Khởi tạo Seller
        seller = new Seller("S01", "Dương Seller", "pass123");

        // Khởi tạo Item mẫu
        laptop = Item.ItemType.ELECTRONICS.create(
                "I01", "Macbook Pro", "ITEM-001",
                "M3 Chip", 30_000_000.0, Item.ItemStatus.APPROVED);
    }

    // ── Test 1: Tạo phiên đấu giá (Create Auction) ────────────────

    @Test
    @DisplayName("Tạo phiên đấu giá mới → Thông tin khởi tạo chính xác")
    void testCreateAuction() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Auction a = seller.createAuction("AUC-101", laptop, 1_000_000.0, start, end);

        assertNotNull(a);
        assertEquals("AUC-101", a.getAuctionId());
        assertEquals(30_000_000.0, a.getStartPrice());
        assertEquals(AuctionStatus.PENDING, a.getStatus());
    }

    // ── Test 2: Sửa phiên đấu giá (Edit Auction) ──────────────────

    @Test
    @DisplayName("Sửa phiên khi chưa RUNNING → Cập nhật thành công")
    void testEditAuctionBeforeRunning() {
        seller.createAuction("AUC-101", laptop, 1_000_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        // Sửa tên và giá khởi điểm
        seller.editAuction("AUC-101", "Macbook Pro M3 Updated", 32_000_000.0);

        // Lấy lại thông tin để kiểm tra (thông qua đấu giá trong danh sách của seller)
        // Lưu ý: Class của bạn không có getMyAuctions(), nên test gián tiếp qua thuộc tính Item
        assertEquals("Macbook Pro M3 Updated", laptop.getName());
        assertEquals(32_000_000.0, laptop.getStartPrice());
    }

    @Test
    @DisplayName("Sửa phiên khi đang RUNNING → Không được phép thay đổi")
    void testEditAuctionWhileRunning() {
        Auction a = seller.createAuction("AUC-101", laptop, 1_000_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        // Giả lập Admin duyệt và phiên bắt đầu
        a.setStatus(AuctionStatus.APPROVED);
        a.startAuction(); // Chuyển sang RUNNING

        // Thử sửa
        seller.editAuction("AUC-101", "Hack Price", 1_000.0);

        // Giá phải giữ nguyên, không bị đổi thành 1,000
        assertNotEquals(1_000.0, laptop.getStartPrice());
        assertEquals(30_000_000.0, laptop.getStartPrice());
    }

    // ── Test 3: Hủy phiên đấu giá (Cancel Auction) ────────────────

    @Test
    @DisplayName("Hủy phiên đang PENDING/APPROVED → Chuyển sang CANCELED")
    void testCancelAuctionSuccess() {
        Auction a = seller.createAuction("AUC-202", laptop, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        seller.cancelAuction("AUC-202");

        assertEquals(AuctionStatus.CANCELED, a.getStatus());
    }

    @Test
    @DisplayName("Hủy phiên đã FINISHED → Không được phép")
    void testCancelFinishedAuction() {
        Auction a = seller.createAuction("AUC-202", laptop, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        // Đưa về FINISHED
        a.setStatus(AuctionStatus.FINISHED);

        seller.cancelAuction("AUC-202");

        // Fix: Phải khẳng định nó VẪN LÀ FINISHED, không bị đổi thành CANCELED
        assertEquals(AuctionStatus.FINISHED, a.getStatus(), "Phiên đã kết thúc thì không được phép chuyển sang CANCELED");
    }

    // ── Test 4: Hiển thị (View/Print) ──────────────────────────────

    @Test
    @DisplayName("Xem danh sách và chi tiết → Không gây lỗi")
    void testViewMethods() {
        seller.createAuction("AUC-303", laptop, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        assertDoesNotThrow(() -> {
            seller.viewMyAuctions();
            seller.viewAuctionDetail("AUC-303");
            seller.viewAuctionDetail("ID-KHONG-TON-TAI");
        });
    }

    @Test
    @DisplayName("Kiểm tra thông tin định danh và Menu Seller")
    void testSellerIdentity() {
        assertEquals("SELLER", seller.getRole());
        assertDoesNotThrow(() -> {
            seller.printInfo();
            seller.showMenu();
        });
    }
}