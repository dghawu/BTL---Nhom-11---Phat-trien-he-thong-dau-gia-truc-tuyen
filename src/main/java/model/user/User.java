package model.user;

import model.entity.Entity;

public abstract class User extends Entity {
    String name;
    String password;
    String role;

    public User(String id, String name, String password, String role) {
        super(id);
        this.name     = name;
        this.password = password;
        this.role     = role;
    }

    public String getName()     { return name; }
    public String getRole()     { return role; }
    public String getPassword() { return password; } // thêm cho UserDAO

    public void setPassword(String password) { this.password = password; }

    public boolean login(String name, String pass) {
        return this.name.equals(name) && this.password.equals(pass);
    }

    public abstract void showMenu();
}