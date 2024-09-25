package shop.repository;

import shop.model.Menu;

import java.util.List;

public interface MenuRepository {
    void addMenu(Menu menu);  // Thêm một menu mới
    List<Menu> getAllMenus(); // Lấy tất cả các menu
    Menu getMenuById(int id); // Lấy menu theo id
    void updateMenu(Menu menu); // Cập nhật thông tin menu
    void deleteMenu(int id);    // Xóa menu theo id
    void save(Menu menu);  // Phương thức lưu menu
    List<Menu> findAll();
}
