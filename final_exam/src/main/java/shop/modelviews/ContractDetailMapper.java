package shop.modelviews;

import shop.model.ContractDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContractDetailMapper implements RowMapper<ContractDetail> {
    @Override
    public ContractDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContractDetail contractDetail = new ContractDetail();
        contractDetail.setId(rs.getInt("id"));
        contractDetail.setContractId(rs.getInt("contract_id"));
        contractDetail.setMenuId(rs.getInt("menu_id"));
        contractDetail.setDescription(rs.getString("description"));
        return contractDetail;
    }
}
	