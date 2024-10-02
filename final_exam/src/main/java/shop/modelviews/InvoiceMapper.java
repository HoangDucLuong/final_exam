package shop.modelviews;

import shop.model.Invoice;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class InvoiceMapper implements RowMapper<Invoice> {
    @Override
    public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("id"));
        invoice.setContractId(rs.getInt("contract_id"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount")); // Sử dụng BigDecimal
        invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));   // Sử dụng BigDecimal
        invoice.setDueDate(rs.getObject("due_date", LocalDateTime.class)); // Thay đổi nếu sử dụng LocalDate
        invoice.setPaymentStatus(rs.getInt("payment_status"));
        invoice.setCreatedAt(rs.getObject("created_at", LocalDateTime.class)); // Thay đổi nếu sử dụng LocalDate
        return invoice;
    }
}
