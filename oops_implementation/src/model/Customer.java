// model/Customer.java
package model;
public class Customer extends User {
    public Customer(String id, String username, String password, String email) {
        super(id, username, password, "CUSTOMER", email);
    }
}
