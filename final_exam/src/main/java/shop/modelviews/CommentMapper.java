package shop.modelviews;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import org.springframework.jdbc.core.RowMapper;

import shop.model.Comment;

public class CommentMapper implements RowMapper<Comment> {
	@Override
	public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
		Comment comment = new Comment();
		comment.setId(rs.getInt("id"));
		comment.setContent(rs.getString("content"));
		comment.setCreatedAt(rs.getObject("created_at", Date.class));
		comment.setNewsId(rs.getInt("news_id"));
		comment.setUserId(rs.getInt("user_id"));

		return comment;
	}

}
