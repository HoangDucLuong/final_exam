package shop.modelviews;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.math.BigDecimal;

import org.springframework.jdbc.core.RowMapper;

import shop.model.Contract;

public class ContractRowMapper implements RowMapper<Contract> {
    @Override
    public Contract mapRow(ResultSet rs, int rowNum) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getInt("id"));
        contract.setUsrId(rs.getInt("usr_id"));
        contract.setStartDate(rs.getObject("start_date", LocalDate.class));
        contract.setEndDate(rs.getObject("end_date", LocalDate.class));
        contract.setTotalAmount(rs.getBigDecimal("total_amount"));
        contract.setDepositAmount(rs.getBigDecimal("deposit_amount"));
        contract.setStatus(rs.getInt("status"));
        contract.setPaymentStatus(rs.getInt("payment_status")); // Thêm dòng này
        return contract;
    }
}
