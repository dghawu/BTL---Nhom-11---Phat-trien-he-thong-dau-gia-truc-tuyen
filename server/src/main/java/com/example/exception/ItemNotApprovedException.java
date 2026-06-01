package com.example.exception;

/**
 * Ném khi Seller cố tạo phiên đấu giá từ sản phẩm chưa được Admin duyệt.
 * Sản phẩm phải ở trạng thái APPROVED mới được đưa vào đấu giá.
 */
public class ItemNotApprovedException extends AuctionSystemException {

    private final String itemId;
    private final String itemStatus;

    public ItemNotApprovedException(String itemId, String itemStatus) {
        super("ITEM_NOT_APPROVED",
                "Sản phẩm '" + itemId + "' chưa được duyệt (trạng thái: " + itemStatus + "). "
                        + "Chỉ sản phẩm APPROVED mới được đưa vào đấu giá.");
        this.itemId = itemId;
        this.itemStatus = itemStatus;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemStatus() {
        return itemStatus;
    }
}
