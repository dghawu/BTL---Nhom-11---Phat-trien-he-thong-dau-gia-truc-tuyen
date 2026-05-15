package service;

import dao.AuctionDAO;
import exception.*;
import model.enums.AuctionStatus;
import model.auction.Auction;

import java.util.List;

public class AuctionManager {

    private static volatile AuctionManager instance;
    private final AuctionDAO auctionDAO;

    private AuctionManager() { this.auctionDAO = new AuctionDAO(); }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) instance = new AuctionManager();
            }
        }
        return instance;
    }

    public void addAuction(Auction auction) {
        // Kiểm tra trùng ID trước khi lưu
        if (auctionDAO.findById(auction.getAuctionId()) != null) {
            throw new AuctionAlreadyExistsException(auction.getAuctionId());
        }
        auctionDAO.save(auction);
        System.out.println("[AuctionManager] Thêm phiên: " + auction.getAuctionId());
    }

    public void removeAuction(String auctionId) { auctionDAO.delete(auctionId); }

    /** @throws AuctionNotFoundException nếu không tìm thấy */
    public Auction findAuction(String auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) throw new AuctionNotFoundException(auctionId);
        return auction;
    }

    public List<Auction> getAllAuctions()     { return auctionDAO.findAll(); }
    public List<Auction> getRunningAuctions(){ return auctionDAO.findByStatus(AuctionStatus.RUNNING); }
    public List<Auction> getPendingAuctions(){ return auctionDAO.findByStatus(AuctionStatus.PENDING); }

    /**
     * Duyệt phiên — chỉ được khi PENDING.
     * @throws AuctionNotFoundException nếu không tìm thấy
     * @throws AuctionClosedException   nếu phiên không ở trạng thái PENDING
     */
    public void approveAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        if (a.getStatus() != AuctionStatus.PENDING) {
            throw new AuctionClosedException(auctionId, a.getStatus());
        }
        a.setStatus(AuctionStatus.APPROVED);
        auctionDAO.updateStatus(auctionId, AuctionStatus.APPROVED);
        System.out.println("[AuctionManager] Duyệt phiên: " + auctionId);
    }

    /**
     * Từ chối phiên.
     * @throws AuctionNotFoundException nếu không tìm thấy
     */
    public void rejectAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        a.setStatus(AuctionStatus.REJECTED);
        auctionDAO.updateStatus(auctionId, AuctionStatus.REJECTED);
        System.out.println("[AuctionManager] Từ chối phiên: " + auctionId);
    }
}
