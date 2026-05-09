package server;

import exception.UserNotFoundException;
import model.user.Admin;
import model.user.Bidder;
import model.user.Seller;
import model.user.User;
import org.junit.jupiter.api.*;
import service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserService Security & Management Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = UserService.getInstance();
        // Làm sạch danh sách user trước mỗi lần test để tránh ảnh hưởng chéo
        // Nếu List<User> là private, bạn có thể cân nhắc thêm hàm clear cho mục đích test
        service.getAllUsers().clear();
    }

    // ── Test 1: Đăng ký (Registration) ───────────────────────────

    @Test
    @Order(1)
    @DisplayName("Register: Đăng ký các loại User khác nhau")
    void testRegisterUsers() {
        Bidder b = service.registerBidder("B01", "AnBidder", "pass123");
        Seller s = service.registerSeller("S01", "BinhSeller", "pass456");
        Admin a = service.registerAdmin("A01", "AdminUET", "root123");

        assertNotNull(b);
        assertTrue(service.getAllBidders().contains(b));
        assertTrue(service.getAllSellers().contains(s));
        assertEquals(3, service.getAllUsers().size());
    }

    @Test
    @Order(2)
    @DisplayName("Register: Chặn đăng ký trùng tên")
    void testDuplicateNameRegistration() {
        service.registerBidder("B01", "NguoiDung1", "123");

        assertThrows(IllegalArgumentException.class, () -> {
            service.registerSeller("S02", "NguoiDung1", "456");
        }, "Phải ném exception khi đăng ký trùng tên người dùng");
    }

    // ── Test 2: Đăng nhập (Login) ────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("Login: Đăng nhập thành công và thất bại")
    void testLoginLogic() throws UserNotFoundException {
        service.registerBidder("B02", "Dung", "secret");

        // Thành công
        User u = service.login("Dung", "secret");
        assertNotNull(u);
        assertEquals("B02", u.getId());

        // Sai mật khẩu/tên -> Ném Exception
        assertThrows(UserNotFoundException.class, () -> service.login("Dung", "wrongpass"));
        assertThrows(UserNotFoundException.class, () -> service.login("UnknowUser", "123"));
    }

    // ── Test 3: Quản lý mật khẩu & Ban User ───────────────────────

    @Test
    @Order(4)
    @DisplayName("Management: Đổi mật khẩu thành công")
    void testChangePassword() throws UserNotFoundException {
        service.registerBidder("B03", "Lan", "oldPass");

        service.changePassword("B03", "oldPass", "newPass");

        // Kiểm tra mật khẩu mới có đăng nhập được không
        assertNotNull(service.login("Lan", "newPass"));
    }

    @Test
    @Order(5)
    @DisplayName("Management: Ban user và kiểm tra biến mất khỏi list")
    void testBanUser() throws UserNotFoundException {
        service.registerBidder("B04", "UserBiBan", "123");

        service.banUser("B04");

        assertEquals(0, service.getAllUsers().size());
        assertThrows(UserNotFoundException.class, () -> service.findById("B04"));
    }

    // ── Test 4: Lọc danh sách (Stream & Filter) ────────────────────

    @Test
    @Order(6)
    @DisplayName("Filter: Phân loại danh sách Bidders và Sellers")
    void testFilterUserTypes() {
        service.registerBidder("B05", "B1", "1");
        service.registerBidder("B06", "B2", "1");
        service.registerSeller("S05", "S1", "1");

        List<Bidder> bidders = service.getAllBidders();
        List<Seller> sellers = service.getAllSellers();

        assertEquals(2, bidders.size());
        assertEquals(1, sellers.size());
    }
}