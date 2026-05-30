package com.example.server.handler;

import auth.AuthResult;
import auth.TokenGuard;
import model.item.Item;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

/**
 * ItemHandler — thêm/sửa/lấy sản phẩm.
 */
public class ItemHandler extends BaseHandler {

    @Override
    public String handle(JSONObject req) {
        return switch (req.getString("action")) {
            case "getMyItems"                    -> handleGetMyItems(req);
            case "addItem"                       -> handleAddItem(req);
            case "addItemWithImage"              -> handleAddItemWithImage(req);
            case "addItemWithImageAndAttributes" -> handleAddItemWithImageAndAttributes(req);
            case "updateItem"                    -> handleUpdateItem(req);
            case "updateItemWithImage"           -> handleUpdateItemWithImage(req);
            case "getAllItems"                   -> handleGetAllItems(req);
            case "approveItem"                   -> handleApproveItem(req);
            case "rejectItem"                    -> handleRejectItem(req);
            case "cancelItem"                    -> handleCancelItem(req);
            default                              -> fail("Action không hợp lệ.");
        };
    }

    private String handleGetMyItems(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        List<Item> items = itemDAO.findBySellerId(auth.getUserId());
        JSONArray arr = new JSONArray();
        for (Item item : items) {
            JSONObject obj = new JSONObject()
                    .put("id",          item.getId())
                    .put("name",        item.getName())
                    .put("description", item.getDescription())
                    .put("startPrice",  item.getStartPrice())
                    .put("status",      item.getStatus().name())
                    .put("type",        item.getClass().getSimpleName())
                    .put("sellerId",    item.getSellerId())
                    .put("image",       item.getImage() != null ? item.getImage() : "");
            putItemAttrs(obj, item);          // ← dùng helper, không lặp instanceof
            arr.put(obj);
        }
        return success().put("items", arr).toString();
    }

    private String handleAddItem(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        try {
            Item newItem = buildItem(req, auth.getUserId(), Item.ItemStatus.PENDING);
            itemDAO.save(newItem);
            return success().toString();
        } catch (Exception e) {
            return fail("Không tạo được sản phẩm: " + e.getMessage());
        }
    }

    private String handleAddItemWithImage(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        try {
            Item newItem  = buildItem(req, auth.getUserId(), Item.ItemStatus.PENDING);
            String imgData = req.optString("imageData", "");
            itemDAO.saveWithImage(newItem, imgData);
            return success().toString();
        } catch (Exception e) {
            return fail("Không tạo được sản phẩm: " + e.getMessage());
        }
    }

    private String handleAddItemWithImageAndAttributes(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        try {
            Item newItem = buildItem(req, auth.getUserId(), Item.ItemStatus.PENDING);
            applyAttributes(newItem, req.optString("attr1", ""), req.optString("attr2", ""));
            itemDAO.saveWithImage(newItem, req.optString("imageData", ""));
            return success().toString();
        } catch (Exception e) {
            return fail("Không tạo được sản phẩm: " + e.getMessage());
        }
    }

    private String handleUpdateItem(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String itemId = req.getString("itemId");
        Item item = itemDAO.findById(itemId);
        if (item == null) return fail("Không tìm thấy sản phẩm.");
        if (!auth.getUserId().equals(item.getSellerId()))
            return fail("Bạn không có quyền sửa sản phẩm này.");

        itemDAO.update(itemId,
                req.getString("name"),
                req.optString("description", ""),
                req.getDouble("startPrice"),
                req.optString("status", "PENDING").toUpperCase());
        return success().toString();
    }

    private String handleUpdateItemWithImage(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String itemId = req.getString("itemId");
        Item item = itemDAO.findById(itemId);
        if (item == null) return fail("Không tìm thấy sản phẩm.");
        if (!auth.getUserId().equals(item.getSellerId()))
            return fail("Bạn không có quyền sửa sản phẩm này.");

        itemDAO.updateWithImage(itemId,
                req.getString("name"),
                req.optString("description", ""),
                req.getDouble("startPrice"),
                req.optString("status", "PENDING").toUpperCase(),
                req.optString("imageData", ""));
        return success().toString();
    }

    private String handleGetAllItems(JSONObject req) {
        AuthResult auth = TokenGuard.check(req);
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        JSONArray arr = new JSONArray();
        for (Item item : itemDAO.findAll()) {
            JSONObject obj = new JSONObject()
                    .put("id",         item.getId())
                    .put("name",       item.getName())
                    .put("startPrice", item.getStartPrice())
                    .put("status",     item.getStatus().name())
                    .put("sellerId",   item.getSellerId())
                    .put("sellerName", item.getSellerName() != null ? item.getSellerName() : item.getSellerId())
                    .put("type",       item.getClass().getSimpleName())
                    .put("image",      item.getImage() != null ? item.getImage() : "");
            putItemAttrs(obj, item);          // ← dùng helper, không lặp instanceof
            arr.put(obj);
        }
        return success().put("items", arr).toString();
    }

    private String handleApproveItem(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String itemId = req.optString("itemId", "");
        if (itemId.isBlank()) return fail("Thiếu itemId.");

        Item item = itemDAO.findById(itemId);
        if (item == null) return fail("Không tìm thấy sản phẩm.");
        if (item.getStatus() != Item.ItemStatus.PENDING)
            return fail("Chỉ duyệt được sản phẩm PENDING.");

        itemDAO.updateStatus(itemId, "APPROVED");
        return success().put("message", "Đã duyệt sản phẩm.").toString();
    }

    private String handleRejectItem(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String itemId = req.optString("itemId", "");
        if (itemId.isBlank()) return fail("Thiếu itemId.");

        Item item = itemDAO.findById(itemId);
        if (item == null) return fail("Không tìm thấy sản phẩm.");
        if (item.getStatus() != Item.ItemStatus.PENDING)
            return fail("Chỉ từ chối được sản phẩm PENDING.");

        itemDAO.updateStatus(itemId, "REJECTED");
        return success().put("message", "Đã từ chối sản phẩm.").toString();
    }

    private String handleCancelItem(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "SELLER");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String itemId = req.getString("itemId");
        Item item = itemDAO.findById(itemId);
        if (item == null) return fail("Không tìm thấy sản phẩm.");
        if (!auth.getUserId().equals(item.getSellerId()))
            return fail("Bạn không có quyền hủy sản phẩm này.");
        if (item.getStatus() == Item.ItemStatus.IN_AUCTION
                || item.getStatus() == Item.ItemStatus.SOLD)
            return fail("Không thể hủy sản phẩm này.");

        itemDAO.updateStatus(itemId, "CANCELED");
        return success().put("message", "Đã hủy sản phẩm.").toString();
    }

    // ── Private helpers ─────────────────────────────────────────────── //

    /** Tạo Item từ request — dùng chung cho add/addWithImage. */
    private Item buildItem(JSONObject req, String sellerId, Item.ItemStatus status) {
        String category = req.getString("category")
                .toUpperCase().replace(" ", "_").replace("Đ", "D");
        Item.ItemType type = Item.ItemType.valueOf(category);
        return type.create(
                sellerId,
                req.getString("name"),
                UUID.randomUUID().toString(),
                req.getString("description"),
                req.getDouble("startPrice"),
                status
        );
    }

    /** Gắn thuộc tính attr1/attr2 vào item theo loại. */
    private void applyAttributes(Item item, String attr1, String attr2) {
        if (item instanceof model.item.Fashion f) {
            f.setBrand(attr1); f.setSize(attr2);
        } else if (item instanceof model.item.Art a) {
            a.setArtist(attr1); a.setMedium(attr2);
        } else if (item instanceof model.item.Vehicle v) {
            v.setBrand(attr1);
            try { v.setMileage(Long.parseLong(attr2)); } catch (NumberFormatException e) { v.setMileage(0); }
        } else if (item instanceof model.item.Electronics e) {
            e.setBrand(attr1);
            try { e.setWarrantyMonths(Integer.parseInt(attr2)); } catch (NumberFormatException ex) { e.setWarrantyMonths(12); }
        }
    }
}