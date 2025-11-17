// service/PricingService.java
package service;
import model.Room;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.*;
public class PricingService {
    private static final double TAX = 0.12;
    private static final Map<String,Double> BASE = new HashMap<>();
    static { BASE.put("SINGLE",1500.0); BASE.put("DOUBLE",2500.0); BASE.put("SUITE",4500.0); }
    public double computePrice(Room room, LocalDate ci, LocalDate co) {
        long nights = ChronoUnit.DAYS.between(ci, co);
        if(nights <= 0) return 0;
        double base = room.getPricePerNight() > 0 ? room.getPricePerNight() : BASE.getOrDefault(room.getType(),1500.0);
        double total = base * nights * (1 + TAX);
        return Math.round(total * 100.0) / 100.0;
    }
}
