package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import shop.model.Like;

@Repository
public class LikeRepositoryImpl implements LikeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int countLikesByNewsId(int newsId) {
        String sql = "SELECT COUNT(*) FROM tbl_like WHERE news_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{newsId}, Integer.class);
    }

    @Override
    public boolean hasUserLikedNews(int userId, int newsId) {
        String sql = "SELECT COUNT(*) FROM tbl_like WHERE user_id = ? AND news_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{userId, newsId}, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public void addLike(Like like) {
        String sql = "INSERT INTO tbl_like (user_id, news_id, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, like.getUserId(), like.getNewsId(), like.getCreatedAt());
    }

    @Override
    public void removeLike(int userId, int newsId) {
        String sql = "DELETE FROM tbl_like WHERE user_id = ? AND news_id = ?";
        jdbcTemplate.update(sql, userId, newsId);
    }
}
