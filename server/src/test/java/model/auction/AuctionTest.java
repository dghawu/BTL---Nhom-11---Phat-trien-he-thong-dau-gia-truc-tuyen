package model.auction;

import exception.*;
import model.enums.AuctionStatus;
import model.item.Item;
import observer.AuctionObserver;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ test hoàn chỉnh cho Auction — gộp từ AuctionTest và AuctionAdditionalTest.
 * Bao gồm: luồng trạng thái, handleNewBid, SelfBidException, bidderName,
 * Observer pattern, anti-sniping, InvalidItemPriceException, getters/setters,
 * BidTransaction getters/setters.
 */
@DisplayName("Auction - Complete Test Suite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionTest {

    // ── Shared fields ──────────────────────────────────────────────

    private Auction auction;
    private Item laptop;
    private Item item;

    // NoOp observer để theo dõi số lần được gọi
    static class CountingObserver implements AuctionObserver {
        int updateCount = 0;

        @Override
        public void update(Auction auction, double newPrice, String lastBidderId) {
            updateCount++;
        }
    }

    // ── Setup ──────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        // Dùng chung cho cả hai nhóm test
        laptop = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Dell", "ITEM-001",
                "Core i7", 10_000_000.0, Item.ItemStatus.APPROVED);

        item = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Test", "ITEM-TEST",
                "Mô tả test", 1_000_000.0, Item.ItemStatus.APPROVED);

        auction = new Auction(
                "AUC-001", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30)
        );
        auction.setStatus(AuctionStatus.APPROVED);
        auction.startAuction(); // → RUNNING
    }

    /**
     * Helper: tạo auction RUNNING với item nhỏ (startPrice = 1_000_000)
     */
    private Auction makeRunningAuction(String id, int minutesFromNow) {
        Auction a = new Auction(id, item, 1_000_000.0, 100_000.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(minutesFromNow));
        a.setStatus(AuctionStatus.APPROVED);
        a.startAuction();
        return a;
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 1 — handleNewBid (từ AuctionTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Đặt giá hợp lệ → thành công")
    void testValidBid() {
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 10_500_000.0);
        assertDoesNotThrow(() -> auction.handleNewBid(bid));
        assertEquals(10_500_000.0, auction.getCurrentPrice());
        assertEquals("B01", auction.getCurrentWinner());
    }

    @Test
    @Order(2)
    @DisplayName("Đặt giá thấp hơn giá tối thiểu → InvalidBidException")
    void testBidTooLow() {
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 10_100_000.0);
        assertThrows(InvalidBidException.class, () -> auction.handleNewBid(bid));
    }

    @Test
    @Order(3)
    @DisplayName("Đặt giá bằng đúng mức tối thiểu → thành công")
    void testBidExactMinimum() {
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 10_500_000.0);
        assertDoesNotThrow(() -> auction.handleNewBid(bid));
    }

    @Test
    @Order(4)
    @DisplayName("Đặt giá khi phiên chưa RUNNING → AuctionClosedException")
    void testBidOnPendingAuction() {
        Auction pendingAuction = new Auction(
                "AUC-002", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        BidTransaction bid = new BidTransaction("B01", "AUC-002", 11_000_000.0);
        assertThrows(AuctionClosedException.class, () -> pendingAuction.handleNewBid(bid));
    }

    @Test
    @Order(5)
    @DisplayName("Đặt giá khi phiên đã FINISHED → AuctionClosedException")
    void testBidOnFinishedAuction() {
        auction.closeAuction();
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 11_000_000.0);
        assertThrows(AuctionClosedException.class, () -> auction.handleNewBid(bid));
    }

    @Test
    @Order(6)
    @DisplayName("Nhiều bid liên tiếp → giá tăng dần đúng")
    void testMultipleBids() {
        auction.handleNewBid(new BidTransaction("B01", "AUC-001", 10_500_000.0));
        auction.handleNewBid(new BidTransaction("B02", "AUC-001", 11_000_000.0));
        auction.handleNewBid(new BidTransaction("B01", "AUC-001", 11_500_000.0));

        assertEquals(11_500_000.0, auction.getCurrentPrice());
        assertEquals("B01", auction.getCurrentWinner());
        assertEquals(3, auction.getBidHistory().size());
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 2 — startAuction (từ AuctionTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("startAuction khi APPROVED → RUNNING")
    void testStartAuction() {
        Auction newAuction = new Auction(
                "AUC-003", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        newAuction.setStatus(AuctionStatus.APPROVED);
        newAuction.startAuction();
        assertEquals(AuctionStatus.RUNNING, newAuction.getStatus());
    }

    @Test
    @Order(8)
    @DisplayName("startAuction khi PENDING → AuctionNotApprovedException")
    void testStartAuctionWhenPending() {
        Auction newAuction = new Auction(
                "AUC-004", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        assertThrows(AuctionNotApprovedException.class, newAuction::startAuction,
                "PENDING auction phải ném AuctionNotApprovedException khi gọi startAuction()");
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 3 — closeAuction & Status flow (từ AuctionTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("closeAuction → status FINISHED")
    void testCloseAuction() {
        auction.closeAuction();
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
    }

    @Test
    @Order(10)
    @DisplayName("closeAuction có winner → currentWinner đúng")
    void testCloseAuctionWithWinner() {
        auction.handleNewBid(new BidTransaction("B01", "AUC-001", 10_500_000.0));
        auction.closeAuction();
        assertEquals("B01", auction.getCurrentWinner());
    }

    @Test
    @Order(11)
    @DisplayName("Status flow: PENDING → APPROVED → RUNNING → FINISHED")
    void testStatusFlow() {
        Auction a = new Auction(
                "AUC-005", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        assertEquals(AuctionStatus.PENDING, a.getStatus());
        a.setStatus(AuctionStatus.APPROVED);
        assertEquals(AuctionStatus.APPROVED, a.getStatus());
        a.startAuction();
        assertEquals(AuctionStatus.RUNNING, a.getStatus());
        a.closeAuction();
        assertEquals(AuctionStatus.FINISHED, a.getStatus());
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 4 — SelfBidException (từ AuctionAdditionalTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("handleNewBid: Bidder đang thắng đặt giá lại → SelfBidException")
    void testSelfBidException() {
        Auction a = makeRunningAuction("AUC-SELF-01", 30);
        a.handleNewBid(new BidTransaction("B01", "AUC-SELF-01", 1_200_000.0));
        assertEquals("B01", a.getCurrentWinner());

        BidTransaction selfBid = new BidTransaction("B01", "AUC-SELF-01", 1_500_000.0);
        assertThrows(SelfBidException.class,
                () -> a.handleNewBid(selfBid),
                "Bidder đang thắng không được tự đặt giá lại");
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 5 — BidTransaction bidderName (từ AuctionAdditionalTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(13)
    @DisplayName("handleNewBid: BidTransaction có bidderName → currentWinner dùng name")
    void testBidWithBidderName() {
        Auction a = makeRunningAuction("AUC-NAME-01", 30);
        BidTransaction bid = new BidTransaction("B01", "AUC-NAME-01", 1_200_000.0);
        bid.setBidderName("Nguyen Van A");
        a.handleNewBid(bid);

        assertEquals("Nguyen Van A", a.getCurrentWinner(),
                "Khi bidderName có giá trị, currentWinner phải dùng name thay vì id");
    }

    @Test
    @Order(14)
    @DisplayName("handleNewBid: BidTransaction không có bidderName → currentWinner dùng bidderId")
    void testBidWithoutBidderName() {
        Auction a = makeRunningAuction("AUC-ID-01", 30);
        BidTransaction bid = new BidTransaction("B01", "AUC-ID-01", 1_200_000.0);
        assertNull(bid.getBidderName());
        a.handleNewBid(bid);

        assertEquals("B01", a.getCurrentWinner(),
                "Khi bidderName null, currentWinner phải dùng bidderId");
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 6 — Observer pattern (từ AuctionAdditionalTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(15)
    @DisplayName("Observer: addObserver → nhận thông báo khi đặt giá thành công")
    void testObserverReceivesUpdate() {
        Auction a = makeRunningAuction("AUC-OBS-01", 30);
        CountingObserver observer = new CountingObserver();
        a.addObserver(observer);

        a.handleNewBid(new BidTransaction("B01", "AUC-OBS-01", 1_200_000.0));

        assertEquals(1, observer.updateCount, "Observer phải được gọi đúng 1 lần");
    }

    @Test
    @Order(16)
    @DisplayName("Observer: removeObserver → không nhận thông báo sau khi xóa")
    void testObserverRemovedDoesNotReceiveUpdate() {
        Auction a = makeRunningAuction("AUC-OBS-02", 30);
        CountingObserver observer = new CountingObserver();
        a.addObserver(observer);
        a.removeObserver(observer);

        a.handleNewBid(new BidTransaction("B01", "AUC-OBS-02", 1_200_000.0));

        assertEquals(0, observer.updateCount, "Observer đã xóa không được nhận thông báo");
    }

    @Test
    @Order(17)
    @DisplayName("Observer: closeAuction → notifyObservers được gọi")
    void testObserverCalledOnClose() {
        Auction a = makeRunningAuction("AUC-OBS-03", 30);
        CountingObserver observer = new CountingObserver();
        a.addObserver(observer);

        a.closeAuction();

        assertEquals(1, observer.updateCount, "closeAuction phải gọi notifyObservers");
    }

    @Test
    @Order(18)
    @DisplayName("Observer: nhiều observers đều nhận thông báo")
    void testMultipleObservers() {
        Auction a = makeRunningAuction("AUC-OBS-04", 30);
        CountingObserver obs1 = new CountingObserver();
        CountingObserver obs2 = new CountingObserver();
        CountingObserver obs3 = new CountingObserver();
        a.addObserver(obs1);
        a.addObserver(obs2);
        a.addObserver(obs3);

        a.handleNewBid(new BidTransaction("B01", "AUC-OBS-04", 1_200_000.0));

        assertEquals(1, obs1.updateCount);
        assertEquals(1, obs2.updateCount);
        assertEquals(1, obs3.updateCount);
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 7 — Constructor validation (từ AuctionAdditionalTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(19)
    @DisplayName("Constructor: startPrice <= 0 → InvalidItemPriceException")
    void testInvalidStartPrice() {
        assertThrows(InvalidItemPriceException.class,
                () -> new Auction("AUC-INVAL-01", item, 0.0, 100.0,
                        LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                "startPrice = 0 phải ném InvalidItemPriceException");

        assertThrows(InvalidItemPriceException.class,
                () -> new Auction("AUC-INVAL-02", item, -500.0, 100.0,
                        LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                "startPrice âm phải ném InvalidItemPriceException");
    }

    @Test
    @Order(20)
    @DisplayName("Constructor: minIncrement <= 0 → InvalidItemPriceException")
    void testInvalidMinIncrement() {
        assertThrows(InvalidItemPriceException.class,
                () -> new Auction("AUC-INVAL-03", item, 1_000_000.0, 0.0,
                        LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                "minIncrement = 0 phải ném InvalidItemPriceException");
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 8 — Getters / Setters (từ AuctionAdditionalTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(21)
    @DisplayName("Getters: Trả về đúng giá trị sau khi khởi tạo")
    void testGetters() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);

        Auction a = new Auction("AUC-GET-01", item, 1_000_000.0, 100_000.0, start, end);

        assertEquals("AUC-GET-01", a.getAuctionId());
        assertEquals(1_000_000.0, a.getStartPrice());
        assertEquals(1_000_000.0, a.getCurrentPrice());
        assertEquals(100_000.0, a.getMinIncrement());
        assertEquals(AuctionStatus.PENDING, a.getStatus());
        assertNotNull(a.getItem());
        assertEquals(start, a.getStartTime());
        assertEquals(end, a.getEndTime());
        assertTrue(a.getBidHistory().isEmpty());
        assertNull(a.getCurrentWinner());
    }

    @Test
    @Order(22)
    @DisplayName("Setters: setCurrentPrice, setCurrentWinner, setEndTime, setMinIncrement hoạt động đúng")
    void testSetters() {
        Auction a = new Auction("AUC-SET-01", item, 1_000_000.0, 100_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        a.setCurrentPrice(2_000_000.0);
        assertEquals(2_000_000.0, a.getCurrentPrice());

        a.setCurrentWinner("B99");
        assertEquals("B99", a.getCurrentWinner());

        LocalDateTime newEnd = LocalDateTime.now().plusHours(3);
        a.setEndTime(newEnd);
        assertEquals(newEnd, a.getEndTime());

        a.setMinIncrement(500_000.0);
        assertEquals(500_000.0, a.getMinIncrement());
    }

    @Test
    @Order(23)
    @DisplayName("printInfo(): Không ném exception")
    void testPrintInfo() {
        Auction a = makeRunningAuction("AUC-PRINT-01", 30);
        assertDoesNotThrow(a::printInfo);
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 9 — closeAuction không có winner (từ AuctionAdditionalTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(24)
    @DisplayName("closeAuction: Không có người thắng → in thông báo 'không có người mua'")
    void testCloseAuctionNoWinner() {
        Auction a = makeRunningAuction("AUC-CLOSE-01", 30);
        assertNull(a.getCurrentWinner());
        assertDoesNotThrow(a::closeAuction);
        assertEquals(AuctionStatus.FINISHED, a.getStatus());
    }

    // ══════════════════════════════════════════════════════════════
    // Nhóm 10 — BidTransaction getters / setters (từ AuctionAdditionalTest)
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(25)
    @DisplayName("BidTransaction: Getters trả về giá trị đúng")
    void testBidTransactionGetters() {
        BidTransaction txn = new BidTransaction("B01", "AUC-001", 500_000.0);

        assertEquals("B01", txn.getBidderId());
        assertEquals("AUC-001", txn.getAuctionId());
        assertEquals(500_000.0, txn.getAmount());
        assertNotNull(txn.getTimestamp());
        assertNotNull(txn.getId());
        assertTrue(txn.getId().startsWith("TXN-"));
        assertNull(txn.getBidderName());
    }

    @Test
    @Order(26)
    @DisplayName("BidTransaction: setBidderName và setTimestamp hoạt động đúng")
    void testBidTransactionSetters() {
        BidTransaction txn = new BidTransaction("B01", "AUC-001", 1_000_000.0);
        txn.setBidderName("Tran Van B");
        LocalDateTime customTime = LocalDateTime.of(2024, 6, 1, 10, 30);
        txn.setTimestamp(customTime);

        assertEquals("Tran Van B", txn.getBidderName());
        assertEquals(customTime, txn.getTimestamp());
    }

    @Test
    @Order(27)
    @DisplayName("BidTransaction: printInfo() không ném exception")
    void testBidTransactionPrintInfo() {
        BidTransaction txn = new BidTransaction("B01", "AUC-001", 1_000_000.0);
        assertDoesNotThrow(txn::printInfo);
    }
}