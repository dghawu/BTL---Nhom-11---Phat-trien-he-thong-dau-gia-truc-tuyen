package exception;

public class UserNotFoundException extends Exception{
    private final String keyword;
    public UserNotFoundException(String keyword) {
        super("Không tìm thấy người dùng: '" + keyword + "'");
        this.keyword = keyword;
    }
    public String getKeyword() {
        return keyword;
    }
}
