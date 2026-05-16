package observer;

import model.auction.Auction;

public interface Subject {

    void addObserver(AuctionObserver observer);

    void removeObserver(AuctionObserver observer);

    void notifyObservers(Auction auction, double newPrice, String lastBidderId);
}