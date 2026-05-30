package com.example.server.handler;

import auth.AuthResult;
import auth.JwtUtil;
import auth.TokenGuard;
import model.user.User;
import org.json.JSONObject;

import java.util.UUID;

/**
 * AuthHandler — xử lý đăng nhập, đăng ký, đổi tên/mật khẩu.
 */
public class AuthHandler extends BaseHandler {

    @Override
    public String handle(JSONObject req) {
        return switch (req.getString("action")) {
            case "login"          -> handleLogin(req);
            case "register"       -> handleRegister(req);
            case "changeUsername" -> handleChangeUsername(req);
            case "changePassword" -> handleChangePassword(req);
            default               -> fail("Action không hợp lệ.");
        };
    }

    private String handleLogin(JSONObject req) {
        String username = req.getString("username");
        String password = req.getString("password");

        User user = userDAO.findByCredentials(username, password);
        if (user == null)
            return fail("Tên đăng nhập hoặc mật khẩu không đúng.");

        String token = JwtUtil.generateToken(user.getId(), user.getName(), user.getRole());
        return success()
                .put("token",    token)
                .put("userId",   user.getId())
                .put("username", user.getName())
                .put("role",     user.getRole())
                .toString();
    }

    private String handleRegister(JSONObject req) {
        String name     = req.getString("name");
        String password = req.getString("password");
        String role     = req.getString("role").toUpperCase();

        // Không cho tự đăng ký ADMIN
        if ("ADMIN".equals(role))
            return fail("Không thể đăng ký với vai trò ADMIN.");

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
        AuthResult auth = TokenGuard.check(req);
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String userId      = auth.getUserId();
        String newUsername = req.getString("newUsername");
        String password    = req.getString("password");

        User user = userDAO.findById(userId);
        if (user == null) return fail("Không tìm thấy user.");
        if (!util.PasswordUtil.verify(password, user.getPassword())) return fail("Sai mật khẩu.");
        if (userDAO.findByName(newUsername) != null) return fail("Tên đã tồn tại.");

        userDAO.updateName(userId, newUsername);
        return success().toString();
    }

    private String handleChangePassword(JSONObject req) {
        AuthResult auth = TokenGuard.check(req);
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        String userId      = auth.getUserId();
        String oldPassword = req.getString("oldPassword");
        String newPassword = req.getString("newPassword");

        User user = userDAO.findById(userId);
        if (user == null) return fail("Không tìm thấy user.");
        if (!util.PasswordUtil.verify(oldPassword, user.getPassword()))
            return fail("Sai mật khẩu cũ.");

        userDAO.updatePassword(userId, newPassword);
        return success().toString();
    }
}