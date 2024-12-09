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

	List<MealRequest> getMealRequestsByUserId(int userId);

	void updateTotalMeals(int mealRequestId, int totalMeals);

	MealRequest findById(int id);

	List<MealRequest> getMealRequestsByUserId(int userId, int page, int size);

	// Phương thức lọc theo contractId
	List<MealRequest> getMealRequestsByContractId(int contractId, int page, int size);

	// Thêm phương thức để lấy số lượng MealRequest
	int countMealRequestsByUserId(int userId);

	int countMealRequestsByContractId(int contractId);
	
	List<MealRequest> findByContractId(int contractId);
	
	List<MealRequest> getMealRequestsByContractId(int invoiceId);
	
	List<MealRequest> findMealRequestsByInvoiceId(int invoiceId);
	List<MealRequest> getMealRequestsByInvoiceId(int invoiceId);
}
