package model.item;

import factory.ItemFactory;
import model.entity.Entity;

public abstract class Item extends Entity implements ItemFactory {

    private String sellerId;
    private String sellerName;
    private String name;
    private String description;
    private double startPrice;
    private ItemStatus status;
    private String image;
    private String imageBase64;
    private String imageName;

    /**
     * Constructor rỗng — cần thiết để các subclass dùng làm prototype.
     */
    protected Item() {
        super();
    }

    /**
     * Constructor đầy đủ — gọi từ createItem() trong từng subclass.
     */
    protected Item(String id, String sellerId, String name,
                   String description, double startingPrice, ItemStatus status) {
        super(id);
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.startPrice = startingPrice;
        this.status = status;
    }

    // ── Getters & Setters ───────────────────────────────────────────
    public String getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    // ── Template methods ────────────────────────────────────────────
    @Override
    public void printInfo() {
        System.out.printf("[%s] %s (Seller: %s)%n", getClass().getSimpleName(), name, sellerId);
        System.out.printf("  Mô tả        : %s%n", description);
        System.out.printf("  Giá khởi điểm: %.0f VNĐ%n", startPrice);
        System.out.printf("  Trạng thái   : %s%n", status);
        printExtraInfo();
    }

    /**
     * Subclass ghi đè để in thêm thông tin riêng (warranty, mileage...).
     * Mặc định không in gì thêm.
     */
    protected void printExtraInfo() {
    }

    public enum ItemStatus {
        PENDING,
        APPROVED,
        REJECTED,
        IN_AUCTION,
        SOLD,
        CANCELED
    }

    public enum ItemType {
        ELECTRONICS, ART, VEHICLE, FASHION, ETC;

        /**
         * Tạo model.item.Item tương ứng với loại enum — dùng như static factory.
         */
        public Item create(String sellerId, String name, String id,
                           String description, double startPrice,
                           ItemStatus status) {
            Item prototype = switch (this) {
                case ELECTRONICS -> new Electronics();
                case ART -> new Art();
                case VEHICLE -> new Vehicle();
                case FASHION -> new Fashion();
                case ETC -> new ETC();

            };
            return prototype.createItem(sellerId, name, id, description, startPrice, status);
        }
    }

}