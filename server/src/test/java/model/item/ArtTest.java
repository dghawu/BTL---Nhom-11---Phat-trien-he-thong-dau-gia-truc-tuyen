package model.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Art Model & Factory Tests")
class ArtTest {

    private final String sellerId = "S_ART_01";
    private final String artName = "Mona Lisa Re-imagined";
    private final String artId = "ART-001";
    private final String description = "A modern take on a classic.";
    private final double price = 150_000_000.0;

    // ── Test 1: Khởi tạo mặc định qua Factory ─────────────────────

    @Test
    @DisplayName("Tạo Art mặc định → Artist và Medium phải là 'Không rõ'")
    void testCreateArtDefault() {
        // Sử dụng phương thức createItem chuẩn (override từ Item)
        Art art = (Art) Item.ItemType.ART.create(
                sellerId, artName, artId, description, price, Item.ItemStatus.PENDING
        );

        assertNotNull(art);
        assertEquals("Không rõ", art.getArtist());
        assertEquals("Không rõ", art.getMedium());
        assertEquals(price, art.getStartPrice());
    }

    // ── Test 2: Khởi tạo đầy đủ thông tin ──────────────────────────

    @Test
    @DisplayName("Tạo Art với thông tin nghệ sĩ → Dữ liệu phải chính xác")
    void testCreateArtWithDetails() {
        // Sử dụng prototype để gọi hàm createItem có tham số mở rộng
        Art prototype = new Art();
        Art art = (Art) prototype.createItem(
                sellerId, artName, artId, description, price,
                Item.ItemStatus.APPROVED, "Leonardo da Vinci", "Sơn dầu"
        );

        assertNotNull(art);
        assertEquals("Leonardo da Vinci", art.getArtist());
        assertEquals("Sơn dầu", art.getMedium());
        assertEquals(artId, art.getId());
    }

    // ── Test 3: Getters & Setters ──────────────────────────────────

    @Test
    @DisplayName("Kiểm tra cập nhật thông tin Artist và Medium")
    void testArtSetters() {
        Art art = new Art(); // Tạo prototype rỗng

        art.setArtist("Picasso");
        art.setMedium("Gốm sứ");
        art.setName("Bình cổ");

        assertEquals("Picasso", art.getArtist());
        assertEquals("Gốm sứ", art.getMedium());
        assertEquals("Bình cổ", art.getName());
    }

    // ── Test 4: In thông tin (Extra Info) ──────────────────────────

    @Test
    @DisplayName("Hàm printInfo không gây lỗi khi in thông tin Art")
    void testPrintInfoSuccess() {
        Art art = (Art) Item.ItemType.ART.create(
                sellerId, artName, artId, description, price, Item.ItemStatus.PENDING
        );

        // Kiểm tra xem việc gọi printInfo (bao gồm cả printExtraInfo của Art) có hoạt động không
        assertDoesNotThrow(() -> art.printInfo());
    }

    // ── Test 5: Kiểm tra tính toàn vẹn dữ liệu từ lớp cha ───────────

    @Test
    @DisplayName("Dữ liệu Art phải được truyền lên lớp cha Item đầy đủ")
    void testInheritanceData() {
        Art art = (Art) Item.ItemType.ART.create(
                sellerId, artName, artId, description, price, Item.ItemStatus.PENDING
        );

        // Kiểm tra logic kế thừa: Giá khởi điểm và Status
        assertEquals(price, art.getStartPrice());
        assertEquals(Item.ItemStatus.PENDING, art.getStatus());
        assertEquals(sellerId, art.getSellerId());
    }
}