package shop.repository;

import shop.model.MealRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void addMealRequest(MealRequest mealRequest) {
        String sql = "INSERT INTO tbl_meal_request (contract_id, request_date, delivery_date, total_meals, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, mealRequest.getContractId(), mealRequest.getRequestDate(),
                mealRequest.getDeliveryDate(), mealRequest.getTotalMeals(), mealRequest.getStatus());
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
                mealRequest.getDeliveryDate(), mealRequest.getTotalMeals(), mealRequest.getStatus(), mealRequest.getId());
    }

    @Override
    public void deleteMealRequest(int id) {
        String sql = "DELETE FROM tbl_meal_request WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
