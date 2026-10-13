package model;

/** Premium seat: charged at the show's premium price. */
public class PremiumSeat extends Seat {
    @Override
    public String getSeatType() { return "PREMIUM"; }

    @Override
    public double getPrice(Show show) { return show.getPremiumPrice(); }
}
