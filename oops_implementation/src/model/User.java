// model/User.java
package model;

public abstract class User {
    protected String id;
    protected String username;
    protected String password;
    protected String role;
    protected String email;

    public User(String id, String username, String password, String role, String email) {
        this.id = id; this.username = username; this.password = password; this.role = role; this.email = email;
    }
    public String getId(){return id;}
    public String getUsername(){return username;}
    public String getPassword(){return password;}
    public String getRole(){return role;}
    public String getEmail(){return email;}
}
