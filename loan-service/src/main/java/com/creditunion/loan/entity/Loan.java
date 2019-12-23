package com.creditunion.loan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a loan in the system.
 * Supports multiple loan types (Personal, Auto, Mortgage) with
 * comprehensive tracking of loan lifecycle from application to closure.
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@Entity
@Table(name = "loans", indexes = {
        @Index(name = "idx_loan_customer_id", columnList = "customer_id"),
        @Index(name = "idx_loan_status", columnList = "status"),
        @Index(name = "idx_loan_type", columnList = "loan_type"),
        @Index(name = "idx_loan_application_date", columnList = "application_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long loanId;

    /**
     * Foreign key reference to customer in customer-service.
     * Cross-service reference handled via REST API calls.
     */
    @NotNull(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Unique loan account number generated after approval.
     * Format: LOAN-YYYY-NNNNNN (e.g., LOAN-2019-000123)
     */
    @Column(name = "loan_number", unique = true, length = 20)
    private String loanNumber;

    /**
     * Type of loan product.
     */
    @NotNull(message = "Loan type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 20)
    private LoanType loanType;

    /**
     * Original loan amount approved and disbursed.
     */
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Loan amount must be at least $1,000")
    @DecimalMax(value = "1000000.00", message = "Loan amount cannot exceed $1,000,000")
    @Column(name = "loan_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal loanAmount;

    /**
     * Annual interest rate as percentage (e.g., 5.99 for 5.99% APR).
     */
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
    @DecimalMax(value = "36.00", message = "Interest rate cannot exceed 36%")
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    /**
     * Loan term in months.
     */
    @NotNull(message = "Term months is required")
    @Min(value = 6, message = "Loan term must be at least 6 months")
    @Max(value = 360, message = "Loan term cannot exceed 360 months")
    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    /**
     * Calculated monthly payment amount based on amortization schedule.
     */
    @NotNull
    @Column(name = "monthly_payment", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPayment;

    /**
     * Current outstanding principal balance.
     * Updated with each payment received.
     */
    @NotNull
    @Column(name = "outstanding_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal outstandingBalance;

    /**
     * Total interest paid to date.
     * Cumulative sum from all payments.
     */
    @Builder.Default
    @Column(name = "total_interest_paid", precision = 12, scale = 2)
    private BigDecimal totalInterestPaid = BigDecimal.ZERO;

    /**
     * Total principal paid to date.
     */
    @Builder.Default
    @Column(name = "total_principal_paid", precision = 12, scale = 2)
    private BigDecimal totalPrincipalPaid = BigDecimal.ZERO;

    /**
     * Current status of the loan.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    /**
     * Date when loan application was submitted.
     */
    @NotNull
    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    /**
     * Date when loan was approved.
     */
    @Column(name = "approval_date")
    private LocalDate approvalDate;

    /**
     * Date when loan funds were disbursed.
     */
    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    /**
     * Expected maturity date (last payment due date).
     */
    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    /**
     * Date when loan was fully paid off or closed.
     */
    @Column(name = "closed_date")
    private LocalDate closedDate;

    /**
     * First payment due date.
     */
    @Column(name = "first_payment_date")
    private LocalDate firstPaymentDate;

    /**
     * Next payment due date.
     */
    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    /**
     * Number of days past due (0 if current).
     */
    @Builder.Default
    @Column(name = "days_past_due")
    private Integer daysPastDue = 0;

    /**
     * Number of missed payments.
     */
    @Builder.Default
    @Column(name = "missed_payments")
    private Integer missedPayments = 0;

    /**
     * Loan purpose/reason for borrowing.
     */
    @Column(name = "loan_purpose", length = 100)
    private String loanPurpose;

    /**
     * User ID who approved the loan (reference to user-service).
     */
    @Column(name = "approved_by")
    private Long approvedBy;

    /**
     * Approval notes or conditions.
     */
    @Column(name = "approval_notes", length = 500)
    private String approvalNotes;

    /**
     * Collateral description for secured loans (auto, mortgage).
     */
    @Column(name = "collateral_description", length = 500)
    private String collateralDescription;

    /**
     * Collateral value for secured loans.
     */
    @Column(name = "collateral_value", precision = 12, scale = 2)
    private BigDecimal collateralValue;

    /**
     * Loan-to-value ratio for secured loans (percentage).
     */
    @Column(name = "loan_to_value_ratio", precision = 5, scale = 2)
    private BigDecimal loanToValueRatio;

    /**
     * Reference to external loan application ID.
     */
    @Column(name = "application_id")
    private Long applicationId;

    /**
     * Indicates if loan is in default status.
     */
    @Builder.Default
    @Column(name = "is_defaulted")
    private Boolean isDefaulted = false;

    /**
     * Indicates if loan has been written off.
     */
    @Builder.Default
    @Column(name = "is_written_off")
    private Boolean isWrittenOff = false;

    /**
     * Soft delete flag.
     */
    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    /**
     * Record creation timestamp.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record last update timestamp.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * One-to-many relationship with loan documents.
     */
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoanDocument> documents = new ArrayList<>();

    /**
     * Calculates remaining payments based on current balance and monthly payment.
     *
     * @return number of payments remaining
     */
    public int calculateRemainingPayments() {
        if (monthlyPayment == null || monthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return outstandingBalance.divide(monthlyPayment, 0, BigDecimal.ROUND_UP).intValue();
    }

    /**
     * Checks if loan is currently delinquent.
     *
     * @return true if loan has missed payments or is past due
     */
    public boolean isDelinquent() {
        return daysPastDue != null && daysPastDue > 0;
    }

    /**
     * Calculates total amount paid to date (principal + interest).
     *
     * @return total paid amount
     */
    public BigDecimal calculateTotalPaid() {
        BigDecimal principal = totalPrincipalPaid != null ? totalPrincipalPaid : BigDecimal.ZERO;
        BigDecimal interest = totalInterestPaid != null ? totalInterestPaid : BigDecimal.ZERO;
        return principal.add(interest);
    }

    /**
     * Calculates percentage of loan paid off.
     *
     * @return percentage paid (0-100)
     */
    public BigDecimal calculatePercentagePaid() {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal principal = totalPrincipalPaid != null ? totalPrincipalPaid : BigDecimal.ZERO;
        return principal.divide(loanAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Updates outstanding balance after payment.
     *
     * @param principalAmount principal portion of payment
     * @param interestAmount  interest portion of payment
     */
    public void applyPayment(BigDecimal principalAmount, BigDecimal interestAmount) {
        this.outstandingBalance = this.outstandingBalance.subtract(principalAmount);
        this.totalPrincipalPaid = this.totalPrincipalPaid.add(principalAmount);
        this.totalInterestPaid = this.totalInterestPaid.add(interestAmount);

        // Reset delinquency counters on successful payment
        if (this.daysPastDue > 0) {
            this.daysPastDue = 0;
        }

        // Check if loan is fully paid
        if (this.outstandingBalance.compareTo(BigDecimal.ONE) < 0) {
            this.status = LoanStatus.CLOSED;
            this.closedDate = LocalDate.now();
        }
    }

    /**
     * Marks loan as delinquent with specified days past due.
     *
     * @param daysOverdue number of days past due
     */
    public void markDelinquent(int daysOverdue) {
        this.daysPastDue = daysOverdue;
        this.missedPayments++;

        if (daysOverdue >= 90) {
            this.status = LoanStatus.DEFAULT;
            this.isDefaulted = true;
        } else if (daysOverdue >= 30) {
            this.status = LoanStatus.DELINQUENT;
        }
    }
}

/**
 * Enum representing types of loans offered.
 */
enum LoanType {
    /**
     * Unsecured personal loan for general purposes.
     */
    PERSONAL,

    /**
     * Secured auto loan for vehicle purchase.
     */
    AUTO,

    /**
     * Secured mortgage loan for home purchase.
     */
    MORTGAGE,

    /**
     * Home equity line of credit.
     */
    HOME_EQUITY,

    /**
     * Business loan for commercial purposes.
     */
    BUSINESS
}

/**
 * Enum representing loan lifecycle status.
 */
enum LoanStatus {
    /**
     * Application submitted, awaiting review.
     */
    PENDING,

    /**
     * Under review by underwriting team.
     */
    UNDER_REVIEW,

    /**
     * Loan approved, awaiting disbursement.
     */
    APPROVED,

    /**
     * Loan application rejected.
     */
    REJECTED,

    /**
     * Funds disbursed, loan active.
     */
    ACTIVE,

    /**
     * Loan payment overdue (1-89 days).
     */
    DELINQUENT,

    /**
     * Loan in default (90+ days overdue).
     */
    DEFAULT,

    /**
     * Loan fully paid and closed.
     */
    CLOSED,

    /**
     * Loan written off as bad debt.
     */
    WRITTEN_OFF,

    /**
     * Application cancelled by customer.
     */
    CANCELLED
}
