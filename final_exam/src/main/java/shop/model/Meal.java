package shop.model;

import java.math.BigDecimal;

public class Meal {
    private int id;
    private int mealGroupId;
    private String mealName;
    private BigDecimal price;
    private String description;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMealGroupId() {
		return mealGroupId;
	}
	public void setMealGroupId(int mealGroupId) {
		this.mealGroupId = mealGroupId;
	}
	public String getMealName() {
		return mealName;
	}
	public void setMealName(String mealName) {
		this.mealName = mealName;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}

