package com.example.observer;

import com.example.model.auction.Auction;

public interface Subject {

    void addObserver(AuctionObserver observer);

    void removeObserver(AuctionObserver observer);

    void notifyObservers(Auction auction, double newPrice, String lastBidderId);
}