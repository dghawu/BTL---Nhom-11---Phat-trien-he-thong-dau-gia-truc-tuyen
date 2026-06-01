package com.example.model.item;

public class ETC extends Item {


    private String note; // Ghi chú thêm cho các loại mặt hàng không phân loại cụ thể


    public ETC() {
    }


    private ETC(String id, String sellerId, String name,
                String description, double startingPrice,
                Item.ItemStatus status, String note) {
        super(id, sellerId, name, description, startingPrice, status);
        this.note = note;
    }


    // ── Factory Method ──────────────────────────────────────────────
    @Override
    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status) {
        return new ETC(id, sellerId, name, description, startingPrice, status, "Không có ghi chú");
    }


    public Item createItem(String sellerId, String name, String id,
                           String description, double startingPrice,
                           Item.ItemStatus status, String note) {
        return new ETC(id, sellerId, name, description, startingPrice, status, note);
    }


    // ── Getters & Setters ───────────────────────────────────────────
    public String getNote() {
        return note;
    }

    public void setNote(String n) {
        this.note = n;
    }


    @Override
    protected void printExtraInfo() {
        System.out.printf("  Ghi chú      : %s%n", note);
    }
}
