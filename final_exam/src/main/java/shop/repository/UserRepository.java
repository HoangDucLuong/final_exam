package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import shop.model.User;

@Repository
public class UserRepository {
	@Autowired
	JdbcTemplate db;

	public void addUser(User user) {
		String sql = "INSERT INTO tbl_user ( name, email, pwd, usr_type, phone, address, created_at, status) "
				+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)";

		db.update(sql, user.getName(), user.getEmail(), user.getPwd(),
				 user.getUsrType(), user.getPhone(), user.getAddress(), user.getCreatedAt(), user.getStatus());
	}
}
