public class User {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean login(String u, String p) {
        return this.username.equals(u) && this.password.equals(p);
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; } // ← tambahkan ini
    public String getRole() { return role; }
}