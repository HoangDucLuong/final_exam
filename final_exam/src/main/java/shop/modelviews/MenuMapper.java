package shop.modelviews;

import org.springframework.jdbc.core.RowMapper;
import shop.model.Menu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MenuMapper implements RowMapper<Menu> {

    @Override
    public Menu mapRow(ResultSet rs, int rowNum) throws SQLException {
        Menu menu = new Menu();
        menu.setId(rs.getInt("id"));
        menu.setMenuName(rs.getString("menu_name"));
        menu.setMenuType(rs.getInt("menu_type"));

        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        if (createdAtTimestamp != null) {
            menu.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        } else {
            menu.setCreatedAt(null);
        }

        return menu;
    }

}
