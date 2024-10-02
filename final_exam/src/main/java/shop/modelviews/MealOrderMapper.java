package shop.modelviews;

import shop.model.MealOrder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MealOrderMapper implements RowMapper<MealOrder> {
    @Override
    public MealOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
        MealOrder mealOrder = new MealOrder();
        mealOrder.setId(rs.getInt("id"));
        mealOrder.setMealId(rs.getInt("meal_id"));
        mealOrder.setMenuId(rs.getInt("menu_id"));
        mealOrder.setOrderDate(rs.getDate("order_date"));
        mealOrder.setQuantity(rs.getInt("quantity"));
        mealOrder.setStatus(rs.getInt("status"));
        return mealOrder;
    }
}
