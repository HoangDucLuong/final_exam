package shop.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import shop.model.Payment;

import java.util.List;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Lưu thông tin thanh toán
    public void save(Payment payment) {
        String query = "INSERT INTO tbl_payment (contract_id, payment_amount, payment_date, payment_status, payment_method, transaction_ref) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, payment.getContractId(),
                                  payment.getPaymentAmount(),
                                  payment.getPaymentDate(),
                                  payment.getPaymentStatus(),
                                  payment.getPaymentMethod(),
                                  payment.getTransactionRef());
    }

    // Lấy danh sách thanh toán theo contractId
    public List<Payment> findByContractId(int contractId) {
        String query = "SELECT * FROM tbl_payment WHERE contract_id = ?";
        return jdbcTemplate.query(query, new Object[]{contractId}, (rs, rowNum) -> {
            Payment payment = new Payment();
            payment.setId(rs.getInt("id"));
            payment.setContractId(rs.getInt("contract_id"));
            payment.setPaymentAmount(rs.getBigDecimal("payment_amount"));
            payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
            payment.setPaymentStatus(rs.getInt("payment_status"));
            payment.setPaymentMethod(rs.getString("payment_method"));
            payment.setTransactionRef(rs.getString("transaction_ref"));
            return payment;
        });
    }

    // Lấy thanh toán theo ID
    public Payment findById(int paymentId) {
        String query = "SELECT * FROM tbl_payment WHERE id = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{paymentId}, (rs, rowNum) -> {
            Payment payment = new Payment();
            payment.setId(rs.getInt("id"));
            payment.setContractId(rs.getInt("contract_id"));
            payment.setPaymentAmount(rs.getBigDecimal("payment_amount"));
            payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
            payment.setPaymentStatus(rs.getInt("payment_status"));
            payment.setPaymentMethod(rs.getString("payment_method"));
            payment.setTransactionRef(rs.getString("transaction_ref"));
            return payment;
        });
    }

    // Cập nhật trạng thái thanh toán
    public void updatePaymentStatus(int paymentId, int status) {
        String query = "UPDATE tbl_payment SET payment_status = ? WHERE id = ?";
        jdbcTemplate.update(query, status, paymentId);
    }

    // Xóa thanh toán theo ID
    public void deleteById(int paymentId) {
        String query = "DELETE FROM tbl_payment WHERE id = ?";
        jdbcTemplate.update(query, paymentId);
    }
 // Thêm phương thức findByTransactionRef
    public List<Payment> findByTransactionRef(String transactionRef) {
        String query = "SELECT * FROM tbl_payment WHERE transaction_ref = ?";
        return jdbcTemplate.query(query, new Object[]{transactionRef}, (rs, rowNum) -> {
            Payment payment = new Payment();
            payment.setId(rs.getInt("id"));
            payment.setContractId(rs.getInt("contract_id"));
            payment.setPaymentAmount(rs.getBigDecimal("payment_amount"));
            payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
            payment.setPaymentStatus(rs.getInt("payment_status"));
            payment.setPaymentMethod(rs.getString("payment_method"));
            payment.setTransactionRef(rs.getString("transaction_ref"));
            return payment;
        });
    }

}
