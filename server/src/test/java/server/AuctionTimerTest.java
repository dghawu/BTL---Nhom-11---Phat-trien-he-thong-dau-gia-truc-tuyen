package server;

import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import org.junit.jupiter.api.*;
import service.AuctionTimer;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuctionTimer Threading & Scheduling Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionTimerTest {

    private AuctionTimer timer;
    private Auction testAuction;
    private final String auctionId = "TIMER-AUC-001";

    @BeforeEach
    void setUp() {
        timer = AuctionTimer.getInstance();

        // Tạo Item và Auction mẫu
        Item item = Item.ItemType.ETC.create("S01", "Timer Item", "I-TIME", "Desc", 1000.0, Item.ItemStatus.APPROVED);

        // Tạo phiên kết thúc sau 2 giây để test cho nhanh
        testAuction = new Auction(auctionId, item, 1000.0, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusSeconds(2));
    }

    @AfterAll
    static void tearDown() {
        // Tắt scheduler sau khi chạy xong tất cả test để tránh rò rỉ luồng
        AuctionTimer.getInstance().shutdown();
    }

    // ── Test 1: Đóng phiên tự động ────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Schedule: Tự động đóng phiên sau khi hết thời gian")
    void testAutoCloseAuction() throws InterruptedException {
        // Bắt đầu phiên và đưa vào bộ đếm giờ
        testAuction.setStatus(AuctionStatus.RUNNING);
        timer.schedule(testAuction);

        // Đợi 3 giây (nhiều hơn 2 giây của phiên)
        TimeUnit.SECONDS.sleep(3);

        // Kiểm tra xem AuctionTimer đã gọi closeAuction() chưa
        assertEquals(AuctionStatus.FINISHED, testAuction.getStatus(),
                "Phiên đấu giá phải tự động chuyển sang FINISHED sau khi hết giờ");
    }

    // ── Test 2: Hủy lịch (Cancel Task) ───────────────────────────

    @Test
    @Order(2)
    @DisplayName("Cancel: Hủy lịch đóng phiên thành công")
    void testCancelTask() throws InterruptedException {
        testAuction.setStatus(AuctionStatus.RUNNING);
        timer.schedule(testAuction);

        // Hủy ngay lập tức
        timer.cancelTask(auctionId);

        // Đợi đến khi hết giờ (2 giây)
        TimeUnit.SECONDS.sleep(3);

        // Vì đã hủy lịch, phiên không được phép tự đóng (vẫn phải là RUNNING)
        assertNotEquals(AuctionStatus.FINISHED, testAuction.getStatus(),
                "Phiên không được đóng nếu task đã bị hủy");
    }

    // ── Test 3: Lên lịch lại (Reschedule) ──────────────────────────

    @Test
    @Order(3)
    @DisplayName("Reschedule: Lên lịch lại khi thời gian được gia hạn")
    void testRescheduleAuction() throws InterruptedException {
        testAuction.setStatus(AuctionStatus.RUNNING);
        timer.schedule(testAuction);

        // Giả lập gia hạn thêm 2 giây nữa (tổng là 4s từ đầu)
        testAuction.setEndTime(testAuction.getEndTime().plusSeconds(2));
        timer.reschedule(testAuction);

        // Đợi 3 giây (lúc này mốc 2s ban đầu đã qua)
        TimeUnit.SECONDS.sleep(3);

        // Phiên vẫn phải đang chạy vì đã được gia hạn
        assertEquals(AuctionStatus.RUNNING, testAuction.getStatus(),
                "Phiên phải tiếp tục chạy do đã được gia hạn thêm thời gian");

        // Đợi thêm 2 giây nữa để phiên thực sự kết thúc
        TimeUnit.SECONDS.sleep(2);
        assertEquals(AuctionStatus.FINISHED, testAuction.getStatus());
    }

    // ── Test 4: Xử lý phiên đã hết hạn ngay lập tức ───────────────

    @Test
    @Order(4)
    @DisplayName("Schedule: Đóng ngay lập tức nếu phiên đã quá giờ")
    void testSchedulePastAuction() {
        // Thiết lập thời gian kết thúc ở quá khứ
        testAuction.setEndTime(LocalDateTime.now().minusMinutes(1));

        timer.schedule(testAuction);

        assertEquals(AuctionStatus.FINISHED, testAuction.getStatus(),
                "Nếu thời gian kết thúc ở quá khứ, phiên phải đóng ngay lập tức");
    }
}