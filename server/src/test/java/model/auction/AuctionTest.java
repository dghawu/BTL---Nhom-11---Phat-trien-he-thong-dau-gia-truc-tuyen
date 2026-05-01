package model.auction;

import exception.AuctionClosedException;
import exception.InvalidBidException;
import model.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auction Tests")
public class AuctionTest {

    private Auction auction;
    private Item laptop;

    @BeforeEach
    void setUp() {
        // Tạo item và phiên đấu giá mới trước mỗi test
        laptop = Item.ItemType.ELECTRONICS.create(
                "S01", "Laptop Dell", "ITEM-001",
                "Core i7", 10_000_000.0, Item.ItemStatus.APPROVED);

        auction = new Auction(
                "AUC-001", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30)
        );
        auction.setStatus("APPROVED");
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
        // Cần tối thiểu 10_000_000 + 500_000 = 10_500_000
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
        newAuction.setStatus("APPROVED");
        newAuction.startAuction();
        assertEquals("RUNNING", newAuction.getStatus());
    }

    @Test
    @DisplayName("startAuction khi PENDING → AuctionClosedException")
    void testStartAuctionWhenPending() {
        Auction newAuction = new Auction(
                "AUC-004", laptop, 10_000_000.0, 500_000.0,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)
        );
        // status = PENDING
        assertThrows(AuctionClosedException.class, newAuction::startAuction);
    }

    // ── closeAuction ───────────────────────────────────────────────

    @Test
    @DisplayName("closeAuction → status FINISHED")
    void testCloseAuction() {
        auction.closeAuction();
        assertEquals("FINISHED", auction.getStatus());
    }

    @Test
    @DisplayName("closeAuction có winner → currentWinner đúng")
    void testCloseAuctionWithWinner() {
        auction.handleNewBid(new BidTransaction("B01", "AUC-001", 10_500_000.0));
        auction.closeAuction();
        assertEquals("B01", auction.getCurrentWinner());
    }
}