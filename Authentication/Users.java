package Authentication;



import java.time.LocalDate;

public class Users {
    protected String firstName;
    protected String lastName;
    protected String password;
    protected String securityAnswer;
    protected String cnic;
    protected LocalDate dateOfBirth;
    protected Gender gender;
    protected String phone;
    protected String email;
    public static int idCounter=1 ;
    protected String userID;


    /*
    First constructor will be used when there will be a new signup, An id will be automatically
    generated.
    Second constructor will be used when running saveUser Method.
     */

    public Users(String firstName, String lastName, String email, String password,
                 String securityAnswer, String cnic, LocalDate dob,
                 Gender gender, String phone) {
        this.firstName=firstName;
        this.lastName=lastName;
        this.password = password;
        this.securityAnswer = securityAnswer;
        this.cnic =cnic;
        this.dateOfBirth = dob;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.userID = "APP-" + idCounter++;
    }

    public Users(String firstName, String lastName, String email, String password,
                 String securityAnswer, String cnic, LocalDate dob,
                 Gender gender, String phone,String userID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.securityAnswer = securityAnswer;
        this.cnic = cnic;
        this.dateOfBirth = dob;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public String getCnic() {
        return cnic;
    }


    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
