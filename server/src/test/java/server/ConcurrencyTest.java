package server;

import com.example.exception.AuctionClosedException;
import com.example.exception.DuplicateUsernameException;
import com.example.exception.InvalidBidException;
import com.example.exception.SelfBidException;
import com.example.model.auction.Auction;
import com.example.model.auction.BidTransaction;
import com.example.model.enums.AuctionStatus;
import com.example.model.item.Item;
import com.example.model.user.Bidder;
import org.junit.jupiter.api.*;
import com.example.service.AuctionTimer;
import com.example.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConcurrencyTest — kiểm tra hệ thống dưới tải 50 người dùng đồng thời.
 * <p>
 * Mỗi test giả lập đúng hành vi của 50 client thật:
 * - Tạo 50 thread độc lập (1 thread = 1 user)
 * - Tất cả bắt đầu cùng lúc nhờ CountDownLatch (startGun)
 * - Chờ tất cả hoàn thành nhờ CountDownLatch (doneLatch)
 * - Kiểm tra tính đúng đắn của kết quả cuối
 * <p>
 * Các tình huống được kiểm tra:
 * 1. 50 bidder đặt giá đồng thời → chỉ 1 người thắng hợp lệ
 * 2. 50 user đăng ký đồng thời  → không có trùng ID
 * 3. Race condition: bid + closeAuction cùng lúc → không corrupt dữ liệu
 * 4. 50 bidder trên 5 phiên song song → mỗi phiên độc lập
 */
@DisplayName("Concurrency Test — 50 Users Simultaneously")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConcurrencyTest {

    private static final int NUM_USERS = 50;
    private static final int TIMEOUT_SEC = 15; // timeout tối đa mỗi test

    @AfterAll
    static void globalTearDown() {
        AuctionTimer.getInstance().shutdown();
    }

    // ── Helper: tạo Auction đang RUNNING ──────────────────────────
    private Auction makeRunningAuction(String id, double startPrice, double minIncrement) {
        Item item = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Test " + id, id + "-ITEM",
                "Mô tả", startPrice, Item.ItemStatus.APPROVED);
        Auction a = new Auction(id, item, startPrice, minIncrement,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1));
        a.setStatus(AuctionStatus.APPROVED);
        a.startAuction();
        return a;
    }

    // ════════════════════════════════════════════════════════════════
    // TEST 1: 50 Bidder đặt giá đồng thời trên 1 phiên
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(1)
    @DisplayName("50 bidder đặt giá cùng lúc → Giá cuối hợp lệ, không race condition")
    void testConcurrentBidding() throws InterruptedException {
        Auction auction = makeRunningAuction("CONC-AUC-01", 1_000_000.0, 100_000.0);

        CountDownLatch startGun = new CountDownLatch(1);   // súng lệnh: tất cả cùng bắt đầu
        CountDownLatch doneLatch = new CountDownLatch(NUM_USERS); // chờ tất cả xong

        AtomicInteger successCount = new AtomicInteger(0);  // số lần đặt thành công
        AtomicInteger failCount = new AtomicInteger(0);  // số lần thất bại (hợp lệ)
        List<String> errors = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(NUM_USERS);

        for (int i = 0; i < NUM_USERS; i++) {
            final String bidderId = "BIDDER-" + String.format("%02d", i);
            // Mỗi bidder đặt giá cao dần để tránh InvalidBidException quá nhiều
            final double bidAmount = 1_000_000.0 + (i + 1) * 100_000.0;

            pool.submit(() -> {
                try {
                    startGun.await(); // chờ súng lệnh — đảm bảo TẤT CẢ bắt đầu cùng lúc
                    BidTransaction bid = new BidTransaction(bidderId, "CONC-AUC-01", bidAmount);
                    auction.handleNewBid(bid);
                    successCount.incrementAndGet();
                } catch (InvalidBidException | SelfBidException | AuctionClosedException e) {
                    failCount.incrementAndGet(); // lỗi nghiệp vụ hợp lệ — không phải bug
                } catch (Exception e) {
                    errors.add("BUG [" + bidderId + "]: " + e.getClass().getSimpleName()
                            + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGun.countDown(); // BẮT ĐẦU! Tất cả 50 thread chạy cùng lúc
        boolean finished = doneLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        pool.shutdown();

        // ── Kiểm tra kết quả ──────────────────────────────────────
        assertTrue(finished, "Timeout! " + NUM_USERS + " bidder không hoàn thành trong "
                + TIMEOUT_SEC + "s — có thể deadlock");

        assertTrue(errors.isEmpty(),
                "Phát hiện BUG concurrency:\n" + String.join("\n", errors));

        // Giá cuối PHẢI lớn hơn giá khởi điểm
        assertTrue(auction.getCurrentPrice() > 1_000_000.0,
                "Giá cuối phải lớn hơn giá khởi điểm sau khi có bid thành công");

        // Tổng bid = thành công + thất bại (nghiệp vụ) = NUM_USERS (không mất bid nào)
        assertEquals(NUM_USERS, successCount.get() + failCount.get(),
                "Tổng số bid phải bằng NUM_USERS — không được mất request nào");

        System.out.println("[TEST 1] Thành công: " + successCount.get()
                + " | Thất bại hợp lệ: " + failCount.get()
                + " | Giá cuối: " + auction.getCurrentPrice()
                + " | Winner: " + auction.getCurrentWinner());
    }

    // ════════════════════════════════════════════════════════════════
    // TEST 2: 50 User đăng ký cùng lúc → Không trùng ID, không corrupt
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(2)
    @DisplayName("50 user đăng ký đồng thời → Không trùng ID, không mất dữ liệu")
    void testConcurrentRegistration() throws InterruptedException {
        UserService service = UserService.getInstance();

        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_USERS);

        // Dùng ConcurrentHashMap để đếm ID đã tạo một cách thread-safe
        Set<String> registeredIds = ConcurrentHashMap.newKeySet();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger dupCount = new AtomicInteger(0);
        List<String> errors = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(NUM_USERS);

        for (int i = 0; i < NUM_USERS; i++) {
            final String userId = "CONC-USER-" + String.format("%03d", i);
            final String userName = "ConcUser_" + i; // tên duy nhất cho mỗi user

            pool.submit(() -> {
                try {
                    startGun.await();
                    Bidder b = service.registerBidder(userId, userName, "pass123");
                    boolean added = registeredIds.add(b.getId());
                    if (!added) {
                        errors.add("DUPLICATE ID: " + b.getId());
                    }
                    successCount.incrementAndGet();
                } catch (DuplicateUsernameException e) {
                    dupCount.incrementAndGet(); // trùng tên — hợp lệ
                } catch (Exception e) {
                    errors.add("BUG: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGun.countDown();
        boolean finished = doneLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        pool.shutdown();

        assertTrue(finished, "Timeout! Đăng ký đồng thời bị treo");
        assertTrue(errors.isEmpty(),
                "Phát hiện BUG:\n" + String.join("\n", errors));
        assertEquals(successCount.get(), registeredIds.size(),
                "Số ID thực tế phải bằng số đăng ký thành công — không được trùng ID");

        System.out.println("[TEST 2] Đăng ký thành công: " + successCount.get()
                + " | Trùng tên (hợp lệ): " + dupCount.get());
    }

    // ════════════════════════════════════════════════════════════════
    // TEST 3: Race condition — bid và closeAuction xảy ra đồng thời
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(3)
    @DisplayName("Race condition: bid + closeAuction cùng lúc → Không corrupt dữ liệu")
    void testBidVsCloseRaceCondition() throws InterruptedException {
        Auction auction = makeRunningAuction("CONC-AUC-RACE", 500_000.0, 50_000.0);

        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_USERS + 1); // +1 cho closeAuction thread

        AtomicInteger bidAfterClose = new AtomicInteger(0); // bid sau khi phiên đóng
        List<String> errors = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(NUM_USERS + 1);

        // 1 thread đóng phiên sau 50ms
        pool.submit(() -> {
            try {
                startGun.await();
                Thread.sleep(50); // chờ một chút để bid thread kịp chạy
                auction.closeAuction();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });

        // 50 thread đặt giá liên tục
        for (int i = 0; i < NUM_USERS; i++) {
            final String bidderId = "RACE-BIDDER-" + i;
            final double amount = 500_000.0 + (i + 1) * 50_000.0;

            pool.submit(() -> {
                try {
                    startGun.await();
                    BidTransaction bid = new BidTransaction(bidderId, "CONC-AUC-RACE", amount);
                    auction.handleNewBid(bid);
                } catch (AuctionClosedException e) {
                    bidAfterClose.incrementAndGet(); // đúng: bid khi phiên đã đóng
                } catch (InvalidBidException | SelfBidException e) {
                    // hợp lệ
                } catch (Exception e) {
                    errors.add("BUG: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGun.countDown();
        boolean finished = doneLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        pool.shutdown();

        assertTrue(finished, "Timeout! Race condition gây deadlock");
        assertTrue(errors.isEmpty(),
                "BUG phát hiện khi bid + close đồng thời:\n" + String.join("\n", errors));

        // Sau khi close, giá và winner phải ở trạng thái nhất quán (không null/NaN)
        assertEquals(AuctionStatus.FINISHED, auction.getStatus(),
                "Phiên phải kết thúc ở trạng thái FINISHED");
        assertTrue(auction.getCurrentPrice() >= 500_000.0,
                "Giá cuối không được bị corrupt về giá trị bất hợp lệ");

        System.out.println("[TEST 3] Bid bị từ chối sau khi close: " + bidAfterClose.get()
                + " | Giá cuối: " + auction.getCurrentPrice()
                + " | Winner: " + auction.getCurrentWinner());
    }

    // ════════════════════════════════════════════════════════════════
    // TEST 4: 50 bidder trên 5 phiên song song — tải đúng như production
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(4)
    @DisplayName("50 bidder phân bổ vào 5 phiên song song → Mỗi phiên hoạt động độc lập")
    void testMultipleAuctionsConcurrently() throws InterruptedException {
        final int NUM_AUCTIONS = 5;
        final int BIDDERS_EACH = NUM_USERS / NUM_AUCTIONS; // 10 bidder/phiên

        // Tạo 5 phiên đang chạy
        Auction[] auctions = new Auction[NUM_AUCTIONS];
        for (int i = 0; i < NUM_AUCTIONS; i++) {
            auctions[i] = makeRunningAuction(
                    "MULTI-AUC-0" + i, 1_000_000.0, 100_000.0);
        }

        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_USERS);

        AtomicInteger totalSuccess = new AtomicInteger(0);
        List<String> errors = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(NUM_USERS);

        for (int i = 0; i < NUM_USERS; i++) {
            final int auctionIdx = i % NUM_AUCTIONS; // phân bổ đều vào 5 phiên
            final String bidderId = "MULTI-BIDDER-" + i;
            final double amount = 1_000_000.0 + (i + 1) * 100_000.0;

            pool.submit(() -> {
                try {
                    startGun.await();
                    BidTransaction bid = new BidTransaction(
                            bidderId, auctions[auctionIdx].getAuctionId(), amount);
                    auctions[auctionIdx].handleNewBid(bid);
                    totalSuccess.incrementAndGet();
                } catch (InvalidBidException | SelfBidException | AuctionClosedException e) {
                    // hợp lệ
                } catch (Exception e) {
                    errors.add("BUG [phiên " + auctionIdx + "]: "
                            + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGun.countDown();
        boolean finished = doneLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        pool.shutdown();

        assertTrue(finished, "Timeout! 5 phiên song song bị treo");
        assertTrue(errors.isEmpty(),
                "BUG trong multi-auction concurrency:\n" + String.join("\n", errors));

        // Mỗi phiên phải có giá cuối hợp lệ và độc lập nhau
        for (int i = 0; i < NUM_AUCTIONS; i++) {
            assertEquals(AuctionStatus.RUNNING, auctions[i].getStatus(),
                    "Phiên " + i + " phải vẫn RUNNING");
            assertTrue(auctions[i].getCurrentPrice() >= 1_000_000.0,
                    "Phiên " + i + " có giá không hợp lệ: " + auctions[i].getCurrentPrice());
            System.out.println("[TEST 4] Phiên " + i
                    + " | Giá: " + auctions[i].getCurrentPrice()
                    + " | Winner: " + auctions[i].getCurrentWinner());
        }

        System.out.println("[TEST 4] Tổng bid thành công trên 5 phiên: " + totalSuccess.get());
    }

    // ════════════════════════════════════════════════════════════════
    // TEST 5: Đo throughput — hệ thống xử lý được bao nhiêu bid/giây
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(5)
    @DisplayName("Throughput: Đo tốc độ xử lý bid của hệ thống")
    void testBiddingThroughput() throws InterruptedException {
        Auction auction = makeRunningAuction("THROUGHPUT-AUC", 1_000.0, 1.0);

        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_USERS);
        AtomicInteger processed = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(NUM_USERS);

        for (int i = 0; i < NUM_USERS; i++) {
            final String bidderId = "PERF-BIDDER-" + i;
            final double amount = 1_000.0 + (i + 1);

            pool.submit(() -> {
                try {
                    startGun.await();
                    BidTransaction bid = new BidTransaction(bidderId, "THROUGHPUT-AUC", amount);
                    auction.handleNewBid(bid);
                    processed.incrementAndGet();
                } catch (Exception e) {
                    processed.incrementAndGet(); // đếm cả lỗi nghiệp vụ (vẫn được xử lý)
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startGun.countDown();
        boolean finished = doneLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - startTime;
        pool.shutdown();

        assertTrue(finished, "Timeout throughput test");
        assertEquals(NUM_USERS, processed.get(), "Tất cả request phải được xử lý");

        double throughput = (double) NUM_USERS / elapsed * 1000; // bid/giây
        System.out.println("[TEST 5] " + NUM_USERS + " requests xử lý trong " + elapsed + "ms"
                + " → Throughput: " + String.format("%.1f", throughput) + " requests/giây");

        // Ngưỡng tối thiểu: phải xử lý ít nhất 10 requests/giây
        assertTrue(throughput >= 10,
                "Throughput quá thấp: " + String.format("%.1f", throughput) + " req/s "
                        + "(tối thiểu cần 10 req/s)");
    }
}