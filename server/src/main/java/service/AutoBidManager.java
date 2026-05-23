package service;

import model.auction.AutoBidConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidManager {

    private static volatile AutoBidManager instance;
    // sessionId → list config của các bidder đã set auto
    private final Map<String, List<AutoBidConfig>> configs = new ConcurrentHashMap<>();

    private AutoBidManager() {
    }

    public static AutoBidManager getInstance() {
        if (instance == null) {
            synchronized (AutoBidManager.class) {
                if (instance == null) instance = new AutoBidManager();
            }
        }
        return instance;
    }

    /**
     * Bidder đăng ký auto-bid cho một phiên
     */
    public void register(String sessionId, AutoBidConfig config) {
        configs.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()))
                .removeIf(c -> c.getBidderId().equals(config.getBidderId())); // xóa config cũ nếu có
        configs.get(sessionId).add(config);
        System.out.println("[AutoBid] Đăng ký: " + config.getBidderName()
                + " | max=" + config.getMaxBid() + " | step=" + config.getIncrement());
    }

    /**
     * Hủy auto-bid của một bidder trong phiên
     */
    public void unregister(String sessionId, String bidderId) {
        List<AutoBidConfig> list = configs.get(sessionId);
        if (list != null) list.removeIf(c -> c.getBidderId().equals(bidderId));
    }

    /**
     * Lấy danh sách auto-bid của phiên
     */
    public List<AutoBidConfig> getConfigs(String sessionId) {
        return configs.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * Xóa toàn bộ auto-bid của phiên khi phiên kết thúc
     */
    public void clear(String sessionId) {
        configs.remove(sessionId);
    }
}