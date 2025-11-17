// model/Staff.java
package model;
public class Staff extends User {
    public Staff(String id, String username, String password, String email) {
        super(id, username, password, "STAFF", email);
    }
}
