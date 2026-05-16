package model.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fashion Model & Factory Tests")
class FashionTest {

    private final String sellerId = "S_FASHION_01";
    private final String itemName = "Áo khoác Vintage";
    private final String itemId = "FASH-789";
    private final String desc = "Áo khoác dạ màu nâu";
    private final double price = 1_200_000.0;

    // ── Test 1: Khởi tạo mặc định qua Factory ─────────────────────

    @Test
    @DisplayName("Factory: Tạo Fashion mặc định → Size và Brand phải là giá trị mặc định")
    void testCreateFashionDefault() {
        // Sử dụng enum ItemType để tạo đối tượng
        Fashion f = (Fashion) Item.ItemType.FASHION.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.PENDING
        );

        assertNotNull(f);
        assertEquals("One size", f.getSize());
        assertEquals("Không rõ", f.getBrand());
        assertEquals(price, f.getStartPrice());
    }

    // ── Test 2: Khởi tạo đầy đủ thông tin tùy chỉnh ───────────────

    @Test
    @DisplayName("Factory: Tạo Fashion tùy chỉnh thông số → Dữ liệu phải khớp")
    void testCreateFashionCustom() {
        Fashion prototype = new Fashion();
        Fashion f = (Fashion) prototype.createItem(
                sellerId, itemName, itemId, desc, price,
                Item.ItemStatus.APPROVED, "XL", "Nike"
        );

        assertNotNull(f);
        assertEquals("XL", f.getSize());
        assertEquals("Nike", f.getBrand());
        assertEquals(itemId, f.getId());
    }

    // ── Test 3: Getters & Setters ──────────────────────────────────

    @Test
    @DisplayName("Kiểm tra cập nhật Brand và Size qua Setter")
    void testFashionSetters() {
        Fashion f = new Fashion();
        f.setSize("M");
        f.setBrand("Adidas");
        f.setName("Quần Jogger");

        assertEquals("M", f.getSize());
        assertEquals("Adidas", f.getBrand());
        assertEquals("Quần Jogger", f.getName());
    }

    // ── Test 4: In thông tin đặc thù ──────────────────────────────

    @Test
    @DisplayName("Hàm printInfo không gây lỗi khi in thông tin thời trang")
    void testPrintInfoSuccess() {
        Fashion f = (Fashion) Item.ItemType.FASHION.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.PENDING
        );

        // Đảm bảo printExtraInfo (in brand, size) chạy bình thường
        assertDoesNotThrow(() -> f.printInfo());
    }

    // ── Test 5: Verify dữ liệu lớp cha (Kiểm tra lỗi 0.0) ──────────

    @Test
    @DisplayName("Đảm bảo giá khởi điểm Fashion không bị reset về 0.0")
    void testFashionPriceIntegrity() {
        double expectedPrice = 500000.0;
        Item f = Item.ItemType.FASHION.create(
                sellerId, "Test Shirt", "ID-01", "Description", expectedPrice, Item.ItemStatus.PENDING
        );

        // Kiểm tra xem giá trị có thực sự được truyền qua super() không
        assertEquals(expectedPrice, f.getStartPrice(), "Giá khởi điểm phải được lưu đúng vào lớp cha Item");
    }
}