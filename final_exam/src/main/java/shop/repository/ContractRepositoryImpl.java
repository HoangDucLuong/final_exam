package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import shop.model.Contract;
import shop.model.Meal;
import shop.modelviews.ContractRowMapper;
import shop.modelviews.MealMapper;

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
        return jdbcTemplate.query(sql, new Object[]{usrId}, new ContractRowMapper());
    }
    @Override
    public List<Meal> findMealsByContractId(int contractId) {
    	// Thêm meal_group_id vào câu truy vấn
    	String sql = "SELECT m.id, m.meal_group_id, m.meal_name, m.price, m.description, mg.group_name " +
    	             "FROM tbl_meal m " +
    	             "JOIN contract_detail cd ON m.id = cd.meal_id " +
    	             "JOIN tbl_meal_group mg ON m.meal_group_id = mg.id " +
    	             "WHERE cd.contract_id = ?";

        return jdbcTemplate.query(sql, new Object[]{contractId}, new MealMapper());
    }


    @Override
    public Contract getContractById(int id) {
        String sql = "SELECT * FROM tbl_contract WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new ContractRowMapper());
    }

    @Override
    public void addContract(Contract contract) {
        String sql = "INSERT INTO tbl_contract (usr_id, start_date, end_date, total_amount, deposit_amount, status, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, contract.getUsrId(), contract.getStartDate(), contract.getEndDate(),
                contract.getTotalAmount(), contract.getDepositAmount(), contract.getStatus(), contract.getPaymentStatus());
    }

    @Override
    public void updateContract(Contract contract) {
        String sql = "UPDATE tbl_contract SET usr_id = ?, start_date = ?, end_date = ?, total_amount = ?, deposit_amount = ?, status = ?, payment_status = ? WHERE id = ?";
        jdbcTemplate.update(sql, contract.getUsrId(), contract.getStartDate(), contract.getEndDate(),
                contract.getTotalAmount(), contract.getDepositAmount(), contract.getStatus(), contract.getPaymentStatus(), contract.getId());
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
        if (contract.getId() == 0) { // Giả sử 0 là giá trị chưa có id, cần thêm mới
            addContract(contract);
        } else {
            updateContract(contract);
        }
    }
}
