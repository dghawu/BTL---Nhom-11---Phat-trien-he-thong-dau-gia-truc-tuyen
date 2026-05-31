package com.example.server;

import observer.SocketBroadcaster;

import java.util.concurrent.ConcurrentHashMap;

/**
 * BidRegistry — Singleton giữ map auctionId → SocketBroadcaster.
 * <p>
 * Tại sao cần class này?
 * - ClientHandler (port 8888) xử lý placeBid → cần biết
 *   Broadcaster của phiên để push.
 * - BidPushHandler (port 8889) đăng ký client
 *   vào Broadcaster của phiên.
 * → Hai handler chạy trên 2 cổng khác nhau,
 *   cần 1 nơi chung để chia sẻ Broadcaster.
 * <p>
 * Luồng sử dụng:
 * 1. Client A kết nối port 8889, gửi:
 *    {"action":"joinSession","sessionId":"AUC-001","token":"..."}
 * → BidPushHandler gọi BidRegistry.getOrCreate("AUC-001").addSubscriber(socket)
 * <p>
 * 2. Client B gửi placeBid lên port 8888
 * → ClientHandler gọi BidRegistry.get("AUC-001")
 *     .broadcast("BID_UPDATE:...")
 * <p>
 * 3. Phiên kết thúc
 * → ClientHandler/AuctionTimer gọi
 *     BidRegistry.get("AUC-001").broadcastClose(...)
 * → BidRegistry.remove("AUC-001")
 */
public final class BidRegistry {

    private static final BidRegistry INSTANCE = new BidRegistry();

    /**
     * auctionId → broadcaster của phiên đó.
     */
    private final ConcurrentHashMap<String, SocketBroadcaster> map =
            new ConcurrentHashMap<>();

    private BidRegistry() {
    }

    public static BidRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Lấy Broadcaster của phiên, tạo mới nếu chưa có.
     * Gọi từ BidPushHandler khi client joinSession.
     *
     * @param auctionId id phiên đấu giá
     * @return Broadcaster của phiên tương ứng
     */
    public SocketBroadcaster getOrCreate(final String auctionId) {
        return map.computeIfAbsent(auctionId, SocketBroadcaster::new);
    }

    /**
     * Lấy Broadcaster đã tồn tại.
     * Gọi từ ClientHandler sau khi placeBid thành công.
     * Trả về null nếu chưa có client nào join phiên.
     *
     * @param auctionId id phiên đấu giá
     * @return Broadcaster hoặc null nếu chưa có
     */
    public SocketBroadcaster get(final String auctionId) {
        return map.get(auctionId);
    }

    /**
     * Xóa Broadcaster khi phiên kết thúc.
     *
     * @param auctionId id phiên đấu giá
     */
    public void remove(final String auctionId) {
        map.remove(auctionId);
        String logMessage = "[BidRegistry] Đã xóa broadcaster của phiên: "
                + auctionId;
        System.out.println(logMessage);
    }

    public int size() {
        return map.size();
    }
}