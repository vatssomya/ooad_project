// MainProcedural.java
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MainProcedural {
    static Scanner sc = new Scanner(System.in);

    // Data stores
    static Map<String, Map<String,Object>> users = new HashMap<>(); // id -> map
    static Map<String, Map<String,Object>> rooms = new HashMap<>(); // id -> map
    static List<Map<String,Object>> reservations = new ArrayList<>();
    static List<Map<String,Object>> payments = new ArrayList<>();

    // Constants
    static Map<String, Double> RATES = new HashMap<>();
    static final double TAX = 0.12;

    // Bootstrap demo data
    static {
        RATES.put("SINGLE", 1500.0);
        RATES.put("DOUBLE", 2500.0);
        RATES.put("SUITE", 4500.0);

        // create demo users: admin/password, staff/password, customer/password
        users.put("admin", makeUser("admin","admin","ADMIN","admin@hrs.com"));
        users.put("staff", makeUser("staff","staff","STAFF","staff@hrs.com"));
        users.put("cust", makeUser("cust","cust","CUSTOMER","cust@hrs.com"));

        // rooms
        addRoomInternal("R1","101","SINGLE", "AVAILABLE", 1500);
        addRoomInternal("R2","102","DOUBLE", "AVAILABLE", 2500);
        addRoomInternal("R3","103","SUITE", "AVAILABLE", 4500);
    }

    public static void main(String[] args) {
        System.out.println("=== HOTEL RESERVATION SYSTEM (Procedural) ===");
        mainMenu();
    }

    static Map<String,Object> makeUser(String id, String pwd, String role, String email) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", id);
        m.put("username", id);
        m.put("password", pwd);
        m.put("role", role);
        m.put("email", email);
        return m;
    }

    static void addRoomInternal(String id, String number, String type, String status, double price) {
        Map<String,Object> r = new HashMap<>();
        r.put("id", id);
        r.put("number", number);
        r.put("type", type);
        r.put("status", status);
        r.put("price", price);
        rooms.put(id, r);
    }

    // ---------- Menus ----------
    static void mainMenu() {
        while(true) {
            System.out.println("\n1) Login\n2) Register (Customer)\n3) Exit");
            String ch = sc.nextLine().trim();
            if(ch.equals("1")) login();
            else if(ch.equals("2")) registerCustomer();
            else if(ch.equals("3")) { System.out.println("Bye!"); break; }
        }
    }

    static void login() {
        System.out.print("Username: "); String u = sc.nextLine().trim();
        System.out.print("Password: "); String p = sc.nextLine().trim();
        Map<String,Object> user = users.get(u);
        if(user == null || !user.get("password").equals(p)) {
            System.out.println("Invalid credentials.");
            return;
        }
        String role = (String)user.get("role");
        System.out.println("Welcome " + u + " [" + role + "]");
        if(role.equals("ADMIN")) adminMenu(u);
        else if(role.equals("STAFF")) staffMenu(u);
        else customerMenu(u);
    }

    static void registerCustomer() {
        System.out.print("Choose username: "); String u = sc.nextLine().trim();
        if(users.containsKey(u)) { System.out.println("Username exists."); return; }
        System.out.print("Password: "); String p = sc.nextLine().trim();
        System.out.print("Email: "); String e = sc.nextLine().trim();
        users.put(u, makeUser(u,p,"CUSTOMER",e));
        System.out.println("Registered. Please login.");
    }

    // ---------- Customer ----------
    static void customerMenu(String uid) {
        while(true) {
            System.out.println("\nCustomer Menu: 1) Search & Book  2) My Bookings  3) Cancel Booking  4) Modify Booking  5) Logout");
            String ch = sc.nextLine().trim();
            if(ch.equals("1")) searchAndBookFlow(uid);
            else if(ch.equals("2")) listMyBookings(uid);
            else if(ch.equals("3")) cancelFlow(uid);
            else if(ch.equals("4")) modifyFlow(uid);
            else if(ch.equals("5")) break;
        }
    }

    static void searchAndBookFlow(String uid) {
        LocalDate ci = readDate("Enter check-in (YYYY-MM-DD): ");
        if(ci==null) return;
        LocalDate co = readDate("Enter check-out (YYYY-MM-DD): ");
        if(co==null) return;
        if(!validateDateRange(ci,co)) return;
        System.out.print("Room type (SINGLE/DOUBLE/SUITE/ALL): ");
        String type = sc.nextLine().trim().toUpperCase();
        List<Map<String,Object>> avail = searchRooms(ci,co,type);
        if(avail.isEmpty()) { System.out.println("No rooms available."); return; }
        System.out.println("Available rooms:");
        for(Map<String,Object> r: avail) {
            System.out.printf("%s | No:%s | Type:%s | Price/night:%.2f\n",
                r.get("id"), r.get("number"), r.get("type"), ((Number)r.get("price")).doubleValue());
        }
        System.out.print("Enter room id to book: "); String rid = sc.nextLine().trim();
        if(!rooms.containsKey(rid)) { System.out.println("Invalid room id"); return; }
        double total = calculatePrice((String)rooms.get(rid).get("type"), ci, co);
        System.out.printf("Total for %d nights: ₹%.2f (incl tax)\n", ChronoUnit.DAYS.between(ci,co), total);
        System.out.print("Confirm booking? (yes/no): ");
        if(!sc.nextLine().trim().equalsIgnoreCase("yes")) { System.out.println("Aborted."); return; }
        // process payment
        String method = choosePaymentMethod();
        Map<String,Object> pay = processPayment(total, method);
        if(!"SUCCESS".equals(pay.get("status"))) { System.out.println("Payment failed."); return; }
        Map<String,Object> res = createReservation(uid, rid, ci, co, total, pay.get("txnRef").toString());
        System.out.println("Booked. Reservation id: " + res.get("id"));
        System.out.println("Confirmation sent to: " + users.get(uid).get("email"));
    }

    static String choosePaymentMethod() {
        System.out.println("Payment methods: 1) CARD 2) UPI");
        String ch = sc.nextLine().trim();
        if(ch.equals("2")) return "UPI"; return "CARD";
    }

    static Map<String,Object> processPayment(double amount, String method) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", UUID.randomUUID().toString());
        m.put("amount", amount);
        m.put("method", method);
        m.put("status", "SUCCESS"); // always success mock
        String txn = method + "-" + UUID.randomUUID().toString().substring(0,8);
        m.put("txnRef", txn);
        payments.add(m);
        System.out.println("Payment success. TxnRef: " + txn);
        return m;
    }

    static Map<String,Object> createReservation(String uid, String roomId, LocalDate ci, LocalDate co, double total, String txnRef) {
        Map<String,Object> r = new HashMap<>();
        String id = UUID.randomUUID().toString().substring(0,8);
        r.put("id", id);
        r.put("customerId", uid);
        r.put("roomId", roomId);
        r.put("checkIn", ci);
        r.put("checkOut", co);
        r.put("total", total);
        r.put("status", "CONFIRMED");
        r.put("txnRef", txnRef);
        reservations.add(r);
        return r;
    }

    static List<Map<String,Object>> searchRooms(LocalDate ci, LocalDate co, String type) {
        List<Map<String,Object>> res = new ArrayList<>();
        for(Map<String,Object> room : rooms.values()) {
            String rid = (String)room.get("id");
            if(!"AVAILABLE".equals(room.get("status"))) continue;
            if(!type.equals("ALL") && !((String)room.get("type")).equals(type)) continue;
            if(isAvailable(rid, ci, co)) res.add(room);
        }
        return res;
    }

    static boolean isAvailable(String roomId, LocalDate ci, LocalDate co) {
        for(Map<String,Object> r: reservations) {
            if(!"CONFIRMED".equals(r.get("status"))) continue;
            if(!roomId.equals(r.get("roomId"))) continue;
            LocalDate a = (LocalDate) r.get("checkIn");
            LocalDate b = (LocalDate) r.get("checkOut");
            if(!(co.compareTo(a) <= 0 || ci.compareTo(b) >= 0)) {
                return false; // overlap
            }
        }
        return true;
    }

    static double calculatePrice(String roomType, LocalDate ci, LocalDate co) {
        long nights = ChronoUnit.DAYS.between(ci, co);
        if(nights <= 0) return 0;
        double base = RATES.getOrDefault(roomType, 0.0) * nights;
        double finalPrice = base * (1 + TAX);
        return Math.round(finalPrice * 100.0) / 100.0;
    }

    static LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date format.");
            return null;
        }
    }

    static boolean validateDateRange(LocalDate ci, LocalDate co) {
        if(ci == null || co == null) { System.out.println("Dates required"); return false; }
        if(!ci.isBefore(co)) { System.out.println("Check-out must be after check-in."); return false; }
        return true;
    }

    static void listMyBookings(String uid) {
        System.out.println("Your bookings:");
        for(Map<String,Object> r : reservations) {
            if(uid.equals(r.get("customerId"))) {
                System.out.printf("ID:%s Room:%s %s -> %s Total:₹%.2f Status:%s\n",
                    r.get("id"), r.get("roomId"), r.get("checkIn"), r.get("checkOut"),
                    ((Number)r.get("total")).doubleValue(), r.get("status"));
            }
        }
    }

    static void cancelFlow(String uid) {
        System.out.print("Enter reservation id to cancel: ");
        String id = sc.nextLine().trim();
        for(Map<String,Object> r: reservations) {
            if(id.equals(r.get("id")) && uid.equals(r.get("customerId"))) {
                r.put("status", "CANCELLED");
                System.out.println("Cancelled.");
                return;
            }
        }
        System.out.println("Reservation not found or not owned by you.");
    }

    static void modifyFlow(String uid) {
        System.out.print("Enter reservation id to modify: ");
        String id = sc.nextLine().trim();
        for(Map<String,Object> r: reservations) {
            if(id.equals(r.get("id")) && uid.equals(r.get("customerId"))) {
                LocalDate ci = readDate("New check-in (YYYY-MM-DD): ");
                LocalDate co = readDate("New check-out (YYYY-MM-DD): ");
                if(!validateDateRange(ci,co)) return;
                String rid = (String) r.get("roomId");
                // check availability excluding this reservation
                boolean ok = true;
                for(Map<String,Object> other: reservations) {
                    if(other == r) continue;
                    if(!"CONFIRMED".equals(other.get("status"))) continue;
                    if(!rid.equals(other.get("roomId"))) continue;
                    LocalDate a = (LocalDate) other.get("checkIn");
                    LocalDate b = (LocalDate) other.get("checkOut");
                    if(!(co.compareTo(a) <= 0 || ci.compareTo(b) >= 0)) { ok=false; break; }
                }
                if(!ok) { System.out.println("New dates not available."); return; }
                double total = calculatePrice((String)rooms.get(rid).get("type"), ci, co);
                r.put("checkIn", ci); r.put("checkOut", co); r.put("total", total);
                System.out.println("Modified. New total ₹" + total);
                return;
            }
        }
        System.out.println("Reservation not found or not owned by you.");
    }

    // ---------- Staff ----------
    static void staffMenu(String uid) {
        while(true) {
            System.out.println("\nStaff Menu: 1) Add Room 2) Update Room 3) View Bookings 4) Check-In 5) Check-Out 6) Logout");
            String ch = sc.nextLine().trim();
            if(ch.equals("1")) staffAddRoom();
            else if(ch.equals("2")) staffUpdateRoom();
            else if(ch.equals("3")) staffViewBookings();
            else if(ch.equals("4")) staffCheckIn();
            else if(ch.equals("5")) staffCheckOut();
            else if(ch.equals("6")) break;
        }
    }

    static void staffAddRoom() {
        System.out.print("Room id (R4 etc): "); String id = sc.nextLine().trim();
        System.out.print("Room number: "); String num = sc.nextLine().trim();
        System.out.print("Type SINGLE/DOUBLE/SUITE: "); String type = sc.nextLine().trim().toUpperCase();
        System.out.print("Price per night: "); double p = Double.parseDouble(sc.nextLine().trim());
        addRoomInternal(id,num,type,"AVAILABLE",p);
        System.out.println("Added room " + id);
    }

    static void staffUpdateRoom() {
        System.out.print("Room id: "); String id = sc.nextLine().trim();
        Map<String,Object> r = rooms.get(id);
        if(r == null) { System.out.println("No such room."); return; }
        System.out.println("1) Change status  2) Change price");
        String ch = sc.nextLine().trim();
        if(ch.equals("1")) {
            System.out.print("New status (AVAILABLE/MAINTENANCE): "); String s = sc.nextLine().trim();
            r.put("status", s);
            System.out.println("Updated.");
        } else {
            System.out.print("New price: "); double np = Double.parseDouble(sc.nextLine().trim());
            r.put("price", np);
            System.out.println("Updated.");
        }
    }

    static void staffViewBookings() {
        System.out.println("All bookings:");
        for(Map<String,Object> r: reservations) {
            System.out.printf("ID:%s Cust:%s Room:%s %s->%s Status:%s\n",
                r.get("id"), r.get("customerId"), r.get("roomId"), r.get("checkIn"), r.get("checkOut"), r.get("status"));
        }
    }

    static void staffCheckIn() {
        System.out.print("Reservation id to check-in: "); String id = sc.nextLine().trim();
        for(Map<String,Object> r: reservations) {
            if(id.equals(r.get("id"))) {
                r.put("status","CHECKEDIN");
                System.out.println("Checked in.");
                return;
            }
        }
        System.out.println("Not found.");
    }

    static void staffCheckOut() {
        System.out.print("Reservation id to check-out: "); String id = sc.nextLine().trim();
        for(Map<String,Object> r: reservations) {
            if(id.equals(r.get("id"))) {
                r.put("status","CHECKEDOUT");
                System.out.println("Checked out.");
                return;
            }
        }
        System.out.println("Not found.");
    }

    // ---------- Admin ----------
    static void adminMenu(String uid) {
        while(true) {
            System.out.println("\nAdmin Menu: 1) Create User 2) Generate Reports 3) Logout");
            String ch = sc.nextLine().trim();
            if(ch.equals("1")) adminCreateUser();
            else if(ch.equals("2")) adminReports();
            else if(ch.equals("3")) break;
        }
    }

    static void adminCreateUser() {
        System.out.print("Username: "); String u = sc.nextLine().trim();
        System.out.print("Password: "); String p = sc.nextLine().trim();
        System.out.print("Role (STAFF/ADMIN): "); String role = sc.nextLine().trim().toUpperCase();
        System.out.print("Email: "); String e = sc.nextLine().trim();
        users.put(u, makeUser(u,p,role,e));
        System.out.println("Created user " + u);
    }

    static void adminReports() {
        System.out.println("Reports: 1) Occupancy 2) Revenue 3) Back");
        String ch = sc.nextLine().trim();
        if(ch.equals("1")) {
            LocalDate from = readDate("From date: "); LocalDate to = readDate("To date: ");
            if(from==null||to==null) return;
            double occ = calculateOccupancy(from,to);
            System.out.printf("Occupancy between %s and %s = %.2f%%\n", from, to, occ*100);
        } else if(ch.equals("2")) {
            LocalDate from = readDate("From date: "); LocalDate to = readDate("To date: ");
            if(from==null||to==null) return;
            double rev = calculateRevenue(from,to);
            System.out.printf("Revenue between %s and %s = ₹%.2f\n", from, to, rev);
        }
    }

    static double calculateOccupancy(LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from,to);
        if(days<=0) return 0;
        long roomCount = rooms.size();
        if(roomCount==0) return 0;
        long totalRoomNights = roomCount * days;
        long bookedNights = 0;
        for(Map<String,Object> r: reservations) {
            if(!"CONFIRMED".equals(r.get("status")) && !"CHECKEDIN".equals(r.get("status")) && !"CHECKEDOUT".equals(r.get("status"))) continue;
            LocalDate a = (LocalDate) r.get("checkIn");
            LocalDate b = (LocalDate) r.get("checkOut");
            LocalDate start = a.isBefore(from) ? from : a;
            LocalDate end = b.isAfter(to) ? to : b;
            long overlap = ChronoUnit.DAYS.between(start, end);
            if(overlap>0) bookedNights += overlap;
        }
        return totalRoomNights==0 ? 0 : (double)bookedNights / totalRoomNights;
    }

    static double calculateRevenue(LocalDate from, LocalDate to) {
        double sum = 0;
        for(Map<String,Object> r: reservations) {
            LocalDate a = (LocalDate) r.get("checkIn");
            LocalDate b = (LocalDate) r.get("checkOut");
            if(b.isBefore(from) || a.isAfter(to)) continue;
            sum += ((Number)r.get("total")).doubleValue();
        }
        return sum;
    }
}
