package shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    private int id;
    private int contractId;
    private BigDecimal totalAmount; // Sử dụng BigDecimal
    private BigDecimal paidAmount;   // Sử dụng BigDecimal
    private LocalDateTime dueDate;   // Có thể sử dụng LocalDate nếu chỉ cần ngày
    private int paymentStatus;
    private LocalDateTime createdAt;  // Sử dụng LocalDateTime
    private boolean sent; // Thuộc tính mới
    private String recipientEmail;
    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getRecipientEmail() {
		return recipientEmail;
	}

	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}

	public void setId(int id) {
        this.id = id;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}

//    public boolean isSent() { // Getter cho thuộc tính sent
//        return sent;
//    }
//
//    public void setSent(boolean sent) { // Setter cho thuộc tính sent
//        this.sent = sent;
//    }
    
}
