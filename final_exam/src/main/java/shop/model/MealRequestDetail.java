package shop.model;

public class MealRequestDetail {
    private int id;
    private int mealRequestId;
    private int mealId;
    private int quantity;
    private double price; // Thêm thuộc tính price
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

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() { // Thêm getter cho price
        return price;
    }

    public void setPrice(double price) { // Thêm setter cho price
        this.price = price;
    }
    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }
}
