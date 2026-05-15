package model.user;

import exception.AuctionCancelNotAllowedException;
import exception.AuctionEditNotAllowedException;
import exception.AuctionNotFoundException;
import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import org.junit.jupiter.api.*;
import service.AuctionTimer;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Seller Logic Tests")
class SellerTest {

    private Seller seller;
    private Item laptop;

    @BeforeEach
    void setUp() {
        seller = new Seller("S01", "Dương Seller", "pass123");
        laptop = Item.ItemType.ELECTRONICS.create(
                "I01", "Macbook Pro", "ITEM-001",
                "M3 Chip", 30_000_000.0, Item.ItemStatus.APPROVED);
    }

    @AfterAll
    static void tearDown() {
        AuctionTimer.getInstance().shutdown();
    }

    // ── Test 1: Tạo phiên ─────────────────────────────────────────

    @Test
    @DisplayName("Tạo phiên đấu giá mới → Thông tin khởi tạo chính xác")
    void testCreateAuction() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end   = LocalDateTime.now().plusDays(2);

        Auction a = seller.createAuction("AUC-101", laptop, 1_000_000.0, start, end);

        assertNotNull(a);
        assertEquals("AUC-101", a.getAuctionId());
        assertEquals(30_000_000.0, a.getStartPrice());
        assertEquals(AuctionStatus.PENDING, a.getStatus());
    }

    // ── Test 2: Sửa phiên ────────────────────────────────────────

    @Test
    @DisplayName("Sửa phiên khi còn PENDING → Cập nhật thành công")
    void testEditAuctionBeforeRunning() {
        seller.createAuction("AUC-101", laptop, 1_000_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        seller.editAuction("AUC-101", "Macbook Pro M3 Updated", 32_000_000.0);

        assertEquals("Macbook Pro M3 Updated", laptop.getName());
        assertEquals(32_000_000.0, laptop.getStartPrice());
    }

    @Test
    @DisplayName("Sửa phiên khi đang RUNNING → AuctionEditNotAllowedException")
    void testEditAuctionWhileRunning() {
        Auction a = seller.createAuction("AUC-101", laptop, 1_000_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        a.setStatus(AuctionStatus.APPROVED);
        a.startAuction(); // → RUNNING

        // Phải ném AuctionEditNotAllowedException
        assertThrows(AuctionEditNotAllowedException.class,
                () -> seller.editAuction("AUC-101", "Hack Price", 1_000.0),
                "Phải ném AuctionEditNotAllowedException khi sửa phiên RUNNING");

        // Giá không bị thay đổi
        assertEquals(30_000_000.0, laptop.getStartPrice());
    }

    // ── Test 3: Hủy phiên ────────────────────────────────────────

    @Test
    @DisplayName("Hủy phiên đang PENDING → Chuyển sang CANCELED")
    void testCancelAuctionSuccess() {
        Auction a = seller.createAuction("AUC-202", laptop, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        seller.cancelAuction("AUC-202");

        assertEquals(AuctionStatus.CANCELED, a.getStatus());
    }

    @Test
    @DisplayName("Hủy phiên đã FINISHED → AuctionCancelNotAllowedException")
    void testCancelFinishedAuction() {
        Auction a = seller.createAuction("AUC-202", laptop, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        a.setStatus(AuctionStatus.FINISHED);

        // Phải ném exception
        assertThrows(AuctionCancelNotAllowedException.class,
                () -> seller.cancelAuction("AUC-202"),
                "Phải ném AuctionCancelNotAllowedException khi hủy phiên đã FINISHED");

        // Trạng thái vẫn là FINISHED
        assertEquals(AuctionStatus.FINISHED, a.getStatus());
    }

    // ── Test 4: View ──────────────────────────────────────────────

    @Test
    @DisplayName("Xem danh sách và chi tiết → Hành vi đúng")
    void testViewMethods() {
        seller.createAuction("AUC-303", laptop, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        // ID hợp lệ → không ném exception
        assertDoesNotThrow(() -> seller.viewMyAuctions());
        assertDoesNotThrow(() -> seller.viewAuctionDetail("AUC-303"));

        // ID không tồn tại → AuctionNotFoundException (hành vi mới, đặc thù hơn "in thông báo")
        assertThrows(AuctionNotFoundException.class,
                () -> seller.viewAuctionDetail("ID-KHONG-TON-TAI"),
                "Phải ném AuctionNotFoundException khi xem phiên không tồn tại");
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
