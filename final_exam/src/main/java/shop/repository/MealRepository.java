package shop.repository;

import shop.model.Meal;
import shop.model.Menu;

import java.util.List;
import java.util.Optional;

public interface MealRepository {
    List<Meal> getAllMeals();
    Meal getMealById(int id);
    void addMeal(Meal meal);
    void updateMeal(Meal meal);
    void deleteMeal(int id);
    List<Meal> findMealsByIds(List<Integer> ids); // Thêm phương thức này
    Optional<Meal> findById(int id);
    List<Meal> findMealsByGroupId(int groupId);
    List<Meal> findMealsByMenuId(int menuId);

    List<Meal> findAll(); // Thêm phương thức findAll để lấy tất cả món ăn
  


    List<Meal> findAllMeals(int page, String search);
 // Tìm kiếm và phân trang menu
    int countAllMeals(String search);
    List<Meal> getMealsByContractId(int contractId);
    double getMealPriceById(int mealId);

}
