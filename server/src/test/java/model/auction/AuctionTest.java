package model.auction;

import exception.AuctionClosedException;
import exception.AuctionNotApprovedException;
import exception.InvalidBidException;
import model.enums.AuctionStatus;
import model.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auction Tests")
class AuctionTest {

    private Auction auction;
    private Item laptop;

    @BeforeEach
    void setUp() {
        laptop = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Dell", "ITEM-001",
                "Core i7", 10_000_000.0, Item.ItemStatus.APPROVED);

        auction = new Auction(
                "AUC-001", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30)
        );
        // FIX: dùng enum thay vì String
        auction.setStatus(AuctionStatus.APPROVED);
        auction.startAuction(); // → RUNNING
    }

    // ── handleNewBid ───────────────────────────────────────────────

    @Test
    @DisplayName("Đặt giá hợp lệ → thành công")
    void testValidBid() {
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 10_500_000.0);
        assertDoesNotThrow(() -> auction.handleNewBid(bid));
        assertEquals(10_500_000.0, auction.getCurrentPrice());
        assertEquals("B01", auction.getCurrentWinner());
    }

    @Test
    @DisplayName("Đặt giá thấp hơn giá tối thiểu → InvalidBidException")
    void testBidTooLow() {
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 10_100_000.0);
        assertThrows(InvalidBidException.class, () -> auction.handleNewBid(bid));
    }

    @Test
    @DisplayName("Đặt giá bằng đúng mức tối thiểu → thành công")
    void testBidExactMinimum() {
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 10_500_000.0);
        assertDoesNotThrow(() -> auction.handleNewBid(bid));
    }

    @Test
    @DisplayName("Đặt giá khi phiên chưa RUNNING → AuctionClosedException")
    void testBidOnPendingAuction() {
        Auction pendingAuction = new Auction(
                "AUC-002", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        // status = PENDING, chưa startAuction()
        BidTransaction bid = new BidTransaction("B01", "AUC-002", 11_000_000.0);
        assertThrows(AuctionClosedException.class, () -> pendingAuction.handleNewBid(bid));
    }

    @Test
    @DisplayName("Đặt giá khi phiên đã FINISHED → AuctionClosedException")
    void testBidOnFinishedAuction() {
        auction.closeAuction(); // → FINISHED
        BidTransaction bid = new BidTransaction("B01", "AUC-001", 11_000_000.0);
        assertThrows(AuctionClosedException.class, () -> auction.handleNewBid(bid));
    }

    @Test
    @DisplayName("Nhiều bid liên tiếp → giá tăng dần đúng")
    void testMultipleBids() {
        auction.handleNewBid(new BidTransaction("B01", "AUC-001", 10_500_000.0));
        auction.handleNewBid(new BidTransaction("B02", "AUC-001", 11_000_000.0));
        auction.handleNewBid(new BidTransaction("B01", "AUC-001", 11_500_000.0));

        assertEquals(11_500_000.0, auction.getCurrentPrice());
        assertEquals("B01", auction.getCurrentWinner());
        assertEquals(3, auction.getBidHistory().size());
    }

    // ── startAuction ───────────────────────────────────────────────

    @Test
    @DisplayName("startAuction khi APPROVED → RUNNING")
    void testStartAuction() {
        Auction newAuction = new Auction(
                "AUC-003", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        // FIX: dùng enum
        newAuction.setStatus(AuctionStatus.APPROVED);
        newAuction.startAuction();
        assertEquals(AuctionStatus.RUNNING, newAuction.getStatus());
    }

    @Test
    @DisplayName("startAuction khi PENDING → AuctionNotApprovedException")
    void testStartAuctionWhenPending() {
        Auction newAuction = new Auction(
                "AUC-004", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        // status = PENDING mặc định
        assertThrows(AuctionNotApprovedException.class, newAuction::startAuction,
                "PENDING auction phải ném AuctionNotApprovedException khi gọi startAuction()");
    }

    // ── closeAuction ───────────────────────────────────────────────

    @Test
    @DisplayName("closeAuction → status FINISHED")
    void testCloseAuction() {
        auction.closeAuction();
        // FIX: so sánh với enum
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
    }

    @Test
    @DisplayName("closeAuction có winner → currentWinner đúng")
    void testCloseAuctionWithWinner() {
        auction.handleNewBid(new BidTransaction("B01", "AUC-001", 10_500_000.0));
        auction.closeAuction();
        assertEquals("B01", auction.getCurrentWinner());
    }

    // ── Status flow ────────────────────────────────────────────────

    @Test
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
}