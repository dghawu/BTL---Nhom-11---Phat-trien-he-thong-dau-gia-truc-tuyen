package model.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Item & Factory Logic Tests")
class ItemTest {

    private String sellerId = "S01";
    private String itemName = "Laptop Gaming";
    private String itemId = "ITEM-001";
    private String desc = "Core i9, RTX 4090";
    private double price = 50_000_000.0;

    // ── Test 1: Khởi tạo thông qua Factory ─────────────────────────

    @Test
    @DisplayName("Factory: Tạo Electronics thành công với đúng dữ liệu")
    void testCreateElectronics() {
        // Sử dụng enum ItemType để tạo đối tượng
        Item electronics = Item.ItemType.ELECTRONICS.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.PENDING
        );

        assertNotNull(electronics);
        assertTrue(electronics instanceof Electronics);
        assertEquals(itemName, electronics.getName());
        assertEquals(price, electronics.getStartPrice());
        assertEquals(Item.ItemStatus.PENDING, electronics.getStatus());
    }

    @Test
    @DisplayName("Factory: Tạo Art thành công và giữ đúng ID")
    void testCreateArt() {
        Item art = Item.ItemType.ART.create(
                sellerId, "Bức tranh Mùa Thu", "ART-99", "Tranh sơn dầu", 10_000_000.0, Item.ItemStatus.APPROVED
        );

        assertNotNull(art);
        assertTrue(art instanceof Art);
        assertEquals("ART-99", art.getId()); // Kiểm tra kế thừa từ Entity
        assertEquals(Item.ItemStatus.APPROVED, art.getStatus());
    }

    // ── Test 2: Getters & Setters ──────────────────────────────────

    @Test
    @DisplayName("Kiểm tra cập nhật thông tin qua Setters")
    void testItemSetters() {
        Item item = Item.ItemType.ETC.create(sellerId, "Đồ cũ", "ETC-01", "Mô tả", 100.0, Item.ItemStatus.PENDING);

        item.setName("Đồ mới");
        item.setStartPrice(500.0);
        item.setStatus(Item.ItemStatus.REJECTED);
        item.setDescription("Mô tả mới");

        assertEquals("Đồ mới", item.getName());
        assertEquals(500.0, item.getStartPrice());
        assertEquals(Item.ItemStatus.REJECTED, item.getStatus());
        assertEquals("Mô tả mới", item.getDescription());
    }

    // ── Test 3: In thông tin (Template Method) ─────────────────────

    @Test
    @DisplayName("In thông tin không gây lỗi Exception")
    void testPrintInfo() {
        Item electronics = Item.ItemType.ELECTRONICS.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.APPROVED
        );

        // Đảm bảo printInfo và printExtraInfo (warranty) chạy bình thường
        assertDoesNotThrow(() -> electronics.printInfo());
    }

    // ── Test 4: Logic Đa hình (Polymorphism) ────────────────────────

    @Test
    @DisplayName("Đảm bảo mỗi subclass có Type riêng biệt")
    void testSubclassTypes() {
        Item e = Item.ItemType.ELECTRONICS.create(sellerId, "E", "ID", "D", 1.0, Item.ItemStatus.PENDING);
        Item v = Item.ItemType.VEHICLE.create(sellerId, "V", "ID", "D", 1.0, Item.ItemStatus.PENDING);

        assertNotEquals(e.getClass(), v.getClass());
    }
}