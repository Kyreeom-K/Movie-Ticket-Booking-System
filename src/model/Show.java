package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** One screening of a movie in a theatre at a given date and time. */
public class Show {
    private int showId;
    private int movieId;
    private int theatreId;
    private LocalDate showDate;
    private LocalTime showTime;
    private double regularPrice;
    private double premiumPrice;
    private int totalSeats;

    // Extra display-only fields filled by SQL JOINs (not columns of the shows table)
    private String movieTitle;
    private String theatreName;

    public Show() { }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }
    public double getRegularPrice() { return regularPrice; }
    public void setRegularPrice(double regularPrice) { this.regularPrice = regularPrice; }
    public double getPremiumPrice() { return premiumPrice; }
    public void setPremiumPrice(double premiumPrice) { this.premiumPrice = premiumPrice; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getTheatreName() { return theatreName; }
    public void setTheatreName(String theatreName) { this.theatreName = theatreName; }

    /** Combines date + time so we can compare with "now". */
    public LocalDateTime getShowDateTime() {
        return LocalDateTime.of(showDate, showTime);
    }
}
