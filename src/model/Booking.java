package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** A customer's booking of one or more seats for a show. Implements TicketPrintable. */
public class Booking implements TicketPrintable {
    private int bookingId;
    private int userId;
    private int showId;
    private LocalDateTime bookingTime;
    private String status;            // CONFIRMED or CANCELLED

    // Filled by JOINs / extra queries
    private String customerName;
    private Show show;
    private List<Seat> seats = new ArrayList<>();
    private Payment payment;

    public Booking() { }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Show getShow() { return show; }
    public void setShow(Show show) { this.show = show; }
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    /** Total = sum of the price paid for each seat (auto-calculated). */
    public double getTotalAmount() {
        double total = 0;
        for (Seat s : seats) {
            total += s.getPricePaid();
        }
        return total;
    }

    /** Comma separated seat labels, e.g. "A1, A2". */
    public String getSeatLabels() {
        StringBuilder sb = new StringBuilder();
        for (Seat s : seats) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(s.getLabel());
        }
        return sb.toString();
    }

    @Override
    public String generateTicket() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        String line = "==============================================\n";
        String thin = "----------------------------------------------\n";
        sb.append(line);
        sb.append(String.format("        MOVIE TICKET  -  BOOKING #%d%n", bookingId));
        sb.append(line);
        sb.append(String.format("Customer : %s%n", customerName));
        sb.append(String.format("Movie    : %s%n", show.getMovieTitle()));
        sb.append(String.format("Theatre  : %s%n", show.getTheatreName()));
        sb.append(String.format("Date     : %s%n", show.getShowDate()));
        sb.append(String.format("Time     : %s%n", show.getShowTime()));
        sb.append(thin);
        sb.append("Seats\n");
        for (Seat s : seats) {
            sb.append(String.format("  %-4s %-8s          Rs. %8.2f%n",
                    s.getLabel(), s.getSeatType(), s.getPricePaid()));
        }
        sb.append(thin);
        sb.append(String.format("TOTAL AMOUNT            Rs. %8.2f%n", getTotalAmount()));
        if (payment != null) {
            sb.append(String.format("Payment  : %s - %s%n", payment.getPaymentMode(), payment.getPaymentStatus()));
        }
        sb.append(String.format("Status   : %s%n", status));
        if (bookingTime != null) {
            sb.append(String.format("Booked on: %s%n", bookingTime.format(fmt)));
        }
        sb.append(line);
        sb.append("   Please show this ticket at the entrance.\n");
        sb.append(line);
        return sb.toString();
    }
}
