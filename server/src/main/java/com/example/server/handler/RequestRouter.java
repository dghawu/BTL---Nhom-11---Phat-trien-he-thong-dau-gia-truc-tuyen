package com.example.server.handler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * RequestRouter — ánh xạ action → Handler tương ứng.
 * Thay thế switch-case trong ClientHandler cũ.
 *
 * Thêm action mới: chỉ cần đăng ký thêm 1 dòng trong registerHandlers(),
 * không cần chạm vào ClientHandler hay các Handler khác. (Open/Closed)
 */
public class RequestRouter {

    private final Map<String, BaseHandler> routes = new HashMap<>();

    public RequestRouter() {
        registerHandlers();
    }

    private void registerHandlers() {
        AuthHandler        auth        = new AuthHandler();
        ItemHandler        item        = new ItemHandler();
        SessionHandler     session     = new SessionHandler();
        BidHandler         bid         = new BidHandler();
        TransactionHandler transaction = new TransactionHandler();
        AdminHandler       admin       = new AdminHandler();

        // Auth
        register("login",          auth);
        register("register",       auth);
        register("changeUsername", auth);
        register("changePassword", auth);

        // Items
        register("getMyItems",                    item);
        register("addItem",                       item);
        register("addItemWithImage",              item);
        register("addItemWithImageAndAttributes", item);
        register("updateItem",                    item);
        register("updateItemWithImage",           item);
        register("getAllItems",                   item);
        register("approveItem",                   item);
        register("rejectItem",                    item);
        register("cancelItem",                    item);

        // Sessions
        register("createSession",  session);
        register("getAllSessions", session);
        register("getMySessions",  session);
        register("updateSession",  session);
        register("approveSession", session);
        register("rejectSession",  session);
        register("cancelAuction",  session);

        // Bidding
        register("placeBid",         bid);
        register("setAutoBid",       bid);
        register("getBidHistory",    bid);
        register("cancelAutoBid",    bid);
        register("getAutoBidStatus", bid);

        // Transactions
        register("getMyTransactions", transaction);
        register("getAllTransactions", transaction);  // ← đã thêm, không còn bị bỏ sót
        register("confirmWin",         transaction);
        register("pay",                transaction);
        register("getMyWonSessions",   transaction);

        // Admin
        register("getAllUsers", admin);
        register("banUser",     admin);
        register("makeAdmin",   admin);
    }

    private void register(String action, BaseHandler handler) {
        routes.put(action, handler);
    }

    /**
     * Route request đến đúng handler.
     * @return JSON string response
     */
    public String route(JSONObject req) {
        String action = req.optString("action", "");
        BaseHandler handler = routes.get(action);

        if (handler == null)
            return new JSONObject()
                    .put("success", false)
                    .put("message", "Action không hợp lệ: " + action)
                    .toString();

        return handler.handle(req);
    }
}