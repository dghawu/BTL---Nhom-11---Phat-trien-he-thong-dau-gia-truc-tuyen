package model.user;

import com.example.model.auction.Auction;
import com.example.model.auction.BidTransaction;
import com.example.model.item.Item;
import com.example.model.user.Admin;
import com.example.model.user.Bidder;
import com.example.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Admin Logic Tests")
class AdminTest {

    private Admin admin;
    private List<User> userList;
    private List<Auction> auctionList;
    private List<BidTransaction> transactionList;

    @BeforeEach
    void setUp() {
        // 1. Khởi tạo Admin
        admin = new Admin("AD01", "UET_Admin", "admin123");

        // 2. Khởi tạo danh sách User giả lập
        userList = new ArrayList<>();
        userList.add(new Bidder("B01", "NguyenVanA", "pass1"));
        userList.add(new Bidder("B02", "TranThiB", "pass2"));

        // 3. Khởi tạo danh sách Auction giả lập
        auctionList = new ArrayList<>();
        Item item = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop", "I-01", "Del", 1000.0, Item.ItemStatus.PENDING);
        auctionList.add(new Auction("AUC-01", item, 1000.0, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)));

        // 4. Khởi tạo danh sách giao dịch
        transactionList = new ArrayList<>();
        transactionList.add(new BidTransaction("B01", "AUC-01", 1100.0));
    }

    // ── Test 1: Ban User ──────────────────────────────────────────

    @Test
    @DisplayName("Ban user tồn tại trong danh sách → List giảm 1 phần tử")
    void testBanUserSuccess() {
        int originalSize = userList.size();
        admin.banUser(userList, "NguyenVanA");

        assertEquals(originalSize - 1, userList.size());
        // Kiểm tra xem user còn lại có đúng là TranThiB không
        assertEquals("TranThiB", userList.get(0).getName());
    }

    @Test
    @DisplayName("Ban user không tồn tại → List giữ nguyên kích thước")
    void testBanUserNotFound() {
        int originalSize = userList.size();
        admin.banUser(userList, "User_Khong_Ton_Tai");

        assertEquals(originalSize, userList.size());
    }

    // ── Test 2: Kiểm duyệt sản phẩm (Moderate Item) ───────────────

    @Test
    @DisplayName("Xóa sản phẩm vi phạm bằng index hợp lệ → thành công")
    void testModerateItemSuccess() {
        assertFalse(auctionList.isEmpty());
        admin.moderateItem(auctionList, 0);

        assertTrue(auctionList.isEmpty());
    }

    @Test
    @DisplayName("Xóa sản phẩm bằng index sai → không xóa gì")
    void testModerateItemInvalidIndex() {
        admin.moderateItem(auctionList, 99); // Index không tồn tại
        assertEquals(1, auctionList.size());
    }

    // ── Test 3: Xóa phiên đấu giá (Remove Auction) ────────────────

    @Test
    @DisplayName("Xóa phiên đấu giá bằng index → thành công")
    void testRemoveAuctionSuccess() {
        admin.removeInvalidAuction(auctionList, 0);
        assertEquals(0, auctionList.size());
    }

    // ── Test 4: Xem giao dịch ─────────────────────────────────────

    @Test
    @DisplayName("Review giao dịch không gây lỗi (Exception)")
    void testReviewTransactions() {
        // Vì hàm này chỉ in ra màn hình (System.out), ta test để đảm bảo nó không crash
        assertDoesNotThrow(() -> admin.reviewAllTransactions(transactionList));
    }

    // ── Test 5: Kế thừa (Inheritance) ──────────────────────────────

    @Test
    @DisplayName("Kiểm tra ID và Role được kế thừa đúng")
    void testAdminIdentity() {
        assertEquals("AD01", admin.getId());
        // Class của bạn hardcode "ADMIN" trong super()
        // Lưu ý: Nếu bạn dùng Enum ở lớp cha thì nên sửa lại cho đồng bộ
        assertDoesNotThrow(() -> admin.printInfo());
    }
}