package shop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.Comment;
import shop.model.Like;
import shop.model.News;
import shop.model.User;
import shop.repository.CommentRepository;
import shop.repository.LikeRepository;
import shop.repository.NewsRepository;
import shop.repository.UserRepository;
import shop.utils.SecurityUtility;

@Controller
public class HomeController {

	@Autowired
	UserRepository repUser;

	@Autowired
	NewsRepository newsRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private LikeRepository likeRepository;

	@Autowired
	private JavaMailSender mailSender;

	@GetMapping("/")
	public ModelAndView showHomePage() {
		return new ModelAndView("home/index");
	}

	@GetMapping("/about")
	public ModelAndView showAboutPage() {
		return new ModelAndView("home/about");
	}

	@GetMapping("/contact")
	public ModelAndView showContactPage() {
		return new ModelAndView("home/contact");
	}

	@PostMapping("/contact")
	public String sendContactMessage(@RequestParam String name, @RequestParam String email,
			@RequestParam String subject, @RequestParam String message, RedirectAttributes redirectAttributes) {

		try {
			// Tạo email mới
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo("lhoangduc47@gmail.com");
			mailMessage.setSubject("Contact from: " + subject);
			mailMessage.setText("Name: " + name + "\nEmail: " + email + "\n\nMessage: " + message);

			// Gửi email
			mailSender.send(mailMessage);

			// Thêm thông báo thành công
			redirectAttributes.addFlashAttribute("success", "Your message has been sent successfully!");

			// Redirect về trang liên hệ
			return "redirect:/contact";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "An error occurred while sending the message!");
			return "redirect:/contact";
		}
	}

	@GetMapping("/service")
	public ModelAndView showServicePage() {
		return new ModelAndView("home/service");
	}

	@GetMapping("/team")
	public ModelAndView showTeamPage() {
		return new ModelAndView("home/team");
	}

	@GetMapping("/testimonial")
	public ModelAndView showTestimonialPage() {
		return new ModelAndView("home/testimonial");
	}

	@GetMapping("/profile")
	public String profile(HttpServletRequest request, Model model) {
		String email = (String) request.getSession().getAttribute("user");
		if (email != null) {
			User user = repUser.findUserByEmail(email);
			model.addAttribute("user", user);

			if (request.getAttribute("success") != null) {
				model.addAttribute("success", request.getAttribute("success"));
			}
			if (request.getAttribute("error") != null) {
				model.addAttribute("error", request.getAttribute("error"));
			}

			return "home/profile";
		}
		return "redirect:/user/login";
	}

	@PostMapping("/update")
	public String updateProfile(@RequestParam String name, @RequestParam String phone, @RequestParam String address,
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			User user = repUser.findUserByEmail(email);
			user.setName(name);
			user.setPhone(phone);
			user.setAddress(address);

			repUser.updateUser(user);

			// Add success message to RedirectAttributes
			redirectAttributes.addFlashAttribute("success", "Updated information successfully!");

			return "redirect:/profile"; // Redirect to profile page
		}

		// If the user is not logged in
		return "redirect:/user/login";
	}

	@PostMapping("/change-password")
	@ResponseBody
	public ResponseEntity<Map<String, String>> changePassword(@RequestParam String oldPassword,
			@RequestParam String newPassword, @RequestParam String confirmPassword, HttpServletRequest request) {

		String email = (String) request.getSession().getAttribute("user");
		Map<String, String> response = new HashMap<>();

		if (email != null) {
			User user = repUser.findUserByEmail(email);

			if (user == null) {
				// User not found error
				response.put("error", "User not found.");
				return ResponseEntity.badRequest().body(response);
			}

			// Validate old password
			if (!SecurityUtility.compareBcrypt(user.getPwd(), oldPassword)) {
				// Incorrect old password error
				response.put("error", "Incorrect old password.");
				return ResponseEntity.badRequest().body(response);
			}
			if (oldPassword.equals(newPassword)) {
				// New password cannot be the same as the old password
				response.put("error", "New password cannot be the same as the old password.");
				return ResponseEntity.badRequest().body(response);
			}
			// Validate new password and confirmation
			if (!newPassword.equals(confirmPassword)) {
				// New password and confirm password mismatch
				response.put("error", "New password and confirmation do not match.");
				return ResponseEntity.badRequest().body(response);
			}

			// Update the password
			user.setPwd(SecurityUtility.encryptBcrypt(newPassword));
			repUser.updateUser(user);

			// Success message
			response.put("success", "Password changed successfully.");
			return ResponseEntity.ok(response);
		}

		// User not logged in
		response.put("error", "Please log in to change your password.");
		return ResponseEntity.badRequest().body(response);
	}

	@GetMapping("/news")
	public String showNewsPage(Model model) {
		List<News> newsList = newsRepository.findAllActive();
		newsList.forEach(news -> {
			news.setLikeCount(likeRepository.countLikesByNewsId(news.getId()));
			news.setCommentCount(commentRepository.countCommentsByNewsId(news.getId()));
		});
		model.addAttribute("newsList", newsList);
		return "home/news";
	}

	@GetMapping("/news/{id}")
	public String viewNews(@PathVariable("id") int id, Model model, HttpServletRequest request) {
		// Tìm kiếm bài viết
		News news = newsRepository.findById(id);
		if (news == null || news.getStatus() != 1) {
			model.addAttribute("error", "News not found!");
			return "redirect:/news";
		}
		model.addAttribute("news", news);

		// Tìm kiếm các comment
		List<Comment> comments = commentRepository.findCommentsByNewsId(id);

		// Gán tên user cho từng comment
		for (Comment comment : comments) {
			String userName = repUser.findUserNameById(comment.getUserId());
			comment.setUserName(userName); // Thêm thuộc tính userName vào Comment
		}

		model.addAttribute("comments", comments);

		// Lấy số lượng like của bài viết
		int likeCount = likeRepository.countLikesByNewsId(id);
		model.addAttribute("likeCount", likeCount);

		// Lấy email từ session và kiểm tra user đã đăng nhập
		 String email = (String) request.getSession().getAttribute("user");
		    if (email != null) {
		        Integer userId = (Integer) request.getSession().getAttribute("userId");
		        model.addAttribute("userId", userId); // Đưa userId vào model để sử dụng trong Thymeleaf
		        if (userId != null) {
		            boolean userLiked = likeRepository.hasUserLikedNews(userId, id);
		            model.addAttribute("userLiked", userLiked);
		        }
		        // Thêm đối tượng user vào model nếu cần
		        User user = repUser.findById(userId);
		        model.addAttribute("user", user);
		    }
		

		return "home/news-details";
	}

	@PostMapping("/news/{id}/comments")
	public String addComment(@PathVariable("id") int id, @RequestParam String content, HttpServletRequest request) {
		System.out.println("Email from session: " + request.getSession().getAttribute("user"));
		System.out.println("UserId from session: " + request.getSession().getAttribute("userId"));

		String email = (String) request.getSession().getAttribute("user");
		if (email == null) {
			return "redirect:/user/login";
		}

		Integer userId = (Integer) request.getSession().getAttribute("userId");
		if (userId == null) {
			return "redirect:/user/login";
		}

		Comment comment = new Comment();
		comment.setUserId(userId);
		comment.setNewsId(id);
		comment.setContent(content);
		comment.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));
		commentRepository.addComment(comment);

		return "redirect:/news/" + id;
	}

	@PostMapping("/news/{id}/likes")
	public String addLike(@PathVariable("id") int id, HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");
		if (email == null) {
			return "redirect:/user/login";
		}

		Integer userId = (Integer) request.getSession().getAttribute("userId");
		if (userId != null && !likeRepository.hasUserLikedNews(userId, id)) {
			Like like = new Like();
			like.setUserId(userId);
			like.setNewsId(id);
			like.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));
			likeRepository.addLike(like);
		}
		return "redirect:/news/" + id;
	}

	@PostMapping("/news/{newsId}/comments/{commentId}/delete")
	public String deleteComment(@PathVariable("newsId") int newsId, @PathVariable("commentId") int commentId,
			HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");

		if (email == null) {
			return "redirect:/user/login";
		}

		Integer userId = (Integer) request.getSession().getAttribute("userId");
		User user = repUser.findUserByEmail(email);

		if (userId == null || user == null) {
			return "redirect:/user/login";
		}

		// Kiểm tra xem comment có tồn tại không
		Comment comment = commentRepository.findCommentById(commentId);
		if (comment == null) {
			return "redirect:/news/" + newsId;
		}

		// Kiểm tra nếu user là chủ bình luận hoặc admin thì mới được xóa
		if (comment.getUserId() == userId || user.getUsrType() == 1) {
			commentRepository.deleteComment(commentId);
		}

		return "redirect:/news/" + newsId;
	}

	@PostMapping("/news/{id}/unlikes")
	public String removeLike(@PathVariable("id") int id, HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");
		if (email == null) {
		}

		Integer userId = (Integer) request.getSession().getAttribute("userId");
		if (userId != null && likeRepository.hasUserLikedNews(userId, id)) {
			likeRepository.removeLike(userId, id);
		}
		return "redirect:/news/" + id;
	}
}
