package service;

import model.auction.AutoBidConfig;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho AutoBidManager — quản lý cấu hình auto-bid cho các phiên đấu giá.
 * AutoBidConfig cũng được kiểm thử tại đây vì nó là dữ liệu đơn giản.
 */
@DisplayName("AutoBidManager & AutoBidConfig Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AutoBidManagerTest {

    private AutoBidManager manager;

    // Singleton AutoBidManager cần reset giữa các test
    @BeforeEach
    void setUp() {
        manager = AutoBidManager.getInstance();
        // Dọn sạch trạng thái giữa các test
        manager.clear("SESSION-001");
        manager.clear("SESSION-002");
        manager.clear("SESSION-003");
    }

    // ── Test AutoBidConfig ─────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("AutoBidConfig: Tạo config với đầy đủ thông tin")
    void testAutoBidConfigCreation() {
        AutoBidConfig config = new AutoBidConfig("B01", "Alice", 100.0, 5000.0);

        assertEquals("B01", config.getBidderId());
        assertEquals("Alice", config.getBidderName());
        assertEquals(100.0, config.getIncrement());
        assertEquals(5000.0, config.getMaxBid());
    }

    @Test
    @Order(2)
    @DisplayName("AutoBidConfig: Các giá trị được lưu trữ chính xác")
    void testAutoBidConfigValues() {
        AutoBidConfig config = new AutoBidConfig("B99", "Bob", 500.0, 99999.0);

        assertEquals("B99", config.getBidderId());
        assertEquals("Bob", config.getBidderName());
        assertEquals(500.0, config.getIncrement(), 0.001);
        assertEquals(99999.0, config.getMaxBid(), 0.001);
    }

    // ── Test Singleton ─────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("Singleton: Luôn trả về cùng một instance")
    void testSingleton() {
        AutoBidManager m1 = AutoBidManager.getInstance();
        AutoBidManager m2 = AutoBidManager.getInstance();
        assertSame(m1, m2, "AutoBidManager phải là Singleton");
    }

    // ── Test register() ────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("register(): Đăng ký config mới → getConfigs trả về đúng config")
    void testRegisterNewConfig() {
        AutoBidConfig config = new AutoBidConfig("B01", "Alice", 100.0, 3000.0);
        manager.register("SESSION-001", config);

        List<AutoBidConfig> configs = manager.getConfigs("SESSION-001");
        assertEquals(1, configs.size());
        assertEquals("B01", configs.get(0).getBidderId());
    }

    @Test
    @Order(5)
    @DisplayName("register(): Đăng ký nhiều bidder trong cùng phiên")
    void testRegisterMultipleBidders() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-001", new AutoBidConfig("B02", "Bob", 200.0, 3000.0));
        manager.register("SESSION-001", new AutoBidConfig("B03", "Carol", 150.0, 4000.0));

        assertEquals(3, manager.getConfigs("SESSION-001").size());
    }

    @Test
    @Order(6)
    @DisplayName("register(): Đăng ký lại config cũ của cùng bidder → thay thế config cũ")
    void testRegisterOverwritesExisting() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 200.0, 5000.0)); // update

        List<AutoBidConfig> configs = manager.getConfigs("SESSION-001");
        assertEquals(1, configs.size(), "Phải chỉ có 1 config cho bidder B01");
        assertEquals(200.0, configs.get(0).getIncrement(), "Increment phải được cập nhật");
        assertEquals(5000.0, configs.get(0).getMaxBid(), "MaxBid phải được cập nhật");
    }

    @Test
    @Order(7)
    @DisplayName("register(): Mỗi session độc lập nhau")
    void testRegisterDifferentSessions() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-002", new AutoBidConfig("B02", "Bob", 100.0, 2000.0));

        assertEquals(1, manager.getConfigs("SESSION-001").size());
        assertEquals(1, manager.getConfigs("SESSION-002").size());
    }

    @Test
    @Order(8)
    @DisplayName("register(): Cùng bidder đăng ký auto-bid ở 2 phiên khác nhau → cả 2 đều được lưu độc lập")
    void testSameBidderInMultipleSessions() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-002", new AutoBidConfig("B01", "Alice", 200.0, 5000.0));

        // Cả 2 session đều có config của B01
        assertEquals(1, manager.getConfigs("SESSION-001").size());
        assertEquals(1, manager.getConfigs("SESSION-002").size());

        // Config ở mỗi session độc lập, không ảnh hưởng nhau
        assertEquals(100.0, manager.getConfigs("SESSION-001").get(0).getIncrement(),
                "Increment ở SESSION-001 không được bị ghi đè bởi SESSION-002");
        assertEquals(200.0, manager.getConfigs("SESSION-002").get(0).getIncrement(),
                "Increment ở SESSION-002 phải giữ nguyên giá trị riêng");

        // Xóa SESSION-001 không kéo theo SESSION-002
        manager.clear("SESSION-001");
        assertTrue(manager.getConfigs("SESSION-001").isEmpty(),
                "SESSION-001 phải rỗng sau khi clear");
        assertEquals(1, manager.getConfigs("SESSION-002").size(),
                "Auto-bid của B01 ở SESSION-002 phải còn nguyên");
    }

    // ── Test unregister() ──────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("unregister(): Hủy auto-bid của một bidder → xóa khỏi danh sách")
    void testUnregisterBidder() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-001", new AutoBidConfig("B02", "Bob", 100.0, 3000.0));

        manager.unregister("SESSION-001", "B01");

        List<AutoBidConfig> configs = manager.getConfigs("SESSION-001");
        assertEquals(1, configs.size());
        assertEquals("B02", configs.get(0).getBidderId());
    }

    @Test
    @Order(10)
    @DisplayName("unregister(): Hủy bidder không tồn tại → không lỗi")
    void testUnregisterNonExistentBidder() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));

        assertDoesNotThrow(() -> manager.unregister("SESSION-001", "B99"),
                "Hủy bidder không tồn tại không được ném exception");

        assertEquals(1, manager.getConfigs("SESSION-001").size(),
                "Danh sách phải giữ nguyên");
    }

    @Test
    @Order(11)
    @DisplayName("unregister(): Hủy bidder trong phiên chưa có config → không lỗi")
    void testUnregisterFromEmptySession() {
        assertDoesNotThrow(() -> manager.unregister("SESSION-EMPTY", "B01"));
    }

    @Test
    @Order(12)
    @DisplayName("unregister(): Hủy ở một phiên không ảnh hưởng phiên khác cùng bidder")
    void testUnregisterOnlyAffectsTargetSession() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-002", new AutoBidConfig("B01", "Alice", 200.0, 5000.0));

        manager.unregister("SESSION-001", "B01");

        assertTrue(manager.getConfigs("SESSION-001").isEmpty(),
                "B01 phải bị xóa khỏi SESSION-001");
        assertEquals(1, manager.getConfigs("SESSION-002").size(),
                "B01 ở SESSION-002 không được bị ảnh hưởng");
    }

    // ── Test getConfigs() ──────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("getConfigs(): Phiên chưa có config → trả về list rỗng")
    void testGetConfigsEmpty() {
        List<AutoBidConfig> configs = manager.getConfigs("SESSION-NONEXISTENT");
        assertNotNull(configs);
        assertTrue(configs.isEmpty());
    }

    // ── Test clear() ───────────────────────────────────────────────

    @Test
    @Order(14)
    @DisplayName("clear(): Xóa toàn bộ auto-bid của một phiên khi kết thúc")
    void testClearSession() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-001", new AutoBidConfig("B02", "Bob", 100.0, 3000.0));

        manager.clear("SESSION-001");

        assertTrue(manager.getConfigs("SESSION-001").isEmpty(),
                "Sau khi clear, danh sách phải rỗng");
    }

    @Test
    @Order(15)
    @DisplayName("clear(): Chỉ xóa phiên được chỉ định, không ảnh hưởng phiên khác")
    void testClearOnlyTargetSession() {
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 2000.0));
        manager.register("SESSION-002", new AutoBidConfig("B02", "Bob", 100.0, 2000.0));

        manager.clear("SESSION-001");

        assertTrue(manager.getConfigs("SESSION-001").isEmpty());
        assertEquals(1, manager.getConfigs("SESSION-002").size(),
                "Phiên khác không bị ảnh hưởng");
    }

    @Test
    @Order(16)
    @DisplayName("clear(): Xóa phiên không tồn tại → không lỗi")
    void testClearNonExistentSession() {
        assertDoesNotThrow(() -> manager.clear("SESSION-DOES-NOT-EXIST"));
    }

    // ── Test toàn bộ vòng đời ─────────────────────────────────────

    @Test
    @Order(17)
    @DisplayName("Vòng đời đầy đủ: đăng ký → cập nhật → hủy từng bidder → clear phiên")
    void testFullLifecycle() {
        String session = "SESSION-003";

        // Đăng ký 3 bidder
        manager.register(session, new AutoBidConfig("B01", "Alice", 100.0, 5000.0));
        manager.register(session, new AutoBidConfig("B02", "Bob", 200.0, 8000.0));
        manager.register(session, new AutoBidConfig("B03", "Carol", 150.0, 6000.0));
        assertEquals(3, manager.getConfigs(session).size());

        // Alice cập nhật config
        manager.register(session, new AutoBidConfig("B01", "Alice", 300.0, 7000.0));
        assertEquals(3, manager.getConfigs(session).size(), "Cập nhật không thêm mới");

        // Bob hủy
        manager.unregister(session, "B02");
        assertEquals(2, manager.getConfigs(session).size());

        // Phiên kết thúc → clear
        manager.clear(session);
        assertEquals(0, manager.getConfigs(session).size());
    }

    @Test
    @Order(18)
    @DisplayName("Vòng đời đầy đủ: cùng bidder tham gia 2 phiên song song → quản lý độc lập đến khi kết thúc")
    void testFullLifecycleMultiSession() {
        // Alice tham gia 2 phiên cùng lúc với config khác nhau
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 100.0, 3000.0));
        manager.register("SESSION-002", new AutoBidConfig("B01", "Alice", 500.0, 9000.0));

        // Alice cập nhật config riêng ở từng phiên
        manager.register("SESSION-001", new AutoBidConfig("B01", "Alice", 150.0, 4000.0));
        manager.register("SESSION-002", new AutoBidConfig("B01", "Alice", 600.0, 10000.0));

        assertEquals(150.0, manager.getConfigs("SESSION-001").get(0).getIncrement(),
                "Config SESSION-001 phải dùng giá trị cập nhật mới nhất");
        assertEquals(600.0, manager.getConfigs("SESSION-002").get(0).getIncrement(),
                "Config SESSION-002 phải dùng giá trị cập nhật mới nhất");

        // SESSION-001 kết thúc trước
        manager.clear("SESSION-001");
        assertTrue(manager.getConfigs("SESSION-001").isEmpty());
        assertEquals(1, manager.getConfigs("SESSION-002").size(),
                "Alice vẫn đang auto-bid ở SESSION-002");

        // SESSION-002 kết thúc sau
        manager.clear("SESSION-002");
        assertTrue(manager.getConfigs("SESSION-002").isEmpty());
    }
}