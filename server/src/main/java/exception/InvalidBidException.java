package exception;

public class InvalidBidException extends RuntimeException{
    private final double attempedAmount;
    private final double requiredAmount;
    public InvalidBidException(double attempedAmount, double requiredAmount) {
        super("Giá đặt không hợp lệ: bạn đặt " + attempedAmount + "nhưng giá tối thiểu " + requiredAmount);
        this.attempedAmount = attempedAmount;
        this.requiredAmount = requiredAmount;
    }
    public double getAttempedAmount() {
        return attempedAmount;
    }

    public double getRequiredAmount() {
        return requiredAmount;
    }
}
