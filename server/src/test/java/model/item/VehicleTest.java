package model.item;

import com.example.model.item.Item;
import com.example.model.item.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vehicle Model & Factory Tests")
class VehicleTest {

    private final String sellerId = "S_VEHICLE_01";
    private final String itemName = "Toyota Camry 2022";
    private final String itemId = "VEH-999";
    private final String desc = "Xe chính chủ, bảo dưỡng định kỳ";
    private final double price = 800_000_000.0;

    // ── Test 1: Khởi tạo mặc định qua Factory ─────────────────────

    @Test
    @DisplayName("Factory: Tạo Vehicle mặc định → Mileage phải bằng 0")
    void testCreateVehicleDefault() {
        // Sử dụng enum ItemType để khởi tạo qua cơ chế Prototype
        Vehicle v = (Vehicle) Item.ItemType.VEHICLE.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.PENDING
        );

        assertNotNull(v);
        assertEquals(0L, v.getMileage(), "Xe mới tạo qua factory mặc định phải có 0 km");
        assertEquals(price, v.getStartPrice());
        assertEquals(itemId, v.getId());
    }

    // ── Test 2: Khởi tạo với số km tùy chỉnh ──────────────────────

    @Test
    @DisplayName("Factory: Tạo Vehicle đã qua sử dụng → Dữ liệu mileage phải chính xác")
    void testCreateVehicleCustom() {
        Vehicle prototype = new Vehicle();
        long expectedMileage = 15500L;

        Vehicle v = (Vehicle) prototype.createItem(
                sellerId, itemName, itemId, desc, price,
                Item.ItemStatus.APPROVED, expectedMileage
        );

        assertNotNull(v);
        assertEquals(expectedMileage, v.getMileage());
        assertEquals(Item.ItemStatus.APPROVED, v.getStatus());
    }

    // ── Test 3: Getters & Setters ──────────────────────────────────

    @Test
    @DisplayName("Kiểm tra cập nhật Mileage qua Setter")
    void testVehicleSetters() {
        Vehicle v = new Vehicle();
        v.setMileage(50000L);
        v.setName("Honda CR-V");

        assertEquals(50000L, v.getMileage());
        assertEquals("Honda CR-V", v.getName());
    }

    // ── Test 4: In thông tin đặc thù ──────────────────────────────

    @Test
    @DisplayName("Hàm printInfo không gây lỗi khi in thông tin xe cộ")
    void testPrintInfoSuccess() {
        Vehicle v = (Vehicle) Item.ItemType.VEHICLE.create(
                sellerId, itemName, itemId, desc, price, Item.ItemStatus.APPROVED
        );

        // Đảm bảo printExtraInfo (in mileage định dạng %,d) không gây lỗi format
        assertDoesNotThrow(() -> v.printInfo());
    }

    // ── Test 5: Kiểm tra tính toàn vẹn giá tiền (Fix lỗi 0.0) ──────

    @Test
    @DisplayName("Đảm bảo giá xe không bị trả về 0.0 do lỗi kế thừa")
    void testVehiclePriceIntegrity() {
        double luxuryPrice = 2_500_000_000.0;
        Item v = Item.ItemType.VEHICLE.create(
                sellerId, "Mercedes S450", "VEH-001", "Luxury car", luxuryPrice, Item.ItemStatus.PENDING
        );

        // Kiểm tra xem super(...) trong Vehicle có gán đúng startingPrice vào lớp cha không
        assertEquals(luxuryPrice, v.getStartPrice(), "Giá khởi điểm của xe phải khớp với giá mong đợi");
    }
}