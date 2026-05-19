package service;

import model.auction.Auction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.*;
import com.example.server.BidRegistry;
import dao.AuctionDAO;
import model.enums.AuctionStatus;
import observer.SocketBroadcaster;

/**
 * AuctionTimer quản lý việc tự động đóng phiên đấu giá khi hết thời gian.
 * Dùng ScheduledExecutorService để chạy ngầm, không block luồng chính.
 * <p>
 * Lưu ý: scheduler được tự động khởi động lại nếu đã bị shutdown,
 * đảm bảo hoạt động đúng cả trong môi trường production lẫn unit test.
 */
public class AuctionTimer {
    private AuctionDAO getAuctionDAO() {
        return new AuctionDAO();
    }

    // Singleton
    private static volatile AuctionTimer instance;

    // Thread pool — dùng volatile để đảm bảo visibility khi restart
    private volatile ScheduledExecutorService scheduler;

    // Lưu task của từng phiên để có thể hủy nếu cần
    private final Map<String, ScheduledFuture<?>> scheduledTasks;

    private AuctionTimer() {
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.scheduledTasks = new ConcurrentHashMap<>();
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
     * Đảm bảo scheduler đang hoạt động.
     * Tự động tạo lại nếu đã bị shutdown (hữu ích trong môi trường test).
     */
    private synchronized ScheduledExecutorService getActiveScheduler() {
        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            System.out.println("[AuctionTimer] Scheduler đã dừng, khởi động lại...");
            scheduler = Executors.newScheduledThreadPool(4);
        }
        return scheduler;
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
            scheduledTasks.remove(auctionId);
            auction.closeAuction();
            AuctionDAO dao = getAuctionDAO();
            dao.updateStatus(auctionId, AuctionStatus.FINISHED);
            Auction fresh = dao.findById(auctionId);
            String winner = fresh != null ? fresh.getCurrentWinner() : auction.getCurrentWinner();
            double finalPrice = fresh != null ? fresh.getCurrentPrice() : auction.getCurrentPrice();

            SocketBroadcaster broadcaster = BidRegistry.getInstance().get(auctionId);
            if (broadcaster != null) {
                broadcaster.broadcastClose(auctionId, winner, finalPrice);
                BidRegistry.getInstance().remove(auctionId);
            }
            return;
        }

        System.out.println("[AuctionTimer] Lên lịch đóng phiên "
                + auctionId + " sau " + delaySeconds + " giây.");

        ScheduledFuture<?> task = getActiveScheduler().schedule(() -> {
            if (LocalDateTime.now().isBefore(auction.getEndTime())) {
                reschedule(auction);
            } else {
                scheduledTasks.remove(auctionId);
                auction.closeAuction();
                AuctionDAO dao = getAuctionDAO();
                dao.updateStatus(auctionId, AuctionStatus.FINISHED);
                Auction fresh = dao.findById(auctionId);
                String winner = fresh != null ? fresh.getCurrentWinner() : auction.getCurrentWinner();
                double finalPrice = fresh != null ? fresh.getCurrentPrice() : auction.getCurrentPrice();

                SocketBroadcaster broadcaster = BidRegistry.getInstance().get(auctionId);
                if (broadcaster != null) {
                    broadcaster.broadcastClose(auctionId, winner, finalPrice);
                    BidRegistry.getInstance().remove(auctionId);
                }
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
     * Sau khi gọi, getInstance().schedule() vẫn hoạt động vì scheduler tự restart.
     */
    public void shutdown() {
        scheduler.shutdown();
        System.out.println("[AuctionTimer] Scheduler đã dừng.");
    }
}
