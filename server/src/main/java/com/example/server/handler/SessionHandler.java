package com.example.server.handler;

import auth.AuthResult;
import auth.TokenGuard;
import model.auction.Auction;
import model.enums.AuctionStatus;
import model.item.Item;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SessionHandler — tạo/lấy/duyệt/hủy phiên đấu giá.
 */
public final class SessionHandler extends BaseHandler {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Override
    public String handle(JSONObject req) {
        return switch (req.getString("action")) {
            case "createSession" -> handleCreateSession(req);
            case "getAllSessions" -> handleGetAllSessions(req);
            case "getMySessions" -> handleGetMySessions(req);
            case "updateSession" -> handleUpdateSession(req);
            case "approveSession" -> handleApproveSession(req);
            case "rejectSession" -> handleRejectSession(req);
            case "cancelAuction" -> handleCancelAuction(req);
            default -> fail("Action không hợp lệ.");
        };
    }

    private String handleCreateSession(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String itemId = req.getString("itemId");
        Item item = getItemDAO().findById(itemId);
        if (item == null) {
            return fail("Không tìm thấy sản phẩm.");
        }
        if (item.getStatus() == Item.ItemStatus.SOLD) {
            return fail("Sản phẩm này đã được bán, không thể tạo phiên mới.");
        }

        Auction auction = new Auction(
                UUID.randomUUID().toString(), item, item.getStartPrice(),
                req.getDouble("stepPrice"),
                LocalDateTime.parse(req.getString("startTime"), DT_FMT),
                LocalDateTime.parse(req.getString("endTime"), DT_FMT)
        );
        getAuctionDAO().save(auction);
        return success().toString();
    }

    private String handleGetAllSessions(final JSONObject req) {
        AuthResult auth = TokenGuard.check(req);
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String category = req.optString("category", "ALL");
        boolean isAdmin = "ADMIN".equalsIgnoreCase(auth.getRole());
        JSONArray arr = new JSONArray();

        for (Auction a : getAuctionDAO().findAll()) {
            if (!isAdmin) {
                AuctionStatus st = a.getStatus();
                if (st != AuctionStatus.APPROVED && st != AuctionStatus.RUNNING) continue;
                if (a.getEndTime().isBefore(LocalDateTime.now())) continue;
            }
            if (!"ALL".equals(category)) {
                String itemType = a.getItem().getClass().getSimpleName().toUpperCase();
                if (!itemType.contains(category.replace("_", ""))) continue;
            }
            arr.put(auctionToJson(a));
        }
        return success().put("sessions", arr).toString();
    }

    private String handleGetMySessions(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sellerId = auth.getUserId();
        JSONArray arr = new JSONArray();
        for (Auction a : getAuctionDAO().findAll()) {
            if (sellerId.equals(a.getItem().getSellerId()))
                arr.put(auctionToJson(a));
        }
        return success().put("sessions", arr).toString();
    }

    private String handleUpdateSession(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sessionId = req.getString("sessionId");
        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên đấu giá.");
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            return fail("Phiên đấu giá không còn hoạt động.");
        }
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            return fail("Phiên đấu giá đã kết thúc.");
        }
        if (!auth.getUserId().equals(auction.getItem().getSellerId())) {
            return fail("Bạn không có quyền sửa phiên này.");
        }

        try {
            auction.setEndTime(LocalDateTime.parse(req.getString("endTime"), DT_FMT));
            auction.setMinIncrement(req.getDouble("stepPrice"));
            getAuctionDAO().update(auction);
            return success().toString();
        } catch (Exception e) {
            return fail("Lỗi update phiên: " + e.getMessage());
        }
    }

    private String handleApproveSession(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sessionId = req.optString("sessionId", "");
        if (sessionId.isBlank()) return fail("Thiếu sessionId.");

        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên.");
        if (auction.getStatus() != AuctionStatus.PENDING)
            return fail("Chỉ duyệt được phiên PENDING.");

        getAuctionDAO().updateStatus(sessionId, AuctionStatus.APPROVED);
        getItemDAO().updateStatus(auction.getItem().getId(), "IN_AUCTION");

        long delaySeconds = java.time.temporal.ChronoUnit.SECONDS
                .between(LocalDateTime.now(), auction.getStartTime());

        if (delaySeconds <= 0) {
            getAuctionDAO().updateStatus(sessionId, AuctionStatus.RUNNING);
            auction.setStatus(AuctionStatus.RUNNING);
            service.AuctionTimer.getInstance().schedule(auction);
        } else {
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(() -> {
                Auction a = getAuctionDAO().findById(sessionId);
                if (a != null && a.getStatus() == AuctionStatus.APPROVED) {
                    getAuctionDAO().updateStatus(sessionId, AuctionStatus.RUNNING);
                    a.setStatus(AuctionStatus.RUNNING);
                    service.AuctionTimer.getInstance().schedule(a);
                    System.out.println("[Session] Phiên " + sessionId + " bắt đầu!");
                }
            }, delaySeconds, TimeUnit.SECONDS);
        }
        return success().put("message", "Đã duyệt phiên đấu giá.").toString();
    }

    private String handleRejectSession(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String sessionId = req.optString("sessionId", "");
        if (sessionId.isBlank()) return fail("Thiếu sessionId.");

        Auction auction = getAuctionDAO().findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên.");
        if (auction.getStatus() != AuctionStatus.PENDING)
            return fail("Chỉ từ chối được phiên PENDING.");

        getAuctionDAO().updateStatus(sessionId, AuctionStatus.CANCELED);
        getItemDAO().updateStatus(auction.getItem().getId(), "APPROVED");
        return success().put("message", "Đã từ chối phiên đấu giá.").toString();
    }

    private String handleCancelAuction(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        String auctionId = req.getString("auctionId");
        Auction auction = getAuctionDAO().findById(auctionId);
        if (auction == null) return fail("Không tìm thấy phiên đấu giá.");
        if (!auth.getUserId().equals(auction.getItem().getSellerId()))
            return fail("Bạn không có quyền hủy phiên này.");

        AuctionStatus st = auction.getStatus();
        if (st == AuctionStatus.FINISHED || st == AuctionStatus.PAID
                || st == AuctionStatus.CANCELED) {
            return fail("Phiên này không thể hủy.");
        }

        getAuctionDAO().updateStatus(auctionId, AuctionStatus.CANCELED);
        getItemDAO().updateStatus(auction.getItem().getId(), "APPROVED");
        service.AuctionTimer.getInstance().cancelTask(auctionId);
        return success().put("message", "Đã hủy phiên đấu giá.").toString();
    }

    // ── Helper: serialize Auction → JSON ────────────────────────────── //

    public JSONObject auctionToJson(final Auction a) {
        Item item = a.getItem();
        JSONObject obj = new JSONObject()
                .put("id", a.getAuctionId())
                .put("itemId", item.getId())
                .put("itemName", item.getName())
                .put("itemImage", item.getImage() != null ? item.getImage() : "")
                .put("description", item.getDescription() != null ? item.getDescription() : "")
                .put("sellerId", item.getSellerId())
                .put("sellerName", item.getSellerName() != null ? item.getSellerName() : item.getSellerId())
                .put("startPrice", a.getStartPrice())
                .put("currentPrice", a.getCurrentPrice())
                .put("stepPrice", a.getMinIncrement())
                .put("startTime", a.getStartTime().toString())
                .put("endTime", a.getEndTime().toString())
                .put("status", a.getStatus().name())
                .put("category", item.getClass().getSimpleName())
                .put("currentWinner", a.getCurrentWinner() != null ? a.getCurrentWinner() : "")
                .put("imageBase64", item.getImageBase64() != null ? item.getImageBase64() : "")
                .put("imageName", item.getImageName() != null ? item.getImageName() : "");
        putItemAttrs(obj, item);              // ← dùng helper, không lặp instanceof
        return obj;
    }
}