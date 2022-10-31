package ge.ufc.webservices.model;

public class User {
    private String firstname;
    private String lastname;
    private String p_number;
    private double balance;
    private String fullName;


    public User() {
    }


//    public User(String firstname, String lastname, double money) {
//        firstname = firstname.substring(0, 1).toUpperCase(Locale.ROOT);
//        lastname = lastname.substring(0, 1).toUpperCase(Locale.ROOT);//        this.fullName = firstname + "." + lastname+".";
//        this.money = money;
//    }




    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getP_number() {
        return p_number;
    }

    public void setP_number(String p_number) {
        this.p_number = p_number;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


}
