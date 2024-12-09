package shop.model;

import java.sql.Date;

public class Like {
    private int id;
    private int userId;
    private int newsId;
    private Date createdAt;

    // Constructor
    public Like(int id, int userId, int newsId, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.newsId = newsId;
        this.createdAt = createdAt;
    }
    public Like() {}

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNewsId() {
        return newsId;
    }

    public void setNewsId(int newsId) {
        this.newsId = newsId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
