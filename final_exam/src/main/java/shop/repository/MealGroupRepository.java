package shop.repository;

import shop.model.MealGroup;
import shop.model.Meal;
import java.util.List;

public interface MealGroupRepository {
    List<MealGroup> getAllMealGroups();
    MealGroup getMealGroupById(int id);
    void addMealGroup(MealGroup mealGroup);
    void updateMealGroup(MealGroup mealGroup);
    void deleteMealGroup(int id);
    
    // Thêm phương thức này
    List<Meal> getMealsByGroupId(int groupId);
}
