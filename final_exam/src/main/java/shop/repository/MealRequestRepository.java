package shop.repository;

import shop.model.MealRequest;
import java.util.List;

public interface MealRequestRepository {
    // Thêm mới một MealRequest và trả về ID
    int addMealRequest(MealRequest mealRequest);

    // Lấy tất cả MealRequest
    List<MealRequest> getAllMealRequests();

    // Lấy MealRequest theo ID
    MealRequest getMealRequestById(int id);

    // Cập nhật MealRequest
    void updateMealRequest(MealRequest mealRequest);

    // Xóa MealRequest
    void deleteMealRequest(int id);

    // Lấy UserId theo MealRequest ID
    Integer getUserIdByMealRequestId(int mealRequestId);
    
    void updateTotalMeals(int mealRequestId, int totalMeals);
    MealRequest findById(Long id);
}
