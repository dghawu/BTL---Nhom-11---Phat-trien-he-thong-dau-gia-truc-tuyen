package observer;

public interface Subject {

    void addObserver(AuctionObserver observer);
    void removeObserver(AuctionObserver observer);
    void notifyObservers(String message);

}