// model/Room.java
package model;
public class Room {
    private String id;
    private String number;
    private String type; // SINGLE/DOUBLE/SUITE
    private String status; // AVAILABLE/MAINTENANCE
    private double pricePerNight;
    public Room(String id, String number, String type, String status, double pricePerNight) {
        this.id=id; this.number=number; this.type=type; this.status=status; this.pricePerNight=pricePerNight;
    }
    public String getId(){return id;}
    public String getNumber(){return number;}
    public String getType(){return type;}
    public String getStatus(){return status;}
    public void setStatus(String s){ this.status = s; }
    public double getPricePerNight(){ return pricePerNight; }
    public void setPricePerNight(double p){ this.pricePerNight = p; }
}
