package com.example.observer;

import com.example.model.auction.Auction;

public interface AuctionObserver {
    void update(Auction auction, double newPrice, String lastBidderId);
}