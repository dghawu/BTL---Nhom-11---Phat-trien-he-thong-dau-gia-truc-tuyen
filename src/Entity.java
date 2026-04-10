import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Lớp trừu tượng Entity làm nền tảng cho tất cả các đối tượng trong hệ thống.
 * Cung cấp các thuộc tính định danh và thời gian khởi tạo chung.
 */
public abstract class Entity {
    private String id;
    private LocalDateTime createdAt;


    // Constructor mặc định tự động tạo ID duy nhất
    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }


    // Constructor cho phép truyền ID cụ thể
    public Entity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }


    /**
     * Phương thức trừu tượng để hiển thị thông tin đối tượng.
     * Các lớp con bắt buộc phải ghi đè (Polymorphism).
     */
    public abstract void printInfo();


    // Getters
    public String getId() {
        return id;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
