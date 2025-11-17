// service/ReservationService.java
package service;
import repository.*;
import model.*;
import java.time.LocalDate;
import java.util.UUID;
public class ReservationService {
    private ReservationRepository reservationRepo;
    private RoomRepository roomRepo;
    private AvailabilityService avail;
    private PricingService pricing;
    private PaymentService paymentService;
    public ReservationService(ReservationRepository resRepo, RoomRepository roomRepo, AvailabilityService avail, PricingService pricing, PaymentService paymentService) {
        this.reservationRepo = resRepo; this.roomRepo = roomRepo; this.avail = avail; this.pricing = pricing; this.paymentService = paymentService;
    }
    public Reservation createReservation(String customerId, String roomId, LocalDate ci, LocalDate co, String paymentMethod) {
        var rOpt = roomRepo.findById(roomId);
        if(!rOpt.isPresent()) throw new IllegalArgumentException("Room not found");
        if(!avail.isAvailable(roomId, ci, co)) throw new IllegalStateException("Room not available");
        Room room = rOpt.get();
        double total = pricing.computePrice(room, ci, co);
        Payment pay = paymentService.processPayment(total, paymentMethod);
        if(!"SUCCESS".equals(pay.getStatus())) throw new IllegalStateException("Payment failed");
        String id = UUID.randomUUID().toString().substring(0,8);
        Reservation res = new Reservation(id, customerId, roomId, ci, co, total, "CONFIRMED", pay.getTxnRef());
        reservationRepo.save(res);
        return res;
    }
}
