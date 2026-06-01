package com.example.exception;

/**
 * Ném khi số tiền đặt giá thấp hơn mức tối thiểu (currentPrice + minIncrement).
 */
public class InvalidBidException extends AuctionSystemException {

    private final double attemptedAmount;
    private final double requiredAmount;

    public InvalidBidException(double attemptedAmount, double requiredAmount) {
        super("BID_TOO_LOW",
                String.format("Giá đặt không hợp lệ: bạn đặt %.0f VNĐ "
                                + "nhưng mức tối thiểu là %.0f VNĐ "
                                + "(chênh lệch: %.0f VNĐ).",
                        attemptedAmount, requiredAmount,
                        requiredAmount - attemptedAmount));
        this.attemptedAmount = attemptedAmount;
        this.requiredAmount = requiredAmount;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public double getRequiredAmount() {
        return requiredAmount;
    }

    public double getShortfall() {
        return requiredAmount - attemptedAmount;
    }
}
