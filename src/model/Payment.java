package model;

import java.time.LocalDateTime;

/** Payment record for a booking (mode + status). */
public class Payment {
    public static final String[] MODES = {"CARD", "UPI", "NET_BANKING", "CASH"};

    private int paymentId;
    private int bookingId;
    private double amount;
    private String paymentMode;     // CARD, UPI, NET_BANKING, CASH
    private String paymentStatus;   // SUCCESS, FAILED, PENDING, REFUNDED
    private LocalDateTime paymentTime;

    public Payment() { }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
}
