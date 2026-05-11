package com.example.server;

import dao.UserDAO;
import dao.ItemDAO;
import dao.AuctionDAO;
import dao.BidTransactionDAO;
import model.user.User;
import model.item.Item;
import model.auction.Auction;
import model.auction.BidTransaction;
import model.enums.AuctionStatus;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ClientHandler v2 - kết nối DAO thật.
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

    // ------------------------------------------------------------------ //
    //  Router
    // ------------------------------------------------------------------ //
    private String handleRequest(String jsonStr) {
        try {
            JSONObject req = new JSONObject(jsonStr);
            String action = req.getString("action");
            return switch (action) {
                case "login"             -> handleLogin(req);
                case "register"          -> handleRegister(req);
                case "changeUsername"    -> handleChangeUsername(req);
                case "changePassword"    -> handleChangePassword(req);
                case "getMyItems"        -> handleGetMyItems(req);
                case "addItem"           -> handleAddItem(req);
                case "getAllItems"        -> handleGetAllItems(req);
                case "getAllSessions"     -> handleGetAllSessions(req);
                case "createSession"     -> handleCreateSession(req);
                case "getMySessions"     -> handleGetMySessions(req);
                case "placeBid"          -> handlePlaceBid(req);
                case "setAutoBid"        -> handleSetAutoBid(req);
                case "getMyTransactions" -> handleGetMyTransactions(req);
                case "pay"               -> handlePay(req);
                case "getAllUsers"        -> handleGetAllUsers(req);
                case "banUser"           -> handleBanUser(req);
                case "makeAdmin"         -> handleMakeAdmin(req);
                default -> fail("Action không hợp lệ: " + action);
            };
        } catch (Exception e) {
            System.err.println("[Server] Lỗi: " + e.getMessage());
            return fail("Lỗi server: " + e.getMessage());
        }
    }

    // ================================================================== //
    //  AUTH
    // ================================================================== //

    private String handleLogin(JSONObject req) {
        String username = req.getString("username");
        String password = req.getString("password");

        User user = userDAO.findByName(username);
        if (user == null)
            return fail("Tên đăng nhập không tồn tại.");
        if (!user.getPassword().equals(password))
            return fail("Sai mật khẩu.");

        return success()
                .put("userId",   user.getId())
                .put("username", user.getName())
                .put("role",     user.getRole())
                .toString();
    }

    private String handleRegister(JSONObject req) {
        String name     = req.getString("name");
        String password = req.getString("password");
        String role     = req.getString("role").toUpperCase();

        if (userDAO.findByName(name) != null)
            return fail("Tên đăng nhập đã tồn tại.");

        User newUser = switch (role) {
            case "SELLER" -> new model.user.Seller(UUID.randomUUID().toString(), name, password);
            default       -> new model.user.Bidder(UUID.randomUUID().toString(), name, password);
        };

        userDAO.save(newUser);
        return success().put("message", "Đăng ký thành công!").toString();
    }

    private String handleChangeUsername(JSONObject req) {
        String userId      = req.getString("userId");
        String newUsername = req.getString("newUsername");
        String password    = req.getString("password");

        User user = userDAO.findById(userId);
        if (user == null) return fail("Không tìm thấy user.");
        if (!user.getPassword().equals(password)) return fail("Sai mật khẩu.");
        if (userDAO.findByName(newUsername) != null) return fail("Tên đã tồn tại.");

        userDAO.updateName(userId, newUsername);
        return success().toString();
    }

    private String handleChangePassword(JSONObject req) {
        String userId      = req.getString("userId");
        String oldPassword = req.getString("oldPassword");
        String newPassword = req.getString("newPassword");

        User user = userDAO.findById(userId);
        if (user == null) return fail("Không tìm thấy user.");
        if (!user.getPassword().equals(oldPassword)) return fail("Sai mật khẩu cũ.");

        userDAO.updatePassword(userId, newPassword);
        return success().toString();
    }

    // ================================================================== //
    //  ITEMS
    // ================================================================== //

    private String handleGetMyItems(JSONObject req) {
        String sellerId = req.getString("sellerId");
        List<Item> items = itemDAO.findBySellerId(sellerId);
        JSONArray arr = new JSONArray();
        for (Item item : items) {
            arr.put(new JSONObject()
                    .put("id",          item.getId())
                    .put("name",        item.getName())
                    .put("description", item.getDescription())
                    .put("startPrice",  item.getStartPrice())
                    .put("status",      item.getStatus().name())
                    .put("type",        item.getClass().getSimpleName())
                    .put("sellerId",    item.getSellerId())
            );
        }
        return success().put("items", arr).toString();
    }

    private String handleAddItem(JSONObject req) {
        String sellerId    = req.getString("sellerId");
        String name        = req.getString("name");
        String category    = req.getString("category");
        String description = req.getString("description");
        double startPrice  = req.getDouble("startPrice");

        try {
            Item.ItemType type = Item.ItemType.valueOf(
                    category.toUpperCase().replace(" ", "_").replace("Đ", "D")
            );
            Item newItem = type.create(
                    sellerId, name,
                    UUID.randomUUID().toString(),
                    description, startPrice,
                    Item.ItemStatus.PENDING
            );
            itemDAO.save(newItem);
            return success().toString();
        } catch (Exception e) {
            return fail("Không tạo được sản phẩm: " + e.getMessage());
        }
    }

    private String handleGetAllItems(JSONObject req) {
        List<Item> items = itemDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Item item : items) {
            arr.put(new JSONObject()
                    .put("id",         item.getId())
                    .put("name",       item.getName())
                    .put("startPrice", item.getStartPrice())
                    .put("status",     item.getStatus().name())
                    .put("sellerId",   item.getSellerId())
            );
        }
        return success().put("items", arr).toString();
    }

    // ================================================================== //
    //  SESSIONS
    // ================================================================== //

    private String handleGetAllSessions(JSONObject req) {
        String category = req.optString("category", "ALL");
        List<Auction> auctions = auctionDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Auction a : auctions) {
            // Lọc theo category nếu không phải ALL
            if (!"ALL".equals(category)) {
                String itemType = a.getItem().getClass().getSimpleName().toUpperCase();
                if (!itemType.contains(category.replace("_", ""))) continue;
            }
            arr.put(auctionToJson(a));
        }
        return success().put("sessions", arr).toString();
    }

    private String handleGetMySessions(JSONObject req) {
        String sellerId = req.getString("sellerId");
        List<Auction> auctions = auctionDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Auction a : auctions) {
            if (sellerId.equals(a.getItem().getSellerId())) {
                arr.put(auctionToJson(a));
            }
        }
        return success().put("sessions", arr).toString();
    }

    private String handleCreateSession(JSONObject req) {
        String itemId    = req.getString("itemId");
        String startTime = req.getString("startTime");
        String endTime   = req.getString("endTime");
        double stepPrice = req.getDouble("stepPrice");

        Item item = itemDAO.findById(itemId);
        if (item == null) return fail("Không tìm thấy sản phẩm.");

        Auction auction = new Auction(
                UUID.randomUUID().toString(),
                item,
                item.getStartPrice(),
                stepPrice,
                LocalDateTime.parse(startTime),
                LocalDateTime.parse(endTime)
        );
        auctionDAO.save(auction);
        return success().toString();
    }

    // ================================================================== //
    //  BIDDING
    // ================================================================== //

    private String handlePlaceBid(JSONObject req) {
        String sessionId = req.getString("sessionId");
        String bidderId  = req.getString("bidderId");
        double bidAmount = req.getDouble("bidAmount");

        Auction auction = auctionDAO.findById(sessionId);
        if (auction == null) return fail("Không tìm thấy phiên.");
        if (bidAmount <= auction.getCurrentPrice())
            return fail("Giá đặt phải cao hơn giá hiện tại.");

        // Lưu bid transaction
        BidTransaction bid = new BidTransaction(bidderId, sessionId, bidAmount);
        bidDAO.save(bid);

        // Cập nhật giá hiện tại
        User bidder = userDAO.findById(bidderId);
        String winnerName = bidder != null ? bidder.getName() : bidderId;
        auctionDAO.updateBid(sessionId, bidAmount, winnerName);

        return success()
                .put("currentPrice", bidAmount)
                .put("currentWinner", winnerName)
                .toString();
    }

    private String handleSetAutoBid(JSONObject req) {
        // TODO: tích hợp với AuctionService.setAutoBid() của server
        // AuctionService đã có sẵn trong project
        return success().toString();
    }

    // ================================================================== //
    //  TRANSACTIONS
    // ================================================================== //

    private String handleGetMyTransactions(JSONObject req) {
        String bidderId = req.getString("bidderId");
        List<BidTransaction> txList = bidDAO.findByBidderId(bidderId);
        JSONArray arr = new JSONArray();
        for (BidTransaction tx : txList) {
            Auction auction = auctionDAO.findById(tx.getAuctionId());
            arr.put(new JSONObject()
                    .put("id",         tx.getId())
                    .put("auctionId",  tx.getAuctionId())
                    .put("itemName",   auction != null ? auction.getItem().getName() : "")
                    .put("amount",     tx.getAmount())
                    .put("timestamp",  tx.getTimestamp().toString())
                    .put("status",     auction != null ? auction.getStatus().name() : "")
            );
        }
        return success().put("transactions", arr).toString();
    }

    private String handlePay(JSONObject req) {
        String transactionId = req.getString("transactionId");
        // Cập nhật trạng thái auction liên quan
        // TODO: thêm bảng payments nếu cần theo dõi chi tiết
        return success().toString();
    }

    // ================================================================== //
    //  ADMIN
    // ================================================================== //

    private String handleGetAllUsers(JSONObject req) {
        List<User> users = userDAO.findAll();
        JSONArray arr = new JSONArray();
        for (User u : users) {
            arr.put(new JSONObject()
                    .put("id",   u.getId())
                    .put("name", u.getName())
                    .put("role", u.getRole())
            );
        }
        return success().put("users", arr).toString();
    }

    private String handleBanUser(JSONObject req) {
        String userId = req.getString("userId");
        userDAO.delete(userId);
        return success().toString();
    }

    private String handleMakeAdmin(JSONObject req) {
        String userId = req.getString("userId");
        userDAO.updateRole(userId, "ADMIN");
        return success().toString();
    }

    // ================================================================== //
    //  Helpers
    // ================================================================== //

    private JSONObject auctionToJson(Auction a) {
        return new JSONObject()
                .put("id",          a.getAuctionId())
                .put("itemId",      a.getItem().getId())
                .put("itemName",    a.getItem().getName())
                .put("description", a.getItem().getDescription())
                .put("sellerId",    a.getItem().getSellerId())
                .put("startPrice",  a.getStartPrice())
                .put("currentPrice", a.getCurrentPrice())
                .put("stepPrice",   a.getMinIncrement())
                .put("startTime",   a.getStartTime().toString())
                .put("endTime",     a.getEndTime().toString())
                .put("status",      a.getStatus().name())
                .put("category",    a.getItem().getClass().getSimpleName())
                .put("currentWinner", a.getCurrentWinner() != null ? a.getCurrentWinner() : "");
    }

    private JSONObject success() {
        return new JSONObject().put("success", true);
    }

    private String fail(String message) {
        return new JSONObject()
                .put("success", false)
                .put("message", message)
                .toString();
    }
}