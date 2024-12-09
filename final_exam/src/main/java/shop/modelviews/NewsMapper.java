package shop.modelviews;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import org.springframework.jdbc.core.RowMapper;

import shop.model.News;

public class NewsMapper implements RowMapper<News> {    
    @Override
    public News mapRow(ResultSet rs, int rowNum) throws SQLException {
        News news = new News();
        news.setId(rs.getInt("id"));
        news.setTitle(rs.getString("title"));
        news.setContent(rs.getString("content"));
        
        // Kiểm tra nếu thumbnail là null
        String thumbnail = rs.getString("thumbnail");
        if (thumbnail != null) {
            news.setThumbnail(thumbnail);  // Gán thumbnail nếu có
        } else {
            news.setThumbnail("");  // Hoặc gán giá trị mặc định nếu không có thumbnail
        }

        // Xử lý các trường khác
        news.setCreatedAt(rs.getObject("created_at", Date.class));
        news.setUpdatedAt(rs.getObject("updated_at", Date.class));
        news.setStatus(rs.getInt("status"));
        
        return news;
    }
}
