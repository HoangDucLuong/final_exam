package shop.modelviews;

import shop.model.MealRequestDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MealRequestDetailMapper implements RowMapper<MealRequestDetail> {
    @Override
    public MealRequestDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        MealRequestDetail detail = new MealRequestDetail();
        detail.setId(rs.getInt("id"));
        detail.setMealRequestId(rs.getInt("meal_request_id"));
        detail.setMenuId(rs.getInt("menu_id"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setPrice(rs.getBigDecimal("price")); // Lấy giá từ kết quả truy vấn
        return detail;
    }
}
