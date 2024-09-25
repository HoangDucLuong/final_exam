package shop.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import shop.model.Menu;
import shop.modelviews.MenuMapper;

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
    public List<Menu> getAllMenus() {
        String sql = "SELECT * FROM tbl_menu";
        return jdbcTemplate.query(sql, new MenuMapper());
    }

    @Override
    public Menu getMenuById(int id) {
        String sql = "SELECT * FROM tbl_menu WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new MenuMapper(), id);
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
        String sql = "INSERT INTO tbl_menu (menu_name, menu_type, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, menu.getMenuName(), menu.getMenuType(), menu.getCreatedAt());
    }

    // Lấy tất cả các menu từ cơ sở dữ liệu
    @Override
    public List<Menu> findAll() {
        String sql = "SELECT * FROM tbl_menu";
        return jdbcTemplate.query(sql, new MenuMapper());
    }
}
