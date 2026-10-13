package model;

/**
 * ABSTRACTION (interface): anything that can produce a printable ticket text.
 * Booking implements it.
 */
public interface TicketPrintable {
    String generateTicket();
}
