package shop.repository;

import shop.model.MealRequest;
import shop.model.MealRequestDetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class MealRequestRepositoryImpl implements MealRequestRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private MealRequestDetailRepository mealRequestDetailRepository;

	private RowMapper<MealRequest> mealRequestRowMapper = new RowMapper<MealRequest>() {
		@Override
		public MealRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
			MealRequest mealRequest = new MealRequest();
			mealRequest.setId(rs.getInt("id"));
			mealRequest.setContractId(rs.getInt("contract_id"));
			mealRequest.setRequestDate(rs.getObject("request_date", LocalDate.class));
			mealRequest.setDeliveryDate(rs.getObject("delivery_date", LocalDate.class));
			mealRequest.setTotalMeals(rs.getInt("total_meals"));
			mealRequest.setStatus(rs.getInt("status"));
			return mealRequest;
		}
	};

	private BigDecimal calculateMealRequestTotalPrice(List<MealRequestDetail> details) {
		BigDecimal totalPrice = BigDecimal.ZERO;

		if (details != null && !details.isEmpty()) {
			for (MealRequestDetail detail : details) {
				BigDecimal quantity = new BigDecimal(detail.getQuantity());
				totalPrice = totalPrice.add(quantity.multiply(detail.getPrice()));
			}
		}

		return totalPrice;
	}

	private BigDecimal getInvoiceAmount(int invoiceId) {
		String sql = "SELECT total_amount FROM tbl_invoice WHERE id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { invoiceId }, BigDecimal.class);
	}

	@Override
	public int addMealRequest(MealRequest mealRequest) {
		String sql = "INSERT INTO tbl_meal_request (contract_id, request_date, delivery_date, total_meals, status) "
				+ "VALUES (?, ?, ?, ?, ?); SELECT SCOPE_IDENTITY();";
		return jdbcTemplate.queryForObject(
				sql, new Object[] { mealRequest.getContractId(), mealRequest.getRequestDate(),
						mealRequest.getDeliveryDate(), mealRequest.getTotalMeals(), mealRequest.getStatus() },
				Integer.class);
	}

	@Override
	public List<MealRequest> getAllMealRequests() {
		String sql = "SELECT mr.*, c.usr_id " + "FROM tbl_meal_request mr "
				+ "JOIN tbl_contract c ON mr.contract_id = c.id";
		return jdbcTemplate.query(sql, new RowMapper<MealRequest>() {
			@Override
			public MealRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
				MealRequest mealRequest = new MealRequest();
				mealRequest.setId(rs.getInt("id"));
				mealRequest.setContractId(rs.getInt("contract_id"));
				mealRequest.setRequestDate(rs.getObject("request_date", LocalDate.class));
				mealRequest.setDeliveryDate(rs.getObject("delivery_date", LocalDate.class));
				mealRequest.setTotalMeals(rs.getInt("total_meals"));
				mealRequest.setStatus(rs.getInt("status"));
				mealRequest.setUserId(rs.getInt("usr_id"));
				return mealRequest;
			}
		});
	}

	@Override
	public MealRequest getMealRequestById(int id) {
		String sql = "SELECT * FROM tbl_meal_request WHERE id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, mealRequestRowMapper);
	}

	@Override
	public void updateMealRequest(MealRequest mealRequest) {
		String sql = "UPDATE tbl_meal_request SET contract_id = ?, request_date = ?, delivery_date = ?, total_meals = ?, status = ? WHERE id = ?";
		jdbcTemplate.update(sql, mealRequest.getContractId(), mealRequest.getRequestDate(),
				mealRequest.getDeliveryDate(), mealRequest.getTotalMeals(), mealRequest.getStatus(),
				mealRequest.getId());
	}

	@Override
	public void deleteMealRequest(int id) {
		String sql = "DELETE FROM tbl_meal_request WHERE id = ?";
		jdbcTemplate.update(sql, id);
	}

	@Override
	public Integer getUserIdByMealRequestId(int mealRequestId) {
		String sql = "SELECT c.usr_id FROM tbl_meal_request mr JOIN tbl_contract c ON mr.contract_id = c.id WHERE mr.id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { mealRequestId }, Integer.class);
	}

	@Override
	public void updateTotalMeals(int mealRequestId, int totalMeals) {
		String sql = "UPDATE tbl_meal_request SET total_meals = ? WHERE id = ?";
		jdbcTemplate.update(sql, totalMeals, mealRequestId);
	}

	@Override
	public MealRequest findById(int id) {
		String sql = "SELECT * FROM tbl_meal_request WHERE id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<>(MealRequest.class));
	}

	@Override
	public List<MealRequest> getMealRequestsByUserId(int userId) {
		String sql = "SELECT mr.* FROM tbl_meal_request mr " + "JOIN tbl_contract c ON mr.contract_id = c.id "
				+ "WHERE c.usr_id = ?";
		return jdbcTemplate.query(sql, new Object[] { userId }, mealRequestRowMapper);
	}

	@Override
	public List<MealRequest> getMealRequestsByUserId(int userId, int page, int size) {
		String sql = "SELECT mr.* FROM tbl_meal_request mr " + "JOIN tbl_contract c ON mr.contract_id = c.id "
				+ "WHERE c.usr_id = ? " + "ORDER BY mr.id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		return jdbcTemplate.query(sql, new Object[] { userId, (page - 1) * size, size }, mealRequestRowMapper);
	}

	@Override
	public List<MealRequest> getMealRequestsByContractId(int contractId, int page, int size) {
		String sql = "SELECT * FROM tbl_meal_request WHERE contract_id = ? "
				+ "ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		return jdbcTemplate.query(sql, new Object[] { contractId, (page - 1) * size, size }, mealRequestRowMapper);
	}

	@Override
	public int countMealRequestsByUserId(int userId) {
		String sql = "SELECT COUNT(*) FROM tbl_meal_request mr " + "JOIN tbl_contract c ON mr.contract_id = c.id "
				+ "WHERE c.usr_id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { userId }, Integer.class);
	}

	@Override
	public int countMealRequestsByContractId(int contractId) {
		String sql = "SELECT COUNT(*) FROM tbl_meal_request WHERE contract_id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { contractId }, Integer.class);
	}

	@Override
	public List<MealRequest> findByContractId(int contractId) {
		String sql = "SELECT * FROM tbl_meal_request WHERE contract_id = ?";
		return jdbcTemplate.query(sql, new Object[] { contractId }, mealRequestRowMapper);
	}

	@Override
	public List<MealRequest> getMealRequestsByContractId(int contractId) {
		String sql = "SELECT * FROM tbl_meal_request WHERE contract_id = ? ORDER BY id";
		return jdbcTemplate.query(sql, new Object[] { contractId }, mealRequestRowMapper);
	}

	@Override
	public List<MealRequest> findMealRequestsByInvoiceId(int invoiceId) {
		String sql = "SELECT contract_id FROM tbl_invoice WHERE id = ?";
		Integer contractId = jdbcTemplate.queryForObject(sql, new Object[] { invoiceId }, Integer.class);
		if (contractId != null) {
			return getMealRequestsByContractId(contractId);
		}

		return new ArrayList<>();
	}

	@Override
	public List<MealRequest> getMealRequestsByInvoiceId(int invoiceId) {
		String sql = "SELECT mr.id, mr.contract_id, mr.request_date, mr.delivery_date, mr.total_meals, mr.status, mr.invoice_id "
				+ "FROM meal_request mr " + "WHERE mr.invoice_id = ?";

		List<MealRequest> mealRequests = jdbcTemplate.query(sql, new Object[] { invoiceId }, mealRequestRowMapper);

		List<MealRequest> filteredMealRequests = new ArrayList<>();
		for (MealRequest mealRequest : mealRequests) {
			List<MealRequestDetail> mealRequestDetails = mealRequestDetailRepository
					.getDetailsByMealRequestId(mealRequest.getId());
			BigDecimal totalPrice = calculateMealRequestTotalPrice(mealRequestDetails);

			BigDecimal invoiceAmount = getInvoiceAmount(invoiceId);
			if (totalPrice.compareTo(invoiceAmount) == 0) {
				mealRequest.setMealRequestDetails(mealRequestDetails);
				filteredMealRequests.add(mealRequest);
			}
		}

		return filteredMealRequests;
	}
	@Override
	public List<MealRequest> searchMealRequests(String keyword, int offset, int limit) {
	    String sql = "SELECT mr.*, c.usr_id, u.name AS user_name, u.email AS user_email " +
	                 "FROM tbl_meal_request mr " +
	                 "JOIN tbl_contract c ON mr.contract_id = c.id " +
	                 "JOIN tbl_user u ON c.usr_id = u.id " +
	                 "WHERE u.name LIKE ? " +
	                 "OR u.email LIKE ? " +
	                 "OR mr.request_date LIKE ? " +
	                 "OR mr.delivery_date LIKE ? " +
	                 "ORDER BY mr.id " +
	                 "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

	    String searchPattern = "%" + keyword + "%";
	    return jdbcTemplate.query(sql,
	        new Object[]{searchPattern, searchPattern, searchPattern, searchPattern, offset, limit},
	        new RowMapper<MealRequest>() {
	            @Override
	            public MealRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
	                MealRequest mealRequest = new MealRequest();
	                mealRequest.setId(rs.getInt("id"));
	                mealRequest.setContractId(rs.getInt("contract_id"));
	                mealRequest.setRequestDate(rs.getObject("request_date", LocalDate.class));
	                mealRequest.setDeliveryDate(rs.getObject("delivery_date", LocalDate.class));
	                mealRequest.setTotalMeals(rs.getInt("total_meals"));
	                mealRequest.setStatus(rs.getInt("status"));
	                mealRequest.setUserId(rs.getInt("usr_id"));
	                mealRequest.setUserName(rs.getString("user_name")); // Gắn thêm tên user
	                mealRequest.setUserEmail(rs.getString("user_email")); // Gắn thêm email user
	                return mealRequest;
	            }
	        });
	}


	@Override
	public int countMealRequests(String keyword) {
	    String sql = "SELECT COUNT(*) " +
	                 "FROM tbl_meal_request mr " +
	                 "JOIN tbl_contract c ON mr.contract_id = c.id " +
	                 "JOIN tbl_user u ON c.usr_id = u.id " +
	                 "WHERE u.name LIKE ? " +
	                 "OR u.email LIKE ? " +
	                 "OR mr.request_date LIKE ? " +
	                 "OR mr.delivery_date LIKE ?";

	    String searchPattern = "%" + keyword + "%";
	    return jdbcTemplate.queryForObject(sql, 
	        new Object[]{searchPattern, searchPattern, searchPattern, searchPattern}, 
	        Integer.class);
	}


}
