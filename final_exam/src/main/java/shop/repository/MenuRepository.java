package shop.repository;

import shop.model.Meal;
import shop.model.Menu;
import shop.model.MenuDetails;

import java.util.List;

public interface MenuRepository {
    void addMenu(Menu menu);  // Thêm một menu mới
    List<Menu> getAllMenus(); // Lấy tất cả các menu
    Menu getMenuById(int id); // Lấy menu theo id
    void updateMenu(Menu menu); // Cập nhật thông tin menu
    void deleteMenu(int id);    // Xóa menu theo id
    void save(Menu menu);  // Phương thức lưu menu
    List<Menu> findAll();
    List<Integer> getMealsByMenuId(int menuId); // Lấy ID các món ăn theo menu ID
    
    List<Menu> findAllMenus(int page, String search); // Tìm kiếm và phân trang menu
    int countAllMenus(String search); // Đếm tổng số menu theo từ khóa tìm kiếm
    List<Meal> findMealsByMenuId(int menuId); // Lấy danh sách món ăn theo menu ID

    void addMenuDetail(MenuDetails menuDetails); // Thêm chi tiết menu
}
