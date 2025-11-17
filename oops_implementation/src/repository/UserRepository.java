// repository/UserRepository.java
package repository;
import model.User;
import java.util.*;
public class UserRepository {
    private Map<String,User> store = new HashMap<>();
    public void save(User u){ store.put(u.getUsername(), u); }
    public Optional<User> findByUsername(String username){ return Optional.ofNullable(store.get(username)); }
}
