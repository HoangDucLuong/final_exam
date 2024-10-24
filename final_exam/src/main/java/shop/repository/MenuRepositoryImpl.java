package shop.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import shop.model.Meal;
import shop.model.Menu;
import shop.model.MenuDetails;
import shop.modelviews.MenuMapper;
import shop.modelviews.MealMapper;

import java.util.List;

@Repository
public class MenuRepositoryImpl implements MenuRepository {

    private final JdbcTemplate jdbcTemplate;

    public MenuRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addMenu(Menu menu) {
        String sql = "INSERT INTO tbl_menu (menu_name, menu_type, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, menu.getMenuName(), menu.getMenuType(), menu.getCreatedAt());
    }

    @Override
    public void addMenuDetail(MenuDetails menuDetails) {  // Thêm phương thức mới
        String sql = "INSERT INTO tbl_menu_details (menu_id, meal_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, menuDetails.getMenuId(), menuDetails.getMealId());
    }

    @Override
    public List<Menu> getAllMenus() {
        String sql = "SELECT * FROM tbl_menu";
        return jdbcTemplate.query(sql, new MenuMapper());
    }

    @Override
    public Menu getMenuById(int id) {
        String sql = "SELECT * FROM tbl_menu WHERE id = ?";
        Menu menu = jdbcTemplate.queryForObject(sql, new MenuMapper(), id);
        
        // Lấy danh sách các ID món ăn liên quan đến menu
        List<Integer> mealIds = getMealsByMenuId(id);
        menu.setMealIds(mealIds);
        
        return menu;
    }

    @Override
    public void updateMenu(Menu menu) {
        String sql = "UPDATE tbl_menu SET menu_name = ?, menu_type = ? WHERE id = ?";
        jdbcTemplate.update(sql, menu.getMenuName(), menu.getMenuType(), menu.getId());
    }

    @Override
    public void deleteMenu(int id) {
        String sql = "DELETE FROM tbl_menu WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void save(Menu menu) {
        addMenu(menu); // Gọi phương thức addMenu để lưu menu
    }

    @Override
    public List<Menu> findAll() {
        String sql = "SELECT * FROM tbl_menu";
        return jdbcTemplate.query(sql, new MenuMapper());
    }

    @Override
    public List<Integer> getMealsByMenuId(int menuId) {
        String sql = "SELECT meal_id FROM tbl_menu_details WHERE menu_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("meal_id"), menuId);
    }

    @Override
    public List<Menu> findAllMenus(int page, String search) {
        int pageSize = 5; // Số lượng menu trên mỗi trang
        int offset = (page - 1) * pageSize;

        String sql = "SELECT * FROM tbl_menu WHERE menu_name LIKE ? ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        return jdbcTemplate.query(sql, new Object[]{"%" + search + "%", offset, pageSize}, new MenuMapper());
    }

    @Override
    public int countAllMenus(String search) {
        String sql = "SELECT COUNT(*) FROM tbl_menu WHERE menu_name LIKE ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{"%" + search + "%"}, Integer.class);
    }

    @Override
    public List<Meal> findMealsByMenuId(int menuId) {
        String sql = "SELECT m.* FROM tbl_meal m " +
                     "JOIN tbl_menu_details md ON m.id = md.meal_id " +
                     "WHERE md.menu_id = ?";
        return jdbcTemplate.query(sql, new MealMapper(), menuId);
    }
}

