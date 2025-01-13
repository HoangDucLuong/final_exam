package shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import shop.model.MealGroup;
import shop.repository.MealGroupRepository;

import java.util.List;

@Controller
public class MealGroupController {

    @Autowired
    private MealGroupRepository mealGroupRepository;

    @GetMapping("/admin/meal-groups")
    public String listMealGroups(Model model, 
                                 @RequestParam(value = "page", defaultValue = "1") int page, 
                                 @RequestParam(value = "search", required = false, defaultValue = "") String search) {

        int pageSize = 5;
        int totalMealGroups = mealGroupRepository.countMealGroups(search);
        int totalPages = totalMealGroups > 0 ? (int) Math.ceil((double) totalMealGroups / pageSize) : 1;

        List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups(page, search);
        model.addAttribute("mealGroups", mealGroups);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        return "admin/meal/meal-groups";
    }

    @GetMapping("/admin/meal-groups/create")
    public String showCreateForm(Model model) {
        MealGroup mealGroup = new MealGroup();
        model.addAttribute("mealGroup", mealGroup);
        return "admin/meal/create-meal-group";
    }

    @PostMapping("/admin/meal-groups/save")
    public String saveMealGroup(@ModelAttribute("mealGroup") MealGroup mealGroup) {
        mealGroupRepository.addMealGroup(mealGroup);
        return "redirect:/admin/meal-groups";
    }
    @GetMapping("/admin/meal-groups/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        MealGroup mealGroup = mealGroupRepository.getMealGroupById(id);
        model.addAttribute("mealGroup", mealGroup);
        return "admin/meal/edit-meal-group";
    }
    
    @PostMapping("/admin/meal-groups/update")
    public String updateMealGroup(@ModelAttribute("mealGroup") MealGroup mealGroup) {
        mealGroupRepository.updateMealGroup(mealGroup);
        return "redirect:/admin/meal-groups";
    }

}
