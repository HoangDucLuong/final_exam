package shop.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import shop.model.Invoice;

@Repository
public class InvoiceRepositoryImpl implements InvoiceRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Invoice> invoiceRowMapper = new RowMapper<Invoice>() {
        @Override
        public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {
            Invoice invoice = new Invoice();
            invoice.setId(rs.getInt("id"));
            invoice.setContractId(rs.getInt("contract_id"));
            invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
            invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));

            // Safely handle null values for Timestamp
            Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
            Timestamp dueDateTimestamp = rs.getTimestamp("due_date");

            invoice.setCreatedAt(createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null);
            invoice.setDueDate(dueDateTimestamp != null ? dueDateTimestamp.toLocalDateTime() : null);

            invoice.setPaymentStatus(rs.getInt("payment_status"));
            invoice.setSent(rs.getBoolean("sent"));
            return invoice;
        }
    };

    @Override
    public List<Invoice> findAll() {
        String sql = "SELECT * FROM tbl_invoice";
        return jdbcTemplate.query(sql, invoiceRowMapper);
    }

    @Override
    public Invoice findById(int id) {
        String sql = "SELECT * FROM tbl_invoice WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, invoiceRowMapper, id);
    }

    @Override
    public Invoice save(Invoice invoice) {
        if (invoice.getId() == 0) {
            // Insert mới
            String sql = "INSERT INTO tbl_invoice (contract_id, total_amount, paid_amount, due_date, payment_status, sent) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, invoice.getContractId(), invoice.getTotalAmount(), invoice.getPaidAmount(),
                    invoice.getDueDate(), invoice.getPaymentStatus(), invoice.isSent());
        } else {
            // Cập nhật nếu đã tồn tại
            String sql = "UPDATE tbl_invoice SET contract_id = ?, total_amount = ?, paid_amount = ?, due_date = ?, payment_status = ?, sent = ?, created_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, invoice.getContractId(), invoice.getTotalAmount(), invoice.getPaidAmount(),
                    invoice.getDueDate(), invoice.getPaymentStatus(), invoice.isSent(), invoice.getCreatedAt(), invoice.getId());
        }
        return invoice;
    }


    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM tbl_invoice WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Invoice> findByContractId(int contractId) {
        String sql = "SELECT * FROM tbl_invoice WHERE contract_id = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, contractId);
    }


    /**
     * Tìm tất cả các hóa đơn có ngày đến hạn nhất định
     */
    @Override
    public List<Invoice> findByDueDate(Timestamp dueDate) {
        String sql = "SELECT * FROM tbl_invoice WHERE due_date = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, dueDate);
    }
    
    @Override
    public List<Invoice> findByUserId(int userId) {
        String sql = """
            SELECT i.*
            FROM tbl_invoice i
            JOIN tbl_contract c ON i.contract_id = c.id
            WHERE c.usr_id = ?
        """;
        return jdbcTemplate.query(sql, invoiceRowMapper, userId);
    }
    @Override
    public List<Invoice> findByContractIdAndDueDate(int contractId, Timestamp dueDate) {
        String sql = "SELECT * FROM tbl_invoice WHERE contract_id = ? AND due_date = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, contractId, dueDate);
    }
    @Override
    public List<Invoice> findByContractIdAndDueDateBetween(int contractId, Timestamp startDate, Timestamp endDate) {
        String sql = "SELECT * FROM tbl_invoice WHERE contract_id = ? AND due_date BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, contractId, startDate, endDate);
    }
    @Override
    public List<Invoice> findByYear(int year) {
        String sql = "SELECT * FROM tbl_invoice WHERE YEAR(created_at) = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, year);
    }
    @Override
    public List<Invoice> findByContractIdAndSent(int contractId, int isSent) {
        String sql = "SELECT * FROM tbl_invoice WHERE contract_id = ? AND sent = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, contractId, isSent);
    }
    @Override
    public List<Invoice> findByPaymentStatus(int paymentStatus) {
        String sql = "SELECT * FROM tbl_invoice WHERE payment_status = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, paymentStatus);
    }
    @Override
    public Invoice findFirstByContractIdAndTotalAmount(int contractId, double totalAmount) {
        String sql = "SELECT * FROM tbl_invoice WHERE contract_id = ? AND total_amount = ? LIMIT 1";
        List<Invoice> invoices = jdbcTemplate.query(sql, invoiceRowMapper, contractId, totalAmount);
        return invoices.isEmpty() ? null : invoices.get(0);
    }
    @Override
    public List<Invoice> findByContractIdAndPaymentStatus(int contractId, int paymentStatus) {
        String sql = "SELECT * FROM tbl_invoice WHERE contract_id = ? AND payment_status = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, contractId, paymentStatus);
    }

}
