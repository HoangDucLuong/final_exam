package shop.modelviews;

import shop.model.MealGroup;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MealGroupMapper implements RowMapper<MealGroup> {
    @Override
    public MealGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
        MealGroup mealGroup = new MealGroup();
        mealGroup.setId(rs.getInt("id"));
        mealGroup.setGroupName(rs.getString("group_name"));
        mealGroup.setDescription(rs.getString("description"));
        return mealGroup;
    }
}
