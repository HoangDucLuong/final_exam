package shop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Contract {
    private int id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private int status;
    private int paymentStatus; // Thêm trường payment_status
    private int usrId;

    public Contract() {}

    public Contract(int id, LocalDate startDate, LocalDate endDate, BigDecimal totalAmount, 
                    BigDecimal depositAmount, int status, int paymentStatus, int usrId) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalAmount = totalAmount;
        this.depositAmount = depositAmount;
        this.status = status;
        this.paymentStatus = paymentStatus; // Khởi tạo payment_status
        this.usrId = usrId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPaymentStatus() { // Getter cho payment_status
        return paymentStatus;
    }

    public void setPaymentStatus(int paymentStatus) { // Setter cho payment_status
        this.paymentStatus = paymentStatus;
    }

    public int getUsrId() {
        return usrId;
    }

    public void setUsrId(int usrId) {
        this.usrId = usrId;
    }
}
