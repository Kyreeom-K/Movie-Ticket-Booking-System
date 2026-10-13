package service;

import dao.MovieDAO;
import dao.ShowDAO;
import dao.TheatreDAO;
import exception.ValidationException;
import model.Movie;
import model.Show;
import model.Theatre;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Business logic for the admin: validates input, then calls the DAOs.
 * Database constraint errors (duplicates, foreign keys) are turned into friendly messages.
 */
public class AdminService {

    private final MovieDAO movieDAO = new MovieDAO();
    private final TheatreDAO theatreDAO = new TheatreDAO();
    private final ShowDAO showDAO = new ShowDAO();

    // ===================== MOVIES =====================
    public List<Movie> getMovies() throws SQLException {
        return movieDAO.getAllMovies();
    }

    public void addMovie(Movie m) throws SQLException, ValidationException {
        validateMovie(m);
        try {
            movieDAO.addMovie(m);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("A movie with this title and language already exists.");
        }
    }

    public void updateMovie(Movie m) throws SQLException, ValidationException {
        validateMovie(m);
        try {
            movieDAO.updateMovie(m);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("A movie with this title and language already exists.");
        }
    }

    public void deleteMovie(int movieId) throws SQLException, ValidationException {
        try {
            movieDAO.deleteMovie(movieId);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("Cannot delete: this movie has shows. Delete its shows first.");
        }
    }

    private void validateMovie(Movie m) throws ValidationException {
        if (isBlank(m.getTitle())) throw new ValidationException("Movie title is required.");
        if (isBlank(m.getGenre())) throw new ValidationException("Genre is required.");
        if (isBlank(m.getLanguage())) throw new ValidationException("Language is required.");
        if (m.getDurationMinutes() <= 0) throw new ValidationException("Duration must be greater than 0.");
    }

    // ===================== THEATRES =====================
    public List<Theatre> getTheatres() throws SQLException {
        return theatreDAO.getAllTheatres();
    }

    public void addTheatre(Theatre t) throws SQLException, ValidationException {
        validateTheatre(t);
        try {
            theatreDAO.addTheatre(t);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("A theatre with this name already exists in this city.");
        }
    }

    public void updateTheatre(Theatre t) throws SQLException, ValidationException {
        validateTheatre(t);
        try {
            theatreDAO.updateTheatre(t);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("A theatre with this name already exists in this city.");
        }
    }

    public void deleteTheatre(int theatreId) throws SQLException, ValidationException {
        try {
            theatreDAO.deleteTheatre(theatreId);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("Cannot delete: this theatre has shows. Delete its shows first.");
        }
    }

    private void validateTheatre(Theatre t) throws ValidationException {
        if (isBlank(t.getName())) throw new ValidationException("Theatre name is required.");
        if (isBlank(t.getCity())) throw new ValidationException("City is required.");
    }

    // ===================== SHOWS =====================
    public List<Show> getShows() throws SQLException {
        return showDAO.getAllShows();
    }

    public void addShow(Show s) throws SQLException, ValidationException {
        validateShow(s);
        try {
            showDAO.addShow(s);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("This theatre already has a show at that date and time.");
        }
    }

    public void updateShow(Show s) throws SQLException, ValidationException {
        validateShow(s);
        Show old = showDAO.getShowById(s.getShowId());
        // Changing total seats rebuilds the seat map, which is unsafe if seats are already booked
        if (old != null && old.getTotalSeats() != s.getTotalSeats()
                && showDAO.countBookedSeats(s.getShowId()) > 0) {
            throw new ValidationException("Cannot change total seats: some seats are already booked.");
        }
        try {
            showDAO.updateShow(s);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("This theatre already has a show at that date and time.");
        }
    }

    public void deleteShow(int showId) throws SQLException, ValidationException {
        try {
            showDAO.deleteShow(showId);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("Cannot delete: this show already has bookings.");
        }
    }

    private void validateShow(Show s) throws ValidationException {
        if (s.getMovieId() <= 0) throw new ValidationException("Select a movie.");
        if (s.getTheatreId() <= 0) throw new ValidationException("Select a theatre.");
        if (s.getShowDate() == null || s.getShowTime() == null) throw new ValidationException("Date and time are required.");
        if (s.getRegularPrice() < 0) throw new ValidationException("Regular price cannot be negative.");
        if (s.getPremiumPrice() < s.getRegularPrice()) throw new ValidationException("Premium price must be at least the regular price.");
        if (s.getTotalSeats() < 1 || s.getTotalSeats() > 260) throw new ValidationException("Total seats must be between 1 and 260.");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
