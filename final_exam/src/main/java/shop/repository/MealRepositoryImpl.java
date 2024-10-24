package shop.repository;

import shop.model.Meal;
import shop.model.MealGroup; // Thêm import
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
        String sql = "SELECT m.*, g.group_name FROM tbl_meal m LEFT JOIN tbl_meal_group g ON m.meal_group_id = g.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Meal meal = new Meal();
            meal.setId(rs.getInt("id"));
            meal.setMealGroupId(rs.getInt("meal_group_id"));
            meal.setMealName(rs.getString("meal_name"));
            meal.setPrice(rs.getBigDecimal("price"));
            meal.setDescription(rs.getString("description"));
            meal.setMealGroupName(rs.getString("group_name")); // Thêm tên nhóm vào đối tượng Meal
            return meal;
        });
    }

    @Override
    public Meal getMealById(int id) {
        String sql = "SELECT m.*, g.group_name FROM tbl_meal m LEFT JOIN tbl_meal_group g ON m.meal_group_id = g.id WHERE m.id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
            Meal meal = new Meal();
            meal.setId(rs.getInt("id"));
            meal.setMealGroupId(rs.getInt("meal_group_id"));
            meal.setMealName(rs.getString("meal_name"));
            meal.setPrice(rs.getBigDecimal("price"));
            meal.setDescription(rs.getString("description"));
            meal.setMealGroupName(rs.getString("group_name")); // Thêm tên nhóm vào đối tượng Meal
            return meal;
        });
    }

    
    @Override
    public List<Meal> findMealsByGroupId(int groupId) {
        String sql = "SELECT m.*, g.group_name FROM tbl_meal m " +
                     "JOIN tbl_meal_group g ON m.meal_group_id = g.id " +
                     "WHERE m.meal_group_id = ?";
        return jdbcTemplate.query(sql, new Object[]{groupId}, new MealMapper());
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

    @Override
    public List<Meal> findMealsByIds(List<Integer> ids) {
        String sql = "SELECT m.*, g.group_name FROM tbl_meal m LEFT JOIN tbl_meal_group g ON m.meal_group_id = g.id WHERE m.id IN (" + String.join(",", ids.stream().map(String::valueOf).toArray(String[]::new)) + ")";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Meal meal = new Meal();
            meal.setId(rs.getInt("id"));
            meal.setMealGroupId(rs.getInt("meal_group_id"));
            meal.setMealName(rs.getString("meal_name"));
            meal.setPrice(rs.getBigDecimal("price"));
            meal.setDescription(rs.getString("description"));
            meal.setMealGroupName(rs.getString("group_name")); // Thêm tên nhóm vào đối tượng Meal
            return meal;
        });
    }

    @Override
    public Meal findById(int id) {
        String sql = "SELECT * FROM tbl_meal WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new MealMapper(), id);
    }
    @Override
    public List<Meal> findMealsByMenuId(int menuId) {
        String sql = "SELECT m.*, mg.group_name " +
                     "FROM tbl_meal m " +
                     "JOIN tbl_menu_details md ON m.id = md.meal_id " +
                     "JOIN tbl_meal_group mg ON m.meal_group_id = mg.id " +
                     "WHERE md.menu_id = ?";
        
        return jdbcTemplate.query(sql, new Object[]{menuId}, new MealMapper());
    }


}
