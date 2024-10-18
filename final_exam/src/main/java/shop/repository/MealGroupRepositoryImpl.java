package shop.repository;

import shop.model.Meal;
import shop.model.MealGroup;
import shop.modelviews.MealGroupMapper;
import shop.modelviews.MealMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MealGroupRepositoryImpl implements MealGroupRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<Meal> getMealsByGroupId(int groupId) {
		String sql = "SELECT * FROM meals WHERE meal_group_id = ?";
		return jdbcTemplate.query(sql, new Object[] { groupId }, new MealMapper());
	}

	@Override
	public List<MealGroup> getAllMealGroups() {
	    String sql = "SELECT mg.*, m.* FROM tbl_meal_group mg LEFT JOIN tbl_meal m ON mg.id = m.meal_group_id";

	    List<MealGroup> mealGroups = new ArrayList<>();
	    Map<Integer, MealGroup> mealGroupMap = new HashMap<>();

	    jdbcTemplate.query(sql, (ResultSet rs) -> {
	        int mealGroupId = rs.getInt("id"); // Sửa thành "id" để lấy ID của meal_group

	        MealGroup mealGroup = mealGroupMap.get(mealGroupId);
	        
	        if (mealGroup == null) {
	            mealGroup = new MealGroupMapper().mapRow(rs, 0); // Tạo mới MealGroup nếu chưa tồn tại
	            mealGroup.setMeals(new ArrayList<>()); // Khởi tạo danh sách món ăn
	            mealGroups.add(mealGroup);
	            mealGroupMap.put(mealGroupId, mealGroup);
	        }

	        // Thêm món ăn vào nhóm nếu không null
	        Meal meal = new MealMapper().mapRow(rs, 0);
	        if (meal.getId() > 0) { // Kiểm tra ID món ăn hợp lệ
	            mealGroup.getMeals().add(meal);
	        }
	    });

	    return mealGroups;
	}


	@Override
	public MealGroup getMealGroupById(int id) {
		String sql = "SELECT mg.*, m.* FROM tbl_meal_group mg LEFT JOIN meals m ON mg.id = m.meal_group_id WHERE mg.id = ?";
		MealGroup mealGroup = jdbcTemplate.queryForObject(sql, new Object[] { id }, new MealGroupMapper());

		// Initialize the meals list
		mealGroup.setMeals(new ArrayList<>());

		// Load the meals for this group
		String mealSql = "SELECT * FROM meals WHERE meal_group_id = ?";
		List<Meal> meals = jdbcTemplate.query(mealSql, new Object[] { id }, new MealMapper());
		mealGroup.setMeals(meals);

		return mealGroup;
	}

	@Override
	public void addMealGroup(MealGroup mealGroup) {
		String sql = "INSERT INTO tbl_meal_group (group_name, description) VALUES (?, ?)";
		jdbcTemplate.update(sql, mealGroup.getGroupName(), mealGroup.getDescription());
	}

	@Override
	public void updateMealGroup(MealGroup mealGroup) {
		String sql = "UPDATE tbl_meal_group SET group_name = ?, description = ? WHERE id = ?";
		jdbcTemplate.update(sql, mealGroup.getGroupName(), mealGroup.getDescription(), mealGroup.getId());
	}

	@Override
	public void deleteMealGroup(int id) {
		String sql = "DELETE FROM tbl_meal_group WHERE id = ?";
		jdbcTemplate.update(sql, id);
	}
}
