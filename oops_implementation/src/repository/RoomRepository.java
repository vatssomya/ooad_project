// repository/RoomRepository.java
package repository;
import model.Room;
import java.util.*;
public class RoomRepository {
    private Map<String,Room> store = new HashMap<>();
    public void save(Room r){ store.put(r.getId(), r); }
    public Optional<Room> findById(String id){ return Optional.ofNullable(store.get(id)); }
    public Collection<Room> findAll(){ return store.values(); }
}
