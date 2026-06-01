package com.example.exception;

/**
 * Ném khi tra cứu user theo ID hoặc tên nhưng không tìm thấy.
 */
public class UserNotFoundException extends AuctionSystemException {

    private final String keyword;

    public UserNotFoundException(String keyword) {
        super("USER_NOT_FOUND",
                "Không tìm thấy người dùng với từ khóa: '" + keyword + "'");
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
