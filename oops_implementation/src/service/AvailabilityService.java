// service/AvailabilityService.java
package service;
import repository.ReservationRepository;
import model.Reservation;
import java.time.LocalDate;
public class AvailabilityService {
    private ReservationRepository reservationRepo;
    public AvailabilityService(ReservationRepository repo){ this.reservationRepo = repo; }
    public boolean isAvailable(String roomId, LocalDate ci, LocalDate co, String excludingReservationId) {
        for(Reservation r : reservationRepo.findAll()) {
            if(!"CONFIRMED".equals(r.getStatus()) && !"CHECKEDIN".equals(r.getStatus())) continue;
            if(!r.getRoomId().equals(roomId)) continue;
            if(excludingReservationId!=null && excludingReservationId.equals(r.getId())) continue;
            LocalDate a = r.getCheckIn(), b = r.getCheckOut();
            if(!(co.compareTo(a) <= 0 || ci.compareTo(b) >= 0)) return false;
        }
        return true;
    }
    public boolean isAvailable(String roomId, LocalDate ci, LocalDate co) {
        return isAvailable(roomId, ci, co, null);
    }
}
