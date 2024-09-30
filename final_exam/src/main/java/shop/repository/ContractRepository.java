package shop.repository;

import shop.model.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ContractRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper cho Contract
    private RowMapper<Contract> contractRowMapper = new RowMapper<Contract>() {
        @Override
        public Contract mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contract contract = new Contract();
            contract.setId(rs.getInt("id"));
            contract.setUsrId(rs.getInt("usr_id"));
            contract.setStartDate(rs.getDate("start_date").toLocalDate());
            contract.setEndDate(rs.getDate("end_date").toLocalDate());
            contract.setTotalAmount(rs.getBigDecimal("total_amount"));
            contract.setDepositAmount(rs.getBigDecimal("deposit_amount"));
            contract.setStatus(rs.getInt("status"));
            return contract;
        }
    };

    // Thêm hợp đồng
    public void addContract(Contract contract) {
        String sql = "INSERT INTO tbl_contract (usr_id, start_date, end_date, total_amount, deposit_amount, status) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, contract.getUsrId(), contract.getStartDate(), contract.getEndDate(),
                contract.getTotalAmount(), contract.getDepositAmount(), contract.getStatus());
    }

    // Cập nhật hợp đồng
    public void updateContract(Contract contract) {
        String sql = "UPDATE tbl_contract SET usr_id = ?, start_date = ?, end_date = ?, total_amount = ?, deposit_amount = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, contract.getUsrId(), contract.getStartDate(), contract.getEndDate(),
                contract.getTotalAmount(), contract.getDepositAmount(), contract.getStatus(), contract.getId());
    }

    // Xóa hợp đồng
    public void deleteContract(int id) {
        String sql = "DELETE FROM tbl_contract WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    // Lấy tất cả các hợp đồng
    public List<Contract> getAllContracts() {
        String sql = "SELECT * FROM tbl_contract";
        return jdbcTemplate.query(sql, contractRowMapper);
    }

    // Lấy hợp đồng theo ID
    public Contract getContractById(int id) {
        String sql = "SELECT * FROM tbl_contract WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, contractRowMapper);
    }
}
