package shop.repository;

import shop.model.Meal;
import shop.modelviews.MealMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MealRepositoryImpl implements MealRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Meal> getAllMeals() {
        String sql = "SELECT * FROM tbl_meal";
        return jdbcTemplate.query(sql, new MealMapper());
    }

    @Override
    public Meal getMealById(int id) {
        String sql = "SELECT * FROM tbl_meal WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new MealMapper());
    }

    @Override
    public void addMeal(Meal meal) {
        String sql = "INSERT INTO tbl_meal (meal_group_id, meal_name, price, description) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, meal.getMealGroupId(), meal.getMealName(), meal.getPrice(), meal.getDescription());
    }

    @Override
    public void updateMeal(Meal meal) {
        String sql = "UPDATE tbl_meal SET meal_group_id = ?, meal_name = ?, price = ?, description = ? WHERE id = ?";
        jdbcTemplate.update(sql, meal.getMealGroupId(), meal.getMealName(), meal.getPrice(), meal.getDescription(), meal.getId());
    }

    @Override
    public void deleteMeal(int id) {
        String sql = "DELETE FROM tbl_meal WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
