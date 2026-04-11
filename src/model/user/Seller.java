package model.user;

import java.util.ArrayList;
import java.util.List;
public class Seller extends User {
    private List<Auction> myProducts;
    public Seller(String id, String name, String password) {
        super(id, name, password, "SELLER");
        this.myProducts = new ArrayList<>();
    }
    // Hiển thị menu (override từ User)
    @Override
    public void showMenu() {
        System.out.println("\n===== SELLER MENU =====");
        System.out.println("1. Đăng sản phẩm");
        System.out.println("2. Sửa sản phẩm");
        System.out.println("3. Xóa sản phẩm");
        System.out.println("4. Xem sản phẩm của tôi");
        System.out.println("0. Thoát");
    }
    // Override từ Entity (bắt buộc)
    @Override
    public void printInfo() {
        System.out.println("ID: " + getId());
        System.out.println("Tên: " + name);
        System.out.println("Role: " + role);
        System.out.println("Ngày tạo: " + getCreatedAt());
    }
    // Đăng sản phẩm
    public void postProduct(String productName, double startPrice) {
        Auction item = new Auction(productName, startPrice, this); //gán seller, ng đang gọi hàm
        myProducts.add(item);
        System.out.println("Đăng sản phẩm thành công!");
    }
    // Sửa sản phẩm
    public void editProduct(int index, String newName, double newPrice) {
        if (index >= 0 && index < myProducts.size()) {
            Auction item = myProducts.get(index);
            item.setName(newName);
            item.setStartPrice(newPrice);
            System.out.println("Cập nhật thành công!");
        } else {
            System.out.println("Không tìm thấy sản phẩm!");
        }
    }
    // Xóa sản phẩm
    public void removeProduct(int index) {
        if (index >= 0 && index < myProducts.size()) {
            myProducts.remove(index);
            System.out.println("Xóa thành công!");
        } else {
            System.out.println("Không tìm thấy sản phẩm!");
        }
    }
    // Xem sản phẩm của mình
    public void viewMyProducts() {
        System.out.println("\n===== DANH SÁCH SẢN PHẨM =====");
        for (int i = 0; i < myProducts.size(); i++) {
            System.out.println(i + ". " + myProducts.get(i));
        }
    }
}
