public class Electronics extends Item {


    private int warrantyMonths;


    /** Prototype constructor — chỉ dùng nội bộ khi làm factory. */
    public Electronics() {}


    private Electronics(String id, String sellerId, String name,
                        String description, double startingPrice,
                        ItemStatus status, int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice, status);
        this.warrantyMonths = warrantyMonths;
    }


    // ── Factory Method ──────────────────────────────────────────────
    /**
     * Tạo Electronics với warrantyMonths mặc định = 12.
     * Dùng {@link #createItem(String, String, String, String, double, ItemStatus, int)}
     * nếu muốn chỉ định thời hạn bảo hành khác.
     */
    @Override
    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           ItemStatus status) {
        return new Electronics(id, sellerId, name, description, startingPrice, status, 12);
    }


    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           ItemStatus status, int warrantyMonths) {
        return new Electronics(id, sellerId, name, description, startingPrice, status, warrantyMonths);
    }


    // ── Getters & Setters ───────────────────────────────────────────
    public int  getWarrantyMonths()              { return warrantyMonths; }
    public void setWarrantyMonths(int months)    { this.warrantyMonths = months; }


    @Override
    protected void printExtraInfo() {
        System.out.printf("  Bảo hành     : %d tháng%n", warrantyMonths);
    }
}
