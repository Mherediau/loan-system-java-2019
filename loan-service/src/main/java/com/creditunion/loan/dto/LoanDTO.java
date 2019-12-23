package com.creditunion.loan.dto;

import com.creditunion.loan.entity.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Data Transfer Object for loan creation and updates.
 * Used for API request/response payloads.
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDTO {

    /**
     * Customer ID requesting the loan.
     */
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    /**
     * Type of loan.
     */
    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    /**
     * Loan amount to be disbursed.
     */
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is $1,000")
    @DecimalMax(value = "1000000.00", message = "Maximum loan amount is $1,000,000")
    private BigDecimal loanAmount;

    /**
     * Annual interest rate as percentage.
     */
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
    @DecimalMax(value = "36.00", message = "Interest rate cannot exceed 36%")
    private BigDecimal interestRate;

    /**
     * Loan term in months.
     */
    @NotNull(message = "Term months is required")
    @Min(value = 6, message = "Minimum term is 6 months")
    @Max(value = 360, message = "Maximum term is 360 months")
    private Integer termMonths;

    /**
     * Purpose of the loan.
     */
    @NotBlank(message = "Loan purpose is required")
    @Size(max = 100, message = "Loan purpose cannot exceed 100 characters")
    private String loanPurpose;

    /**
     * User ID who approved the loan.
     */
    private Long approvedBy;

    /**
     * Approval notes.
     */
    @Size(max = 500, message = "Approval notes cannot exceed 500 characters")
    private String approvalNotes;

    /**
     * Reference to loan application ID.
     */
    private Long applicationId;

    /**
     * Collateral description for secured loans.
     */
    @Size(max = 500, message = "Collateral description cannot exceed 500 characters")
    private String collateralDescription;

    /**
     * Collateral value for secured loans.
     */
    @DecimalMin(value = "0.00", message = "Collateral value cannot be negative")
    private BigDecimal collateralValue;
}
