package shop.repository;

import shop.model.MealRequest;
import java.util.List;

public interface MealRequestRepository {
	int addMealRequest(MealRequest mealRequest);

	List<MealRequest> getAllMealRequests();

	MealRequest getMealRequestById(int id);

	void updateMealRequest(MealRequest mealRequest);

	void deleteMealRequest(int id);

	Integer getUserIdByMealRequestId(int mealRequestId);

	List<MealRequest> getMealRequestsByUserId(int userId);

	void updateTotalMeals(int mealRequestId, int totalMeals);

	MealRequest findById(int id);

	List<MealRequest> getMealRequestsByUserId(int userId, int page, int size);

	List<MealRequest> getMealRequestsByContractId(int contractId, int page, int size);

	int countMealRequestsByUserId(int userId);

	int countMealRequestsByContractId(int contractId);

	List<MealRequest> findByContractId(int contractId);

	List<MealRequest> getMealRequestsByContractId(int invoiceId);

	List<MealRequest> findMealRequestsByInvoiceId(int invoiceId);

	List<MealRequest> getMealRequestsByInvoiceId(int invoiceId);

	List<MealRequest> searchMealRequests(String keyword, int offset, int limit);

	int countMealRequests(String keyword);


}
