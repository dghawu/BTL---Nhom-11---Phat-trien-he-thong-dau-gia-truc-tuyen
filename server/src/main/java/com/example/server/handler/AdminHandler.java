package com.example.server.handler;

import auth.AuthResult;
import auth.TokenGuard;
import model.user.User;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AdminHandler — quản lý user (xem/ban/nâng quyền admin).
 */
public class AdminHandler extends BaseHandler {

    @Override
    public String handle(JSONObject req) {
        return switch (req.getString("action")) {
            case "getAllUsers" -> handleGetAllUsers(req);
            case "banUser"     -> handleBanUser(req);
            case "makeAdmin"   -> handleMakeAdmin(req);
            default            -> fail("Action không hợp lệ.");
        };
    }

    private String handleGetAllUsers(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        JSONArray arr = new JSONArray();
        for (User u : userDAO.findAll())
            arr.put(new JSONObject()
                    .put("id",   u.getId())
                    .put("name", u.getName())
                    .put("role", u.getRole()));
        return success().put("users", arr).toString();
    }

    private String handleBanUser(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        userDAO.delete(req.getString("userId"));
        return success().toString();
    }

    private String handleMakeAdmin(JSONObject req) {
        AuthResult auth = TokenGuard.checkRole(req, "ADMIN");
        if (!auth.isOk()) return fail(auth.getErrorMessage());

        userDAO.updateRole(req.getString("userId"), "ADMIN");
        return success().toString();
    }
}