package shop.modelviews;

import shop.model.Meal;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MealMapper implements RowMapper<Meal> {
    @Override
    public Meal mapRow(ResultSet rs, int rowNum) throws SQLException {
        Meal meal = new Meal();
        meal.setId(rs.getInt("id"));
        meal.setMealGroupId(rs.getInt("meal_group_id"));
        meal.setMealName(rs.getString("meal_name"));
        meal.setPrice(rs.getBigDecimal("price"));
        meal.setDescription(rs.getString("description"));
        
        // Nếu bạn muốn lấy tên nhóm món ăn từ cơ sở dữ liệu
        meal.setMealGroupName(rs.getString("group_name")); // Có thể đã được ánh xạ từ mealGroup

        return meal;
    }
}
