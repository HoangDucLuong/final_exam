	package shop.controller;

import shop.model.Meal;
import shop.model.MealGroup;
import shop.model.Menu;
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

    // Hiển thị danh sách món ăn
    @GetMapping("/list")
    public String getAllMeals(Model model) {
        List<Meal> meals = mealRepository.getAllMeals();
        model.addAttribute("meals", meals);
        return "admin/meal/meals"; // Đường dẫn view đã thay đổi
    }

    // Hiển thị form thêm món ăn
    @GetMapping("/add")
    public String showAddMealForm(Model model) {
        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
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
        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
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
