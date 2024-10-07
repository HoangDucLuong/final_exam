package shop.repository;

import shop.model.MealGroup;
import java.util.List;

public interface MealGroupRepository {
    List<MealGroup> getAllMealGroups();
    MealGroup getMealGroupById(int id);
    void addMealGroup(MealGroup mealGroup);
    void updateMealGroup(MealGroup mealGroup);
    void deleteMealGroup(int id);
}
