package shop.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String name;
    private String uid;
    private String email;
    private String password;
    private int userType;  // 0: User, 1: Admin
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private int status;  // 0: Inactive, 1: Active

    public User() {}

    public User(int id, String name, String uid, String email, String password, int userType, String phone, String address, LocalDateTime createdAt, int status) {
        this.id = id;
        this.name = name;
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.userType = userType;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
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
}
