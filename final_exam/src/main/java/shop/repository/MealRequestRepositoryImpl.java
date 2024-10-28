package shop.repository;

import shop.model.MealRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class MealRequestRepositoryImpl implements MealRequestRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<MealRequest> mealRequestRowMapper = new RowMapper<MealRequest>() {
        @Override
        public MealRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            MealRequest mealRequest = new MealRequest();
            mealRequest.setId(rs.getInt("id"));
            mealRequest.setContractId(rs.getInt("contract_id"));
            mealRequest.setRequestDate(rs.getObject("request_date", LocalDate.class));
            mealRequest.setDeliveryDate(rs.getObject("delivery_date", LocalDate.class));
            mealRequest.setTotalMeals(rs.getInt("total_meals"));
            mealRequest.setStatus(rs.getInt("status"));
            return mealRequest;
        }
    };

    @Override
    public int addMealRequest(MealRequest mealRequest) {
        String sql = "INSERT INTO tbl_meal_request (contract_id, request_date, delivery_date, total_meals, status) "
                + "VALUES (?, ?, ?, ?, ?); SELECT SCOPE_IDENTITY();"; // Use SCOPE_IDENTITY() to get the last inserted ID

        // Thực hiện câu lệnh SQL để thêm yêu cầu và lấy ID của yêu cầu vừa được tạo
        return jdbcTemplate.queryForObject(sql, new Object[]{
                mealRequest.getContractId(),
                mealRequest.getRequestDate(),
                mealRequest.getDeliveryDate(),
                mealRequest.getTotalMeals(),
                mealRequest.getStatus()
        }, Integer.class);
    }



    @Override
    public List<MealRequest> getAllMealRequests() {
        String sql = "SELECT * FROM tbl_meal_request";
        return jdbcTemplate.query(sql, mealRequestRowMapper);
    }

    @Override
    public MealRequest getMealRequestById(int id) {
        String sql = "SELECT * FROM tbl_meal_request WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, mealRequestRowMapper);
    }

    @Override
    public void updateMealRequest(MealRequest mealRequest) {
        String sql = "UPDATE tbl_meal_request SET contract_id = ?, request_date = ?, delivery_date = ?, total_meals = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, mealRequest.getContractId(), mealRequest.getRequestDate(),
                mealRequest.getDeliveryDate(), mealRequest.getTotalMeals(), mealRequest.getStatus(),
                mealRequest.getId());
    }

    @Override
    public void deleteMealRequest(int id) {
        String sql = "DELETE FROM tbl_meal_request WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Integer getUserIdByMealRequestId(int mealRequestId) {
        String sql = "SELECT c.usr_id FROM tbl_meal_request mr JOIN tbl_contract c ON mr.contract_id = c.id WHERE mr.id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{mealRequestId}, Integer.class);
    }
    
    @Override
    public void updateTotalMeals(int mealRequestId, int totalMeals) {
        String sql = "UPDATE tbl_meal_request SET total_meals = ? WHERE id = ?";
        jdbcTemplate.update(sql, totalMeals, mealRequestId);
    }
    @Override
    public MealRequest findById(Long id) {
        String sql = "SELECT * FROM tbl_meal_request WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(MealRequest.class));
    }
}
