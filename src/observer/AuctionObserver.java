package observer;

import model.auction.Auction;

public interface AuctionObserver {
    void update(Auction auction, double newPrice, String lastBidderId);
}