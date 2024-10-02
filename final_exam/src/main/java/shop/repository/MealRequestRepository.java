package shop.repository;

import shop.model.MealRequest;
import java.util.List;

public interface MealRequestRepository {
    // Thêm mới một MealRequest
    void addMealRequest(MealRequest mealRequest);

    // Lấy tất cả MealRequest
    List<MealRequest> getAllMealRequests();

    // Lấy MealRequest theo ID
    MealRequest getMealRequestById(int id);

    // Cập nhật MealRequest
    void updateMealRequest(MealRequest mealRequest);

    // Xóa MealRequest
    void deleteMealRequest(int id);
}
