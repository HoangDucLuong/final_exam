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

    // Hiển thị danh sách menu cho user đã đăng nhập
    @GetMapping("/menu")
    public String showUserMenus(HttpServletRequest request, Model model) {
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = userRepository.findUserByEmail(email);
            if (user != null) {
                // Lấy danh sách menu của người dùng
                List<Menu> userMenus = menuRepository.findMenusByUserId(user.getId());
                model.addAttribute("userMenus", userMenus);

                // Lấy danh sách menu do admin tạo
                List<Menu> adminMenus = menuRepository.findAdminMenus();
                model.addAttribute("allMenus", adminMenus);

                return "user/menu-list"; 
            }
        }
        return "redirect:/user/login"; 
    }


    // Hiển thị form tạo menu mới
    @GetMapping("/menu/create")
    public String showCreateMenuPage(Model model, HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = userRepository.findUserByEmail(email);
            model.addAttribute("userId", user.getId());

            List<MealGroup> mealGroups = mealGroupRepository.getAllMealGroups();
            model.addAttribute("mealGroups", mealGroups);

            return "user/create-menu"; 
        }

        return "redirect:/user/login"; 
    }

    // Xử lý tạo menu mới
    @PostMapping("/menu/create")
    public String createMenu(@RequestParam String menuName,
                             @RequestParam List<Integer> mealIds,
                             HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = userRepository.findUserByEmail(email);
            if (user != null) {
                Menu menu = new Menu();
                menu.setMenuName(menuName);
                menu.setMenuType(0); // 0: User-created
                menu.setUsrId(user.getId());
                menu.setCreatedAt(LocalDateTime.now());

                // Lưu menu
                menuRepository.save(menu); // Gọi phương thức lưu menu
                
                // Lấy ID của menu vừa tạo
                int newMenuId = menuRepository.getAllMenus().stream()
                                            .filter(m -> m.getMenuName().equals(menu.getMenuName()))
                                            .map(Menu::getId)
                                            .findFirst()
                                            .orElseThrow(() -> new RuntimeException("Không tìm thấy menu mới tạo!"));

                // Lưu chi tiết món ăn liên kết với menu
                for (Integer mealId : mealIds) {
                    // Kiểm tra xem món ăn có tồn tại không
                    if (mealRepository.getMealById(mealId) != null) {
                        MenuDetails menuDetails = new MenuDetails();
                        menuDetails.setMenuId(newMenuId); // Sử dụng ID của menu vừa tạo
                        menuDetails.setMealId(mealId);
                        
                        // Lưu vào tbl_menu_details
                        menuDetailsRepository.addMenuDetail(menuDetails);
                    }
                }

                return "redirect:/menu"; 
            }
        }
        return "redirect:/user/login";
    }
 // Hiển thị chi tiết của một menu
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

    // Xóa menu của user
    @PostMapping("/menu/delete")
    public String deleteMenu(@RequestParam("menuId") int menuId, HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("user");

        if (email != null) {
            User user = userRepository.findUserByEmail(email);
            if (user != null) {
                Menu menu = menuRepository.findMenuByIdAndUserId(menuId, user.getId());
                if (menu != null) {
                    // Xóa menu và tất cả chi tiết liên quan (nếu cần)
                    menuDetailsRepository.deleteByMenuId(menuId); // Xóa chi tiết món ăn liên quan (nếu cần)
                    menuRepository.delete(menu);
                }
                return "redirect:/menu"; 
            }
        }

        return "redirect:/user/login"; 
    }

}
