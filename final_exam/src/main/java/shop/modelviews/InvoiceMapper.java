package shop.modelviews;

import shop.model.Invoice;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class InvoiceMapper implements RowMapper<Invoice> {
	@Override
	public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {
	    Invoice invoice = new Invoice();
	    invoice.setId(rs.getInt("id"));
	    invoice.setContractId(rs.getInt("contract_id"));
	    invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
	    invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));

	    // Kiểm tra null cho dueDate
	    Timestamp dueDateTimestamp = rs.getTimestamp("due_date");
	    invoice.setDueDate(dueDateTimestamp != null ? dueDateTimestamp.toLocalDateTime() : null);

	    invoice.setPaymentStatus(rs.getInt("payment_status"));

	    // Kiểm tra null cho createdAt
	    Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
	    invoice.setCreatedAt(createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null);

	    return invoice;
	}

}
