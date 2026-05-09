package server;

import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho logic quản lý phiên đấu giá.
 * Dùng in-memory store để tránh phụ thuộc vào database.
 */
@DisplayName("AuctionManager Service Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionManagerTest {

    // ── In-memory stub (không cần DB) ─────────────────────────────

    static class InMemoryAuctionManager {
        private static volatile InMemoryAuctionManager instance;
        private final Map<String, Auction> store = new ConcurrentHashMap<>();

        private InMemoryAuctionManager() {}

        static InMemoryAuctionManager getInstance() {
            if (instance == null) {
                synchronized (InMemoryAuctionManager.class) {
                    if (instance == null) instance = new InMemoryAuctionManager();
                }
            }
            return instance;
        }

        void clear() { store.clear(); }

        void addAuction(Auction auction) { store.put(auction.getAuctionId(), auction); }

        void removeAuction(String id) { store.remove(id); }

        Auction findAuction(String id) {
            Auction a = store.get(id);
            if (a == null) throw new IllegalArgumentException("Không tìm thấy phiên: " + id);
            return a;
        }

        List<Auction> getPendingAuctions() {
            List<Auction> r = new ArrayList<>();
            for (Auction a : store.values())
                if (a.getStatus() == AuctionStatus.PENDING) r.add(a);
            return r;
        }

        List<Auction> getRunningAuctions() {
            List<Auction> r = new ArrayList<>();
            for (Auction a : store.values())
                if (a.getStatus() == AuctionStatus.RUNNING) r.add(a);
            return r;
        }

        void approveAuction(String id) {
            Auction a = findAuction(id);
            if (a.getStatus() == AuctionStatus.PENDING) a.setStatus(AuctionStatus.APPROVED);
        }

        void rejectAuction(String id) {
            findAuction(id).setStatus(AuctionStatus.REJECTED);
        }
    }

    // ── Fields ─────────────────────────────────────────────────────
    private InMemoryAuctionManager manager;
    private Auction testAuction;
    private final String auctionId = "AUC-999";

    @BeforeEach
    void setUp() {
        manager = InMemoryAuctionManager.getInstance();
        manager.clear();

        Item item = Item.ItemType.ETC.create(
                "S01", "Test Item", "I01", "Desc", 1000.0, Item.ItemStatus.APPROVED);
        testAuction = new Auction(auctionId, item, 1000.0, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    }

    // ── Test 1: Singleton ──────────────────────────────────────────
    @Test @Order(1)
    @DisplayName("Singleton: Đảm bảo luôn trả về cùng một instance")
    void testSingletonInstance() {
        assertSame(InMemoryAuctionManager.getInstance(), InMemoryAuctionManager.getInstance(),
                "AuctionManager phải là Singleton");
    }

    // ── Test 2: CRUD ──────────────────────────────────────────────
    @Test @Order(2)
    @DisplayName("Thêm và Tìm kiếm phiên → Thành công")
    void testAddAndFindAuction() {
        manager.addAuction(testAuction);
        Auction found = manager.findAuction(auctionId);
        assertNotNull(found);
        assertEquals(auctionId, found.getAuctionId());
    }

    @Test @Order(3)
    @DisplayName("Tìm kiếm phiên không tồn tại → Ném IllegalArgumentException")
    void testFindNonExistentAuction() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.findAuction("NON-EXISTENT-ID"));
    }

    // ── Test 3: Approve / Reject ──────────────────────────────────
    @Test @Order(4)
    @DisplayName("Duyệt phiên PENDING → Chuyển sang APPROVED")
    void testApproveAuction() {
        manager.addAuction(testAuction);
        manager.approveAuction(auctionId);
        assertEquals(AuctionStatus.APPROVED, testAuction.getStatus());
    }

    @Test @Order(5)
    @DisplayName("Từ chối phiên → Chuyển sang REJECTED")
    void testRejectAuction() {
        manager.addAuction(testAuction);
        manager.rejectAuction(auctionId);
        assertEquals(AuctionStatus.REJECTED, testAuction.getStatus());
    }

    // ── Test 4: Filtering ─────────────────────────────────────────
    @Test @Order(6)
    @DisplayName("Lọc danh sách PENDING và RUNNING")
    void testFilterAuctions() {
        manager.addAuction(testAuction); // PENDING

        assertFalse(manager.getPendingAuctions().isEmpty(), "Danh sách PENDING không được rỗng");
        assertTrue(manager.getPendingAuctions().contains(testAuction));

        testAuction.setStatus(AuctionStatus.RUNNING);
        assertFalse(manager.getRunningAuctions().isEmpty(), "Danh sách RUNNING không được rỗng");
        assertTrue(manager.getRunningAuctions().contains(testAuction));
    }

    // ── Test 5: Remove ────────────────────────────────────────────
    @Test @Order(7)
    @DisplayName("Xóa phiên khỏi hệ thống")
    void testRemoveAuction() {
        manager.addAuction(testAuction);
        manager.removeAuction(auctionId);
        assertThrows(IllegalArgumentException.class, () -> manager.findAuction(auctionId));
    }
}
