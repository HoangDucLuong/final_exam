package shop.modelviews;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import org.springframework.jdbc.core.RowMapper;

import shop.model.Like;

public class LikeMapper implements RowMapper<Like> {
	@Override
	public Like mapRow(ResultSet rs, int rowNum) throws SQLException{
		Like like = new Like();
		like.setId(rs.getInt("id"));
		like.setNewsId(rs.getInt("news_id"));
		like.setUserId(rs.getInt("user_id"));
		like.setCreatedAt(rs.getObject("created_at", Date.class));
		return like;
	}
	
}
