package model.enums;

public enum AuctionStatus {
    PENDING, // Vừa tạo, chờ Admin duyệt
    APPROVED, // Admin đã duyệt, chờ bắt đầu
    RUNNING, // Đang diễn ra
    FINISHED, // Đã kết thúc, chờ xác nhận thanh toán
    PAYING, //Chờ thanh toán trong 24h
    PAID,   // Đã thanh toán xong
    CANCELED, // Bị hủy (bởi Seller hoặc Admin)
    REJECTED // Bị Admin từ chối
}

