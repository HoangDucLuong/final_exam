package shop.repository;

import shop.model.MenuDetails;

import java.util.List;

public interface MenuDetailsRepository {
    void addMenuDetail(MenuDetails menuDetails);
    
    List<MenuDetails> findByMenuId(Integer menuId);
    
    void deleteByMenuId(Integer menuId); // Thêm phương thức deleteByMenuId
}
