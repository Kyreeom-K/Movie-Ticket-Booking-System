package service;

import dao.ReportDAO;

import java.sql.SQLException;
import java.util.List;

/** Admin reports. Column names are kept here so the UI stays simple. */
public class ReportService {

    public static final String[] BOOKINGS_COLUMNS =
            {"Show ID", "Movie", "Theatre", "Date", "Time", "Bookings", "Seats Sold", "Revenue (Rs.)"};
    public static final String[] OCCUPANCY_COLUMNS =
            {"Show ID", "Movie", "Theatre", "Date", "Time", "Total Seats", "Booked", "Available", "Occupancy"};

    private final ReportDAO reportDAO = new ReportDAO();

    public List<Object[]> bookingsPerShow() throws SQLException {
        return reportDAO.bookingsPerShow();
    }

    public double totalRevenue() throws SQLException {
        return reportDAO.totalRevenue();
    }

    public List<Object[]> seatOccupancy() throws SQLException {
        return reportDAO.seatOccupancy();
    }
}
