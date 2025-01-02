package shop.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import shop.model.Invoice;

public interface InvoiceRepository {
    List<Invoice> findAll();
    Invoice findById(int id);
    Invoice save(Invoice invoice);
    void deleteById(int id);
    List<Invoice> findByContractId(int contractId);
    List<Invoice> findByDueDate(Timestamp dueDate);
    List<Invoice> findByUserId(int userId);
    List<Invoice> findByContractIdAndDueDate(int contractId, Timestamp dueDate);
    List<Invoice> findByContractIdAndDueDateBetween(int contractId, Timestamp startDate, Timestamp endDate);
    List<Invoice> findByYear(int year);
    List<Invoice> findByContractIdAndSent(int contractId, int isSent);
    List<Invoice> findByPaymentStatus(int paymentStatus);
    Invoice findFirstByContractIdAndTotalAmount(int contractId, double totalAmount);
    List<Invoice> findByContractIdAndPaymentStatus(int contractId, int paymentStatus);

}
