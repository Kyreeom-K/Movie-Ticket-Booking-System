package model;

/** Regular seat: charged at the show's regular price. */
public class RegularSeat extends Seat {
    @Override
    public String getSeatType() { return "REGULAR"; }

    @Override
    public double getPrice(Show show) { return show.getRegularPrice(); }
}
