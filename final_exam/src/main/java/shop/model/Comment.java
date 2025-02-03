package shop.model;

import java.sql.Date;

public class Comment {
	private int id;
	private int userId;
	private int newsId;
	private String content;
	private Date createdAt;
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	// Constructor
	public Comment(int id, int userId, int newsId, String content, Date createdAt) {
		this.id = id;
		this.userId = userId;
		this.newsId = newsId;
		this.content = content;
		this.createdAt = createdAt;
	}

	public Comment() {
	}

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
