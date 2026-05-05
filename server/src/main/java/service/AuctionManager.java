package service;

import dao.AuctionDAO;
import model.enums.AuctionStatus;
import model.auction.Auction;

import java.util.List;

public class AuctionManager {

    private static volatile AuctionManager instance;
    private final AuctionDAO auctionDAO;

    private AuctionManager() {
        this.auctionDAO = new AuctionDAO();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) instance = new AuctionManager();
            }
        }
        return instance;
    }

    // ── Quản lý phiên ─────────────────────────────────────────────

    public void addAuction(Auction auction) {
        auctionDAO.save(auction);
        System.out.println("[AuctionManager] Thêm phiên: " + auction.getAuctionId());
    }

    public void removeAuction(String auctionId) {
        auctionDAO.delete(auctionId);
    }

    public Auction findAuction(String auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên: " + auctionId);
        }
        return auction;
    }

    public List<Auction> getAllAuctions()     { return auctionDAO.findAll(); }

    // ← dùng AuctionStatus enum thay vì String
    public List<Auction> getRunningAuctions() {
        return auctionDAO.findByStatus(AuctionStatus.RUNNING);
    }

    public List<Auction> getPendingAuctions() {
        return auctionDAO.findByStatus(AuctionStatus.PENDING);
    }

    // ── Duyệt / Từ chối ───────────────────────────────────────────

    public void approveAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        // ← so sánh bằng == thay vì .equals()
        if (a.getStatus() == AuctionStatus.PENDING) {
            a.setStatus(AuctionStatus.APPROVED);
            auctionDAO.updateStatus(auctionId, AuctionStatus.APPROVED);
            System.out.println("[AuctionManager] Duyệt phiên: " + auctionId);
        } else {
            System.out.println("Phiên không ở trạng thái PENDING.");
        }
    }

    public void rejectAuction(String auctionId) {
        Auction a = findAuction(auctionId);
        a.setStatus(AuctionStatus.REJECTED);
        auctionDAO.updateStatus(auctionId, AuctionStatus.REJECTED);
        System.out.println("[AuctionManager] Từ chối phiên: " + auctionId);
    }
}