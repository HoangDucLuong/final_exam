package shop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
			HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			User user = repUser.findUserByEmail(email);
			user.setName(name);
			user.setPhone(phone);
			user.setAddress(address);

			repUser.updateUser(user);
			request.setAttribute("success", "Updated information successfully!");
			return "redirect:/profile";
		}
		return "redirect:/user/login";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
			@RequestParam String confirmPassword, HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			User user = repUser.findUserByEmail(email);

			if (user != null && SecurityUtility.compareBcrypt(user.getPwd(), oldPassword)) {
				if (newPassword.equals(confirmPassword)) {
					user.setPwd(SecurityUtility.encryptBcrypt(newPassword)); 
					repUser.updateUser(user); 
					return "redirect:/profile";
				} else {
					return "redirect:/profile";
				}
			} else {
				return "redirect:/profile";
			}
		} else {
			return "redirect:/profile"; 
		}
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
		News news = newsRepository.findById(id);
		if (news == null || news.getStatus() != 1) {
			model.addAttribute("error", "News not found!");
			return "redirect:/news";
		}
		model.addAttribute("news", news);

		List<Comment> comments = commentRepository.findCommentsByNewsId(id);
		model.addAttribute("comments", comments);

		int likeCount = likeRepository.countLikesByNewsId(id);
		model.addAttribute("likeCount", likeCount);

		String email = (String) request.getSession().getAttribute("user");
		if (email != null) {
			Integer userId = (Integer) request.getSession().getAttribute("userId");
			if (userId != null) {
				boolean userLiked = likeRepository.hasUserLikedNews(userId, id);
				model.addAttribute("userLiked", userLiked);
			}
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
