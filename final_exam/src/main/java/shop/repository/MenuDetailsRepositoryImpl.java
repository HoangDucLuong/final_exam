package shop.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import shop.model.MenuDetails;
import shop.model.Meal; // Đảm bảo import Meal class

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MenuDetailsRepositoryImpl implements MenuDetailsRepository {
    
    private final JdbcTemplate jdbcTemplate;

    public MenuDetailsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addMenuDetail(MenuDetails menuDetails) {
        String sql = "INSERT INTO tbl_menu_details (menu_id, meal_id, price) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, menuDetails.getMenuId(), menuDetails.getMealId(), menuDetails.getPrice());
    }


    @Override
    public List<MenuDetails> findByMenuId(Integer menuId) {
        String sql = "SELECT * FROM tbl_menu_details WHERE menu_id = ?";
        return jdbcTemplate.query(sql, new Object[]{menuId}, new RowMapper<MenuDetails>() {
            @Override
            public MenuDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                MenuDetails menuDetails = new MenuDetails();
                menuDetails.setMenuId(rs.getInt("menu_id"));
                menuDetails.setMealId(rs.getInt("meal_id"));
                menuDetails.setPrice(rs.getBigDecimal("price")); // ánh xạ cột price
                return menuDetails;
            }
        });
    }


    @Override
    public List<MenuDetails> findMenuDetailsByMenuId(Integer menuId) {
        String sql = "SELECT * FROM tbl_menu_details WHERE menu_id = ?";
        return jdbcTemplate.query(sql, new Object[]{menuId}, new RowMapper<MenuDetails>() {
            @Override
            public MenuDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                MenuDetails menuDetails = new MenuDetails();
                menuDetails.setMenuId(rs.getInt("menu_id"));
                menuDetails.setMealId(rs.getInt("meal_id"));
                return menuDetails;
            }
        });
    }

    @Override
    public void deleteByMenuId(Integer menuId) {
        String sql = "DELETE FROM tbl_menu_details WHERE menu_id = ?";
        jdbcTemplate.update(sql, menuId);
    }

    // Thêm phương thức để lấy Meal theo mealId
    public Meal findMealById(Integer mealId) {
        String sql = "SELECT * FROM tbl_meals WHERE id = ?"; // Thay đổi tên bảng theo thực tế
        return jdbcTemplate.queryForObject(sql, new Object[]{mealId}, new RowMapper<Meal>() {
            @Override
            public Meal mapRow(ResultSet rs, int rowNum) throws SQLException {
                Meal meal = new Meal();
                meal.setId(rs.getInt("id"));
                meal.setMealName(rs.getString("meal_name"));
                // Thêm các thuộc tính khác nếu cần
                return meal;
            }
        });
    }
}
