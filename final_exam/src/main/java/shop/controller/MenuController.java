package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import shop.model.Menu;
import shop.model.MenuDetails;
import shop.model.MealGroup;
import shop.model.Meal;
import shop.repository.MenuRepository;
import shop.repository.MealGroupRepository;
import shop.repository.MealRepository;
import shop.repository.MenuDetailsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/menu")
public class MenuController {

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private MenuDetailsRepository menuDetailsRepository;

	@Autowired
	private MealGroupRepository mealGroupRepository;

	@Autowired
	private MealRepository mealRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@GetMapping("/list")
	public String listMenus(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "search", defaultValue = "") String search) {
		// Lấy tổng số menu với điều kiện tìm kiếm
		int totalMenus = menuRepository.countAllMenus(search);
		int totalPages = (int) Math.ceil((double) totalMenus / 5);

		// Kiểm tra và điều chỉnh số trang
		if (page < 1) {
			page = 1; // Đặt lại về trang đầu tiên nếu nhỏ hơn 1
		} else if (page > totalPages && totalPages > 0) {
			page = totalPages; // Đặt lại về trang cuối cùng nếu lớn hơn tổng trang
		}

		// Lấy danh sách menu cho trang hiện tại
		List<Menu> menus = menuRepository.findAllMenus(page, search);
		model.addAttribute("menus", menus);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("search", search);

		return "admin/menu/menu-list";
	}

	@GetMapping("/add")
	public String showAddMenuForm(Model model) {
		model.addAttribute("menu", new Menu());
		List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
		model.addAttribute("mealGroups", mealGroups);

		return "admin/menu/add-menu";
	}

	@PostMapping("/add")
	public String addMenu(@ModelAttribute("menu") Menu menu, @RequestParam("mealGroupId") Integer mealGroupId,
			@RequestParam("mealIds") List<Integer> mealIds) {
		menu.setCreatedAt(LocalDateTime.now());
		menu.setMenuType(1);
		menu.setUsrId(1);

		// Lưu menu và nhận ID
		menuRepository.save(menu); // Gọi phương thức save

		// Lưu ID của menu vừa tạo để liên kết với các món ăn
		int newMenuId = menuRepository.getAllMenus().stream().filter(m -> m.getMenuName().equals(menu.getMenuName()))
				.map(Menu::getId).findFirst().orElseThrow(() -> new RuntimeException("Không tìm thấy menu mới tạo!"));

		// Liên kết các món ăn với menu vừa tạo
		for (Integer mealId : mealIds) {
			Meal meal = mealRepository.getMealById(mealId);
			if (mealRepository.getMealById(mealId) != null) {// Tạo đối tượng MenuDetails để lưu vào tbl_menu_details
				MenuDetails menuDetails = new MenuDetails();
				menuDetails.setMenuId(newMenuId);
				menuDetails.setMealId(mealId);
				 menuDetails.setPrice(meal.getPrice());

				// Lưu vào tbl_menu_details
				menuDetailsRepository.addMenuDetail(menuDetails);
			}
		}

		return "redirect:/admin/menu/list";
	}

	@GetMapping("/edit/{id}")
	public String showEditMenuForm(@PathVariable int id, Model model) {
		Menu menu = menuRepository.getMenuById(id);
		model.addAttribute("menu", menu);

		List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
		model.addAttribute("mealGroups", mealGroups);

		return "admin/menu/edit-menu";
	}

	@PostMapping("/edit/{id}")
	public String editMenu(@PathVariable int id, @ModelAttribute("menu") Menu menu) {
		menu.setId(id);
		menuRepository.updateMenu(menu);
		return "redirect:/admin/menu/list";
	}

	@PostMapping("/delete/{id}")
	public String deleteMenu(@PathVariable int id) {
		menuRepository.deleteMenu(id);
		return "redirect:/admin/menu/list";
	}

	@GetMapping("/meals/{mealGroupId}")
	@ResponseBody
	public List<Meal> getMealsByGroup(@PathVariable Integer mealGroupId) {
		return mealRepository.findMealsByGroupId(mealGroupId);
	}

	private void linkMealToMenu(int menuId, int mealId) {
		String sql = "INSERT INTO tbl_menu_details (menu_id, meal_id) VALUES (?, ?)";
		try {
			// Kiểm tra xem menuId và mealId có hợp lệ không
			if (menuId <= 0 || mealId <= 0) {
				System.err.println("Invalid menuId or mealId");
				return;
			}

			jdbcTemplate.update(sql, menuId, mealId);
		} catch (Exception e) {
			System.err.println("Error linking meal to menu: " + e.getMessage());
		}
	}

	@GetMapping("/detail/{id}")
	public String showMenuDetail(@PathVariable int id, Model model) {
		// Lấy thông tin menu theo ID
		Menu menu = menuRepository.getMenuById(id);
		model.addAttribute("menu", menu);

		// Lấy danh sách món ăn liên quan đến menu
		List<Meal> meals = mealRepository.findMealsByMenuId(id);
		model.addAttribute("meals", meals);

		// Lấy chi tiết của từng món ăn và thêm vào model
		for (Meal meal : meals) {
			// Lấy thêm thông tin chi tiết cho mỗi món ăn nếu cần
			// Ví dụ: mealDetails có thể là một phương thức trong mealRepository
			Meal detailedMeal = mealRepository.getMealById(meal.getId());
			model.addAttribute("mealDetail_" + meal.getId(), detailedMeal);
		}

		return "admin/menu/menu-detail";
	}

}