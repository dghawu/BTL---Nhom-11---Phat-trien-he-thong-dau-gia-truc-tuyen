package exception;

/**
 * Ném khi giá khởi điểm hoặc mức tăng tối thiểu không hợp lệ.
 * VD: giá âm, bằng 0, hoặc vượt quá giới hạn cho phép.
 */
public class InvalidItemPriceException extends AuctionSystemException {

    private final double price;
    private final String field; // "startPrice" hoặc "minIncrement"

    public InvalidItemPriceException(String field, double price) {
        super("INVALID_PRICE",
                "Giá không hợp lệ cho trường '" + field + "': " + price + " VNĐ. "
                        + "Giá phải lớn hơn 0.");
        this.price = price;
        this.field = field;
    }

    public double getPrice() {
        return price;
    }

    public String getField() {
        return field;
    }
}
