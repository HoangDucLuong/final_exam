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
	    public List<MealGroup> getAllMealGroups(int page, String search) {
	        
	        int pageSize = 5;
	        int offset = (page - 1) * pageSize;
	        
	        String sql = "SELECT mg.*, m.* FROM tbl_meal_group mg "
	                   + "LEFT JOIN tbl_meal m ON mg.id = m.meal_group_id "
	                   + "WHERE mg.group_name LIKE ? OR mg.description LIKE ? OR m.meal_name LIKE ?";

	        List<MealGroup> mealGroups = new ArrayList<>();
	        Map<Integer, MealGroup> mealGroupMap = new HashMap<>();

	        jdbcTemplate.query(sql, new Object[]{"%" + search + "%", "%" + search + "%", "%" + search + "%"}, (ResultSet rs) -> {
	            int mealGroupId = rs.getInt("id");

	            MealGroup mealGroup = mealGroupMap.get(mealGroupId);
	            
	            if (mealGroup == null) {
	                mealGroup = new MealGroupMapper().mapRow(rs, 0);
	                mealGroup.setMeals(new ArrayList<>()); 
	                mealGroups.add(mealGroup);
	                mealGroupMap.put(mealGroupId, mealGroup);
	            }

	            Meal meal = new MealMapper().mapRow(rs, 0);
	            if (meal.getId() > 0) {
	                mealGroup.getMeals().add(meal);
	            }
	        });

	        return mealGroups;
	    }
	@Override
	public MealGroup getMealGroupById(int id) {
	    String sql = "SELECT mg.*, m.* FROM tbl_meal_group mg LEFT JOIN tbl_meal m ON mg.id = m.meal_group_id WHERE mg.id = ?";
	    
	    List<MealGroup> mealGroups = new ArrayList<>();
	    Map<Integer, MealGroup> mealGroupMap = new HashMap<>();

	    jdbcTemplate.query(sql, new Object[]{id}, (ResultSet rs) -> {
	        int mealGroupId = rs.getInt("id");
	        
	        MealGroup mealGroup = mealGroupMap.get(mealGroupId);
	        if (mealGroup == null) {
	            mealGroup = new MealGroupMapper().mapRow(rs, 0); 
	            mealGroup.setMeals(new ArrayList<>()); 
	            mealGroups.add(mealGroup);
	            mealGroupMap.put(mealGroupId, mealGroup);
	        }

	        Meal meal = new MealMapper().mapRow(rs, 0);
	        if (meal.getId() > 0) { 
	            mealGroup.getMeals().add(meal);
	        }
	    });

	    return mealGroups.isEmpty() ? null : mealGroups.get(0);
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
    public int countMealGroups(String search) {
        String sql = "SELECT COUNT(*) FROM tbl_meal_group WHERE group_name LIKE ? OR description LIKE ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{"%" + search + "%", "%" + search + "%"}, Integer.class);
    }

}
