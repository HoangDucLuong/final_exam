package shop.repository;

import shop.model.Meal;
import shop.model.MealGroup; // Thêm import
import shop.model.Menu;
import shop.modelviews.MealMapper;
import shop.modelviews.MenuMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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


    // Thêm phương thức findAll để lấy tất cả các bản ghi từ bảng tbl_meal
    @Override
    public List<Meal> findAll() {
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
    public Optional<Meal> findById(int id) {
        String sql = "SELECT m.*, g.group_name FROM tbl_meal m LEFT JOIN tbl_meal_group g ON m.meal_group_id = g.id WHERE m.id = ?";
        
        try {
            Meal meal = jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                Meal m = new Meal();
                m.setId(rs.getInt("id"));
                m.setMealGroupId(rs.getInt("meal_group_id"));
                m.setMealName(rs.getString("meal_name"));
                m.setPrice(rs.getBigDecimal("price"));
                m.setDescription(rs.getString("description"));
                m.setMealGroupName(rs.getString("group_name")); // Thêm tên nhóm vào đối tượng Meal
                return m;
            });
            return Optional.of(meal);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // Trả về Optional.empty nếu không tìm thấy món ăn
        }
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
    @Override
    public List<Meal> findAllMeals(int page, String search) {
        int pageSize = 5; // Số lượng món ăn trên mỗi trang
        int offset = (page - 1) * pageSize;

        String sql = "SELECT m.*, g.group_name FROM tbl_meal m " +
                     "LEFT JOIN tbl_meal_group g ON m.meal_group_id = g.id " +
                     "WHERE m.meal_name LIKE ? ORDER BY m.id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        return jdbcTemplate.query(sql, new Object[]{"%" + search + "%", offset, pageSize}, (rs, rowNum) -> {
            Meal meal = new Meal();
            meal.setId(rs.getInt("id"));
            meal.setMealGroupId(rs.getInt("meal_group_id"));
            meal.setMealName(rs.getString("meal_name"));
            meal.setPrice(rs.getBigDecimal("price"));
            meal.setDescription(rs.getString("description"));
            meal.setMealGroupName(rs.getString("group_name"));
            return meal;
        });
    }

    @Override
    public int countAllMeals(String search) {
        String sql = "SELECT COUNT(*) FROM tbl_meal WHERE meal_name LIKE ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{"%" + search + "%"}, Integer.class);
    }

    
    @Override
    public List<Meal> getMealsByContractId(int contractId) {
        String sql = "SELECT m.*, g.group_name " +
                     "FROM tbl_meal m " +
                     "JOIN tbl_menu_details md ON m.id = md.meal_id " +
                     "JOIN tbl_meal_group g ON m.meal_group_id = g.id " +
                     "JOIN contract_detail cd ON md.menu_id = cd.menu_id " +
                     "WHERE cd.contract_id = ?";
        return jdbcTemplate.query(sql, new Object[]{contractId}, new MealMapper());
    }

    @Override
    public double getMealPriceById(int mealId) {
        String sql = "SELECT price FROM tbl_meal WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{mealId}, Double.class);
    }
}
