package server;

import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import observer.AuctionObserver;
import org.junit.jupiter.api.*;
import service.AuctionTimer;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit test cho AuctionTimer (scheduling & threading).
 * <p>
 * Lưu ý thiết kế:
 * - Auction.startAuction() yêu cầu status == APPROVED trước khi chuyển RUNNING.
 * - AuctionTimer.schedule() gọi auction.closeAuction() sau khi hết giờ.
 * - Mỗi test dùng auctionId khác nhau để tránh conflict trên Singleton scheduler.
 * - Thêm NoOpObserver để notifyObservers() không bị NullPointerException.
 */
@DisplayName("AuctionTimer Threading & Scheduling Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionTimerTest {

    // ── Fields ─────────────────────────────────────────────────────
    private AuctionTimer timer;

    // ── Helper: tạo Auction đã sẵn sàng để lên lịch ───────────────
    private static Auction makeAuction(String id, int durationSeconds) {
        Item item = Item.ItemType.ETC.create(
                "S01", "Timer Item", id + "-ITEM", "Desc", 1000.0, Item.ItemStatus.APPROVED);
        Auction a = new Auction(id, item, 1000.0, 100.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(durationSeconds));
        a.addObserver(new NoOpObserver()); // tránh NPE khi closeAuction notifyObservers
        a.setStatus(AuctionStatus.RUNNING); // đặt trực tiếp để không cần qua startAuction()
        return a;
    }

    @AfterAll
    static void globalTearDown() {
        AuctionTimer.getInstance().shutdown();
    }

    @BeforeEach
    void setUp() {
        timer = AuctionTimer.getInstance();
    }

    // ── Test 1: Tự động đóng phiên ────────────────────────────────
    @Test
    @Order(1)
    @DisplayName("Schedule: Tự động đóng phiên sau khi hết thời gian")
    void testAutoCloseAuction() throws InterruptedException {
        Auction auction = makeAuction("TIMER-AUC-T1", 2);

        timer.schedule(auction);

        TimeUnit.SECONDS.sleep(3);

        assertEquals(AuctionStatus.FINISHED, auction.getStatus(),
                "Phiên đấu giá phải tự động chuyển sang FINISHED sau khi hết giờ");
    }

    // ── Test 2: Hủy lịch ─────────────────────────────────────────
    @Test
    @Order(2)
    @DisplayName("Cancel: Hủy lịch đóng phiên thành công")
    void testCancelTask() throws InterruptedException {
        Auction auction = makeAuction("TIMER-AUC-T2", 2);

        timer.schedule(auction);
        timer.cancelTask("TIMER-AUC-T2");

        TimeUnit.SECONDS.sleep(3);

        assertNotEquals(AuctionStatus.FINISHED, auction.getStatus(),
                "Phiên không được đóng nếu task đã bị hủy");
    }

    // ── Test 3: Lên lịch lại ──────────────────────────────────────
    @Disabled
    @Test
    @Order(3)
    @DisplayName("Reschedule: Lên lịch lại khi thời gian được gia hạn")
    void testRescheduleAuction() throws InterruptedException {
        Auction auction = makeAuction("TIMER-AUC-T3", 2);

        timer.schedule(auction);

        // Gia hạn thêm 2 giây → tổng ~4s từ lúc bắt đầu
        auction.setEndTime(auction.getEndTime().plusSeconds(2));
        timer.reschedule(auction);

        // Đợi 3s — nếu không reschedule thì đã FINISHED, nhưng vì gia hạn nên vẫn RUNNING
        TimeUnit.SECONDS.sleep(3);
        assertEquals(AuctionStatus.RUNNING, auction.getStatus(),
                "Phiên phải tiếp tục chạy do đã được gia hạn thêm thời gian");

        // Đợi thêm ~2s cho lần đóng thực sự
        TimeUnit.SECONDS.sleep(3);
        assertEquals(AuctionStatus.FINISHED, auction.getStatus(),
                "Phiên phải FINISHED sau khi gia hạn hết hạn");
    }

    // ── Test 4: Đóng ngay nếu đã quá giờ ─────────────────────────
    @Test
    @Order(4)
    @DisplayName("Schedule: Đóng ngay lập tức nếu phiên đã quá giờ")
    void testSchedulePastAuction() {
        Item item = Item.ItemType.ETC.create(
                "S01", "Past Item", "PAST-ITEM", "Desc", 1000.0, Item.ItemStatus.APPROVED);
        Auction auction = new Auction("TIMER-AUC-T4", item, 1000.0, 100.0,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusMinutes(1)); // endTime ở quá khứ
        auction.addObserver(new NoOpObserver());
        auction.setStatus(AuctionStatus.RUNNING);

        timer.schedule(auction);

        assertEquals(AuctionStatus.FINISHED, auction.getStatus(),
                "Nếu thời gian kết thúc ở quá khứ, phiên phải đóng ngay lập tức");
    }

    // ── Fake observer (không cần DB hay Socket) ────────────────────
    static class NoOpObserver implements AuctionObserver {
        @Override
        public void update(Auction auction, double newPrice, String lastBidderId) {
        }
    }
}
