package com.example.exception;

/**
 * Ném khi tra cứu sản phẩm theo ID nhưng không tìm thấy.
 */
public class ItemNotFoundException extends AuctionSystemException {

    private final String itemId;

    public ItemNotFoundException(String itemId) {
        super("ITEM_NOT_FOUND",
                "Không tìm thấy sản phẩm với ID: '" + itemId + "'");
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }
}
