package model.item;

public class Vehicle extends Item {


    private long mileage;  // km đã đi


    public Vehicle() {
    }


    private Vehicle(String id, String sellerId, String name,
                    String description, double startingPrice,
                    Item.ItemStatus status, long mileage) {
        super(id, sellerId, name, description, startingPrice, status);
        this.mileage = mileage;
    }


    // ── Factory Method ──────────────────────────────────────────────
    @Override
    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status) {
        return new Vehicle(id, sellerId, name, description, startingPrice, status, 0L);
    }


    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status, long mileage) {
        return new Vehicle(id, sellerId, name, description, startingPrice, status, mileage);
    }


    // ── Getters & Setters ───────────────────────────────────────────
    public long getMileage() {
        return mileage;
    }

    public void setMileage(long km) {
        this.mileage = km;
    }


    @Override
    protected void printExtraInfo() {
        System.out.printf("  Số km đã đi  : %,d km%n", mileage);
    }
}
