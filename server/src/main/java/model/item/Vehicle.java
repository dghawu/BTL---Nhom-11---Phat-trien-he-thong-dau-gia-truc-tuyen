package model.item;

public class Vehicle extends Item {
    private String brand;
    private long mileage; // km đã đi


    public Vehicle() {
    }


    private Vehicle(String id, String sellerId, String name,
                    String description, double startingPrice,
                    Item.ItemStatus status, String brand, long mileage) {
        super(id, sellerId, name, description, startingPrice, status);
        this.brand = brand;
        this.mileage = mileage;
    }


    // ── Factory Method ──────────────────────────────────────────────
    @Override
    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status) {
        return new Vehicle(id, sellerId, name, description, startingPrice, status, null, 0L);
    }


    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status, long mileage) {
        return new Vehicle(id, sellerId, name, description, startingPrice, status, brand, mileage);
    }


    // ── Getters & Setters ───────────────────────────────────────────
    public long getMileage() {
        return mileage;
    }

    public void setMileage(long km) {
        this.mileage = km;
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
        System.out.printf("  Số km đã đi  : %,d km%n", mileage);
    }
}
