package model;

import java.time.LocalDate;

/** A movie that can be screened in many shows. */
public class Movie {
    private int movieId;
    private String title;
    private String genre;
    private String language;
    private int durationMinutes;
    private LocalDate releaseDate;   // may be null

    public Movie() { }

    public Movie(int movieId, String title, String genre, String language,
                 int durationMinutes, LocalDate releaseDate) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
    }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    /** Used by combo boxes so they display the title. */
    @Override
    public String toString() { return title + " (" + language + ")"; }
}
