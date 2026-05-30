package com.example.server.handler;

import auth.AuthResult;
import auth.TokenGuard;
import com.example.server.BidRegistry;
import model.auction.Auction;
import model.auction.AutoBidConfig;
import model.auction.BidTransaction;
import model.enums.AuctionStatus;
import model.user.User;
import observer.SocketBroadcaster;
import org.json.JSONArray;
import org.json.JSONObject;
import service.AutoBidManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * BidHandler — đặt giá, auto-bid, lịch sử bid.
 */
public class BidHandler extends BaseHandler {

    private static final ScheduledExecutorService AUTO_BID_SCHEDULER =
            Executors.newScheduledThreadPool(4);
    private static final long AUTO_BID_DELAY_MS = 1000;

    @Override
    public String handle(JSONObject req) {
        return switch (req.getString("action")) {
            case "placeBid"          -> handlePlaceBid(req);
            case "setAutoBid"        -> handleSetAutoBid(req);
            case "getBidHistory"     -> handleGetBidHistory(req);
            case "cancelAutoBid"     -> handleCancelAutoBid(req);
            case "getAutoBidStatus"  -> handleGetAutoBidStatus(req);
            default                  -> fail("Action không hợp lệ.");
        };
    }

    private String handlePlaceBid(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String sessionId = req.getString("sessionId");
        String bidderId  = auth.getUserId();
        double bidAmount = req.getDouble("bidAmount");

        Auction auction = auctionDAO.findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên.");
        if (auction.getStatus() != AuctionStatus.RUNNING)
            return fail("Phiên đấu giá không còn hoạt động.");
        if (auction.getEndTime().isBefore(LocalDateTime.now()))
            return fail("Phiên đấu giá đã kết thúc.");

        User bidder = userDAO.findById(bidderId);
        String bidderName = bidder != null ? bidder.getName() : bidderId;

        if (bidderName.equals(auction.getCurrentWinner()))
            return fail("Bạn đang là người dẫn đầu, không thể tự đặt giá lại.");

        BidTransaction bid = new BidTransaction(bidderId, sessionId, bidAmount);
        bid.setBidderName(bidderName);

        LocalDateTime endTimeBefore = auction.getEndTime();
        try {
            auction.handleNewBid(bid);
        } catch (Exception e) {
            return fail(e.getMessage());
        }

        boolean antiSniped = auction.getEndTime().isAfter(endTimeBefore);
        bidDAO.save(bid);
        auctionDAO.updateBid(sessionId, auction.getCurrentPrice(), bidderName);
        auctionDAO.updateEndTime(sessionId, auction.getEndTime());

        broadcast(sessionId, auction, bidderName, antiSniped, endTimeBefore);

        try { triggerAutoBid(sessionId, bidderId); }
        catch (Exception e) { System.out.println("[AutoBid] Trigger error: " + e.getMessage()); }

        return success()
                .put("currentPrice",  auction.getCurrentPrice())
                .put("currentWinner", bidderName)
                .toString();
    }

    private String handleSetAutoBid(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String sessionId = req.getString("sessionId");
        String bidderId  = auth.getUserId();
        double increment = req.getDouble("stepPrice");
        double maxBid    = req.getDouble("maxPrice");

        Auction auction = auctionDAO.findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên.");
        if (auction.getStatus() != AuctionStatus.RUNNING)
            return fail("Phiên không còn hoạt động.");
        if (maxBid <= auction.getCurrentPrice())
            return fail("Giá tối đa phải cao hơn giá hiện tại.");

        User bidder = userDAO.findById(bidderId);
        String bidderName = bidder != null ? bidder.getName() : bidderId;

        AutoBidConfig config = new AutoBidConfig(bidderId, bidderName, increment, maxBid);
        AutoBidManager.getInstance().register(sessionId, config);

        String currentWinner = auction.getCurrentWinner();
        boolean isSelf = currentWinner != null
                && (bidderName.equals(currentWinner) || bidderId.equals(currentWinner));
        if (!isSelf) {
            try {
                String currentWinnerId = "";
                if (currentWinner != null && !currentWinner.isEmpty()) {
                    User winnerUser = userDAO.findByName(currentWinner);
                    currentWinnerId = winnerUser != null ? winnerUser.getId() : currentWinner;
                }
                triggerAutoBid(sessionId, currentWinnerId);
            } catch (Exception e) {
                System.out.println("[AutoBid] Trigger khi register lỗi: " + e.getMessage());
            }
        }
        return success().put("message", "Đã bật auto-bid!").toString();
    }

    private String handleGetBidHistory(JSONObject req) {
        AuthResult auth = TokenGuard.check(req);
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String sessionId = req.getString("sessionId");
        List<BidTransaction> txList = bidDAO.findByAuctionId(sessionId);
        JSONArray arr = new JSONArray();
        for (BidTransaction tx : txList) {
            User bidder = userDAO.findById(tx.getBidderId());
            arr.put(new JSONObject()
                    .put("bidderName", bidder != null ? bidder.getName() : tx.getBidderId())
                    .put("amount",     tx.getAmount())
                    .put("timestamp",  tx.getTimestamp().toString()));
        }
        return success().put("history", arr).toString();
    }

    private String handleCancelAutoBid(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        AutoBidManager.getInstance().unregister(req.getString("sessionId"), auth.getUserId());
        return success().toString();
    }

    private String handleGetAutoBidStatus(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String sessionId = req.getString("sessionId");
        String bidderId  = auth.getUserId();

        AutoBidConfig config = AutoBidManager.getInstance().getConfigs(sessionId)
                .stream()
                .filter(c -> c.getBidderId().equals(bidderId))
                .findFirst()
                .orElse(null);

        if (config == null) return success().put("active", false).toString();
        return success()
                .put("active",    true)
                .put("increment", config.getIncrement())
                .put("maxBid",    config.getMaxBid())
                .toString();
    }

    // ── Private helpers ─────────────────────────────────────────────── //

    private void broadcast(String sessionId, Auction auction,
                           String bidderName, boolean antiSniped,
                           LocalDateTime endTimeBefore) {
        SocketBroadcaster broadcaster = BidRegistry.getInstance().get(sessionId);
        if (broadcaster == null) {
            System.out.println("[BidHandler] Không có client nào watch phiên " + sessionId);
            return;
        }
        String msg = "BID_UPDATE:" + sessionId
                + ":" + auction.getCurrentPrice()
                + ":" + bidderName
                + ":" + auction.getEndTime();
        broadcaster.broadcast(msg);

        if (antiSniped) {
            long extendedMinutes = java.time.temporal.ChronoUnit.MINUTES
                    .between(endTimeBefore, auction.getEndTime());
            broadcaster.broadcast("ANTI_SNIPE|" + sessionId + "|" + bidderName
                    + "|" + endTimeBefore + "|" + extendedMinutes);
        }
    }

    /**
     * triggerAutoBid — chuyển từ ClientHandler sang đây.
     * Logic giữ nguyên, chỉ tách ra để ClientHandler không ôm thêm trách nhiệm.
     */
    private void triggerAutoBid(String sessionId, String lastWinnerId) {
        List<AutoBidConfig> autoBids = new java.util.ArrayList<>(
                AutoBidManager.getInstance().getConfigs(sessionId));
        if (autoBids.isEmpty()) return;

        Auction auction = auctionDAO.findById(sessionId);
        if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) return;
        if (auction.getEndTime().isBefore(LocalDateTime.now())) return;

        for (AutoBidConfig cfg : autoBids) {
            if (cfg.getBidderId().equals(lastWinnerId)) continue;

            boolean stillActive = AutoBidManager.getInstance()
                    .getConfigs(sessionId).stream()
                    .anyMatch(c -> c.getBidderId().equals(cfg.getBidderId()));
            if (!stillActive) continue;

            double nextBid = auction.getCurrentPrice() + cfg.getIncrement();
            if (nextBid > cfg.getMaxBid()) {
                System.out.println("[AutoBid] " + cfg.getBidderName() + " đạt maxBid, dừng.");
                AutoBidManager.getInstance().unregister(sessionId, cfg.getBidderId());
                continue;
            }

            BidTransaction bid = new BidTransaction(cfg.getBidderId(), sessionId, nextBid);
            bid.setBidderName(cfg.getBidderName());

            try {
                auction.handleNewBid(bid);
                bidDAO.save(bid);
                auctionDAO.updateBid(sessionId, auction.getCurrentPrice(), cfg.getBidderName());
                auctionDAO.updateEndTime(sessionId, auction.getEndTime());

                SocketBroadcaster broadcaster = BidRegistry.getInstance().get(sessionId);
                if (broadcaster != null) {
                    broadcaster.broadcast("BID_UPDATE:" + sessionId
                            + ":" + auction.getCurrentPrice()
                            + ":" + cfg.getBidderName()
                            + ":" + auction.getEndTime());
                }
                System.out.println("[AutoBid] " + cfg.getBidderName() + " tự đặt " + nextBid);

                final String nextLastWinnerId = cfg.getBidderId();
                AUTO_BID_SCHEDULER.schedule(
                        () -> triggerAutoBid(sessionId, nextLastWinnerId),
                        AUTO_BID_DELAY_MS, TimeUnit.MILLISECONDS
                );
                return;
            } catch (Exception e) {
                System.out.println("[AutoBid] Lỗi: " + e.getMessage());
            }
        }
    }
}