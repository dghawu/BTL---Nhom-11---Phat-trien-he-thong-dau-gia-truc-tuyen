package com.example.exception;

/**
 * Ném khi thanh toán thất bại sau khi thắng đấu giá.
 * Lưu thêm transactionId để có thể retry hoặc tra cứu.
 */
public class PaymentException extends AuctionSystemException {

    private final String transactionId;
    private final double amount;

    public PaymentException(String transactionId, double amount, String reason) {
        super("PAYMENT_FAILED",
                "Thanh toán thất bại cho giao dịch '" + transactionId + "' "
                        + "(số tiền: " + String.format("%.0f", amount) + " VNĐ). "
                        + "Lý do: " + reason);
        this.transactionId = transactionId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }
}
