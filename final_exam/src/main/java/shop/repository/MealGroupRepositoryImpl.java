package shop.repository;

import shop.model.MealGroup;
import shop.modelviews.MealGroupMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MealGroupRepositoryImpl implements MealGroupRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<MealGroup> getAllMealGroups() {
        String sql = "SELECT * FROM tbl_meal_group";
        return jdbcTemplate.query(sql, new MealGroupMapper());
    }

    @Override
    public MealGroup getMealGroupById(int id) {
        String sql = "SELECT * FROM tbl_meal_group WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new MealGroupMapper());
    }

    @Override
    public void addMealGroup(MealGroup mealGroup) {
        String sql = "INSERT INTO tbl_meal_group (group_name, description) VALUES (?, ?)";
        jdbcTemplate.update(sql, mealGroup.getGroupName(), mealGroup.getDescription());
    }

    @Override
    public void updateMealGroup(MealGroup mealGroup) {
        String sql = "UPDATE tbl_meal_group SET group_name = ?, description = ? WHERE id = ?";
        jdbcTemplate.update(sql, mealGroup.getGroupName(), mealGroup.getDescription(), mealGroup.getId());
    }

    @Override
    public void deleteMealGroup(int id) {
        String sql = "DELETE FROM tbl_meal_group WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
