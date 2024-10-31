package shop.model;

import java.math.BigDecimal;

public class MealRequestDetail {
    private int id;
    private int mealRequestId;
    private int menuId;
    private int quantity;
    private BigDecimal price; // Thêm thuộc tính price
    private Meal meal;

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMealRequestId() {
        return mealRequestId;
    }

    public void setMealRequestId(int mealRequestId) {
        this.mealRequestId = mealRequestId;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int mealId) {
        this.menuId = mealId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() { // Thêm getter cho price
        return price;
    }

    public void setPrice(BigDecimal price) { // Thêm setter cho price
        this.price = price;
    }
    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }
}
