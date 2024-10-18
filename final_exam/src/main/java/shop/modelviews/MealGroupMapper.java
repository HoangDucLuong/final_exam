package shop.modelviews;

import shop.model.Meal;
import shop.model.MealGroup;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MealGroupMapper implements RowMapper<MealGroup> {
    @Override
    public MealGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
        MealGroup mealGroup = new MealGroup();
        mealGroup.setId(rs.getInt("id"));
        mealGroup.setGroupName(rs.getString("group_name"));
        mealGroup.setDescription(rs.getString("description"));
        
        // Nếu bạn có thông tin về món ăn trong ResultSet, bạn có thể thêm vào đây
        List<Meal> meals = new ArrayList<>();
        // Ví dụ: ánh xạ các món ăn nếu cần
        // meals.add(new MealMapper().mapRow(rs, rowNum)); // Ví dụ ánh xạ từng món ăn
        
        mealGroup.setMeals(meals); // Thiết lập danh sách món ăn
        return mealGroup;
    }
}
