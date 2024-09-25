package shop.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String name;
    private String email;
    private String pwd;
    private int usrType;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private int status;

    // Constructors
    public User() {}

    public User(int id, String name, String email, String pwd, int usrType, String phone, String address, LocalDateTime createdAt, int status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.pwd = pwd;
        this.usrType = usrType;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getUsrType() {
        return usrType;
    }

    public void setUsrType(int usrType) {
        this.usrType = usrType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", pwd='" + pwd + '\'' +
                ", usrType=" + usrType +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                '}';
    }
}
