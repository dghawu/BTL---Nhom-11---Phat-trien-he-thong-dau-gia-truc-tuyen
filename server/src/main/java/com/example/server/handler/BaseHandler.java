package com.example.server.handler;

import dao.AuctionDAO;
import dao.BidTransactionDAO;
import dao.ItemDAO;
import dao.UserDAO;
import model.item.Item;
import org.json.JSONObject;

/**
 * BaseHandler — lớp cha chung cho tất cả Handler.
 * Chứa: DAOs dùng chung, helper success/fail, helper putItemAttrs.
 */
public abstract class BaseHandler {

    protected final UserDAO           userDAO    = new UserDAO();
    protected final ItemDAO           itemDAO    = new ItemDAO();
    protected final AuctionDAO        auctionDAO = new AuctionDAO();
    protected final BidTransactionDAO bidDAO     = new BidTransactionDAO();

    /** Xử lý request JSON, trả về JSON string response. */
    public abstract String handle(JSONObject req);

    // ── Helpers ─────────────────────────────────────────────────────── //

    protected JSONObject success() {
        return new JSONObject().put("success", true);
    }

    protected String fail(String msg) {
        return new JSONObject()
                .put("success", false)
                .put("message", msg)
                .toString();
    }

    /**
     * Gắn attr1/attr2 vào JSONObject tuỳ loại item.
     * Thay thế 3 khối instanceof lặp lại trong phiên bản cũ.
     */
    protected void putItemAttrs(JSONObject obj, Item item) {
        if (item instanceof model.item.Fashion f) {
            obj.put("attr1", f.getBrand()  != null ? f.getBrand()  : "");
            obj.put("attr2", f.getSize()   != null ? f.getSize()   : "");
        } else if (item instanceof model.item.Art a) {
            obj.put("attr1", a.getArtist() != null ? a.getArtist() : "");
            obj.put("attr2", a.getMedium() != null ? a.getMedium() : "");
        } else if (item instanceof model.item.Vehicle v) {
            obj.put("attr1", v.getBrand()  != null ? v.getBrand()  : "");
            obj.put("attr2", String.valueOf(v.getMileage()));
        } else if (item instanceof model.item.Electronics e) {
            obj.put("attr1", e.getBrand()  != null ? e.getBrand()  : "");
            obj.put("attr2", String.valueOf(e.getWarrantyMonths()));
        } else {
            obj.put("attr1", "");
            obj.put("attr2", "");
        }
    }
}