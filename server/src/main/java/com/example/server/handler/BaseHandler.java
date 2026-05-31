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

    /**
     * DAO xử lý user.
     */
    private final UserDAO userDAO = new UserDAO();

    /**
     * DAO xử lý item.
     */
    private final ItemDAO itemDAO = new ItemDAO();

    /**
     * DAO xử lý phiên đấu giá.
     */
    private final AuctionDAO auctionDAO = new AuctionDAO();

    /**
     * DAO xử lý giao dịch đấu giá.
     */
    private final BidTransactionDAO bidDAO = new BidTransactionDAO();

    protected final UserDAO getUserDAO() {
        return userDAO;
    }

    protected final ItemDAO getItemDAO() {
        return itemDAO;
    }

    protected final AuctionDAO getAuctionDAO() {
        return auctionDAO;
    }

    protected final BidTransactionDAO getBidDAO() {
        return bidDAO;
    }

    /**
     * Xử lý request JSON, trả về JSON string response.
     *
     * @param req request JSON
     * @return JSON string response
     */
    public abstract String handle(final JSONObject req);

    // ── Helpers ─────────────────────────────────────────────────────── //

    /**
     * Trả về JSON success cơ bản.
     *
     * @return JSONObject success
     */
    protected JSONObject success() {
        return new JSONObject().put("success", true);
    }

    /**
     * Trả về JSON fail với message.
     *
     * @param msg thông báo lỗi
     * @return JSON string response
     */
    protected String fail(final String msg) {
        return new JSONObject()
                .put("success", false)
                .put("message", msg)
                .toString();
    }

    /**
     * Gắn attr1/attr2 vào JSONObject tuỳ loại item.
     * Thay thế 3 khối instanceof lặp lại trong phiên bản cũ.
     *
     * @param obj  JSONObject đích
     * @param item item cần lấy thuộc tính
     */
    protected void putItemAttrs(final JSONObject obj, final Item item) {
        if (item instanceof model.item.Fashion f) {
            obj.put("attr1", f.getBrand() != null ? f.getBrand() : "");
            obj.put("attr2", f.getSize() != null ? f.getSize() : "");
        } else if (item instanceof model.item.Art a) {
            obj.put("attr1", a.getArtist() != null ? a.getArtist() : "");
            obj.put("attr2", a.getMedium() != null ? a.getMedium() : "");
        } else if (item instanceof model.item.Vehicle v) {
            obj.put("attr1", v.getBrand() != null ? v.getBrand() : "");
            obj.put("attr2", String.valueOf(v.getMileage()));
        } else if (item instanceof model.item.Electronics e) {
            obj.put("attr1", e.getBrand() != null ? e.getBrand() : "");
            obj.put("attr2", String.valueOf(e.getWarrantyMonths()));
        } else {
            obj.put("attr1", "");
            obj.put("attr2", "");
        }
    }
}