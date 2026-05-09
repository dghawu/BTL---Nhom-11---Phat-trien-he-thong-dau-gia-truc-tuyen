package model.item;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ETC Model & Factory Tests")
class ETCTest {

    private final String sellerId = "S_ETC_01";
    private final String itemName = "Bộ sưu tập tem cổ";
    private final String itemId = "ETC-555";
    private final String desc = "Tem từ những năm 1980";
    private final double price = 5_000_000.0;

    // ── Test 1: Khởi tạo mặc định qua Factory ─────────────────────

    @Test
    @DisplayName("Factory: Tạo ETC mặc định → Note phải là 'Không có ghi chú'")
    void testCreateETCDefault() {
        // Kiểm tra luồng khởi tạo thông qua Enum ItemType
        ETC etc = (ETC) Item.ItemType.ETC.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.PENDING
        );

        assertNotNull(etc);
        assertEquals("Không có ghi chú", etc.getNote());
        assertEquals(price, etc.getStartPrice());
        assertEquals(itemName, etc.getName());
    }

    // ── Test 2: Khởi tạo với ghi chú tùy chỉnh ────────────────────

    @Test
    @DisplayName("Factory: Tạo ETC với ghi chú riêng → Dữ liệu note phải chính xác")
    void testCreateETCCustomNote() {
        ETC prototype = new ETC();
        String myNote = "Hàng hiếm, chỉ giao dịch trực tiếp";

        ETC etc = (ETC) prototype.createItem(
                sellerId, itemName, itemId, desc, price,
                Item.ItemStatus.APPROVED, myNote
        );

        assertNotNull(etc);
        assertEquals(myNote, etc.getNote());
        assertEquals(Item.ItemStatus.APPROVED, etc.getStatus());
    }

    // ── Test 3: Getters & Setters ──────────────────────────────────

    @Test
    @DisplayName("Kiểm tra cập nhật Note qua Setter")
    void testETCSetters() {
        ETC etc = new ETC();
        etc.setNote("Cần kiểm tra lại giấy tờ");
        etc.setDescription("Mô tả mới");

        assertEquals("Cần kiểm tra lại giấy tờ", etc.getNote());
        assertEquals("Mô tả mới", etc.getDescription());
    }

    // ── Test 4: In thông tin đặc thù ──────────────────────────────

    @Test
    @DisplayName("Hàm printInfo không gây lỗi khi in thông tin mặt hàng khác")
    void testPrintInfoSuccess() {
        ETC etc = (ETC) Item.ItemType.ETC.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.PENDING
        );

        // Đảm bảo hàm in ghi chú chạy bình thường
        assertDoesNotThrow(() -> etc.printInfo());
    }

    // ── Test 5: Verify logic kế thừa (Anti-0.0 Error) ──────────────

    @Test
    @DisplayName("Đảm bảo giá của ETC được lưu đúng vào biến lớp cha")
    void testETCPriceIntegrity() {
        double testPrice = 123456.0;
        Item etc = Item.ItemType.ETC.create(
                sellerId, "Misc Item", "ID-99", "Desc", testPrice, Item.ItemStatus.PENDING
        );

        // Đây là bước chốt chặn cuối cùng để đảm bảo toàn bộ hệ thống Item không còn lỗi 0.0
        assertEquals(testPrice, etc.getStartPrice(), "Giá khởi điểm ETC phải khớp với giá truyền vào");
    }
}