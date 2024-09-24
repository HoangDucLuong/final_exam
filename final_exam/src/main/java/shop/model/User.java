package shop.model;

import java.time.LocalDateTime;

public class User {
    private int _id;
    private String _name;
    private String _uid;
    private String _email;
    private String _pwd;
    private int _usr_type;  // 0: User, 1: Admin
    private String _phone;
    private String _address;
    private LocalDateTime _created_at;
    private int _status;  // 0: Inactive, 1: Active

    public User() {}

    public User(int _id, String _name, String _uid, String _email, String _pwd, int _usr_type, String _phone, String _address, LocalDateTime _created_at, int _status) {
        this._id = _id;
        this._name = _name;
        this._uid = _uid;
        this._email = _email;
        this._pwd = _pwd;
        this._usr_type = _usr_type;
        this._phone = _phone;
        this._address = _address;
        this._created_at = _created_at;
        this._status = _status;
    }

    // Getters and Setters
    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public String getUid() {
        return _uid;
    }

    public void setUid(String _uid) {
        this._uid = _uid;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String _email) {
        this._email = _email;
    }

    public String getPassword() {
        return _pwd;
    }

    public void setPassword(String _pwd) {
        this._pwd = _pwd;
    }

    public int getUserType() {
        return _usr_type;
    }

    public void setUserType(int _usr_type) {
        this._usr_type = _usr_type;
    }

    public String getPhone() {
        return _phone;
    }

    public void setPhone(String _phone) {
        this._phone = _phone;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String _address) {
        this._address = _address;
    }

    public LocalDateTime getCreatedAt() {
        return _created_at;
    }

    public void setCreatedAt(LocalDateTime _created_at) {
        this._created_at = _created_at;
    }

    public int getStatus() {
        return _status;
    }

    public void setStatus(int _status) {
        this._status = _status;
    }
}
