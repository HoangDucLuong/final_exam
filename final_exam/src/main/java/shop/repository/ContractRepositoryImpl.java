package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import shop.model.Contract;
import shop.model.Menu; // Thay đổi từ Meal thành Menu
import shop.modelviews.ContractRowMapper;
import shop.modelviews.MenuMapper; // Thay đổi từ MealMapper thành MenuMapper
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.List;

@Repository
public class ContractRepositoryImpl implements ContractRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<Contract> findAll() {
		String sql = "SELECT * FROM tbl_contract";
		return jdbcTemplate.query(sql, new ContractRowMapper());
	}

	@Override
	public List<Contract> getAllContracts() {
		String sql = "SELECT * FROM tbl_contract";
		return jdbcTemplate.query(sql, new ContractRowMapper());
	}

	@Override
	public List<Contract> getContractsByUserId(int usrId) {
		String sql = "SELECT * FROM tbl_contract WHERE usr_id = ?";
		return jdbcTemplate.query(sql, new Object[] { usrId }, new ContractRowMapper());
	}

	@Override
	public List<Menu> findMenusByContractId(int contractId) {
		String sql = "SELECT DISTINCT m.id, m.menu_name, m.menu_type, m.created_at " + "FROM tbl_menu m "
				+ "JOIN tbl_menu_details md ON m.id = md.menu_id " + "JOIN contract_detail cd ON m.id = cd.menu_id "
				+ "WHERE cd.contract_id = ?";

		return jdbcTemplate.query(sql, new Object[] { contractId }, new MenuMapper());
	}

	@Override
	public Contract getContractById(int id) {
		String sql = "SELECT * FROM tbl_contract WHERE id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, new ContractRowMapper());
	}

	@Override
	public void addContract(Contract contract) {
		String sql = "INSERT INTO tbl_contract (usr_id, start_date, end_date, total_amount, deposit_amount, status, payment_status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, contract.getUsrId());
			ps.setDate(2, Date.valueOf(contract.getStartDate()));
			ps.setDate(3, Date.valueOf(contract.getEndDate()));
			ps.setBigDecimal(4, contract.getTotalAmount());
			ps.setBigDecimal(5, contract.getDepositAmount());
			ps.setInt(6, contract.getStatus());
			ps.setInt(7, contract.getPaymentStatus());
			return ps;
		}, keyHolder);

		// Gán ID vừa được tạo vào đối tượng Contract
		if (keyHolder.getKey() != null) {
			contract.setId(keyHolder.getKey().intValue());
		}
	}

	@Override
	public void updateContract(Contract contract) {
		String sql = "UPDATE tbl_contract SET usr_id = ?, start_date = ?, end_date = ?, total_amount = ?, deposit_amount = ?, status = ?, payment_status = ? WHERE id = ?";
		jdbcTemplate.update(sql, contract.getUsrId(), contract.getStartDate(), contract.getEndDate(),
				contract.getTotalAmount(), contract.getDepositAmount(), contract.getStatus(),
				contract.getPaymentStatus(), contract.getId());
	}

	@Override
	public void deleteContract(int id) {
		String sql = "DELETE FROM tbl_contract WHERE id = ?";
		jdbcTemplate.update(sql, id);
	}

	@Override
	public void updateContractStatus(int contractId, int status) {
		String sql = "UPDATE tbl_contract SET status = ? WHERE id = ?";
		jdbcTemplate.update(sql, status, contractId);
	}

	@Override
	public void save(Contract contract) {
		if (contract.getId() == 0) {
			addContract(contract);
		} else {
			updateContract(contract);
		}
	}

	@Override
	public List<Contract> getContractsExpiringSoon() {
		String sql = "SELECT * FROM tbl_contract WHERE DATEDIFF(day, GETDATE(), end_date) = 10";
		return jdbcTemplate.query(sql, new ContractRowMapper());
	}

	@Override
	public List<Contract> findActiveContracts() {
		String sql = "SELECT * FROM tbl_contract WHERE status = 1";
		return jdbcTemplate.query(sql, new ContractRowMapper());
	}

	@Override
	public Contract findById(int id) {
		String sql = "SELECT * FROM tbl_contract WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { id }, new ContractRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Contract> searchContracts(String keyword, int offset, int limit) {
		String sql = "SELECT * FROM tbl_contract WHERE usr_id IN " + "(SELECT id FROM tbl_user WHERE name LIKE ?) "
				+ "OR start_date LIKE ? OR end_date LIKE ? " + "ORDER BY id " + // Đảm bảo có cột để sắp xếp
				"OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		String searchPattern = "%" + keyword + "%";
		return jdbcTemplate.query(sql, new Object[] { searchPattern, searchPattern, searchPattern, offset, limit },
				new ContractRowMapper());
	}

	@Override
	public int countContracts(String keyword) {
		String sql = "SELECT COUNT(*) FROM tbl_contract WHERE usr_id IN "
				+ "(SELECT id FROM tbl_user WHERE name LIKE ?) " + "OR start_date LIKE ? OR end_date LIKE ?";
		String searchPattern = "%" + keyword + "%";
		return jdbcTemplate.queryForObject(sql, new Object[] { searchPattern, searchPattern, searchPattern },
				Integer.class);
	}

}
