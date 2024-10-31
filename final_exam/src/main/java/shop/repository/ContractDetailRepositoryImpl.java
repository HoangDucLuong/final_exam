package shop.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import shop.modelviews.ContractDetailMapper;
import shop.model.ContractDetail;

@Repository
public class ContractDetailRepositoryImpl implements ContractDetailRepository {
    
    private final JdbcTemplate jdbcTemplate;

    public ContractDetailRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveContractDetail(ContractDetail contractDetail) {
        String sql = "INSERT INTO contract_detail (contract_id, menu_id, description) VALUES (?, ?, ?)"; // Đổi meal_id thành menu_id
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, contractDetail.getContractId());
            ps.setInt(2, contractDetail.getMenuId()); // Đổi từ mealId thành menuId
            ps.setString(3, contractDetail.getDescription());
            return ps;
        }, keyHolder);

        contractDetail.setId(keyHolder.getKey().intValue());
    }

    @Override
    public List<ContractDetail> getContractDetailsByContractId(int contractId) {
        String sql = "SELECT * FROM contract_detail WHERE contract_id = ?";
        return jdbcTemplate.query(sql, new ContractDetailMapper(), contractId);
    }

    @Override
    public void deleteContractDetailsByContractId(int contractId) {
        String sql = "DELETE FROM contract_detail WHERE contract_id = ?";
        jdbcTemplate.update(sql, contractId);
    }
}
