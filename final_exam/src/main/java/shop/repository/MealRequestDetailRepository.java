package shop.repository;

import shop.model.MealRequestDetail;

import java.util.List;

public interface MealRequestDetailRepository {
    List<MealRequestDetail> getAllMealRequestDetails(); // Lấy tất cả chi tiết yêu cầu suất ăn
    MealRequestDetail getMealRequestDetailById(int id); // Lấy chi tiết yêu cầu suất ăn theo ID
    void addMealRequestDetail(MealRequestDetail mealRequestDetail); // Thêm chi tiết yêu cầu suất ăn
    void updateMealRequestDetail(MealRequestDetail mealRequestDetail); // Cập nhật chi tiết yêu cầu suất ăn
    void deleteMealRequestDetail(int id); // Xóa chi tiết yêu cầu suất ăn
    List<MealRequestDetail> getDetailsByMealRequestId(int mealRequestId);
    List<MealRequestDetail> findByMealRequestId(int mealRequestId);
}
