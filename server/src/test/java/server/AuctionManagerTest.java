package server;

import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import org.junit.jupiter.api.*;
import service.AuctionManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuctionManager Service Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionManagerTest {

    private AuctionManager manager;
    private Auction testAuction;
    private final String auctionId = "AUC-999";

    @BeforeEach
    void setUp() {
        manager = AuctionManager.getInstance();

        // Tạo một item và auction mẫu để test
        Item item = Item.ItemType.ETC.create("S01", "Test Item", "I01", "Desc", 1000.0, Item.ItemStatus.APPROVED);
        testAuction = new Auction(auctionId, item, 1000.0, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        // Làm sạch dữ liệu cũ nếu có (tùy vào AuctionDAO của bạn)
        try { manager.removeAuction(auctionId); } catch (Exception ignored) {}
    }

    // ── Test 1: Singleton ──────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Singleton: Đảm bảo luôn trả về cùng một instance")
    void testSingletonInstance() {
        AuctionManager instance1 = AuctionManager.getInstance();
        AuctionManager instance2 = AuctionManager.getInstance();
        assertSame(instance1, instance2, "AuctionManager phải là Singleton");
    }

    // ── Test 2: Quản lý phiên (CRUD) ────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("Thêm và Tìm kiếm phiên → Thành công")
    void testAddAndFindAuction() {
        manager.addAuction(testAuction);

        Auction found = manager.findAuction(auctionId);
        assertNotNull(found);
        assertEquals(auctionId, found.getAuctionId());
    }

    @Test
    @Order(3)
    @DisplayName("Tìm kiếm phiên không tồn tại → Ném IllegalArgumentException")
    void testFindNonExistentAuction() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.findAuction("NON-EXISTENT-ID");
        });
    }

    // ── Test 3: Duyệt và Từ chối ───────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Duyệt phiên PENDING → Chuyển sang APPROVED")
    void testApproveAuction() {
        manager.addAuction(testAuction); // Mặc định là PENDING

        manager.approveAuction(auctionId);

        assertEquals(AuctionStatus.APPROVED, testAuction.getStatus());
    }

    @Test
    @Order(5)
    @DisplayName("Từ chối phiên → Chuyển sang REJECTED")
    void testRejectAuction() {
        manager.addAuction(testAuction);

        manager.rejectAuction(auctionId);

        assertEquals(AuctionStatus.REJECTED, testAuction.getStatus());
    }

    // ── Test 4: Lọc danh sách (Filtering) ──────────────────────────

    @Test
    @Order(6)
    @DisplayName("Lọc danh sách PENDING và RUNNING")
    void testFilterAuctions() {
        manager.addAuction(testAuction); // testAuction đang là PENDING

        List<Auction> pendingList = manager.getPendingAuctions();
        assertFalse(pendingList.isEmpty(), "Danh sách PENDING không được rỗng");

        // Giả lập một phiên đang chạy
        testAuction.setStatus(AuctionStatus.RUNNING);
        List<Auction> runningList = manager.getRunningAuctions();
        // Lưu ý: Kết quả này phụ thuộc vào việc AuctionDAO lưu trữ trong bộ nhớ hay DB
        assertNotNull(runningList);
    }

    // ── Test 5: Xóa phiên ──────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Xóa phiên khỏi hệ thống")
    void testRemoveAuction() {
        manager.addAuction(testAuction);
        manager.removeAuction(auctionId);

        assertThrows(IllegalArgumentException.class, () -> manager.findAuction(auctionId));
    }
}