package shop.modelviews;

import shop.model.MealRequest;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class MealRequestMapper implements RowMapper<MealRequest> {
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
}
