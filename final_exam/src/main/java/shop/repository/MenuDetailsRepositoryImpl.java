package shop.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import shop.model.MenuDetails;

@Repository
public class MenuDetailsRepositoryImpl implements MenuDetailsRepository {
    
    private final JdbcTemplate jdbcTemplate;

    public MenuDetailsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addMenuDetail(MenuDetails menuDetails) {
        String sql = "INSERT INTO tbl_menu_details (menu_id, meal_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, menuDetails.getMenuId(), menuDetails.getMealId());
    }
}
