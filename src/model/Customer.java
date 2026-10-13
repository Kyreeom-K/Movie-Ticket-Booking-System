package model;

/** Customer browses shows, books seats, cancels bookings and views history. */
public class Customer extends User {

    public Customer() { super(); }

    public Customer(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone);
    }

    @Override
    public String getRole() { return "CUSTOMER"; }

    @Override
    public String getWelcomeMessage() {
        return "Welcome, " + getFullName() + "! Pick a show and book your seats.";
    }
}
