package shop.repository;

import shop.model.MealRequestDetail;
import shop.modelviews.MealRequestDetailMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MealRequestDetailRepositoryImpl implements MealRequestDetailRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<MealRequestDetail> getAllMealRequestDetails() {
        String sql = "SELECT * FROM meal_request_detail";
        return jdbcTemplate.query(sql, new MealRequestDetailMapper());
    }

    @Override
    public MealRequestDetail getMealRequestDetailById(int id) {
        String sql = "SELECT * FROM meal_request_detail WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new MealRequestDetailMapper());
    }

    @Override
    public void addMealRequestDetail(MealRequestDetail mealRequestDetail) {
        String sql = "INSERT INTO meal_request_detail (meal_request_id, menu_id, quantity, price) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, mealRequestDetail.getMealRequestId(), mealRequestDetail.getMenuId(), 
                mealRequestDetail.getQuantity(), mealRequestDetail.getPrice());
    }

    @Override
    public void updateMealRequestDetail(MealRequestDetail mealRequestDetail) {
        String sql = "UPDATE meal_request_detail SET meal_request_id = ?, menu_id = ?, quantity = ?, price = ? WHERE id = ?";
        jdbcTemplate.update(sql, mealRequestDetail.getMealRequestId(), mealRequestDetail.getMenuId(), 
                mealRequestDetail.getQuantity(), mealRequestDetail.getPrice(), mealRequestDetail.getId());
    }

    @Override
    public void deleteMealRequestDetail(int id) {
        String sql = "DELETE FROM meal_request_detail WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    // Thêm phương thức để lấy chi tiết yêu cầu suất ăn theo mealRequestId
    @Override
    public List<MealRequestDetail> getDetailsByMealRequestId(int mealRequestId) {
        String sql = "SELECT mrd.*, m.menu_name " +
                     "FROM meal_request_detail mrd " +
                     "JOIN tbl_menu m ON mrd.menu_id = m.id " +
                     "WHERE mrd.meal_request_id = ?";

        return jdbcTemplate.query(sql, new Object[]{mealRequestId}, (rs, rowNum) -> {
            MealRequestDetail detail = new MealRequestDetail();
            detail.setId(rs.getInt("id"));
            detail.setMealRequestId(rs.getInt("meal_request_id"));
            detail.setMenuId(rs.getInt("menu_id"));
            detail.setQuantity(rs.getInt("quantity"));
            detail.setPrice(rs.getBigDecimal("price"));
            
            // Lấy tên menu từ kết quả truy vấn
            detail.setMenuName(rs.getString("menu_name")); // Đảm bảo rằng MealRequestDetail có trường menuName
            
            return detail;
        });
    }


}
