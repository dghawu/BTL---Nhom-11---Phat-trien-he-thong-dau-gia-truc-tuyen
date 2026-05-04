package observer;

import model.auction.Auction;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SocketBroadcaster lắng nghe bid mới và broadcast đến tất cả client đang xem phiên đấu giá qua Socket.
 * Mỗi phiên đấu giá có 1 SocketBroadcaster riêng.
 * Client kết nối vào → được thêm vào danh sách subscribers.
 * Khi có bid mới → tất cả subscribers nhận được thông báo ngay lập tức.
 */
public class SocketBroadcaster implements AuctionObserver {

    // Dùng ConcurrentHashMap để thread-safe khi nhiều client connect/disconnect đồng thời
    private final Set<PrintWriter> subscribers = ConcurrentHashMap.newKeySet();
    private final String auctionId;

    public SocketBroadcaster(String auctionId) {
        this.auctionId = auctionId;
    }

    // Quản lý subscribers
    /**
     * Thêm client vào danh sách nhận thông báo.
     * Gọi khi client gửi lệnh "WATCH:AUC-001" đến server.
     */
    public void addSubscriber(Socket clientSocket) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            subscribers.add(writer);
            System.out.println("[SocketBroadcaster] Client kết nối vào phiên: " + auctionId
                    + " | Tổng: " + subscribers.size());
        } catch (Exception e) {
            System.out.println("[SocketBroadcaster] Lỗi thêm subscriber: " + e.getMessage());
        }
    }

    /**
     * Xóa client khỏi danh sách (khi client ngắt kết nối).
     */
    public void removeSubscriber(PrintWriter writer) {
        subscribers.remove(writer);
        System.out.println("[SocketBroadcaster] Client rời phiên: " + auctionId
                + " | Còn lại: " + subscribers.size());
    }

    //Observer update
    /**
     * Khi có bid mới → broadcast đến tất cả client đang xem phiên này.
     * Message format: "BID_UPDATE:AUC-001:15500000.0:B01"
     */
    @Override
    public void update(Auction auction, double newPrice, String lastBidderId) {
        String message = buildMessage(auction, newPrice, lastBidderId);
        broadcast(message);
    }

    //Broadcast
    /**
     * Gửi message đến tất cả client.
     * Tự động xóa client bị ngắt kết nối.
     */
    public void broadcast(String message) {
        Set<PrintWriter> disconnected = ConcurrentHashMap.newKeySet();

        for (PrintWriter writer : subscribers) {
            try {
                writer.println(message);
                if (writer.checkError()) {
                    // Client đã ngắt kết nối
                    disconnected.add(writer);
                }
            } catch (Exception e) {
                disconnected.add(writer);
            }
        }

        // Dọn dẹp các client đã ngắt kết nối
        subscribers.removeAll(disconnected);

        System.out.println("[SocketBroadcaster] Broadcast đến "
                + subscribers.size() + " clients: " + message);
    }

    /**
     * Broadcast khi phiên kết thúc.
     * Message format: "AUCTION_CLOSED:AUC-001:WINNER:B01:15500000.0"
     */
    public void broadcastClose(String auctionId, String winnerId, double finalPrice) {
        String message = "AUCTION_CLOSED:" + auctionId
                + ":" + (winnerId != null ? winnerId : "NO_WINNER")
                + ":" + finalPrice;
        broadcast(message);
    }

    //Helper

    private String buildMessage(Auction auction, double newPrice, String lastBidderId) {
        return "BID_UPDATE"
                + ":" + auction.getAuctionId()
                + ":" + newPrice
                + ":" + lastBidderId
                + ":" + auction.getEndTime(); // gửi kèm endTime để client cập nhật countdown
    }

    public int getSubscriberCount() { return subscribers.size(); }
    public String getAuctionId()    { return auctionId; }
}