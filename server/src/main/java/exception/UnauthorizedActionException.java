package exception;

/**
 * Ném khi user cố thực hiện hành động không thuộc quyền của role họ.
 * VD: Bidder gọi approveAuction(), Seller gọi banUser().
 */
public class UnauthorizedActionException extends AuctionSystemException {

    private final String userId;
    private final String action;
    private final String requiredRole;

    public UnauthorizedActionException(String userId, String action, String requiredRole) {
        super("UNAUTHORIZED",
              "Người dùng '" + userId + "' không có quyền thực hiện '"
              + action + "'. Yêu cầu role: " + requiredRole + ".");
        this.userId       = userId;
        this.action       = action;
        this.requiredRole = requiredRole;
    }

    public String getUserId()      { return userId; }
    public String getAction()      { return action; }
    public String getRequiredRole(){ return requiredRole; }
}
