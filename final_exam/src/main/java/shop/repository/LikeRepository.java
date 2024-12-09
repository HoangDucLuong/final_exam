package shop.repository;

import shop.model.Like;

public interface LikeRepository {

    int countLikesByNewsId(int newsId);

    boolean hasUserLikedNews(int userId, int newsId);

    void addLike(Like like);

    void removeLike(int userId, int newsId);
}
