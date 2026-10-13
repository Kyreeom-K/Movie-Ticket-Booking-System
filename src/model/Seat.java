package model;

/**
 * ABSTRACTION + POLYMORPHISM: Seat is abstract. RegularSeat and PremiumSeat
 * override getPrice(), so the same call  seat.getPrice(show)  gives a different
 * answer depending on the real object type.
 */
public abstract class Seat {
    private int seatId;
    private int showId;
    private String rowLabel;
    private int seatNumber;
    private String status;        // AVAILABLE or BOOKED
    private double pricePaid;     // only filled when the seat belongs to a booking

    /** Factory method: creates the right subclass from the DB value. */
    public static Seat create(String seatType) {
        if ("PREMIUM".equals(seatType)) {
            return new PremiumSeat();
        }
        return new RegularSeat();
    }

    // Each subclass decides its own type and price
    public abstract String getSeatType();
    public abstract double getPrice(Show show);

    /** Example: "A1" */
    public String getLabel() { return rowLabel + seatNumber; }

    public boolean isBooked() { return "BOOKED".equals(status); }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getPricePaid() { return pricePaid; }
    public void setPricePaid(double pricePaid) { this.pricePaid = pricePaid; }
}
