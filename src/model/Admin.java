package model;

/** Admin manages movies, theatres, shows and views reports. */
public class Admin extends User {

    public Admin() { super(); }

    public Admin(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone);
    }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, Administrator " + getFullName();
    }
}
