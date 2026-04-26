package service;

import model.auction.Auction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * AuctionTimer quản lý việc tự động đóng phiên đấu giá khi hết thời gian.
 * Dùng ScheduledExecutorService để chạy ngầm, không block luồng chính.
 */
public class AuctionTimer {

    // Singleton
    private static volatile AuctionTimer instance;

    // Thread pool — mỗi phiên đấu giá 1 task lên lịch
    private final ScheduledExecutorService scheduler;

    // Lưu task của từng phiên để có thể hủy nếu cần
    private final Map<String, ScheduledFuture<?>> scheduledTasks;

    private AuctionTimer() {
        // 4 luồng xử lý song song — đủ cho demo, tăng lên khi cần
        this.scheduler       = Executors.newScheduledThreadPool(4);
        this.scheduledTasks  = new ConcurrentHashMap<>();
    }

    public static AuctionTimer getInstance() {
        if (instance == null) {
            synchronized (AuctionTimer.class) {
                if (instance == null) {
                    instance = new AuctionTimer();
                }
            }
        }
        return instance;
    }

    /**
     * Đăng ký phiên đấu giá để tự động đóng khi hết endTime.
     * Gọi hàm này ngay sau khi auction.startAuction().
     *
     * @param auction Phiên cần lên lịch
     */
    public void schedule(Auction auction) {
        String auctionId = auction.getAuctionId();

        // Tính số giây còn lại đến endTime
        long delaySeconds = ChronoUnit.SECONDS.between(
                LocalDateTime.now(), auction.getEndTime());

        if (delaySeconds <= 0) {
            System.out.println("[AuctionTimer] Phiên " + auctionId
                    + " đã hết giờ, đóng ngay.");
            auction.closeAuction();
            return;
        }

        System.out.println("[AuctionTimer] Lên lịch đóng phiên "
                + auctionId + " sau " + delaySeconds + " giây.");

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            // Kiểm tra lại endTime vì anti-sniping có thể đã gia hạn
            if (LocalDateTime.now().isBefore(auction.getEndTime())) {
                // Phiên đã được gia hạn → lên lịch lại
                reschedule(auction);
            } else {
                auction.closeAuction();
                scheduledTasks.remove(auctionId);
                System.out.println("[AuctionTimer] Đã đóng phiên: " + auctionId);
            }
        }, delaySeconds, TimeUnit.SECONDS);

        // Lưu lại task để có thể hủy sau
        scheduledTasks.put(auctionId, task);
    }

    /**
     * Lên lịch lại khi phiên được gia hạn bởi Anti-sniping.
     * Auction.java sẽ gọi hàm này sau khi cập nhật endTime.
     */
    public void reschedule(Auction auction) {
        String auctionId = auction.getAuctionId();

        // Hủy task cũ nếu còn
        cancelTask(auctionId);

        System.out.println("[AuctionTimer] Lên lịch lại phiên: " + auctionId
                + " → kết thúc lúc " + auction.getEndTime());

        // Lên lịch mới
        schedule(auction);
    }

    /**
     * Hủy task của một phiên (dùng khi Admin/Seller hủy phiên thủ công).
     */
    public void cancelTask(String auctionId) {
        ScheduledFuture<?> task = scheduledTasks.get(auctionId);
        if (task != null && !task.isDone()) {
            task.cancel(false); // false = không interrupt nếu đang chạy
            scheduledTasks.remove(auctionId);
            System.out.println("[AuctionTimer] Đã hủy lịch phiên: " + auctionId);
        }
    }

    /**
     * Tắt toàn bộ scheduler khi thoát ứng dụng.
     * Gọi trong Main khi shutdown.
     */
    public void shutdown() {
        scheduler.shutdown();
        System.out.println("[AuctionTimer] Scheduler đã dừng.");
    }
}