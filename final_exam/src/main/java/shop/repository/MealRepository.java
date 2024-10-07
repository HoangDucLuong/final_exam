package shop.repository;

import shop.model.Meal;
import java.util.List;

public interface MealRepository {
    List<Meal> getAllMeals();
    Meal getMealById(int id);
    void addMeal(Meal meal);
    void updateMeal(Meal meal);
    void deleteMeal(int id);
}
