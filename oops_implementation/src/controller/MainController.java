// controller/MainController.java
package controller;
import repository.*;
import model.*;
import service.*;
import java.util.*;
import java.time.LocalDate;
public class MainController {
    private Scanner sc = new Scanner(System.in);
    private UserRepository userRepo = new UserRepository();
    private RoomRepository roomRepo = new RoomRepository();
    private ReservationRepository reservationRepo = new ReservationRepository();

    private PricingService pricing = new PricingService();
    private PaymentService paymentService = new PaymentService();
    private AvailabilityService availability = new AvailabilityService(reservationRepo);
    private ReservationService reservationService = new ReservationService(reservationRepo, roomRepo, availability, pricing, paymentService);

    public MainController() {
        // seed demo data
        userRepo.save(new model.Customer("cust","cust","cust","cust@hrs.com"));
        userRepo.save(new model.Staff("staff","staff","staff","staff@hrs.com"));
        userRepo.save(new model.Customer("admin","admin","admin","admin@hrs.com")); // admin as customer stub for demo

        roomRepo.save(new Room("R1","101","SINGLE","AVAILABLE",1500));
        roomRepo.save(new Room("R2","102","DOUBLE","AVAILABLE",2500));
        roomRepo.save(new Room("R3","103","SUITE","AVAILABLE",4500));
    }

    public void start() {
    System.out.println("=== HOTEL RESERVATION SYSTEM (OOP) ===");
    while(true) {
        System.out.println("\n1) Login  2) Register  3) Exit");
        String ch = sc.nextLine().trim();
        if(ch.equals("1")) login();
        else if(ch.equals("2")) register();
        else break;
    }
    System.out.println("Goodbye");
}


    private void login() {
        System.out.print("Username: "); String u = sc.nextLine().trim();
        System.out.print("Password: "); String p = sc.nextLine().trim();
        var userOpt = userRepo.findByUsername(u);
        if(!userOpt.isPresent() || !userOpt.get().getPassword().equals(p)) { System.out.println("Invalid"); return; }
        User user = userOpt.get();
        System.out.println("Hello " + user.getUsername() + " Role: " + user.getRole());
        showUserMenu(user);
    }

    private void register() {
    System.out.println("\n=== Register New Customer ===");

    System.out.print("Choose username: ");
    String u = sc.nextLine().trim();

    if(userRepo.findByUsername(u).isPresent()) {
        System.out.println("Username already exists.");
        return;
    }

    System.out.print("Password: ");
    String p = sc.nextLine().trim();

    System.out.print("Email: ");
    String e = sc.nextLine().trim();

    Customer c = new Customer(u, u, p, e);
    userRepo.save(c);

    System.out.println("Registration successful! You can now login.");
    }


    private void showUserMenu(User user) {
        if(user.getRole().equals("STAFF")) staffMenu(user);
        else customerMenu(user);
    }

    private void customerMenu(User user) {
        while(true) {
            System.out.println("\nCustomer Menu: 1) Search & Book 2) My Bookings 3) Logout");
            String ch = sc.nextLine().trim();
            if(ch.equals("1")) customerSearchBook(user);
            else if(ch.equals("2")) listBookings(user);
            else break;
        }
    }

    private void customerSearchBook(User user) {
        try {
            System.out.print("Check-in (YYYY-MM-DD): "); LocalDate ci = LocalDate.parse(sc.nextLine().trim());
            System.out.print("Check-out (YYYY-MM-DD): "); LocalDate co = LocalDate.parse(sc.nextLine().trim());
            System.out.print("Room type (SINGLE/DOUBLE/SUITE/ALL): "); String type = sc.nextLine().trim().toUpperCase();
            List<Room> avail = new ArrayList<>();
            for(Room r : roomRepo.findAll()) {
                if(!"AVAILABLE".equals(r.getStatus())) continue;
                if(!type.equals("ALL") && !type.equals(r.getType())) continue;
                if(availability.isAvailable(r.getId(), ci, co)) avail.add(r);
            }
            if(avail.isEmpty()) { System.out.println("No rooms"); return; }
            for(Room r: avail) System.out.println(r.getId() + " | " + r.getNumber() + " | " + r.getType() + " | ₹" + r.getPricePerNight());
            System.out.print("Select room id: "); String rid = sc.nextLine().trim();
            System.out.print("Payment method (CARD/UPI): "); String pm = sc.nextLine().trim().toUpperCase();
            var res = reservationService.createReservation(user.getId(), rid, ci, co, pm);
            System.out.println("Reservation created: " + res.getId() + " Total: ₹" + res.getTotal());
        } catch(Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void listBookings(User user) {
        System.out.println("Bookings:");
        for(Reservation r : reservationRepo.findAll()) {
            if(user.getId().equals(r.getCustomerId())) {
                System.out.println(r.getId() + " Room:" + r.getRoomId() + " " + r.getCheckIn() + "->" + r.getCheckOut() + " Status:" + r.getStatus());
            }
        }
    }

    private void staffMenu(User user) {
        while(true) {
            System.out.println("\nStaff Menu: 1) Add Room 2) Update Room 3) View Bookings 4) Logout");
            String ch = sc.nextLine().trim();
            if(ch.equals("1")) staffAddRoom();
            else if(ch.equals("2")) staffUpdateRoom();
            else if(ch.equals("3")) staffViewBookings();
            else break;
        }
    }

    private void staffAddRoom() {
        System.out.print("Room id: "); String id = sc.nextLine().trim();
        System.out.print("Number: "); String num = sc.nextLine().trim();
        System.out.print("Type: "); String type = sc.nextLine().trim().toUpperCase();
        System.out.print("Price: "); double p = Double.parseDouble(sc.nextLine().trim());
        roomRepo.save(new Room(id,num,type,"AVAILABLE",p));
        System.out.println("Saved.");
    }

    private void staffUpdateRoom() {
        System.out.print("Room id: "); String id = sc.nextLine().trim();
        var ro = roomRepo.findById(id);
        if(!ro.isPresent()) { System.out.println("Not found"); return; }
        Room r = ro.get();
        System.out.println("1) Change status 2) Change price");
        String ch = sc.nextLine().trim();
        if(ch.equals("1")) { System.out.print("New status: "); r.setStatus(sc.nextLine().trim()); }
        else { System.out.print("New price: "); r.setPricePerNight(Double.parseDouble(sc.nextLine().trim())); }
        System.out.println("Updated.");
    }

    private void staffViewBookings() {
        for(Reservation r : reservationRepo.findAll()) {
            System.out.println(r.getId() + " Cust:" + r.getCustomerId() + " Room:" + r.getRoomId() + " " + r.getCheckIn() + "->" + r.getCheckOut() + " Status:" + r.getStatus());
        }
    }
}
