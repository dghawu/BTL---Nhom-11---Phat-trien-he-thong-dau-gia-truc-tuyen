package com.example.server.handler;

import com.example.auth.AuthResult;
import com.example.auth.TokenGuard;
import com.example.model.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AdminHandler — quản lý user (xem/ban/nâng quyền admin).
 */
public final class AdminHandler extends BaseHandler {

    @Override
    public String handle(final JSONObject req) {
        return switch (req.getString("action")) {
            case "getAllUsers" -> handleGetAllUsers(req);
            case "banUser" -> handleBanUser(req);
            case "makeAdmin" -> handleMakeAdmin(req);
            default -> fail("Action không hợp lệ.");
        };
    }

    private String handleGetAllUsers(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        JSONArray arr = new JSONArray();
        for (User u : getUserDAO().findAll()) {
            arr.put(new JSONObject()
                    .put("id", u.getId())
                    .put("name", u.getName())
                    .put("role", u.getRole()));
        }
        return success().put("users", arr).toString();
    }

    private String handleBanUser(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        getUserDAO().delete(req.getString("userId"));
        return success().toString();
    }

    private String handleMakeAdmin(final JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) {
            return fail(auth.getErrorMessage());
        }

        getUserDAO().updateRole(req.getString("userId"), "ADMIN");
        return success().toString();
    }
}