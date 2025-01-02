package shop.repository;

import shop.model.News;
import java.util.List;

public interface NewsRepository {
    List<News> findAll();
    News findById(int id);
    void addNews(News news);
    void updateNews(News news);
    void deleteNews(int id);
    List<News> findAllActive();
    List<News> findAllNews(int page, String search);
    int countNews(String search);

}
