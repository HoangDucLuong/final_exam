package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import shop.model.News;
import shop.modelviews.NewsMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class NewsRepositoryImpl implements NewsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<News> findAll() {
        String sql = "SELECT * FROM tbl_news ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new NewsMapper());
    }

    @Override
    public News findById(int id) {
        String sql = "SELECT * FROM tbl_news WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new NewsMapper());
    }

    @Override
    public void addNews(News news) {
        String sql = "INSERT INTO tbl_news (title, content, thumbnail, created_at, updated_at, status) VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, news.getTitle());
            ps.setString(2, news.getContent());
            ps.setString(3, news.getThumbnail());
            ps.setDate(4, news.getCreatedAt());
            ps.setDate(5, news.getUpdatedAt());
            ps.setInt(6, news.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            news.setId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public void updateNews(News news) {
        String sql = "UPDATE tbl_news SET title = ?, content = ?, thumbnail = ?, updated_at = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, news.getTitle(), news.getContent(), news.getThumbnail(), news.getUpdatedAt(), news.getStatus(), news.getId());
    }

    @Override
    public void deleteNews(int id) {
        // Xóa các bản ghi liên quan trong tbl_like
        String deleteLikesSql = "DELETE FROM tbl_like WHERE news_id = ?";
        jdbcTemplate.update(deleteLikesSql, id);
        String deleteCommentsSql = "DELETE FROM tbl_comment WHERE news_id = ?";
        jdbcTemplate.update(deleteCommentsSql, id);
        // Sau đó xóa bản ghi trong tbl_news
        String deleteNewsSql = "DELETE FROM tbl_news WHERE id = ?";
        jdbcTemplate.update(deleteNewsSql, id);
    }

    @Override
    public List<News> findAllActive() {
        String sql = "SELECT * FROM tbl_news WHERE status = 1";
        return jdbcTemplate.query(sql, new NewsMapper());
    }
    @Override
    public List<News> findAllNews(int page, String search) {
        int pageSize = 5;
        int offset = (page - 1) * pageSize;
        String sql = "SELECT * FROM tbl_news WHERE title LIKE ? OR content LIKE ? ORDER BY created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return jdbcTemplate.query(sql, new Object[]{"%" + search + "%", "%" + search + "%", offset, pageSize}, new NewsMapper());
    }

    @Override
    public int countNews(String search) {String sql = "SELECT COUNT(*) FROM tbl_news WHERE title LIKE ? OR content LIKE ?";
    return jdbcTemplate.queryForObject(sql, new Object[]{"%" + search + "%", "%" + search + "%"}, Integer.class);
}

}