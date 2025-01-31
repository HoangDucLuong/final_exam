package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import shop.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserRepository {
	@Autowired
	JdbcTemplate db;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<User> findAllUsers(int page, String search) {
		int pageSize = 5;
		int offset = (page - 1) * pageSize;

		String sql = "SELECT * FROM tbl_user WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

		return db.query(sql, new UserRowMapper(),
				new Object[] { "%" + search + "%", "%" + search + "%", "%" + search + "%", offset, pageSize });
	}

	public void addUser(User user) {
		String sql = "INSERT INTO tbl_user (name, email, pwd, usr_type, phone, address, created_at, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		db.update(sql, user.getName(), user.getEmail(), user.getPwd(), user.getUsrType(), user.getPhone(),
				user.getAddress(), user.getCreatedAt(), user.getStatus());
	}

	public User findById(int id) {
		String sql = "SELECT * FROM tbl_user WHERE id = ?";
		try {
			return db.queryForObject(sql, new UserRowMapper(), new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public String findNameByUid(String email) {
		String sql = "SELECT name FROM tbl_user WHERE email = ?";
		return db.queryForObject(sql, String.class, new Object[] { email });
	}

	public User findUserByEmail(String email) {
		String sql = "SELECT * FROM tbl_user WHERE email = ?";
		return db.queryForObject(sql, new Object[] { email }, new UserRowMapper());
	}

	public void updateUser(User user) {
		String sql = "UPDATE tbl_user SET name = ?, email = ?, phone = ?, address = ?, pwd = ?, status = ? WHERE id = ?";

		db.update(sql, user.getName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getPwd(),
				user.getStatus(), user.getId());
	}

	public List<User> findAll() {
		String sql = "SELECT * FROM tbl_user";
		return db.query(sql, new UserRowMapper());
	}

	private static class UserRowMapper implements RowMapper<User> {
		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getInt("id"));
			user.setName(rs.getString("name"));
			user.setEmail(rs.getString("email"));
			user.setPwd(rs.getString("pwd"));
			user.setUsrType(rs.getInt("usr_type"));
			user.setPhone(rs.getString("phone"));
			user.setAddress(rs.getString("address"));
			user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
			user.setStatus(rs.getInt("status"));
			return user;
		}
	}

	public List<User> findUsersWithExpiringContracts() {
		String sql = "SELECT u.* FROM tbl_user u " + "JOIN tbl_contract c ON u.id = c.usr_id "
				+ "WHERE DATEDIFF(day, GETDATE(), c.end_date) = 10";

		return db.query(sql, new UserRowMapper());
	}

	public Integer findUserIdByEmail(String email) {
		String sql = "SELECT id FROM tbl_user WHERE email = ?";
		try {
			return db.queryForObject(sql, new Object[] { email }, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public String findEmailByContractId(int contractId) {
		String sql = "SELECT TOP 1 u.email " + "FROM tbl_invoice i " + "JOIN tbl_contract c ON i.contract_id = c.id "
				+ "JOIN tbl_user u ON c.usr_id = u.id " + "WHERE i.contract_id = ? " + "ORDER BY i.id";

		try {
			return db.queryForObject(sql, new Object[] { contractId }, String.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (IncorrectResultSizeDataAccessException e) {
			System.err.println("Tìm thấy nhiều email cho mã hợp đồng: " + contractId);

			List<String> emails = db.queryForList(
					"SELECT TOP 1 u.email " + "FROM tbl_invoice i " + "JOIN tbl_contract c ON i.contract_id = c.id "
							+ "JOIN tbl_user u ON c.usr_id = u.id " + "WHERE i.contract_id = ? " + "ORDER BY i.id",
					String.class, new Object[] { contractId });

			return emails.isEmpty() ? null : emails.get(0);
		}
	}

	public int countUsers(String search) {
		String sql = "SELECT COUNT(*) FROM tbl_user WHERE name LIKE ? OR email LIKE ?";
		return db.queryForObject(sql, new Object[] { "%" + search + "%", "%" + search + "%" }, Integer.class);
	}
	public String findUserNameById(int userId) {
	    String sql = "SELECT name FROM tbl_user WHERE id = ?";
	    try {
	        return db.queryForObject(sql, new Object[] { userId }, String.class);
	    } catch (EmptyResultDataAccessException e) {
	        return null;
	    }
	}


}