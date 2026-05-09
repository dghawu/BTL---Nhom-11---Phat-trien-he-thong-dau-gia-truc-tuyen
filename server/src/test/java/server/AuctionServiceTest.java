package server;

import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import observer.AuctionObserver;
import observer.SocketBroadcaster;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho AuctionService (business logic đấu giá).
 * Dùng in-memory store + stub Observer để tránh phụ thuộc vào DB và Socket thật.
 *
 * QUAN TRỌNG: Không gọi AuctionTimer.shutdown() ở đây vì AuctionTimer là Singleton
 * dùng chung toàn bộ JVM test session — shutdown sẽ phá hủy scheduler cho các test khác.
 */
@DisplayName("AuctionService Business Logic Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionServiceTest {

    // ── Fake SocketBroadcaster (không cần Socket thật) ─────────────
    static class FakeBroadcaster extends SocketBroadcaster {
        FakeBroadcaster(String auctionId) { super(auctionId); }
    }

    // ── Fake Observer (không cần DB) ──────────────────────────────
    static class NoOpObserver implements AuctionObserver {
        @Override public void update(Auction auction, double newPrice, String lastBidderId) {}
    }

    // ── In-memory AuctionService ───────────────────────────────────
    static class InMemoryAuctionService {
        private static volatile InMemoryAuctionService instance;
        private final Map<String, SocketBroadcaster> broadcasters = new ConcurrentHashMap<>();

        private InMemoryAuctionService() {}

        static InMemoryAuctionService getInstance() {
            if (instance == null) {
                synchronized (InMemoryAuctionService.class) {
                    if (instance == null) instance = new InMemoryAuctionService();
                }
            }
            return instance;
        }

        void clear() { broadcasters.clear(); }

        /** Tạo phiên mới ở trạng thái PENDING. Giá khởi điểm lấy từ Item. */
        Auction createAuction(String id, Item item, double minIncrement,
                              LocalDateTime start, LocalDateTime end) {
            return new Auction(id, item, item.getStartPrice(), minIncrement, start, end);
        }

        /**
         * Bắt đầu phiên: approve nếu cần → đăng ký observers → RUNNING + AuctionTimer.
         * Auction.startAuction() yêu cầu status == APPROVED.
         */
        void startAuction(Auction auction) {
            if (auction.getStatus() == AuctionStatus.PENDING) {
                auction.setStatus(AuctionStatus.APPROVED);
            }
            auction.addObserver(new NoOpObserver());

            FakeBroadcaster broadcaster = new FakeBroadcaster(auction.getAuctionId());
            auction.addObserver(broadcaster);
            broadcasters.put(auction.getAuctionId(), broadcaster);

            auction.startAuction(); // → RUNNING + lên lịch AuctionTimer
        }

        /** Đóng phiên và giải phóng broadcaster. */
        void closeAuction(Auction auction) {
            auction.closeAuction();
            SocketBroadcaster b = broadcasters.remove(auction.getAuctionId());
            if (b != null) {
                b.broadcastClose(auction.getAuctionId(),
                        auction.getCurrentWinner(), auction.getCurrentPrice());
            }
        }

        SocketBroadcaster getBroadcaster(String auctionId) {
            return broadcasters.get(auctionId);
        }
    }

    // ── Fields ─────────────────────────────────────────────────────
    private InMemoryAuctionService service;
    private Item testItem;
    private final String auctionId = "SERVICE-AUC-001";

    @BeforeEach
    void setUp() {
        service = InMemoryAuctionService.getInstance();
        service.clear();

        testItem = Item.ItemType.ELECTRONICS.create(
                "SELLER-01", "Macbook Test", "ITEM-999", "Mô tả", 2000.0, Item.ItemStatus.APPROVED);
    }

    // KHÔNG có @AfterAll shutdown() — để AuctionTimer Singleton sống xuyên suốt test session

    // ── Test 1: Singleton ──────────────────────────────────────────
    @Test @Order(1)
    @DisplayName("Singleton: Kiểm tra tính duy nhất của instance")
    void testSingleton() {
        assertSame(InMemoryAuctionService.getInstance(), InMemoryAuctionService.getInstance(),
                "AuctionService phải tuân thủ đúng mẫu Singleton");
    }

    // ── Test 2: Tạo phiên ─────────────────────────────────────────
    @Test @Order(2)
    @DisplayName("Create: Tạo phiên mới và kiểm tra khởi tạo dữ liệu")
    void testCreateAuction() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end   = LocalDateTime.now().plusHours(2);

        Auction auction = service.createAuction(auctionId, testItem, 500.0, start, end);

        assertNotNull(auction);
        assertEquals(auctionId, auction.getAuctionId());
        assertEquals(2000.0, auction.getStartPrice(), "Giá khởi điểm phải lấy từ Item");
        assertEquals(AuctionStatus.PENDING, auction.getStatus(), "Phiên mới tạo phải ở trạng thái PENDING");
    }

    // ── Test 3: Bắt đầu phiên ─────────────────────────────────────
    @Test @Order(3)
    @DisplayName("Start: Đăng ký Observers và chuyển trạng thái RUNNING")
    void testStartAuction() {
        Auction auction = service.createAuction(auctionId, testItem, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        service.startAuction(auction);

        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        assertNotNull(service.getBroadcaster(auctionId), "Broadcaster phải được tạo khi bắt đầu phiên");
    }

    // ── Test 4: Đóng phiên ────────────────────────────────────────
    @Test @Order(4)
    @DisplayName("Close: Kết thúc phiên và giải phóng Broadcaster")
    void testCloseAuction() {
        Auction auction = service.createAuction(auctionId, testItem, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        service.startAuction(auction);

        service.closeAuction(auction);

        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
        assertNull(service.getBroadcaster(auctionId), "Broadcaster phải bị xóa sau khi phiên kết thúc");
    }

    // ── Test 5: Lấy đúng Broadcaster ─────────────────────────────
    @Test @Order(5)
    @DisplayName("Broadcaster: Kiểm tra lấy đúng socket cho đúng phiên")
    void testGetBroadcaster() {
        String id2 = "AUC-REALTIME-02";
        Auction auction = service.createAuction(id2, testItem, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        service.startAuction(auction);

        assertNotNull(service.getBroadcaster(id2));
        assertEquals(id2, service.getBroadcaster(id2).getAuctionId());
    }
}
