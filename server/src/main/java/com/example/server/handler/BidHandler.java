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
    public String handle(final JSONObject req) {
        return switch (req.getString("action")) {
            case "placeBid" -> handlePlaceBid(req);
            case "setAutoBid" -> handleSetAutoBid(req);
            case "getBidHistory" -> handleGetBidHistory(req);
            case "cancelAutoBid" -> handleCancelAutoBid(req);
            case "getAutoBidStatus" -> handleGetAutoBidStatus(req);
            default -> fail("Action không hợp lệ.");
        };
    }

    private String handlePlaceBid(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sessionId = req.getString("sessionId");
        String bidderId = auth.getUserId();
        double bidAmount = req.getDouble("bidAmount");

        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null) {
            return fail("Không tìm thấy phiên.");
        }
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            return fail("Phiên đấu giá không còn hoạt động.");
        }
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            return fail("Phiên đấu giá đã kết thúc.");
        }

        User bidder = getUserDAO().findById(bidderId);
        String bidderName = bidder != null ? bidder.getName() : bidderId;

        if (bidderName.equals(auction.getCurrentWinner())) {
            return fail("Bạn đang là người dẫn đầu, không thể tự đặt giá lại.");
        }

        BidTransaction bid = new BidTransaction(bidderId, sessionId, bidAmount);
        bid.setBidderName(bidderName);

        LocalDateTime endTimeBefore = auction.getEndTime();
        try {
            auction.handleNewBid(bid);
        } catch (Exception e) {
            return fail(e.getMessage());
        }

        boolean antiSniped = auction.getEndTime().isAfter(endTimeBefore);
        getBidDAO().save(bid);
        getAuctionDAO().updateBid(
                sessionId,
                auction.getCurrentPrice(),
                bidderName);
        getAuctionDAO().updateEndTime(sessionId, auction.getEndTime());

        broadcast(sessionId, auction, bidderName, antiSniped, endTimeBefore);

        try {
            triggerAutoBid(sessionId, bidderId);
        } catch (Exception e) {
            System.out.println("[AutoBid] Trigger error: " + e.getMessage());
        }

        return success()
                .put("currentPrice", auction.getCurrentPrice())
                .put("currentWinner", bidderName)
                .toString();
    }

    private String handleSetAutoBid(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sessionId = req.getString("sessionId");
        String bidderId = auth.getUserId();
        double increment = req.getDouble("stepPrice");
        double maxBid = req.getDouble("maxPrice");

        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null) {
            return fail("Không tìm thấy phiên.");
        }
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            return fail("Phiên không còn hoạt động.");
        }
        if (maxBid <= auction.getCurrentPrice()) {
            return fail("Giá tối đa phải cao hơn giá hiện tại.");
        }

        User bidder = getUserDAO().findById(bidderId);
        String bidderName = bidder != null ? bidder.getName() : bidderId;

        AutoBidConfig config = new AutoBidConfig(
                bidderId,
                bidderName,
                increment,
                maxBid);
        AutoBidManager.getInstance().register(sessionId, config);

        String currentWinner = auction.getCurrentWinner();
        boolean isSelf = currentWinner != null
                && (bidderName.equals(currentWinner)
                        || bidderId.equals(currentWinner));
        if (!isSelf) {
            try {
                String currentWinnerId = "";
                if (currentWinner != null && !currentWinner.isEmpty()) {
                    User winnerUser = getUserDAO().findByName(currentWinner);
                    currentWinnerId = winnerUser != null
                            ? winnerUser.getId()
                            : currentWinner;
                }
                triggerAutoBid(sessionId, currentWinnerId);
            } catch (Exception e) {
                String triggerError = "[AutoBid] Trigger khi register lỗi: "
                        + e.getMessage();
                System.out.println(triggerError);
            }
        }
        return success().put("message", "Đã bật auto-bid!").toString();
    }

    private String handleGetBidHistory(final JSONObject req) {
        AuthResult auth = TokenGuard.check(req);
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sessionId = req.getString("sessionId");
        List<BidTransaction> txList = getBidDAO().findByAuctionId(sessionId);
        JSONArray arr = new JSONArray();
        for (BidTransaction tx : txList) {
            User bidder = getUserDAO().findById(tx.getBidderId());
            arr.put(new JSONObject()
                    .put("bidderName",
                            bidder != null
                                    ? bidder.getName()
                                    : tx.getBidderId())
                    .put("amount", tx.getAmount())
                    .put("timestamp", tx.getTimestamp().toString()));
        }
        return success().put("history", arr).toString();
    }

    private String handleCancelAutoBid(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String autoBidSessionId = req.getString("sessionId");
        AutoBidManager.getInstance().unregister(
                autoBidSessionId,
                auth.getUserId());
        return success().toString();
    }

    private String handleGetAutoBidStatus(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sessionId = req.getString("sessionId");
        String bidderId = auth.getUserId();

        AutoBidConfig config = AutoBidManager.getInstance()
                .getConfigs(sessionId)
                .stream()
                .filter(c -> c.getBidderId().equals(bidderId))
                .findFirst()
                .orElse(null);

        if (config == null) {
            return success().put("active", false).toString();
        }
        return success()
                .put("active", true)
                .put("increment", config.getIncrement())
                .put("maxBid", config.getMaxBid())
                .toString();
    }

    // ── Private helpers ─────────────────────────────────────────────── //

    private void broadcast(final String sessionId, final Auction auction,
                           final String bidderName, final boolean antiSniped,
                           final LocalDateTime endTimeBefore) {
        SocketBroadcaster broadcaster = BidRegistry.getInstance()
                .get(sessionId);
        if (broadcaster == null) {
            String noClient = "[BidHandler] Không có client nào watch phiên "
                    + sessionId;
            System.out.println(noClient);
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
    private void triggerAutoBid(
            final String sessionId,
            final String lastWinnerId) {
        List<AutoBidConfig> autoBids = new java.util.ArrayList<>(
                AutoBidManager.getInstance().getConfigs(sessionId));
        if (autoBids.isEmpty()) {
            return;
        }

        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) {
            return;
        }
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            return;
        }

        for (AutoBidConfig cfg : autoBids) {
            if (cfg.getBidderId().equals(lastWinnerId)) {
                continue;
            }

            boolean stillActive = AutoBidManager.getInstance()
                    .getConfigs(sessionId).stream()
                    .anyMatch(c -> c.getBidderId().equals(cfg.getBidderId()));
            if (!stillActive) {
                continue;
            }

            double nextBid = auction.getCurrentPrice() + cfg.getIncrement();
            if (nextBid > cfg.getMaxBid()) {
                String maxBidMessage = "[AutoBid] " + cfg.getBidderName()
                        + " đạt maxBid, dừng.";
                System.out.println(maxBidMessage);
                AutoBidManager.getInstance().unregister(
                        sessionId,
                        cfg.getBidderId());
                continue;
            }

            BidTransaction bid = new BidTransaction(
                    cfg.getBidderId(),
                    sessionId,
                    nextBid);
            bid.setBidderName(cfg.getBidderName());

            try {
                auction.handleNewBid(bid);
                getBidDAO().save(bid);
                getAuctionDAO().updateBid(
                        sessionId,
                        auction.getCurrentPrice(),
                        cfg.getBidderName());
                getAuctionDAO().updateEndTime(sessionId, auction.getEndTime());

                SocketBroadcaster broadcaster = BidRegistry.getInstance()
                        .get(sessionId);
                if (broadcaster != null) {
                    String updateMessage = "BID_UPDATE:" + sessionId
                            + ":" + auction.getCurrentPrice()
                            + ":" + cfg.getBidderName()
                            + ":" + auction.getEndTime();
                    broadcaster.broadcast(updateMessage);
                }
                String autoBidMsg = "[AutoBid] " + cfg.getBidderName()
                        + " tự đặt " + nextBid;
                System.out.println(autoBidMsg);

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