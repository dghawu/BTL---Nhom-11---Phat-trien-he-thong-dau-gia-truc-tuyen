package model.item;

public class Art extends Item {
    private String artist;
    private String medium;  // sơn dầu, màu nước, điêu khắc...

    public Art() {}

    private Art(String id, String sellerId, String name,
                String description, double startingPrice,
                Item.ItemStatus status, String artist, String medium) {
        super(id, sellerId, name, description, startingPrice, status);
        this.artist = artist;
        this.medium = medium;
    }

    // ── Factory Method ──────────────────────────────────────────────
    @Override
    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status) {
        return new Art(id, sellerId, name, description, startingPrice, status, "Không rõ", "Không rõ");
    }

    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status, String artist, String medium) {
        return new Art(id, sellerId, name, description, startingPrice, status, artist, medium);
    }

    // ── Getters & Setters ───────────────────────────────────────────
    public String getArtist()           { return artist; }
    public void   setArtist(String a)   { this.artist = a; }

    public String getMedium()           { return medium; }
    public void   setMedium(String m)   { this.medium = m; }

    @Override
    protected void printExtraInfo() {
        System.out.printf("  Nghệ sĩ      : %s%n", artist);
        System.out.printf("  Chất liệu    : %s%n", medium);
    }
}
