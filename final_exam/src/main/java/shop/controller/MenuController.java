package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import shop.model.Menu;
import shop.model.MealGroup;
import shop.model.Meal;
import shop.repository.MenuRepository;
import shop.repository.MealGroupRepository;
import shop.repository.MealRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/menu")
public class MenuController {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MealGroupRepository mealGroupRepository;

    @Autowired
    private MealRepository mealRepository;

    @GetMapping("/list")
    public String listMenus(Model model,
                            @RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "search", defaultValue = "") String search) {
        // Lấy danh sách menu theo trang và tìm kiếm
        List<Menu> menus = menuRepository.findAllMenus(page, search);
        model.addAttribute("menus", menus);
        
        // Đếm tổng số menu để tính toán phân trang
        int totalMenus = menuRepository.countAllMenus(search);
        int totalPages = (int) Math.ceil((double) totalMenus / 5); // Giả sử mỗi trang có 10 menu
        
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search); // Để hiển thị lại từ khóa tìm kiếm

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
    public String addMenu(@ModelAttribute("menu") Menu menu,
                          @RequestParam("mealGroupId") Integer mealGroupId,
                          @RequestParam("mealIds") List<Integer> mealIds) {
        menu.setCreatedAt(LocalDateTime.now());
        menu.setMenuType(0);
        menuRepository.save(menu);

        for (Integer mealId : mealIds) {
            linkMealToMenu(menu.getId(), mealId);
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
        return mealRepository.findMealsByGroupId(mealGroupId); // Đảm bảo sử dụng phương thức đã triển khai
    }

    private void linkMealToMenu(int menuId, int mealId) {
        String sql = "INSERT INTO menu_meals (menu_id, meal_id) VALUES (?, ?)";
        // Thực hiện logic lưu vào database (sử dụng JdbcTemplate hoặc phương thức khác)
    }
}
