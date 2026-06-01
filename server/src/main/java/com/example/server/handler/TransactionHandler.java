package com.example.server.handler;

import com.example.auth.AuthResult;
import com.example.auth.TokenGuard;
import com.example.model.auction.Auction;
import com.example.model.auction.BidTransaction;
import com.example.model.enums.AuctionStatus;
import com.example.model.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TransactionHandler — xác nhận thắng, thanh toán, xem lịch sử giao dịch.
 */
public class TransactionHandler extends BaseHandler {

    @Override
    public String handle(JSONObject req) {
        return switch (req.getString("action")) {
            case "getMyTransactions" -> handleGetMyTransactions(req);
            case "getAllTransactions" -> handleGetAllTransactions(req);   // ← đã thêm vào switch
            case "confirmWin" -> handleConfirmWin(req);
            case "pay" -> handlePay(req);
            case "getMyWonSessions" -> handleGetMyWonSessions(req);
            default -> fail("Action không hợp lệ.");
        };
    }

    private String handleGetMyTransactions(JSONObject req) {
        AuthResult auth = TokenGuard.check(req);
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        return success()
                .put("transactions", txToJson(getBidDAO().findByBidderId(auth.getUserId())))
                .toString();
    }

    private String handleGetAllTransactions(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        JSONArray arr = new JSONArray();
        for (Auction a : getAuctionDAO().findAll()) {
            for (BidTransaction tx : getBidDAO().findByAuctionId(a.getAuctionId())) {
                User bidder = getUserDAO().findById(tx.getBidderId());
                arr.put(new JSONObject()
                        .put("id", tx.getId())
                        .put("auctionId", tx.getAuctionId())
                        .put("itemName", a.getItem().getName())
                        .put("bidderName", bidder != null ? bidder.getName() : tx.getBidderId())
                        .put("amount", tx.getAmount())
                        .put("timestamp", tx.getTimestamp().toString())
                        .put("status", a.getStatus().name()));
            }
        }
        return success().put("transactions", arr).toString();
    }

    private String handleConfirmWin(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String sessionId = req.getString("sessionId");
        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên.");
        if (auction.getStatus() != AuctionStatus.FINISHED)
            return fail("Phiên chưa kết thúc.");

        User winner = getUserDAO().findByName(auction.getCurrentWinner());
        String winnerId = winner != null ? winner.getId() : "";
        if (!auth.getUserId().equals(winnerId))
            return fail("Bạn không phải người thắng phiên này.");

        getAuctionDAO().updateStatus(sessionId, AuctionStatus.PAYING);

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            Auction a = getAuctionDAO().findById(sessionId);
            if (a != null && a.getStatus() == AuctionStatus.PAYING) {
                getAuctionDAO().updateStatus(sessionId, AuctionStatus.CANCELED);
                getItemDAO().updateStatus(a.getItem().getId(), "APPROVED");
                System.out.println("[Pay] Phiên " + sessionId + " hết 24h, tự hủy.");
            }
        }, 24, TimeUnit.HOURS);

        return success().put("message", "Đã xác nhận! Bạn có 24h để thanh toán.").toString();
    }

    private String handlePay(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String sessionId = req.getString("sessionId");
        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên.");
        if (auction.getStatus() != AuctionStatus.PAYING)
            return fail("Phiên không ở trạng thái chờ thanh toán.");

        getAuctionDAO().updateStatus(sessionId, AuctionStatus.PAID);
        getItemDAO().updateStatus(auction.getItem().getId(), "SOLD");
        return success().put("message", "Thanh toán thành công!").toString();
    }

    private String handleGetMyWonSessions(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "BIDDER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String bidderName = auth.getUsername();
        JSONArray arr = new JSONArray();
        for (Auction a : getAuctionDAO().findAll()) {
            AuctionStatus st = a.getStatus();
            if (st != AuctionStatus.FINISHED && st != AuctionStatus.PAYING
                    && st != AuctionStatus.PAID) continue;
            if (!bidderName.equals(a.getCurrentWinner())) continue;
            arr.put(new SessionHandler().auctionToJson(a));
        }
        return success().put("sessions", arr).toString();
    }

    // ── Helper ──────────────────────────────────────────────────────── //

    private JSONArray txToJson(List<BidTransaction> list) {
        JSONArray arr = new JSONArray();
        for (BidTransaction tx : list) {
            Auction a = getAuctionDAO().findById(tx.getAuctionId());
            arr.put(new JSONObject()
                    .put("id", tx.getId())
                    .put("auctionId", tx.getAuctionId())
                    .put("itemName", a != null ? a.getItem().getName() : "")
                    .put("amount", tx.getAmount())
                    .put("timestamp", tx.getTimestamp().toString())
                    .put("status", a != null ? a.getStatus().name() : ""));
        }
        return arr;
    }
}