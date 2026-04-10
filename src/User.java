public abstract class User extends Entity {
String name;
String password;
String role;

public User(String id, String name, String password, String role) {
    super(id);
    this.name = name;
    this.password = password;
    this.role = role;
}
public String getName() {
    return name;
}
public String getRole() {
    return role;
}
public boolean login(String user, String pass) {
    return this.name.equals(user) && this.password.equals(pass);
}

public abstract void showMenu();
}

			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			