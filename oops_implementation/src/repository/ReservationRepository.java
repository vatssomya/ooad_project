// repository/ReservationRepository.java
package repository;
import model.Reservation;
import java.util.*;
public class ReservationRepository {
    private Map<String,Reservation> store = new HashMap<>();
    public void save(Reservation r){ store.put(r.getId(), r); }
    public Collection<Reservation> findAll(){ return store.values(); }
    public Optional<Reservation> findById(String id){ return Optional.ofNullable(store.get(id)); }
}
