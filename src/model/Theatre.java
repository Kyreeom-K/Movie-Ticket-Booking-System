package model;

/** A theatre (cinema hall) where shows are held. */
public class Theatre {
    private int theatreId;
    private String name;
    private String city;
    private String address;

    public Theatre() { }

    public Theatre(int theatreId, String name, String city, String address) {
        this.theatreId = theatreId;
        this.name = name;
        this.city = city;
        this.address = address;
    }

    public int getTheatreId() { return theatreId; }
    public void setTheatreId(int theatreId) { this.theatreId = theatreId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() { return name + " - " + city; }
}
