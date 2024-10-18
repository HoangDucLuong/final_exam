package shop.model;

import java.math.BigDecimal;

public class Meal {
    private int id;
    private Integer mealGroupId;
    private String mealName;
    private BigDecimal price;
    private String description;
    private String mealGroupName; // Thêm thuộc tính này

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Integer getMealGroupId() {
        return mealGroupId;
    }
    public void setMealGroupId(Integer mealGroupId) {
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
    public String getMealGroupName() { // Thêm phương thức getter
        return mealGroupName;
    }
    public void setMealGroupName(String mealGroupName) { // Thêm phương thức setter
        this.mealGroupName = mealGroupName;
    }
}
