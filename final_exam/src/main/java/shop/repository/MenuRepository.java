package shop.repository;

import shop.model.Meal;
import shop.model.Menu;
import shop.model.MenuDetails;

import java.math.BigDecimal;
import java.util.List;

public interface MenuRepository {
    void addMenu(Menu menu);  // Thêm một menu mới
    List<Menu> getAllMenus(); // Lấy tất cả các menu
    List<Menu> findAdminMenus(); // Lấy danh sách menu do admin tạo

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

    // Thêm các phương thức cần thiết
    List<Menu> findMenusByUserId(int userId); // Lấy danh sách menu theo ID người dùng
    Menu findMenuByIdAndUserId(int menuId, int userId); // Lấy menu theo ID và ID người dùng
    void delete(Menu menu); // Xóa menu

    // Thêm phương thức để lấy giá món ăn theo ID
    BigDecimal getMenuPriceById(int menuId); // Lấy giá món ăn theo ID
}
