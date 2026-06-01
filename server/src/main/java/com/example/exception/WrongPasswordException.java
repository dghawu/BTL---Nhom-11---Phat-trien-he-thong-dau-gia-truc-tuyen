package com.example.exception;

/**
 * Ném khi mật khẩu đăng nhập hoặc mật khẩu cũ khi đổi mật khẩu không khớp.
 * Không tiết lộ tên user có tồn tại hay không (tránh user enumeration attack).
 */
public class WrongPasswordException extends AuctionSystemException {

    public WrongPasswordException() {
        super("WRONG_PASSWORD",
                "Tên đăng nhập hoặc mật khẩu không đúng.");
    }
}
