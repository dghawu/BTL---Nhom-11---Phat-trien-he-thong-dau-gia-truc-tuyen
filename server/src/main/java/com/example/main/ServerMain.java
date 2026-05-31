package com.example.main;

import com.example.server.BidPushServer;
import com.example.server.SocketServer;
import dao.AuctionDAO;
import model.auction.Auction;
import model.enums.AuctionStatus;
import service.AuctionTimer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ServerMain - khởi động cả 2 server:
 * - SocketServer  (port 8888): API request/response (login, bid, ...)
 * - BidPushServer (port 8889): Push realtime (BID_UPDATE, AUCTION_CLOSED)
 * <p>
 * Chạy file này TRƯỚC khi chạy client.
 */
public final class ServerMain {

    /** Port lắng nghe API request/response. */
    private static final int API_PORT = 8888;

    /** Port lắng nghe Push realtime. */
    private static final int PUSH_PORT = 8889;

    private ServerMain() {
    }

    /**
     * Điểm vào của ứng dụng server.
     */
    public static void main(final String[] args) {
        System.out.println("=================================");
        System.out.println("   AUCTION SYSTEM - SERVER");
        System.out.println("=================================");

        SocketServer apiServer = new SocketServer(API_PORT);
        BidPushServer pushServer = new BidPushServer(PUSH_PORT);

        recoverRunningAuctions();

        // BidPushServer chạy trong background thread riêng
        pushServer.startInBackground();

        // Xử lý Ctrl+C để tắt cả 2 server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Đang tắt...");
            pushServer.stop();
            apiServer.stop();
        }));

        // SocketServer chạy blocking ở main thread
        apiServer.start();
    }

    /**
     * Tải lại các phiên đang chạy sau khi server khởi động lại.
     */
    private static void recoverRunningAuctions() {
        AuctionDAO auctionDAO = new AuctionDAO();
        for (Auction a : auctionDAO.findAll()) {
            if (a.getStatus() != AuctionStatus.RUNNING) {
                continue;
            }
            // Nếu đã hết giờ → schedule() sẽ đóng ngay + update DB
            // Nếu chưa hết giờ → schedule() lên lịch timer bình thường
            AuctionTimer.getInstance().schedule(a);
            if (a.getStatus() == AuctionStatus.APPROVED) {
                long delay = ChronoUnit.SECONDS.between(
                        LocalDateTime.now(),
                        a.getStartTime());
                if (delay <= 0) {
                    auctionDAO.updateStatus(
                            a.getAuctionId(),
                            AuctionStatus.RUNNING);
                    a.setStatus(AuctionStatus.RUNNING);
                    AuctionTimer.getInstance().schedule(a);
                } else {
                    // schedule chuyển RUNNING khi đến giờ
                    Executors.newSingleThreadScheduledExecutor()
                            .schedule(
                                    () -> {
                                        AuctionTimer.getInstance().schedule(a);
                                        auctionDAO.updateStatus(
                                                a.getAuctionId(),
                                                AuctionStatus.RUNNING);
                                    },
                                    delay,
                                    TimeUnit.SECONDS);
                }
            }
        }
    }
}