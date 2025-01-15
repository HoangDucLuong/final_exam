package shop.controller;

import shop.model.Meal;
import shop.model.MealGroup;
import shop.repository.MealRepository;
import shop.repository.MealGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/meal")
public class MealController {
    
    @Autowired
    private MealRepository mealRepository;
    
    @Autowired
    private MealGroupRepository mealGroupRepository;

    // Hiển thị danh sách món ăn với phân trang và tìm kiếm
    @GetMapping("/list")
    public String getAllMeals(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "search", required = false, defaultValue = "") String search) {
    	
    	int pageSize = 5; 
		int totalMeals = mealRepository.countAllMeals(search); 
		int totalPages = totalMeals > 0 ? (int) Math.ceil((double) totalMeals / pageSize) : 1;
		
        
        List<Meal> meals = mealRepository.findAllMeals(page, search);
       

        model.addAttribute("meals", meals);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        return "admin/meal/meals"; // Đường dẫn view đã thay đổi
    }


    // Hiển thị form thêm món ăn
    @GetMapping("/add")
    public String showAddMealForm(Model model) {
    	int page = 1; // Trang mặc định
    	String search = ""; // Chuỗi tìm kiếm trống
    	List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups(page, search);
        model.addAttribute("meal", new Meal()); // Tạo đối tượng Meal mới cho form
        model.addAttribute("mealGroups", mealGroups);
        return "admin/meal/add-meal"; // Đường dẫn view đã thay đổi
    }

    // Xử lý khi thêm món ăn mới
    @PostMapping("/add")
    public String addMeal(@ModelAttribute Meal meal) {
        mealRepository.addMeal(meal);
        return "redirect:/admin/meal/list"; // Quay lại danh sách món ăn
    }

    // Hiển thị form chỉnh sửa món ăn
    @GetMapping("/edit/{id}")
    public String showEditMealForm(@PathVariable("id") int id, Model model) {
        Meal meal = mealRepository.getMealById(id);
        int page = 1; // Trang mặc định
        String search = ""; // Chuỗi tìm kiếm trống
        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups(page, search);
        model.addAttribute("meal", meal);
        model.addAttribute("mealGroups", mealGroups);
        return "admin/meal/edit-meal"; // Đường dẫn view đã thay đổi
    }

    // Xử lý khi cập nhật món ăn
    @PostMapping("/edit/{id}")
    public String updateMeal(@PathVariable("id") int id, @ModelAttribute Meal meal) {
        meal.setId(id);
        mealRepository.updateMeal(meal);
        return "redirect:/admin/meal/list";
    }

    // Xử lý khi xóa món ăn
    @GetMapping("/delete/{id}")
    public String deleteMeal(@PathVariable("id") int id) {
        mealRepository.deleteMeal(id);
        return "redirect:/admin/meal/list";
    }
}
