package shop.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import shop.model.Meal;
import shop.model.MealGroup;
import shop.model.Menu;
import shop.model.MenuDetails;
import shop.model.User;
import shop.repository.MenuRepository;
import shop.repository.UserRepository;
import shop.repository.MealRepository;
import shop.repository.MenuDetailsRepository;
import shop.repository.MealGroupRepository;

@Controller
public class MenuUserController {

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MealRepository mealRepository;

	@Autowired
	private MenuDetailsRepository menuDetailsRepository;

	@Autowired
	private MealGroupRepository mealGroupRepository;

	@GetMapping("/menu")
	public String showUserMenus(HttpServletRequest request, Model model) {
		String email = (String) request.getSession().getAttribute("user");

		// Lấy danh sách menu do admin tạo
		List<Menu> adminMenus = menuRepository.findAdminMenus();
		model.addAttribute("allMenus", adminMenus);

		if (email != null) {
			User user = userRepository.findUserByEmail(email);
			if (user != null) {
				List<Menu> userMenus = menuRepository.findMenusByUserId(user.getId());
				model.addAttribute("userMenus", userMenus);
			}
		}

		return "user/menu-list";
	}

	@GetMapping("/menu/create")
	public String showCreateMenuPage(Model model, HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			User user = userRepository.findUserByEmail(email);
			model.addAttribute("userId", user.getId());
			int page = 1;
			String search = "";
			List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups(page, search);
			model.addAttribute("mealGroups", mealGroups);

			return "user/create-menu";
		}

		return "redirect:/user/login";
	}

	@PostMapping("/menu/create")
	public String createMenu(@RequestParam String menuName, @RequestParam List<Integer> mealIds,
			HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			User user = userRepository.findUserByEmail(email);
			if (user != null) {
				Menu menu = new Menu();
				menu.setMenuName(menuName);
				menu.setMenuType(0);
				menu.setUsrId(user.getId());
				menu.setCreatedAt(LocalDateTime.now());

				menuRepository.save(menu);

				int newMenuId = menuRepository.getAllMenus().stream()
						.filter(m -> m.getMenuName().equals(menu.getMenuName())).map(Menu::getId).findFirst()
						.orElseThrow(() -> new RuntimeException("Newly created menu not found!"));

				for (Integer mealId : mealIds) {
					Meal meal = mealRepository.getMealById(mealId);
					if (mealRepository.getMealById(mealId) != null) {
						MenuDetails menuDetails = new MenuDetails();
						menuDetails.setMenuId(newMenuId);
						menuDetails.setMealId(mealId);
						menuDetails.setPrice(meal.getPrice());

						menuDetailsRepository.addMenuDetail(menuDetails);
					}
				}

				return "redirect:/menu";
			}
		}
		return "redirect:/user/login";
	}

	@GetMapping("/menu/details/{menuId}")
	public String getMenuDetails(@PathVariable int menuId, Model model) {
		Menu menu = menuRepository.getMenuById(menuId);

		if (menu == null) {
			return "redirect:/menu";
		}

		List<MenuDetails> menuDetailsList = menuDetailsRepository.findByMenuId(menuId);
		List<Meal> meals = new ArrayList<>();

		for (MenuDetails details : menuDetailsList) {
			Optional<Meal> mealOptional = mealRepository.findById(details.getMealId());

			if (mealOptional.isPresent()) {
				meals.add(mealOptional.get());
			} else {
				System.out.println("Món ăn với ID " + details.getMealId() + " không tồn tại.");
			}
		}

		model.addAttribute("menu", menu);
		model.addAttribute("menuDetails", menuDetailsList);
		model.addAttribute("meals", meals);
		return "user/menu-details";
	}

	@PostMapping("/menu/delete")
	public String deleteMenu(@RequestParam("menuId") int menuId, HttpServletRequest request) {
		String email = (String) request.getSession().getAttribute("user");

		if (email != null) {
			User user = userRepository.findUserByEmail(email);
			if (user != null) {
				Menu menu = menuRepository.findMenuByIdAndUserId(menuId, user.getId());
				if (menu != null) {
					menuDetailsRepository.deleteByMenuId(menuId);
					menuRepository.delete(menu);
				}
				return "redirect:/menu";
			}
		}

		return "redirect:/user/login";
	}

}
