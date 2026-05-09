package server;

import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import org.junit.jupiter.api.*;
import service.AuctionService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuctionService Business Logic Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionServiceTest {

    private AuctionService service;
    private Item testItem;
    private final String auctionId = "SERVICE-AUC-001";

    @BeforeEach
    void setUp() {
        service = AuctionService.getInstance();
        // Khởi tạo một Item mẫu (Electronics) để dùng cho việc tạo Auction
        testItem = Item.ItemType.ELECTRONICS.create(
                "SELLER-01", "Macbook Test", "ITEM-999", "Mô tả", 2000.0, Item.ItemStatus.APPROVED
        );
    }

    // ── Test 1: Singleton ──────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Singleton: Kiểm tra tính duy nhất của instance")
    void testSingleton() {
        AuctionService instance1 = AuctionService.getInstance();
        AuctionService instance2 = AuctionService.getInstance();
        assertSame(instance1, instance2, "AuctionService phải tuân thủ đúng mẫu Singleton");
    }

    // ── Test 2: Tạo phiên đấu giá ──────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("Create: Tạo phiên mới và kiểm tra khởi tạo dữ liệu")
    void testCreateAuction() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        Auction auction = service.createAuction(auctionId, testItem, 500.0, start, end);

        assertNotNull(auction);
        assertEquals(auctionId, auction.getAuctionId());
        assertEquals(2000.0, auction.getStartPrice(), "Giá khởi điểm phải lấy từ Item");
        assertEquals(AuctionStatus.PENDING, auction.getStatus(), "Phiên mới tạo phải ở trạng thái PENDING");
    }

    // ── Test 3: Bắt đầu phiên (Observer Registration) ──────────────

    @Test
    @Order(3)
    @DisplayName("Start: Đăng ký Observers và chuyển trạng thái RUNNING")
    void testStartAuction() {
        Auction auction = service.createAuction(auctionId, testItem, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        service.startAuction(auction);

        // Kiểm tra trạng thái
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());

        // Kiểm tra Broadcaster đã được tạo và lưu vào Map chưa
        assertNotNull(service.getBroadcaster(auctionId), "Broadcaster phải được tạo khi bắt đầu phiên");
    }

    // ── Test 4: Đóng phiên và dọn dẹp Broadcaster ───────────────────

    @Test
    @Order(4)
    @DisplayName("Close: Kết thúc phiên và giải phóng Broadcaster")
    void testCloseAuction() {
        Auction auction = service.createAuction(auctionId, testItem, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        service.startAuction(auction);

        // Đóng phiên
        service.closeAuction(auction);

        assertEquals(AuctionStatus.FINISHED, auction.getStatus());

        // Sau khi đóng, broadcaster phải được remove khỏi map để tránh leak memory
        assertNull(service.getBroadcaster(auctionId), "Broadcaster phải bị xóa sau khi phiên kết thúc");
    }

    // ── Test 5: Luồng Watcher (Realtime) ───────────────────────────

    @Test
    @Order(5)
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