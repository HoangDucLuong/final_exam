package shop.model;

import java.time.LocalDateTime;
import java.util.List;

public class Menu {
    private int id;
    private String menuName;
    private int menuType;
    private LocalDateTime createdAt;
    private List<Integer> mealIds;
    private int usrId;

    // Các phương thức getter và setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getMenuType() {
        return menuType;
    }

    public void setMenuType(int menuType) {
        this.menuType = menuType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Integer> getMealIds() {
        return mealIds;
    }

    public void setMealIds(List<Integer> mealIds) {
        this.mealIds = mealIds;
    }

    public int getUsrId() {
        return usrId;
    }

    public void setUsrId(int usrId) {
        this.usrId = usrId;
    }
}
