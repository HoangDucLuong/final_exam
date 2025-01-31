package shop.repository;

import shop.model.Comment;

import java.util.List;

public interface CommentRepository {

	List<Comment> findCommentsByNewsId(int newsId);

	void addComment(Comment comment);

	void deleteComment(int commentId);

	void updateComment(Comment comment);

	int countCommentsByNewsId(int newsId);

	Comment findCommentById(int commentId); // Thêm phương thức này

}
