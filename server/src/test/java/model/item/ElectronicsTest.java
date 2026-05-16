package model.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Electronics Model & Factory Tests")
class ElectronicsTest {

    private final String sellerId = "S_ELEC_01";
    private final String itemName = "Macbook Pro M3";
    private final String itemId = "ELEC-123";
    private final String desc = "16GB RAM, 512GB SSD";
    private final double price = 45_000_000.0;

    // ── Test 1: Khởi tạo mặc định (Default Warranty) ───────────────

    @Test
    @DisplayName("Factory: Tạo Electronics mặc định → Bảo hành phải là 12 tháng")
    void testCreateElectronicsDefault() {
        // Sử dụng enum Factory (ItemType)
        Electronics elec = (Electronics) Item.ItemType.ELECTRONICS.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.APPROVED
        );

        assertNotNull(elec);
        assertEquals(12, elec.getWarrantyMonths(), "Mặc định bảo hành phải là 12 tháng");
        assertEquals(price, elec.getStartPrice());
        assertEquals(itemName, elec.getName());
    }

    // ── Test 2: Khởi tạo tùy chỉnh (Custom Warranty) ───────────────

    @Test
    @DisplayName("Factory: Tạo Electronics tùy chỉnh bảo hành → Dữ liệu phải chính xác")
    void testCreateElectronicsCustomWarranty() {
        // Sử dụng trực tiếp Prototype để gọi hàm overload createItem
        Electronics prototype = new Electronics();
        Electronics elec = (Electronics) prototype.createItem(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.PENDING, 24
        );

        assertNotNull(elec);
        assertEquals(24, elec.getWarrantyMonths(), "Bảo hành phải được cập nhật thành 24 tháng");
        assertEquals(itemId, elec.getId());
    }

    // ── Test 3: Getters & Setters ──────────────────────────────────

    @Test
    @DisplayName("Kiểm tra cập nhật Warranty qua Setter")
    void testElectronicsSetters() {
        Electronics elec = new Electronics();
        elec.setWarrantyMonths(36);
        elec.setName("iPhone 15");

        assertEquals(36, elec.getWarrantyMonths());
        assertEquals("iPhone 15", elec.getName());
    }

    // ── Test 4: In thông tin (Extra Info) ──────────────────────────

    @Test
    @DisplayName("Hàm printInfo không gây lỗi khi in thông tin thiết bị")
    void testPrintInfoSuccess() {
        Electronics elec = (Electronics) Item.ItemType.ELECTRONICS.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.APPROVED
        );

        // Đảm bảo printExtraInfo (in warranty) chạy không lỗi
        assertDoesNotThrow(() -> elec.printInfo());
    }

    // ── Test 5: Verify dữ liệu lớp cha (Anti-0.0 Error) ───────────

    @Test
    @DisplayName("Đảm bảo giá khởi điểm không bị đưa về 0.0 sau khi khởi tạo")
    void testPriceIntegrity() {
        double expectedPrice = 30000000.0;
        Item elec = Item.ItemType.ELECTRONICS.create(
                sellerId, "Test Price", "ID", "Desc", expectedPrice, Item.ItemStatus.PENDING
        );

        // Đây là dòng quan trọng nhất để fix lỗi bạn gặp phải trong ảnh
        assertEquals(expectedPrice, elec.getStartPrice(), "Giá khởi điểm phải được lưu đúng vào lớp cha");
    }
}