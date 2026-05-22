package model.auction;

public class AutoBidConfig {
    private final String bidderId;
    private final String bidderName;
    private final double increment;
    private final double maxBid;

    public AutoBidConfig(String bidderId, String bidderName, double increment, double maxBid) {
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.increment = increment;
        this.maxBid = maxBid;
    }

    public String getBidderId()   { return bidderId; }
    public String getBidderName() { return bidderName; }
    public double getIncrement()  { return increment; }
    public double getMaxBid()     { return maxBid; }
}