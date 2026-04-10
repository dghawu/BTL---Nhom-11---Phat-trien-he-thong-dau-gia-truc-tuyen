public class Fashion extends Item {


    private String size;
    private String brand;


    public Fashion() {}


    private Fashion(String id, String sellerId, String name,
                    String description, double startingPrice,
                    ItemStatus status, String size, String brand) {
        super(id, sellerId, name, description, startingPrice, status);
        this.size  = size;
        this.brand = brand;
    }


    // ── Factory Method ──────────────────────────────────────────────
    @Override
    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           ItemStatus status) {
        return new Fashion(id, sellerId, name, description, startingPrice, status, "One size", "Không rõ");
    }


    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           ItemStatus status, String size, String brand) {
        return new Fashion(id, sellerId, name, description, startingPrice, status, size, brand);
    }


    // ── Getters & Setters ───────────────────────────────────────────
    public String getSize()           { return size; }
    public void   setSize(String s)   { this.size = s; }


    public String getBrand()          { return brand; }
    public void   setBrand(String b)  { this.brand = b; }


    @Override
    protected void printExtraInfo() {
        System.out.printf("  Thương hiệu  : %s%n", brand);
        System.out.printf("  Kích cỡ      : %s%n", size);
    }
}
