package Authentication;

public class Admins {
    String email;
    String password;
    private int adminID;  // Admin ID from database
    private boolean isSuperAdmin;  // new flag


    public Admins(String email, String password) {
        this.email = email;
        this.password = password;
        this.adminID = -1;  // Default: not set
        this.isSuperAdmin = false;
    }

    public Admins(int adminID, String email, String password) {
        this.adminID = adminID;
        this.email = email;
        this.password = password;
        this.isSuperAdmin = false;
    }

    // getters and setters
    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.isSuperAdmin = superAdmin;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
