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

    @Override
    public List<MealRequestDetail> getDetailsByMealRequestId(int mealRequestId) {
        String sql = "SELECT " +
                     "    mrd.id AS detail_id, " +
                     "    mrd.meal_request_id, " +
                     "    mrd.menu_id, " +
                     "    mrd.quantity, " +
                     "    mrd.price AS detail_price, " +
                     "    m.menu_name, " +
                     "    me.id AS meal_id, " +
                     "    me.meal_name, " +
                     "    me.price AS meal_price " +
                     "FROM " +
                     "    meal_request_detail mrd " +
                     "JOIN " +
                     "    tbl_menu m ON mrd.menu_id = m.id " +
                     "JOIN " +
                     "    tbl_menu_details md ON m.id = md.menu_id " +
                     "JOIN " +
                     "    tbl_meal me ON md.meal_id = me.id " +
                     "WHERE " +
                     "    mrd.meal_request_id = ?";
        
        // Ánh xạ kết quả trả về vào đối tượng MealRequestDetail
        return jdbcTemplate.query(sql, new Object[]{mealRequestId}, (rs, rowNum) -> {
            MealRequestDetail detail = new MealRequestDetail();
            detail.setId(rs.getInt("detail_id"));
            detail.setMealRequestId(rs.getInt("meal_request_id"));
            detail.setMenuId(rs.getInt("menu_id"));
            detail.setQuantity(rs.getInt("quantity"));
            detail.setPrice(rs.getBigDecimal("detail_price"));

            // Thêm thông tin món ăn
            detail.setMenuName(rs.getString("menu_name"));
            detail.setMealId(rs.getInt("meal_id"));
            detail.setMealName(rs.getString("meal_name"));
            detail.setMealPrice(rs.getBigDecimal("meal_price"));

            return detail;
        });
    }

}
