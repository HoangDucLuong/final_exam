package shop.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import shop.model.Meal;
import shop.model.Menu;
import shop.model.MenuDetails;
import shop.modelviews.MenuMapper;
import shop.modelviews.MealMapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class MenuRepositoryImpl implements MenuRepository {

    private final JdbcTemplate jdbcTemplate;

    public MenuRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public BigDecimal getMenuPriceById(int menuId) {
        String sql = "SELECT price FROM tbl_menu_details WHERE menu_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{menuId}, (rs, rowNum) -> rs.getBigDecimal("price"));
    }

    @Override
    public List<Menu> findMenusByUserId(int userId) {
        String sql = "SELECT * FROM tbl_menu WHERE usr_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId}, new MenuMapper()); // Bạn cần có MenuMapper
    }

    @Override
    public Menu findMenuByIdAndUserId(int menuId, int userId) {
        String sql = "SELECT * FROM tbl_menu WHERE id = ? AND usr_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{menuId, userId}, new MenuMapper());
    }

    @Override
    public void delete(Menu menu) {
        String sql = "DELETE FROM tbl_menu WHERE id = ?";
        jdbcTemplate.update(sql, menu.getId());
    }

    // Các phương thức khác đã được khai báo

    @Override
    public void addMenu(Menu menu) {
        String sql = "INSERT INTO tbl_menu (menu_name, menu_type, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, menu.getMenuName(), menu.getMenuType(), menu.getCreatedAt());
    }

    @Override
    public void addMenuDetail(MenuDetails menuDetails) {
        String sql = "INSERT INTO tbl_menu_details (menu_id, meal_id, price) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, menuDetails.getMenuId(), menuDetails.getMealId(), menuDetails.getPrice());
    }

    @Override
    public List<Menu> getAllMenus() {
        String sql = "SELECT * FROM tbl_menu";
        return jdbcTemplate.query(sql, new MenuMapper());
    }
    @Override
    public List<Menu> findAdminMenus() {
        String sql = "SELECT * FROM tbl_menu WHERE usr_id =1";
        return jdbcTemplate.query(sql, new MenuMapper()); // Sử dụng MenuMapper để ánh xạ kết quả vào đối tượng Menu
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
        // Gộp câu lệnh chèn và lấy ID vào một câu lệnh duy nhất
        String sql = "INSERT INTO tbl_menu (menu_name, menu_type, usr_id, created_at) VALUES (?, ?, ?, ?); SELECT CAST(SCOPE_IDENTITY() AS int)";

        // Thực hiện câu lệnh và lấy ID của bản ghi mới tạo
        Integer newId = jdbcTemplate.queryForObject(sql, new Object[]{
            menu.getMenuName(),
            menu.getMenuType(),
            menu.getUsrId(),
            menu.getCreatedAt()
        }, Integer.class);

        if (newId != null) {
            menu.setId(newId); // Cập nhật ID vào đối tượng menu
        } else {
            throw new RuntimeException("Không lấy được ID của menu mới tạo!");
        }
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
    @Override
    public String getMenuNameById(int menuId) {
        String sql = "SELECT menu_name FROM tbl_menu WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{menuId}, String.class);
    }
}

