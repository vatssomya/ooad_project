// model/Reservation.java
package model;
import java.time.LocalDate;
public class Reservation {
    private String id;
    private String customerId;
    private String roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double total;
    private String status;
    private String txnRef;
    public Reservation(String id, String customerId, String roomId, LocalDate checkIn, LocalDate checkOut, double total, String status, String txnRef) {
        this.id=id; this.customerId=customerId; this.roomId=roomId; this.checkIn=checkIn; this.checkOut=checkOut; this.total=total; this.status=status; this.txnRef=txnRef;
    }
    public String getId(){return id;}
    public String getCustomerId(){return customerId;}
    public String getRoomId(){return roomId;}
    public java.time.LocalDate getCheckIn(){return checkIn;}
    public java.time.LocalDate getCheckOut(){return checkOut;}
    public double getTotal(){return total;}
    public String getStatus(){return status;}
    public void setStatus(String s){ status = s; }
    public void setDates(java.time.LocalDate ci, java.time.LocalDate co){ this.checkIn=ci; this.checkOut=co; }
}
