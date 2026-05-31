package model.item;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;


    /**
     * Prototype constructor — chỉ dùng nội bộ khi làm factory.
     */
    public Electronics() {
    }


    private Electronics(String id, String sellerId, String name,
                        String description, double startingPrice,
                        Item.ItemStatus status, String brand, int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice, status);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }


    // ── Factory Method ──────────────────────────────────────────────

    /**
     * Tạo model.item.Electronics với warrantyMonths mặc định = 12.
     * Dùng {@link #createItem(String, String, String, String, double, Item.ItemStatus, int)}
     * nếu muốn chỉ định thời hạn bảo hành khác.
     */
    @Override
    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status) {
        return new Electronics(id, sellerId, name, description, startingPrice, status, null, 12);
    }


    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status, int warrantyMonths) {
        return new Electronics(id, sellerId, name, description, startingPrice, status, brand, warrantyMonths);
    }


    // ── Getters & Setters ───────────────────────────────────────────
    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int months) {
        this.warrantyMonths = months;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @Override
    protected void printExtraInfo() {
        if (brand != null && !brand.isEmpty()) {
            System.out.printf("  Thương hiệu   : %s%n", brand);
        }
        System.out.printf("  Bảo hành     : %d tháng%n", warrantyMonths);
    }
}
