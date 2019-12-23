package com.creditunion.loan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing a loan application submitted by a customer.
 * Captures all application details needed for underwriting decision.
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@Entity
@Table(name = "loan_applications", indexes = {
        @Index(name = "idx_app_customer_id", columnList = "customer_id"),
        @Index(name = "idx_app_status", columnList = "status"),
        @Index(name = "idx_app_date", columnList = "application_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    /**
     * Customer submitting the application.
     */
    @NotNull(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Type of loan requested.
     */
    @NotNull(message = "Loan type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 20)
    private LoanType loanType;

    /**
     * Requested loan amount.
     */
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is $1,000")
    @Column(name = "requested_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedAmount;

    /**
     * Preferred loan term in months.
     */
    @NotNull(message = "Preferred term is required")
    @Min(value = 6, message = "Minimum term is 6 months")
    @Max(value = 360, message = "Maximum term is 360 months")
    @Column(name = "preferred_term_months", nullable = false)
    private Integer preferredTermMonths;

    /**
     * Loan purpose description.
     */
    @NotBlank(message = "Loan purpose is required")
    @Column(name = "loan_purpose", nullable = false, length = 200)
    private String loanPurpose;

    /**
     * Employment information stored as JSON.
     * Contains: employerName, jobTitle, yearsEmployed, monthlyIncome,
     * employmentType
     */
    @Type(type = "jsonb")
    @Column(name = "employment_info", columnDefinition = "jsonb")
    private Map<String, Object> employmentInfo;

    /**
     * Annual gross income reported by applicant.
     */
    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "12000.00", message = "Minimum annual income is $12,000")
    @Column(name = "annual_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal annualIncome;

    /**
     * Monthly housing payment (rent or mortgage).
     */
    @Column(name = "monthly_housing_payment", precision = 10, scale = 2)
    private BigDecimal monthlyHousingPayment;

    /**
     * Other monthly debt obligations.
     */
    @Column(name = "other_monthly_debts", precision = 10, scale = 2)
    private BigDecimal otherMonthlyDebts;

    /**
     * Calculated debt-to-income ratio (percentage).
     */
    @Column(name = "debt_to_income_ratio", precision = 5, scale = 2)
    private BigDecimal debtToIncomeRatio;

    /**
     * Credit score at time of application.
     */
    @Column(name = "credit_score")
    private Integer creditScore;

    /**
     * Credit bureau used for credit check.
     */
    @Column(name = "credit_bureau", length = 50)
    private String creditBureau;

    /**
     * Income verification details stored as JSON.
     * Contains: verificationType, documentIds, verificationStatus, verifiedDate
     */
    @Type(type = "jsonb")
    @Column(name = "income_verification", columnDefinition = "jsonb")
    private Map<String, Object> incomeVerification;

    /**
     * Collateral information for secured loans (JSON).
     * Contains: collateralType, description, estimatedValue, ownershipProof
     */
    @Type(type = "jsonb")
    @Column(name = "collateral_info", columnDefinition = "jsonb")
    private Map<String, Object> collateralInfo;

    /**
     * Co-borrower information if applicable (JSON).
     * Contains: name, ssn, income, creditScore, relationship
     */
    @Type(type = "jsonb")
    @Column(name = "co_borrower_info", columnDefinition = "jsonb")
    private Map<String, Object> coBorrowerInfo;

    /**
     * Current application status.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    /**
     * Underwriting decision code.
     */
    @Column(name = "underwriting_decision", length = 20)
    private String underwritingDecision;

    /**
     * Underwriting decision reason/notes.
     */
    @Column(name = "decision_notes", length = 1000)
    private String decisionNotes;

    /**
     * Approved loan amount (may differ from requested).
     */
    @Column(name = "approved_amount", precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    /**
     * Approved interest rate.
     */
    @Column(name = "approved_rate", precision = 5, scale = 2)
    private BigDecimal approvedRate;

    /**
     * Approved term in months.
     */
    @Column(name = "approved_term_months")
    private Integer approvedTermMonths;

    /**
     * User ID who reviewed the application.
     */
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    /**
     * Date application was submitted.
     */
    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    /**
     * Date application was reviewed.
     */
    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    /**
     * Loan ID if application was approved and loan created.
     */
    @Column(name = "loan_id")
    private Long loanId;

    /**
     * IP address of applicant submission.
     */
    @Column(name = "submission_ip", length = 45)
    private String submissionIp;

    /**
     * Device/browser information.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Indicates if fraud check was performed.
     */
    @Builder.Default
    @Column(name = "fraud_check_completed")
    private Boolean fraudCheckCompleted = false;

    /**
     * Fraud check result.
     */
    @Column(name = "fraud_check_score")
    private Integer fraudCheckScore;

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
     * Calculates debt-to-income ratio based on income and debts.
     */
    public void calculateDebtToIncomeRatio() {
        if (annualIncome != null && annualIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyIncome = annualIncome.divide(new BigDecimal("12"), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalMonthlyDebts = BigDecimal.ZERO;

            if (monthlyHousingPayment != null) {
                totalMonthlyDebts = totalMonthlyDebts.add(monthlyHousingPayment);
            }
            if (otherMonthlyDebts != null) {
                totalMonthlyDebts = totalMonthlyDebts.add(otherMonthlyDebts);
            }

            this.debtToIncomeRatio = totalMonthlyDebts
                    .divide(monthlyIncome, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
    }

    /**
     * Checks if application is eligible for automated approval.
     *
     * @return true if eligible for auto-approval
     */
    public boolean isEligibleForAutoApproval() {
        return creditScore != null && creditScore >= 750
                && debtToIncomeRatio != null && debtToIncomeRatio.compareTo(new BigDecimal("36")) <= 0
                && fraudCheckCompleted && fraudCheckScore != null && fraudCheckScore >= 80;
    }

    /**
     * Marks application as submitted.
     */
    public void submit() {
        this.status = ApplicationStatus.SUBMITTED;
        this.applicationDate = LocalDateTime.now();
    }

    /**
     * Marks application as under review.
     */
    public void startReview() {
        this.status = ApplicationStatus.UNDER_REVIEW;
    }

    /**
     * Approves application with specified terms.
     */
    public void approve(BigDecimal amount, BigDecimal rate, Integer termMonths, Long reviewerId) {
        this.status = ApplicationStatus.APPROVED;
        this.approvedAmount = amount;
        this.approvedRate = rate;
        this.approvedTermMonths = termMonths;
        this.reviewedBy = reviewerId;
        this.reviewedDate = LocalDateTime.now();
        this.underwritingDecision = "APPROVED";
    }

    /**
     * Rejects application with reason.
     */
    public void reject(String reason, Long reviewerId) {
        this.status = ApplicationStatus.REJECTED;
        this.decisionNotes = reason;
        this.reviewedBy = reviewerId;
        this.reviewedDate = LocalDateTime.now();
        this.underwritingDecision = "REJECTED";
    }
}

/**
 * Enum representing application status.
 */
enum ApplicationStatus {
    /**
     * Application started but not submitted.
     */
    DRAFT,

    /**
     * Application submitted by customer.
     */
    SUBMITTED,

    /**
     * Under review by underwriting.
     */
    UNDER_REVIEW,

    /**
     * Additional information requested.
     */
    INFO_REQUESTED,

    /**
     * Application approved.
     */
    APPROVED,

    /**
     * Application rejected.
     */
    REJECTED,

    /**
     * Application withdrawn by customer.
     */
    WITHDRAWN,

    /**
     * Application expired (no action taken).
     */
    EXPIRED
}
