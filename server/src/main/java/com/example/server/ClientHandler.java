package com.example.server;

import dao.UserDAO;
import dao.ItemDAO;
import dao.AuctionDAO;
import dao.BidTransactionDAO;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.Socket;

/**
 * ClientHandler - chạy trong thread riêng, xử lý tất cả request từ 1 client.
 *
 * Mỗi request là 1 dòng JSON, mỗi response cũng là 1 dòng JSON.
 * Format request bắt buộc phải có field "action" để biết làm gì.
 *
 * Ví dụ:
 *   Request:  {"action":"login","username":"abc","password":"123"}
 *   Response: {"success":true,"userId":1,"username":"abc","role":"SELLER"}
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final UserDAO           userDAO    = new UserDAO();
    private final ItemDAO           itemDAO    = new ItemDAO();
    private final AuctionDAO        auctionDAO = new AuctionDAO();
    private final BidTransactionDAO bidDAO     = new BidTransactionDAO();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Server] Nhận: " + line);
                String response = handleRequest(line);
                writer.println(response);
                System.out.println("[Server] Gửi:  " + response);
            }
        } catch (IOException e) {
            System.err.println("[Server] Client ngắt kết nối: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    //  Router - phân phối đến đúng handler
    private String handleRequest(String jsonStr) {
        try {
            JSONObject req = new JSONObject(jsonStr);
            String action = req.getString("action");

            return switch (action) {
                // Auth
                case "login"          -> handleLogin(req);
                case "register"       -> handleRegister(req);
                case "changeUsername" -> handleChangeUsername(req);
                case "changePassword" -> handleChangePassword(req);

                // Items
                case "getMyItems"     -> handleGetMyItems(req);
                case "addItem"        -> handleAddItem(req);
                case "getAllItems"     -> handleGetAllItems(req);

                // Sessions
                case "getAllSessions"  -> handleGetAllSessions(req);
                case "createSession"  -> handleCreateSession(req);
                case "getMySessions"  -> handleGetMySessions(req);

                // Bidding
                case "placeBid"       -> handlePlaceBid(req);
                case "setAutoBid"     -> handleSetAutoBid(req);

                // Transactions
                case "getMyTransactions" -> handleGetMyTransactions(req);
                case "pay"               -> handlePay(req);

                // Admin
                case "getAllUsers"    -> handleGetAllUsers(req);
                case "banUser"        -> handleBanUser(req);
                case "makeAdmin"      -> handleMakeAdmin(req);

                default -> error("Action không hợp lệ: " + action);
            };

        } catch (Exception e) {
            System.err.println("[Server] Lỗi xử lý request: " + e.getMessage());
            return error("Lỗi server: " + e.getMessage());
        }
    }

    //  AUTH handlers
    private String handleLogin(JSONObject req) {
        String username = req.getString("username");
        String password = req.getString("password");

        // TODO: gọi userDAO.findByCredentials(username, password)
        // Thay đoạn mock dưới bằng:
        // model.User user = userDAO.findByCredentials(username, password);
        // if (user == null) return fail("Sai tên đăng nhập hoặc mật khẩu.");
        // JSONObject res = new JSONObject();
        // res.put("success",  true);
        // res.put("userId",   user.getId());
        // res.put("username", user.getUsername());
        // res.put("role",     user.getRole());
        // return res.toString();

        // --- Mock tạm để test UI ---
        if (username.startsWith("admin")) {
            return success().put("userId", 1).put("username", username).put("role", "ADMIN").toString();
        } else if (username.startsWith("seller")) {
            return success().put("userId", 2).put("username", username).put("role", "SELLER").toString();
        } else if (username.startsWith("bidder")) {
            return success().put("userId", 3).put("username", username).put("role", "BIDDER").toString();
        } else {
            return fail("Sai tên đăng nhập hoặc mật khẩu.");
        }
    }

    private String handleRegister(JSONObject req) {
        String name     = req.getString("name");
        String email    = req.getString("email");
        String password = req.getString("password");
        String role     = req.getString("role");

        // TODO: userDAO.register(name, email, password, role)
        // boolean ok = userDAO.register(new model.User(name, email, password, role));
        // return ok ? success().toString() : fail("Email đã tồn tại.");

        return success().toString(); // mock
    }

    private String handleChangeUsername(JSONObject req) {
        int    userId      = req.getInt("userId");
        String newUsername = req.getString("newUsername");
        String password    = req.getString("password");

        // TODO: userDAO.changeUsername(userId, newUsername, password)
        return success().toString();
    }

    private String handleChangePassword(JSONObject req) {
        int    userId      = req.getInt("userId");
        String oldPassword = req.getString("oldPassword");
        String newPassword = req.getString("newPassword");

        // TODO: userDAO.changePassword(userId, oldPassword, newPassword)
        return success().toString();
    }

    //  ITEMS handlers
    private String handleGetMyItems(JSONObject req) {
        int sellerId = req.getInt("sellerId");
        // TODO: List<Item> items = itemDAO.findBySeller(sellerId);
        // JSONArray arr = new JSONArray(items.stream().map(this::itemToJson).toList());
        // return success().put("items", arr).toString();
        return success().put("items", new JSONArray()).toString(); // mock
    }

    private String handleAddItem(JSONObject req) {
        // TODO: itemDAO.insert(new Item(...))
        return success().toString();
    }

    private String handleGetAllItems(JSONObject req) {
        // TODO: itemDAO.findAll()
        return success().put("items", new JSONArray()).toString();
    }

    //  SESSIONS handlers
    private String handleGetAllSessions(JSONObject req) {
        String category = req.optString("category", "ALL");
        // TODO: auctionDAO.findByCategory(category)
        return success().put("sessions", new JSONArray()).toString();
    }

    private String handleCreateSession(JSONObject req) {
        // TODO: auctionDAO.createSession(...)
        return success().toString();
    }

    private String handleGetMySessions(JSONObject req) {
        int sellerId = req.getInt("sellerId");
        // TODO: auctionDAO.findBySeller(sellerId)
        return success().put("sessions", new JSONArray()).toString();
    }

    //  BIDDING handlers
    private String handlePlaceBid(JSONObject req) {
        int    sessionId = req.getInt("sessionId");
        int    bidderId  = req.getInt("bidderId");
        double bidAmount = req.getDouble("bidAmount");

        // TODO: gọi AuctionService.placeBid(sessionId, bidderId, bidAmount)
        // AuctionService đã có sẵn trong server của bạn!
        return success().toString();
    }

    private String handleSetAutoBid(JSONObject req) {
        // TODO: AuctionService.setAutoBid(...)
        return success().toString();
    }

    //  TRANSACTIONS handlers
    private String handleGetMyTransactions(JSONObject req) {
        int bidderId = req.getInt("bidderId");
        // TODO: bidDAO.findByBidder(bidderId)
        return success().put("transactions", new JSONArray()).toString();
    }

    private String handlePay(JSONObject req) {
        int transactionId = req.getInt("transactionId");
        // TODO: bidDAO.markAsPaid(transactionId)
        return success().toString();
    }


    //  ADMIN handlers
    private String handleGetAllUsers(JSONObject req) {
        // TODO: userDAO.findAll()
        return success().put("users", new JSONArray()).toString();
    }

    private String handleBanUser(JSONObject req) {
        int userId = req.getInt("userId");
        // TODO: userDAO.banUser(userId)
        return success().toString();
    }

    private String handleMakeAdmin(JSONObject req) {
        int userId = req.getInt("userId");
        // TODO: userDAO.setRole(userId, "ADMIN")
        return success().toString();
    }

    //  Helpers
    /** Tạo JSONObject success base */
    private JSONObject success() {
        return new JSONObject().put("success", true);
    }

    /** Trả về JSON lỗi có message */
    private String fail(String message) {
        return new JSONObject()
                .put("success", false)
                .put("message", message)
                .toString();
    }

    private String error(String message) {
        return fail(message);
    }
}