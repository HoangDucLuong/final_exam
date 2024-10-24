package shop.modelviews;

import org.springframework.jdbc.core.RowMapper;

import shop.model.MenuDetails;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuDetailMapper implements RowMapper<MenuDetails> {

    @Override
    public MenuDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        MenuDetails menuDetail = new MenuDetails();
        menuDetail.setId(rs.getInt("id"));
        menuDetail.setMenuId(rs.getInt("menu_id"));
        menuDetail.setMealId(rs.getInt("meal_id"));
        return menuDetail;
    }
}
