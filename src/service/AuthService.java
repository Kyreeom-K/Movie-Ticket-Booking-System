package service;

import dao.UserDAO;
import exception.DuplicateEmailException;
import exception.InvalidLoginException;
import exception.ValidationException;
import model.Admin;
import model.Customer;
import model.User;
import util.PasswordUtil;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Business logic for registration and login. */
public class AuthService {

    /** Anyone registering as ADMIN must type this key (so customers cannot become admins). */
    public static final String ADMIN_SECRET_KEY = "ADMIN@2026";

    private final UserDAO userDAO = new UserDAO();

    public User register(String fullName, String email, String phone, String password,
                         String role, String adminKey)
            throws SQLException, ValidationException, DuplicateEmailException {

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        phone = phone == null ? "" : phone.trim();

        // ----- validation -----
        if (fullName.isEmpty()) throw new ValidationException("Name is required.");
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) throw new ValidationException("Enter a valid email address.");
        if (!phone.isEmpty() && !phone.matches("\\d{10}")) throw new ValidationException("Phone must be 10 digits.");
        if (password == null || password.length() < 6) throw new ValidationException("Password must be at least 6 characters.");
        if ("ADMIN".equals(role) && !ADMIN_SECRET_KEY.equals(adminKey)) {
            throw new ValidationException("Wrong admin secret key.");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new DuplicateEmailException("This email is already registered.");
        }

        // ----- create the right kind of user (polymorphism) -----
        User user = "ADMIN".equals(role) ? new Admin() : new Customer();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone.isEmpty() ? null : phone);
        user.setPasswordHash(PasswordUtil.hash(password));   // never store the plain password

        try {
            user.setUserId(userDAO.addUser(user));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateEmailException("This email is already registered.");
        }
        return user;
    }

    /** Checks email + password + role. Returns the logged-in user (Admin or Customer). */
    public User login(String email, String password, String expectedRole)
            throws SQLException, InvalidLoginException {

        email = email == null ? "" : email.trim().toLowerCase();
        User user = userDAO.findByEmail(email);

        // Same message for "no such user" and "wrong password" so attackers learn nothing
        if (user == null || password == null
                || !user.getPasswordHash().equals(PasswordUtil.hash(password))) {
            throw new InvalidLoginException("Invalid email or password.");
        }
        if (!user.getRole().equals(expectedRole)) {
            throw new InvalidLoginException("This account is not a " + expectedRole + " account.");
        }
        return user;
    }
}
