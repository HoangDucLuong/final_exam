package shop.repository;

import shop.model.MealGroup;
import shop.model.Meal;
import java.util.List;

public interface MealGroupRepository {
    List<MealGroup> getAllMealGroups(int page, String search);
    MealGroup getMealGroupById(int id);
    void addMealGroup(MealGroup mealGroup);
    void updateMealGroup(MealGroup mealGroup);

    List<Meal> getMealsByGroupId(int groupId);
    int countMealGroups(String search);
}
